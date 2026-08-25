package com.momentum.nutrition.repository;

import com.momentum.core.model.enums.ModerationStatus;
import com.momentum.fitness.model.enums.SourceType;
import com.momentum.nutrition.model.Product;
import com.momentum.nutrition.model.enums.ProductType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ProductRepositoryIntegrationTest {

    @Autowired
    private ProductRepository productRepository;

    private Product buildProduct(String name, UUID ownerId, SourceType source, boolean isPublic, ModerationStatus status) {
        Product product = new Product();
        product.setName(name);
        product.setType(ProductType.FRUIT);
        product.setCalories(100);
        product.setOwnerId(ownerId);
        product.setSource(source);
        product.setIsPublic(isPublic);
        product.setModerationStatus(status);
        return product;
    }

    @Test
    void save_ShouldPersistProduct() {
        UUID ownerId = UUID.randomUUID();
        Product product = buildProduct("Apple", ownerId, SourceType.CUSTOM, false, ModerationStatus.APPROVED);

        Product saved = productRepository.save(product);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Apple");
        assertThat(saved.getType()).isEqualTo(ProductType.FRUIT);
    }

    @Test
    void findByOwnerIdAndName_ShouldReturnProduct_WhenExists() {
        UUID ownerId = UUID.randomUUID();
        productRepository.save(buildProduct("Banana", ownerId, SourceType.CUSTOM, false, ModerationStatus.APPROVED));

        Optional<Product> found = productRepository.findByOwnerIdAndName(ownerId, "Banana");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Banana");
    }

    @Test
    void findByOwnerIdAndName_ShouldReturnEmpty_WhenNotExists() {
        Optional<Product> found = productRepository.findByOwnerIdAndName(UUID.randomUUID(), "Nonexistent");

        assertThat(found).isEmpty();
    }

    @Test
    void findByIsPublicTrueAndModerationStatus_ShouldReturnOnlyPendingPublicProducts() {
        UUID ownerId = UUID.randomUUID();
        productRepository.save(buildProduct("Pending Product", ownerId, SourceType.CUSTOM, true, ModerationStatus.PENDING));
        productRepository.save(buildProduct("Approved Product", ownerId, SourceType.CUSTOM, true, ModerationStatus.APPROVED));
        productRepository.save(buildProduct("Private Product", ownerId, SourceType.CUSTOM, false, ModerationStatus.APPROVED));

        List<Product> pending = productRepository.findByIsPublicTrueAndModerationStatus(ModerationStatus.PENDING);

        assertThat(pending).extracting(Product::getName).containsExactly("Pending Product");
    }

    @Test
    void findProductById_ShouldFetchSharedUsersEagerly() {
        UUID ownerId = UUID.randomUUID();
        Product saved = productRepository.save(buildProduct("Orange", ownerId, SourceType.CUSTOM, false, ModerationStatus.APPROVED));

        Optional<Product> found = productRepository.findProductById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getSharedUsers()).isNotNull();
    }

    @Test
    void deleteById_ShouldRemoveProduct() {
        UUID ownerId = UUID.randomUUID();
        Product saved = productRepository.save(buildProduct("Grapes", ownerId, SourceType.CUSTOM, false, ModerationStatus.APPROVED));

        productRepository.deleteById(saved.getId());

        assertThat(productRepository.findById(saved.getId())).isEmpty();
    }
}
