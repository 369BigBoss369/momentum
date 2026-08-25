package com.momentum.nutrition.service;

import com.momentum.core.model.enums.ModerationStatus;
import com.momentum.exception.nutrition.CustomFoodAlreadyExists;
import com.momentum.exception.nutrition.FoodNotFoundException;
import com.momentum.exception.UnauthorizedResourceAccessException;
import com.momentum.fitness.model.enums.SourceType;
import com.momentum.nutrition.dto.CreateCompositeFoodDTO;
import com.momentum.nutrition.dto.FoodSearchView;
import com.momentum.nutrition.dto.enums.FoodItemType;
import com.momentum.nutrition.dto.enums.OwnershipType;
import com.momentum.nutrition.dto.seed.CompositeFoodSeedDTO;
import com.momentum.nutrition.model.CompositeFood;
import com.momentum.nutrition.model.enums.CompositeFoodType;
import com.momentum.nutrition.repository.CompositeFoodRepository;
import com.momentum.user.model.User;
import com.momentum.user.service.NutritionActivityService;
import com.momentum.user.service.UserService;
import com.momentum.util.AccessControlUtil;
import com.momentum.util.ModerationUtil;
import com.momentum.util.NutritionMapper;
import com.momentum.util.ImagePathResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CompositeFoodService {
    private final CompositeFoodRepository compositeFoodRepository;
    private final NutritionActivityService nutritionActivityService;
    private final UserService userService;

    @Autowired
    public CompositeFoodService(CompositeFoodRepository compositeFoodRepository,
                                NutritionActivityService nutritionActivityService,
                                UserService userService) {
        this.compositeFoodRepository = compositeFoodRepository;
        this.nutritionActivityService = nutritionActivityService;
        this.userService = userService;
    }

    @Transactional
    public void seedCompositeFood(CompositeFoodSeedDTO compositeFoodSeedDto) {
        CompositeFood compositeFood = NutritionMapper.mapToFood(compositeFoodSeedDto, new CompositeFood());
        compositeFood.setType(compositeFoodSeedDto.getType());

        if (compositeFood.getImagePath() == null || compositeFood.getImagePath().isEmpty()) {
            compositeFood.setImagePath(ImagePathResolver.resolveCompositeFoodImage(compositeFood.getType()));
        }
        compositeFood.setSource(SourceType.DEFAULT);

        compositeFoodRepository.save(compositeFood);
    }

    public int getCount() {
        return compositeFoodRepository.findAll().size();
    }

    @Transactional
    public CompositeFood createCompositeFood(CreateCompositeFoodDTO createCompositeFoodDTO, UUID userId) {
        Optional<CompositeFood> optional = compositeFoodRepository.findByOwnerIdAndName(userId, createCompositeFoodDTO.getName());
        if (optional.isPresent()) {
            throw new CustomFoodAlreadyExists(String.format("You have already added custom composite food with the name '%s'", createCompositeFoodDTO.getName()));
        }

        CompositeFood compositeFood = NutritionMapper.mapToFood(createCompositeFoodDTO, new CompositeFood());
        compositeFood.setType(createCompositeFoodDTO.getType());
        if (compositeFood.getImagePath() == null) {
            compositeFood.setImagePath(ImagePathResolver.resolveCompositeFoodImage(compositeFood.getType()));
        }
        compositeFood.setIsPublic(createCompositeFoodDTO.getIsPublic());
        ModerationUtil.applyPublicityChange(compositeFood, false);
        compositeFood.setOwnerId(userId);
        compositeFood.setSource(SourceType.CUSTOM);

        CompositeFood saved = compositeFoodRepository.save(compositeFood);
        nutritionActivityService.logFoodCreated(userId, compositeFood.getName(), "composite food");
        return saved;
    }

    @Transactional(readOnly = true)
    public CompositeFood getById(UUID id) {
        return compositeFoodRepository.findById(id).orElseThrow(() -> new FoodNotFoundException("Composite food does not exist"));
    }

    @Transactional
    public CompositeFood updateCompositeFood(UUID id, CreateCompositeFoodDTO dto, UUID userId) {
        CompositeFood compositeFood = compositeFoodRepository.findById(id).orElseThrow(() -> new FoodNotFoundException("Composite food does not exist"));

        if (!userId.equals(compositeFood.getOwnerId())) {
            throw new UnauthorizedResourceAccessException("You do not have permission to modify this food");
        }

        Optional<CompositeFood> optional = compositeFoodRepository.findByOwnerIdAndName(userId, dto.getName());
        if (optional.isPresent() && !optional.get().getId().equals(id)) {
            throw new CustomFoodAlreadyExists(String.format("You have already added custom composite food with the name '%s'", dto.getName()));
        }

        NutritionMapper.mapToFood(dto, compositeFood);

        compositeFood.setType(dto.getType());
        if (compositeFood.getImagePath() == null || compositeFood.getImagePath().isEmpty()) {
            compositeFood.setImagePath(ImagePathResolver.resolveCompositeFoodImage(compositeFood.getType()));
        }

        boolean wasPublic = Boolean.TRUE.equals(compositeFood.getIsPublic());
        compositeFood.setIsPublic(dto.getIsPublic());
        ModerationUtil.applyPublicityChange(compositeFood, wasPublic);

        CompositeFood saved = compositeFoodRepository.save(compositeFood);
        nutritionActivityService.logFoodUpdated(userId, compositeFood.getName(), "composite food");
        return saved;
    }

    @Transactional
    public void addToLibrary(UUID compositeFoodId, UUID userId) {
        CompositeFood compositeFood = compositeFoodRepository.findById(compositeFoodId).orElseThrow(() -> new FoodNotFoundException("Composite food does not exist"));

        if (!Boolean.TRUE.equals(compositeFood.getIsPublic()) || !ModerationUtil.isVisible(compositeFood)) {
            throw new IllegalStateException("This item is not yet approved and cannot be added to a library");
        }

        User user = userService.getById(userId);

        boolean isShared = compositeFood.getSharedUsers().stream().anyMatch(u -> u.getId().equals(userId));

        if (!isShared) {
            compositeFood.getSharedUsers().add(user);
            compositeFoodRepository.save(compositeFood);

            nutritionActivityService.logFoodAddedToLibrary(userId, compositeFood.getName(), "composite food");
        }
    }

    @Transactional
    public void removeFromLibrary(UUID compositeFoodId, UUID userId) {
        CompositeFood compositeFood = compositeFoodRepository.findById(compositeFoodId).orElseThrow(() -> new FoodNotFoundException("Composite food does not exist"));

        boolean removed = compositeFood.getSharedUsers().removeIf(u -> u.getId().equals(userId));

        if (removed) {
            compositeFoodRepository.save(compositeFood);
            nutritionActivityService.logFoodRemovedFromLibrary(userId, compositeFood.getName(), "composite food");
        }
    }

    @Transactional(readOnly = true)
    public List<FoodSearchView> search(String name, String compositeType, UUID userId, Boolean inLibrary, OwnershipType ownership) {
        List<CompositeFood> foods;

        if (compositeType == null || compositeType.trim().isEmpty()) {
            if (inLibrary != null) {
                foods = compositeFoodRepository.findAllAccessibleWithLibrary(userId, userId, inLibrary);
            } else {
                foods = compositeFoodRepository.findAllAccessible(userId, userId);
            }
        } else {
            if (inLibrary != null) {
                foods = compositeFoodRepository.findByTypeAndOwnerIdAndSharedUsersIdWithLibrary(CompositeFoodType.valueOf(compositeType), userId, userId, inLibrary);
            } else {
                foods = getByType(CompositeFoodType.valueOf(compositeType), userId, userId);
            }
        }

        return foods.stream()
                .filter(cf -> cf.getSource() != SourceType.DEFAULT)
                .filter(cf -> {

                    if (ownership == null || ownership == OwnershipType.ALL) {
                        return true;
                    } else if (ownership == OwnershipType.OWN) {

                        return cf.getSource() == SourceType.CUSTOM && cf.getOwnerId().equals(userId);
                    } else if (ownership == OwnershipType.OTHERS) {

                        return !cf.getOwnerId().equals(userId) && 
                               ((cf.getIsPublic() != null && cf.getIsPublic() && ModerationUtil.isVisible(cf)) ||
                                (cf.getSource() == SourceType.SHARED && cf.getSharedUsers() != null && 
                                 cf.getSharedUsers().stream().anyMatch(u -> u.getId().equals(userId))));
                    }
                    return true;
                })
                .filter(cf -> name == null || cf.getName().toLowerCase().contains(name.toLowerCase().trim()))
                .map(cf -> {
                    FoodSearchView view = FoodSearchView.from(cf, userId);

                    view.setItemType(FoodItemType.COMPOSITE);
                    view.setFoodType(cf.getType().getDisplayName());

                    return view;
                })
                .toList();
    }

    public List<CompositeFood> getPendingApproval() {
        return compositeFoodRepository.findByIsPublicTrueAndModerationStatus(ModerationStatus.PENDING);
    }

    @Transactional
    public void approve(UUID id) {
        CompositeFood compositeFood = compositeFoodRepository.findById(id).orElseThrow(() -> new FoodNotFoundException("Composite food does not exist"));
        compositeFood.setModerationStatus(ModerationStatus.APPROVED);
        compositeFoodRepository.save(compositeFood);
    }

    @Transactional
    public void reject(UUID id) {
        CompositeFood compositeFood = compositeFoodRepository.findById(id).orElseThrow(() -> new FoodNotFoundException("Composite food does not exist"));
        compositeFood.setModerationStatus(ModerationStatus.REJECTED);
        compositeFood.setIsPublic(false);
        compositeFoodRepository.save(compositeFood);
    }

    public boolean isOwner(CompositeFood compositeFood, UUID userId) {
        return userId.equals(compositeFood.getOwnerId());
    }

    public String getOwnerUsernameIfNotOwner(CompositeFood compositeFood, UUID userId) {
        if (isOwner(compositeFood, userId)) {
            return null;
        }
        User owner = userService.getById(compositeFood.getOwnerId());
        return owner != null ? owner.getUsername() : null;
    }

    public int getSharedUsersCount(CompositeFood compositeFood) {
        return java.util.Optional.ofNullable(compositeFood.getSharedUsers())
                .map(java.util.Set::size)
                .orElse(0);
    }

    public boolean isInLibrary(CompositeFood compositeFood, UUID userId) {
        return java.util.Optional.ofNullable(compositeFood.getSharedUsers())
                .map(users -> users.stream().anyMatch(u -> u.getId().equals(userId)))
                .orElse(false);
    }

    public List<CompositeFood> getByType(CompositeFoodType type, UUID ownerId, UUID sharedUserId) {
        return compositeFoodRepository.findByTypeAndOwnerIdAndSharedUsersId(type, ownerId, sharedUserId);
    }

    public CompositeFood getByIdForViewing(UUID id, User currentUser) {
        CompositeFood compositeFood = compositeFoodRepository.findCompositeFoodById(id).orElseThrow(() -> new FoodNotFoundException("Composite food does not exist"));

        if (!AccessControlUtil.canView(compositeFood, currentUser)) {
            throw new FoodNotFoundException("Composite food does not exist");
        }

        return compositeFood;
    }

    public long count() {
        return compositeFoodRepository.count();
    }
}

