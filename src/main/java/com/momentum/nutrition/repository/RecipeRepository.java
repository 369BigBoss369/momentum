package com.momentum.nutrition.repository;

import com.momentum.core.model.enums.ModerationStatus;
import com.momentum.nutrition.model.Recipe;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.momentum.nutrition.model.enums.CompositeFoodType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, UUID> {
    Optional<Recipe> findByOwnerIdAndName(UUID ownerId, String name);
    List<Recipe> findByIsPublicTrueAndModerationStatus(ModerationStatus status);

    @Query("""
    SELECT DISTINCT r
    FROM Recipe r
    LEFT JOIN r.sharedUsers u
    WHERE r.type = :type
      AND (
          r.source = 'DEFAULT'
          OR (r.source = 'CUSTOM' AND r.ownerId = :userId)
          OR u.id = :sharedUserId
      )
    """)
    List<Recipe> findByTypeAndOwnerIdAndSharedUsersId(CompositeFoodType type, UUID userId, UUID sharedUserId);

    @EntityGraph(attributePaths = {"ingredients", "ingredients.food", "steps", "sharedUsers"})
    Optional<Recipe> findRecipeById(UUID id);

    @EntityGraph(attributePaths = {"ingredients", "sharedUsers"})
    @Query("""
        SELECT DISTINCT r FROM Recipe r
        LEFT JOIN FETCH r.ingredients
        WHERE (:name IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', :name, '%')))
          AND (:ingredients IS NULL OR EXISTS (
                SELECT 1 FROM r.ingredients ri WHERE ri.food.id IN :ingredients))
          AND (:inLibrary IS NULL OR 
               (:inLibrary = true AND EXISTS (SELECT 1 FROM r.sharedUsers u WHERE u.id = :userId)) OR
               (:inLibrary = false AND NOT EXISTS (SELECT 1 FROM r.sharedUsers u WHERE u.id = :userId)))
          AND (
                (:type = 'ALL' AND (
                    r.source = 'DEFAULT'
                    OR (r.source = 'CUSTOM' AND r.ownerId = :userId)
                    OR (r.source = 'CUSTOM' AND r.ownerId != :userId AND r.isPublic = true AND (r.moderationStatus IS NULL OR r.moderationStatus = 'APPROVED'))
                    OR (r.source = 'SHARED' AND EXISTS (
                            SELECT 1 FROM r.sharedUsers u WHERE u.id = :userId
                        ))
                ))
             OR (:type = 'OWN' AND r.source = 'CUSTOM' AND r.ownerId = :userId)
             OR (:type = 'OTHERS' AND r.isPublic = true AND (r.moderationStatus IS NULL OR r.moderationStatus = 'APPROVED') AND r.ownerId != :userId AND (
                    r.source = 'DEFAULT'
                    OR (r.source = 'CUSTOM' AND r.ownerId != :userId)
                    OR (r.source = 'SHARED' AND EXISTS (
                            SELECT 1 FROM r.sharedUsers u WHERE u.id = :userId
                        ) AND r.ownerId != :userId)
             ))
          )
        ORDER BY r.name ASC
    """)
    List<Recipe> searchRecipes(String name, UUID userId, String type, List<UUID> ingredients, Boolean inLibrary);
}

