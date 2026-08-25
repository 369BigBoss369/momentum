package com.momentum.fitness.repository;

import com.momentum.fitness.model.Exercise;
import com.momentum.fitness.model.enums.ExerciseType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ExerciseRepositoryIntegrationTest {

    @Autowired
    private ExerciseRepository exerciseRepository;

    @Test
    void save_ShouldPersistExercise() {
        // Given
        Exercise exercise = Exercise.builder()
                .name("Push-up")
                .type(ExerciseType.STRENGTH)
                .build();

        // When
        Exercise saved = exerciseRepository.save(exercise);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Push-up");
        assertThat(saved.getType()).isEqualTo(ExerciseType.STRENGTH);
    }

    @Test
    void findById_ShouldReturnExercise_WhenExists() {
        // Given
        Exercise exercise = Exercise.builder()
                .name("Push-up")
                .type(ExerciseType.STRENGTH)
                .build();
        Exercise saved = exerciseRepository.save(exercise);

        // When
        Optional<Exercise> found = exerciseRepository.findById(saved.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Push-up");
    }

    @Test
    void findById_ShouldReturnEmpty_WhenNotExists() {
        // When
        Optional<Exercise> found = exerciseRepository.findById(java.util.UUID.randomUUID());

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void findAll_ShouldReturnAllExercises() {
        // Given
        long initialCount = exerciseRepository.count();

        Exercise exercise1 = Exercise.builder()
                .name("Push-up")
                .type(ExerciseType.STRENGTH)
                .build();
        Exercise exercise2 = Exercise.builder()
                .name("Squat")
                .type(ExerciseType.STRENGTH)
                .build();

        exerciseRepository.save(exercise1);
        exerciseRepository.save(exercise2);

        // When
        List<Exercise> exercises = exerciseRepository.findAll();

        // Then
        assertThat(exercises).hasSize((int) (initialCount + 2));
    }

    @Test
    void existsById_ShouldReturnTrue_WhenExists() {
        // Given
        Exercise exercise = Exercise.builder()
                .name("Deadlift")
                .type(ExerciseType.STRENGTH)
                .build();
        Exercise saved = exerciseRepository.save(exercise);

        // When
        boolean exists = exerciseRepository.existsById(saved.getId());

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsById_ShouldReturnFalse_WhenNotExists() {
        // When
        boolean exists = exerciseRepository.existsById(java.util.UUID.randomUUID());

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void delete_ShouldRemoveExercise() {
        // Given
        Exercise exercise = Exercise.builder()
                .name("Bench Press")
                .type(ExerciseType.STRENGTH)
                .build();
        Exercise saved = exerciseRepository.save(exercise);

        // When
        exerciseRepository.delete(saved);
        Optional<Exercise> found = exerciseRepository.findById(saved.getId());

        // Then
        assertThat(found).isEmpty();
    }
}
