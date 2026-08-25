package com.momentum.fitness.service;

import com.momentum.exception.fitness.CustomActivityAlreadyExists;
import com.momentum.fitness.dto.CreateExerciseDTO;
import com.momentum.fitness.model.Exercise;
import com.momentum.fitness.model.enums.ExerciseType;
import com.momentum.fitness.model.enums.SourceType;
import com.momentum.fitness.repository.ExerciseRepository;
import com.momentum.user.model.User;
import com.momentum.user.model.enums.UserRole;
import com.momentum.user.service.FitnessActivityService;
import com.momentum.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExerciseServiceUnitTest {

    @Mock
    private ExerciseRepository exerciseRepository;

    @Mock
    private UserService userService;

    @Mock
    private FitnessActivityService fitnessActivityService;

    @InjectMocks
    private ExerciseService exerciseService;

    private CreateExerciseDTO createExerciseDto;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        createExerciseDto = CreateExerciseDTO.builder()
                .name("Push-ups")
                .type(ExerciseType.STRENGTH)
                .imageUrl("http://example.com/image.jpg")
                .videoUrl("http://example.com/video.mp4")
                .isPublic(true)
                .build();
    }

    @Test
    void createExercise_ShouldCreateExercise_WhenValidData() {
        when(exerciseRepository.findByOwnerIdAndName(userId, "Push-ups")).thenReturn(Optional.empty());

        exerciseService.createExercise(createExerciseDto, userId);

        verify(exerciseRepository).save(any(Exercise.class));
    }

    @Test
    void createExercise_ShouldThrowException_WhenExerciseAlreadyExists() {
        when(exerciseRepository.findByOwnerIdAndName(userId, "Push-ups"))
                .thenReturn(Optional.of(Exercise.builder().name("Push-ups").build()));

        assertThrows(CustomActivityAlreadyExists.class,
                () -> exerciseService.createExercise(createExerciseDto, userId));

        verify(exerciseRepository, never()).save(any(Exercise.class));
    }

    @Test
    void getAccessibleById_ShouldReturnExercise_WhenExists() {
        UUID exerciseId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        Exercise exercise = Exercise.builder()
                .id(exerciseId)
                .name("Push-ups")
                .build();
        User currentUser = User.builder().id(ownerId).role(UserRole.USER).build();

        when(exerciseRepository.findByIdWithMuscleTargetsForViewing(exerciseId)).thenReturn(Optional.of(exercise));

        Exercise result = exerciseService.getAccessibleById(exerciseId, currentUser);

        assertEquals(exercise, result);
    }

    @Test
    void getAccessibleById_ShouldThrowException_WhenNotFound() {
        UUID exerciseId = UUID.randomUUID();
        User currentUser = User.builder().id(UUID.randomUUID()).role(UserRole.USER).build();
        when(exerciseRepository.findByIdWithMuscleTargetsForViewing(exerciseId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> exerciseService.getAccessibleById(exerciseId, currentUser));
    }

    @Test
    void getAccessibleById_ShouldThrowException_WhenUserCannotView() {
        UUID exerciseId = UUID.randomUUID();
        Exercise exercise = Exercise.builder()
                .id(exerciseId)
                .name("Push-ups")
                .build();
        User currentUser = User.builder().id(UUID.randomUUID()).role(UserRole.USER).build();

        when(exerciseRepository.findByIdWithMuscleTargetsForViewing(exerciseId)).thenReturn(Optional.of(exercise));

        assertThrows(RuntimeException.class, () -> exerciseService.getAccessibleById(exerciseId, currentUser));
    }
}
