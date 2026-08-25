package com.momentum.fitness.repository;

import com.momentum.fitness.model.Completion;
import com.momentum.fitness.model.enums.CompletionType;
import com.momentum.user.model.User;
import com.momentum.user.model.enums.UserRole;
import com.momentum.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class CompletionRepositoryIntegrationTest {

    @Autowired
    private CompletionRepository completionRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void save_ShouldPersistCompletion() {
        // Given
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .role(UserRole.USER)
                .build();
        user = userRepository.save(user);

        Completion completion = Completion.builder()
                .user(user)
                .type(CompletionType.EXERCISE)
                .targetId(java.util.UUID.randomUUID())
                .completedAt(LocalDateTime.now())
                .build();

        // When
        Completion saved = completionRepository.save(completion);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getType()).isEqualTo(CompletionType.EXERCISE);
    }

    @Test
    void findById_ShouldReturnCompletion_WhenExists() {
        // Given
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .role(UserRole.USER)
                .build();
        user = userRepository.save(user);

        Completion completion = Completion.builder()
                .user(user)
                .type(CompletionType.EXERCISE)
                .targetId(java.util.UUID.randomUUID())
                .completedAt(LocalDateTime.now())
                .build();
        Completion saved = completionRepository.save(completion);

        // When
        Optional<Completion> found = completionRepository.findById(saved.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getType()).isEqualTo(CompletionType.EXERCISE);
    }

    @Test
    void findById_ShouldReturnEmpty_WhenNotExists() {
        // When
        Optional<Completion> found = completionRepository.findById(java.util.UUID.randomUUID());

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void findAll_ShouldReturnAllCompletions() {
        // Given
        long initialCount = completionRepository.count();

        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .role(UserRole.USER)
                .build();
        user = userRepository.save(user);

        Completion completion1 = Completion.builder()
                .user(user)
                .type(CompletionType.EXERCISE)
                .targetId(java.util.UUID.randomUUID())
                .completedAt(LocalDateTime.now())
                .build();

        Completion completion2 = Completion.builder()
                .user(user)
                .type(CompletionType.WORKOUT)
                .targetId(java.util.UUID.randomUUID())
                .completedAt(LocalDateTime.now())
                .build();

        completionRepository.save(completion1);
        completionRepository.save(completion2);

        // When
        List<Completion> completions = completionRepository.findAll();

        // Then
        assertThat(completions).hasSize((int) (initialCount + 2));
    }

    @Test
    void existsById_ShouldReturnTrue_WhenExists() {
        // Given
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .role(UserRole.USER)
                .build();
        user = userRepository.save(user);

        Completion completion = Completion.builder()
                .user(user)
                .type(CompletionType.EXERCISE)
                .targetId(java.util.UUID.randomUUID())
                .completedAt(LocalDateTime.now())
                .build();
        Completion saved = completionRepository.save(completion);

        // When
        boolean exists = completionRepository.existsById(saved.getId());

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsById_ShouldReturnFalse_WhenNotExists() {
        // When
        boolean exists = completionRepository.existsById(java.util.UUID.randomUUID());

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void delete_ShouldRemoveCompletion() {
        // Given
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .role(UserRole.USER)
                .build();
        user = userRepository.save(user);

        Completion completion = Completion.builder()
                .user(user)
                .type(CompletionType.EXERCISE)
                .targetId(java.util.UUID.randomUUID())
                .completedAt(LocalDateTime.now())
                .build();
        Completion saved = completionRepository.save(completion);

        // When
        completionRepository.delete(saved);
        Optional<Completion> found = completionRepository.findById(saved.getId());

        // Then
        assertThat(found).isEmpty();
    }
}