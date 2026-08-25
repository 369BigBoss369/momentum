package com.momentum.core;

import com.momentum.fitness.repository.CompletionRepository;
import com.momentum.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduledTasksServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CompletionRepository completionRepository;

    @InjectMocks
    private ScheduledTasksService scheduledTasksService;

    @BeforeEach
    void setUp() {
        // Setup mocks if needed
    }

    @Test
    void performDailyMaintenance_ShouldClearCachesAndLogStatistics() {
        // Given
        when(userRepository.count()).thenReturn(100L);
        when(completionRepository.count()).thenReturn(500L);

        // When
        scheduledTasksService.performDailyMaintenance();

        // Then
        verify(userRepository).count();
        verify(completionRepository).count();
        // Cache clearing would be verified if we had access to cache manager
    }

    @Test
    void updateUserStatistics_ShouldProcessAllUsers() {
        // Given - mocking the complex logic inside the method
        // This is a basic test to ensure the method can be called without exceptions

        // When
        scheduledTasksService.updateUserStatistics();

        // Then
        // Verify that userRepository.findAll() was called
        verify(userRepository).findAll();
    }
}

