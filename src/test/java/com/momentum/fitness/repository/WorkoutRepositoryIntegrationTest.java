package com.momentum.fitness.repository;

import com.momentum.core.model.enums.ModerationStatus;
import com.momentum.fitness.model.Workout;
import com.momentum.fitness.model.enums.SourceType;
import com.momentum.fitness.model.enums.WorkoutType;
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
class WorkoutRepositoryIntegrationTest {

    @Autowired
    private WorkoutRepository workoutRepository;

    private Workout buildWorkout(String name, UUID ownerId, SourceType source, boolean isPublic, ModerationStatus status) {
        Workout workout = new Workout();
        workout.setName(name);
        workout.setType(WorkoutType.STRENGTH);
        workout.setOwnerId(ownerId);
        workout.setSource(source);
        workout.setIsPublic(isPublic);
        workout.setModerationStatus(status);
        return workout;
    }

    @Test
    void save_ShouldPersistWorkout() {
        UUID ownerId = UUID.randomUUID();
        Workout workout = buildWorkout("Push Day", ownerId, SourceType.CUSTOM, false, ModerationStatus.APPROVED);

        Workout saved = workoutRepository.save(workout);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getType()).isEqualTo(WorkoutType.STRENGTH);
        assertThat(saved.getWorkoutExercises()).isEmpty();
    }

    @Test
    void findByOwnerIdAndName_ShouldReturnWorkout_WhenExists() {
        UUID ownerId = UUID.randomUUID();
        workoutRepository.save(buildWorkout("Leg Day", ownerId, SourceType.CUSTOM, false, ModerationStatus.APPROVED));

        Optional<Workout> found = workoutRepository.findByOwnerIdAndName(ownerId, "Leg Day");

        assertThat(found).isPresent();
    }

    @Test
    void findByIsPublicTrueAndModerationStatus_ShouldReturnOnlyMatching() {
        UUID ownerId = UUID.randomUUID();
        workoutRepository.save(buildWorkout("Pending Workout", ownerId, SourceType.CUSTOM, true, ModerationStatus.PENDING));
        workoutRepository.save(buildWorkout("Approved Workout", ownerId, SourceType.CUSTOM, true, ModerationStatus.APPROVED));

        List<Workout> pending = workoutRepository.findByIsPublicTrueAndModerationStatus(ModerationStatus.PENDING);

        assertThat(pending).extracting(Workout::getName).containsExactly("Pending Workout");
    }

    @Test
    void findAllAccessible_ShouldIncludeOwnedWorkouts() {
        UUID ownerId = UUID.randomUUID();
        UUID otherId = UUID.randomUUID();
        workoutRepository.save(buildWorkout("My Workout", ownerId, SourceType.CUSTOM, false, ModerationStatus.APPROVED));
        workoutRepository.save(buildWorkout("Someone Else's Workout", otherId, SourceType.CUSTOM, false, ModerationStatus.APPROVED));

        List<Workout> accessible = workoutRepository.findAllAccessible(ownerId);

        assertThat(accessible).extracting(Workout::getName).contains("My Workout");
        assertThat(accessible).extracting(Workout::getName).doesNotContain("Someone Else's Workout");
    }

    @Test
    void findAccessibleByName_ShouldReturnEmpty_WhenNotAccessible() {
        UUID ownerId = UUID.randomUUID();
        UUID otherId = UUID.randomUUID();
        workoutRepository.save(buildWorkout("Private Workout", otherId, SourceType.CUSTOM, false, ModerationStatus.APPROVED));

        Optional<Workout> found = workoutRepository.findAccessibleByName(ownerId, "Private Workout");

        assertThat(found).isEmpty();
    }

    @Test
    void findByIdWithSharedUsers_ShouldFetchSharedUsersEagerly() {
        UUID ownerId = UUID.randomUUID();
        Workout saved = workoutRepository.save(buildWorkout("Pull Day", ownerId, SourceType.CUSTOM, false, ModerationStatus.APPROVED));

        Optional<Workout> found = workoutRepository.findByIdWithSharedUsers(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getSharedUsers()).isNotNull();
    }
}
