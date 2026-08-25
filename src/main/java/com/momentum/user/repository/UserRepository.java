package com.momentum.user.repository;

import com.momentum.user.model.User;
import com.momentum.user.model.enums.AuthProvider;
import com.momentum.user.model.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    User findByProviderAndProviderId(AuthProvider provider, String providerId);
    List<User> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(String username, String email);
    List<User> findByRole(UserRole role);
    long countByRole(UserRole role);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.currentPlan WHERE u.id = :id")
    Optional<User> findByIdWithCurrentPlan(@Param("id") UUID id);

    @Query("SELECT u FROM User u WHERE " +
            "(:query IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%'))) " +
            "AND (:role IS NULL OR u.role = :role) " +
            "AND (:enabled IS NULL OR (:enabled = true AND (u.enabled IS NULL OR u.enabled = true)) " +
            "     OR (:enabled = false AND u.enabled = false))")
    List<User> searchAndFilterUsers(@Param("query") String query, @Param("role") UserRole role, @Param("enabled") Boolean enabled);
}

