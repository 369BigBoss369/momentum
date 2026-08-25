package com.momentum.fitness.service;

import com.momentum.fitness.model.Completion;
import com.momentum.fitness.model.enums.CompletionType;
import com.momentum.fitness.repository.CompletionRepository;
import com.momentum.user.model.User;
import com.momentum.user.model.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(CompletionService.class)
class CompletionServiceIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CompletionRepository completionRepository;

    @Autowired
    private CompletionService completionService;

    private User testUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        testUser = User.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .role(UserRole.USER)
                .build();

        entityManager.persist(testUser);
        entityManager.flush();
    }

    @Test
    void getCompletionCountForUser_ShouldReturnCorrectCount() {
        Completion completion1 = Completion.builder()
                .user(testUser)
                .type(CompletionType.EXERCISE)
                .targetId(UUID.randomUUID())
                .build();

        Completion completion2 = Completion.builder()
                .user(testUser)
                .type(CompletionType.WORKOUT)
                .targetId(UUID.randomUUID())
                .build();

        entityManager.persist(completion1);
        entityManager.persist(completion2);
        entityManager.flush();

        long count = completionService.getCompletionCountForUser(userId);

        assertEquals(2, count);
    }

    @Test
    void getRecentCompletionsForUser_ShouldReturnOrderedByDate() {
        Completion olderCompletion = Completion.builder()
                .user(testUser)
                .type(CompletionType.EXERCISE)
                .targetId(UUID.randomUUID())
                .completedAt(LocalDateTime.now().minusDays(1))
                .build();

        Completion newerCompletion = Completion.builder()
                .user(testUser)
                .type(CompletionType.WORKOUT)
                .targetId(UUID.randomUUID())
                .completedAt(LocalDateTime.now())
                .build();

        entityManager.persist(olderCompletion);
        entityManager.persist(newerCompletion);
        entityManager.flush();

        List<Completion> recent = completionService.getRecentCompletionsForUser(userId, 10);

        assertEquals(2, recent.size());
        assertEquals(newerCompletion.getId(), recent.get(0).getId());
        assertEquals(olderCompletion.getId(), recent.get(1).getId());
    }

    @Test
    void markAsCompleted_ShouldCreateCompletionRecord() {
        UUID exerciseId = UUID.randomUUID();

        Completion result = completionService.markAsCompleted(testUser, exerciseId, "EXERCISE", null, null);

        assertNotNull(result);
        assertEquals(CompletionType.EXERCISE, result.getType());
        assertEquals(exerciseId, result.getTargetId());
        assertEquals(testUser.getId(), result.getUser().getId());

        Completion saved = entityManager.find(Completion.class, result.getId());
        assertNotNull(saved);
        assertEquals(CompletionType.EXERCISE, saved.getType());
    }

    @Test
    void isExerciseCompleted_ShouldReturnTrue_WhenCompletionExists() {
        UUID exerciseId = UUID.randomUUID();
        UUID planDayId = UUID.randomUUID();

        Completion completion = Completion.builder()
                .user(testUser)
                .type(CompletionType.EXERCISE)
                .targetId(exerciseId)
                .planDayId(planDayId)
                .workoutPosition(0)
                .build();

        entityManager.persist(completion);
        entityManager.flush();

        boolean isCompleted = completionService.isExerciseCompleted(userId, exerciseId, planDayId, 0);

        assertTrue(isCompleted);
    }

    @Test
    void isExerciseCompleted_ShouldReturnFalse_WhenNoCompletionExists() {
        UUID exerciseId = UUID.randomUUID();
        UUID planDayId = UUID.randomUUID();

        boolean isCompleted = completionService.isExerciseCompleted(userId, exerciseId, planDayId, 0);

        assertFalse(isCompleted);
    }
}

