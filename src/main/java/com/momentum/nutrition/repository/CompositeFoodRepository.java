package com.momentum.nutrition.repository;

import com.momentum.core.model.enums.ModerationStatus;
import com.momentum.nutrition.model.CompositeFood;
import com.momentum.nutrition.model.enums.CompositeFoodType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CompositeFoodRepository extends JpaRepository<CompositeFood, UUID> {
    Optional<CompositeFood> findByOwnerIdAndName(UUID ownerId, String name);
    List<CompositeFood> findByIsPublicTrueAndModerationStatus(ModerationStatus status);

    @EntityGraph(attributePaths = {"sharedUsers"})
    Optional<CompositeFood> findCompositeFoodById(UUID id);

    @Query("SELECT DISTINCT c FROM CompositeFood c LEFT JOIN FETCH c.sharedUsers " +
           "WHERE TYPE(c) = CompositeFood AND c.type = :type AND " +
           "(c.source = 'DEFAULT' OR " +
           "(c.source = 'CUSTOM' AND c.ownerId = :userId) OR " +
           "(c.source = 'SHARED' AND EXISTS (SELECT 1 FROM c.sharedUsers u WHERE u.id = :sharedUserId)))")
    List<CompositeFood> findByTypeAndOwnerIdAndSharedUsersId(CompositeFoodType type, UUID userId, UUID sharedUserId);

    @Query("SELECT DISTINCT c FROM CompositeFood c " +
           "WHERE TYPE(c) = CompositeFood AND " +
           "      (c.source = 'DEFAULT' OR " +
           "       (c.source = 'CUSTOM' AND c.ownerId = :userId) OR " +
           "       (c.source = 'SHARED' AND EXISTS (SELECT 1 FROM c.sharedUsers u WHERE u.id = :sharedUserId)) OR " +
           "       (c.isPublic = true)) " +
           "AND (:inLibrary IS NULL OR " +
           "     (:inLibrary = true AND EXISTS (SELECT 1 FROM c.sharedUsers u WHERE u.id = :userId)) OR " +
           "     (:inLibrary = false AND NOT EXISTS (SELECT 1 FROM c.sharedUsers u WHERE u.id = :userId)))")
    List<CompositeFood> findAllAccessibleWithLibrary(UUID userId, UUID sharedUserId, Boolean inLibrary);

    @Query("SELECT DISTINCT c FROM CompositeFood c LEFT JOIN FETCH c.sharedUsers " +
           "WHERE TYPE(c) = CompositeFood AND " +
           "      (c.source = 'DEFAULT' OR " +
           "       (c.source = 'CUSTOM' AND c.ownerId = :userId) OR " +
           "       (c.source = 'SHARED' AND EXISTS (SELECT 1 FROM c.sharedUsers u WHERE u.id = :sharedUserId)) OR " +
           "       (c.isPublic = true))")
    List<CompositeFood> findAllAccessible(UUID userId, UUID sharedUserId);


    @Query("SELECT DISTINCT c FROM CompositeFood c " +
           "WHERE TYPE(c) = CompositeFood AND c.type = :type AND " +
           "(c.source = 'DEFAULT' OR " +
           "(c.source = 'CUSTOM' AND c.ownerId = :userId) OR " +
           "(c.source = 'SHARED' AND EXISTS (SELECT 1 FROM c.sharedUsers u WHERE u.id = :sharedUserId))) " +
           "AND (:inLibrary IS NULL OR " +
           "     (:inLibrary = true AND EXISTS (SELECT 1 FROM c.sharedUsers u WHERE u.id = :sharedUserId)) OR " +
           "     (:inLibrary = false AND NOT EXISTS (SELECT 1 FROM c.sharedUsers u WHERE u.id = :sharedUserId)))")
    List<CompositeFood> findByTypeAndOwnerIdAndSharedUsersIdWithLibrary(CompositeFoodType type, UUID userId, UUID sharedUserId, Boolean inLibrary);
}


