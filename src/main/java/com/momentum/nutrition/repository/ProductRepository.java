package com.momentum.nutrition.repository;

import com.momentum.core.model.enums.ModerationStatus;
import com.momentum.nutrition.model.Product;
import com.momentum.nutrition.model.enums.ProductType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    Optional<Product> findByOwnerIdAndName(UUID ownerId, String name);
    List<Product> findByIsPublicTrueAndModerationStatus(ModerationStatus status);

    @EntityGraph(attributePaths = {"sharedUsers"})
    Optional<Product> findProductById(UUID id);

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.sharedUsers WHERE p.type = :type AND " +
            "(p.source = 'DEFAULT' OR " +
            "(p.source = 'CUSTOM' AND p.ownerId = :userId) OR " +
            "(p.source = 'SHARED' AND EXISTS (SELECT 1 FROM p.sharedUsers u WHERE u.id = :sharedUserId)))")
    List<Product> findByTypeAndOwnerIdAndSharedUsersId(ProductType type, UUID userId, UUID sharedUserId);

    @Query("SELECT DISTINCT p FROM Product p " +
           "WHERE (p.source = 'DEFAULT' OR " +
           "       (p.source = 'CUSTOM' AND p.ownerId = :userId) OR " +
           "       (p.source = 'SHARED' AND EXISTS (SELECT 1 FROM p.sharedUsers u WHERE u.id = :sharedUserId)) OR " +
           "       (p.isPublic = true)) " +
           "AND (:inLibrary IS NULL OR " +
           "     (:inLibrary = true AND EXISTS (SELECT 1 FROM p.sharedUsers u WHERE u.id = :userId)) OR " +
           "     (:inLibrary = false AND NOT EXISTS (SELECT 1 FROM p.sharedUsers u WHERE u.id = :userId)))")
    List<Product> findAllAccessibleWithLibrary(UUID userId, UUID sharedUserId, Boolean inLibrary);

    @Query("SELECT DISTINCT p FROM Product p " +
           "WHERE p.type = :type AND " +
            "(p.source = 'DEFAULT' OR " +
            "(p.source = 'CUSTOM' AND p.ownerId = :userId) OR " +
            "(p.source = 'SHARED' AND EXISTS (SELECT 1 FROM p.sharedUsers u WHERE u.id = :sharedUserId))) " +
            "AND (:inLibrary IS NULL OR " +
            "     (:inLibrary = true AND EXISTS (SELECT 1 FROM p.sharedUsers u WHERE u.id = :sharedUserId)) OR " +
            "     (:inLibrary = false AND NOT EXISTS (SELECT 1 FROM p.sharedUsers u WHERE u.id = :sharedUserId)))")
    List<Product> findByTypeAndOwnerIdAndSharedUsersIdWithLibrary(ProductType type, UUID userId, UUID sharedUserId, Boolean inLibrary);
}

