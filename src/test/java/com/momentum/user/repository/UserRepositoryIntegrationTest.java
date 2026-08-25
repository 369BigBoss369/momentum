package com.momentum.user.repository;

import com.momentum.user.model.User;
import com.momentum.user.model.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void save_ShouldPersistUser() {
        // Given
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .role(UserRole.USER)
                .build();

        // When
        User saved = userRepository.save(user);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUsername()).isEqualTo("testuser");
        assertThat(saved.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void findById_ShouldReturnUser_WhenExists() {
        // Given
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .role(UserRole.USER)
                .build();
        User saved = userRepository.save(user);

        // When
        Optional<User> found = userRepository.findById(saved.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testuser");
    }

    @Test
    void findById_ShouldReturnEmpty_WhenNotExists() {
        // When
        Optional<User> found = userRepository.findById(java.util.UUID.randomUUID());

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void findAll_ShouldReturnAllUsers() {
        // Given
        long initialCount = userRepository.count();

        User user1 = User.builder()
                .username("user1")
                .email("user1@example.com")
                .role(UserRole.USER)
                .build();
        User user2 = User.builder()
                .username("user2")
                .email("user2@example.com")
                .role(UserRole.USER)
                .build();

        userRepository.save(user1);
        userRepository.save(user2);

        // When
        var users = userRepository.findAll();

        // Then
        assertThat(users).hasSize((int) (initialCount + 2));
    }

    @Test
    void existsById_ShouldReturnTrue_WhenExists() {
        // Given
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .role(UserRole.USER)
                .build();
        User saved = userRepository.save(user);

        // When
        boolean exists = userRepository.existsById(saved.getId());

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsById_ShouldReturnFalse_WhenNotExists() {
        // When
        boolean exists = userRepository.existsById(java.util.UUID.randomUUID());

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void delete_ShouldRemoveUser() {
        // Given
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .role(UserRole.USER)
                .build();
        User saved = userRepository.save(user);

        // When
        userRepository.delete(saved);
        Optional<User> found = userRepository.findById(saved.getId());

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void count_ShouldReturnCorrectCount() {
        // Given
        long initialCount = userRepository.count();

        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .role(UserRole.USER)
                .build();
        userRepository.save(user);

        // When
        long count = userRepository.count();

        // Then
        assertThat(count).isEqualTo(initialCount + 1);
    }
}
