package com.momentum.nutrition.service;

import com.momentum.core.model.enums.ModerationStatus;
import com.momentum.exception.nutrition.CustomFoodAlreadyExists;
import com.momentum.exception.nutrition.FoodNotFoundException;
import com.momentum.exception.UnauthorizedResourceAccessException;
import com.momentum.fitness.model.enums.SourceType;
import com.momentum.nutrition.dto.CreateProductDTO;
import com.momentum.nutrition.dto.FoodSearchView;
import com.momentum.nutrition.dto.enums.FoodItemType;
import com.momentum.nutrition.dto.enums.OwnershipType;
import com.momentum.nutrition.dto.seed.ProductSeedDTO;
import com.momentum.nutrition.model.Product;
import com.momentum.nutrition.model.enums.ProductType;
import com.momentum.nutrition.repository.ProductRepository;
import com.momentum.user.model.User;
import com.momentum.user.service.NutritionActivityService;
import com.momentum.user.service.UserService;
import com.momentum.util.AccessControlUtil;
import com.momentum.util.ModerationUtil;
import com.momentum.util.NutritionMapper;
import com.momentum.util.ImagePathResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class ProductService {
    private final ProductRepository productRepository;
    private final NutritionActivityService nutritionActivityService;
    private final UserService userService;

    @Autowired
    public ProductService(ProductRepository productRepository,
                          NutritionActivityService nutritionActivityService,
                          UserService userService) {
        this.productRepository = productRepository;
        this.nutritionActivityService = nutritionActivityService;
        this.userService = userService;
    }

    @Transactional
    public Product createProduct(CreateProductDTO createProductDTO, UUID userId) {
        log.info("Creating product: {} for user: {}", createProductDTO.getName(), userId);

        Optional<Product> optional = productRepository.findByOwnerIdAndName(userId, createProductDTO.getName());
        if (optional.isPresent()) {
            log.warn("Product creation failed - product with name '{}' already exists for user: {}", createProductDTO.getName(), userId);
            throw new CustomFoodAlreadyExists(String.format("You have already added custom product with the name '%s'", createProductDTO.getName()));
        }

        Product product = NutritionMapper.mapToFood(createProductDTO, new Product());
        product.setType(createProductDTO.getType());
        if (product.getImagePath() == null || product.getImagePath().isEmpty()) {
            product.setImagePath(ImagePathResolver.resolveProductImage(product.getType()));
        }
        product.setIsPublic(createProductDTO.getIsPublic());
        ModerationUtil.applyPublicityChange(product, false);
        product.setOwnerId(userId);
        product.setSource(SourceType.CUSTOM);

        Product saved = productRepository.save(product);
        log.info("Product created successfully: {} (ID: {}) for user: {}", createProductDTO.getName(), product.getId(), userId);
        nutritionActivityService.logFoodCreated(userId, product.getName(), "product");
        return saved;
    }

    @Transactional(readOnly = true)
    public Product getById(UUID id) {
        return productRepository.findById(id).orElseThrow(() -> new FoodNotFoundException("Product does not exist"));
    }

    @Transactional
    public Product updateProduct(UUID id, CreateProductDTO dto, UUID userId) {
        Product product = productRepository.findById(id).orElseThrow(() -> new FoodNotFoundException("Product does not exist"));

        if (!userId.equals(product.getOwnerId())) {
            throw new UnauthorizedResourceAccessException("You do not have permission to modify this food");
        }

        Optional<Product> optional = productRepository.findByOwnerIdAndName(userId, dto.getName());
        if (optional.isPresent() && !optional.get().getId().equals(id)) {
            throw new CustomFoodAlreadyExists(String.format("You have already added custom product with the name '%s'", dto.getName()));
        }

        NutritionMapper.mapToFood(dto, product);

        product.setType(dto.getType());
        if (product.getImagePath() == null || product.getImagePath().isEmpty()) {
            product.setImagePath(ImagePathResolver.resolveProductImage(product.getType()));
        }

        boolean wasPublic = Boolean.TRUE.equals(product.getIsPublic());
        product.setIsPublic(dto.getIsPublic());
        ModerationUtil.applyPublicityChange(product, wasPublic);

        Product saved = productRepository.save(product);
        nutritionActivityService.logFoodUpdated(userId, product.getName(), "product");
        return saved;
    }

    @Transactional
    public void addToLibrary(UUID productId, UUID userId) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new FoodNotFoundException("Product does not exist"));

        if (!Boolean.TRUE.equals(product.getIsPublic()) || !ModerationUtil.isVisible(product)) {
            throw new IllegalStateException("This item is not yet approved and cannot be added to a library");
        }

        User user = userService.getById(userId);

        boolean isShared = product.getSharedUsers().stream().anyMatch(u -> u.getId().equals(userId));

        if (!isShared) {
            product.getSharedUsers().add(user);
            productRepository.save(product);

            nutritionActivityService.logFoodAddedToLibrary(userId, product.getName(), "product");
        }
    }

    @Transactional
    public void removeFromLibrary(UUID productId, UUID userId) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new FoodNotFoundException("Product does not exist"));

        boolean removed = product.getSharedUsers().removeIf(u -> u.getId().equals(userId));

        if (removed) {
            productRepository.save(product);
            nutritionActivityService.logFoodRemovedFromLibrary(userId, product.getName(), "product");
        }
    }

    @Transactional
    public void seedProduct(ProductSeedDTO productSeedDTO) {
        Product product = NutritionMapper.mapToFood(productSeedDTO, new Product());
        product.setType(productSeedDTO.getType());

        if (product.getImagePath() == null || product.getImagePath().isEmpty()) {
            product.setImagePath(ImagePathResolver.resolveProductImage(product.getType()));
        }
        product.setSource(SourceType.DEFAULT);

        productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public List<FoodSearchView> search(String name, String productType, UUID userId, Boolean inLibrary, OwnershipType ownership) {
        List<Product> products;

        if (productType == null || productType.trim().isEmpty()) {
            if (inLibrary != null) {
                products = productRepository.findAllAccessibleWithLibrary(userId, userId, inLibrary);
            } else {

                products = productRepository.findAll().stream()
                        .filter(p -> p.getSource() == SourceType.DEFAULT ||
                                   (p.getSource() == SourceType.CUSTOM && p.getOwnerId().equals(userId)) ||
                                   (p.getSource() == SourceType.SHARED && p.getSharedUsers() != null && 
                                    p.getSharedUsers().stream().anyMatch(u -> u.getId().equals(userId))) ||
                                   (p.getIsPublic() != null && p.getIsPublic() && ModerationUtil.isVisible(p)))
                        .toList();
            }
        } else {
            if (inLibrary != null) {
                products = productRepository.findByTypeAndOwnerIdAndSharedUsersIdWithLibrary(ProductType.valueOf(productType), userId, userId, inLibrary);
            } else {
                products = getByType(ProductType.valueOf(productType), userId, userId);
            }
        }

        return products.stream()
                .filter(p -> p.getSource() != SourceType.DEFAULT)
                .filter(p -> {

                    if (ownership == null || ownership == OwnershipType.ALL) {
                        return true;
                    } else if (ownership == OwnershipType.OWN) {

                        return p.getSource() == SourceType.CUSTOM && p.getOwnerId().equals(userId);
                    } else if (ownership == OwnershipType.OTHERS) {

                        return !p.getOwnerId().equals(userId) && 
                               (p.getIsPublic() != null && p.getIsPublic() || 
                                (p.getSource() == SourceType.SHARED && p.getSharedUsers() != null && 
                                 p.getSharedUsers().stream().anyMatch(u -> u.getId().equals(userId))));
                    }
                    return true;
                })
                .filter(p -> name == null || p.getName().toLowerCase().contains(name.toLowerCase().trim()))
                .map(p -> {
                    FoodSearchView view = FoodSearchView.from(p, userId);

                    view.setItemType(FoodItemType.PRODUCT);
                    view.setFoodType(p.getType().getDisplayName());

                    return view;
                })
                .toList();
    }

    public List<Product> getPendingApproval() {
        return productRepository.findByIsPublicTrueAndModerationStatus(ModerationStatus.PENDING);
    }

    @Transactional
    public void approve(UUID id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new FoodNotFoundException("Product does not exist"));
        product.setModerationStatus(ModerationStatus.APPROVED);
        productRepository.save(product);
    }

    @Transactional
    public void reject(UUID id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new FoodNotFoundException("Product does not exist"));
        product.setModerationStatus(ModerationStatus.REJECTED);
        product.setIsPublic(false);
        productRepository.save(product);
    }

    public boolean isOwner(Product product, UUID userId) {
        return userId.equals(product.getOwnerId());
    }

    public String getOwnerUsernameIfNotOwner(Product product, UUID userId) {
        if (isOwner(product, userId)) {
            return null;
        }
        User owner = userService.getById(product.getOwnerId());
        return owner != null ? owner.getUsername() : null;
    }

    public int getSharedUsersCount(Product product) {
        return java.util.Optional.ofNullable(product.getSharedUsers())
                .map(java.util.Set::size)
                .orElse(0);
    }

    public boolean isInLibrary(Product product, UUID userId) {
        return java.util.Optional.ofNullable(product.getSharedUsers())
                .map(users -> users.stream().anyMatch(u -> u.getId().equals(userId)))
                .orElse(false);
    }

    public int getCount() {
        return productRepository.findAll().size();
    }

    public List<Product> getByType(ProductType type, UUID userId, UUID sharedUserId) {
        return productRepository.findByTypeAndOwnerIdAndSharedUsersId(type, userId, sharedUserId);
    }

    public Product getByIdForViewing(UUID id, User currentUser) {
        Product product = productRepository.findProductById(id).orElseThrow(() -> new FoodNotFoundException("Product does not exist"));
        if (!AccessControlUtil.canView(product, currentUser)) {
            throw new FoodNotFoundException("Product does not exist");
        }
        return product;
    }

    public long count() {
        return productRepository.count();
    }
}

