package com.momentum.core;

import com.momentum.fitness.repository.CompletionRepository;
import com.momentum.fitness.service.CompletionService;
import com.momentum.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class ScheduledTasksService {

    private final CompletionRepository completionRepository;
    private final UserRepository userRepository;
    private final CompletionService completionService;
    private final CacheManager cacheManager;

    @Autowired
    public ScheduledTasksService(CompletionRepository completionRepository,
                                UserRepository userRepository,
                                CompletionService completionService,
                                CacheManager cacheManager) {
        this.completionRepository = completionRepository;
        this.userRepository = userRepository;
        this.completionService = completionService;
        this.cacheManager = cacheManager;
    }
    
    @Scheduled(cron = "0 0 0 * * ?")
    public void performDailyMaintenance() {
        log.info("Starting daily maintenance task at {}", LocalDateTime.now());

        try {
            if (cacheManager != null) {
                cacheManager.getCacheNames().forEach(cacheName -> {
                    cacheManager.getCache(cacheName).clear();
                    log.info("Cleared cache: {}", cacheName);
                });
            }

            long totalUsers = userRepository.count();
            long totalCompletions = completionRepository.count();
            log.info("Daily system statistics - Total Users: {}, Total Completions: {}", totalUsers, totalCompletions);

            log.info("Daily maintenance task completed successfully");
        } catch (Exception e) {
            log.error("Error during daily maintenance task", e);
        }
    }

    @Scheduled(fixedRate = 3600000)
    public void updateUserStatistics() {
        log.info("Starting hourly user statistics update at {}", LocalDateTime.now());

        try {
            userRepository.findAll().forEach(user -> {
                try {
                    long workoutsThisWeek = completionService.getWorkoutsCompletedThisWeek(user.getId());
                    long totalMinutesThisWeek = completionService.getTotalMinutesExercisedThisWeek(user.getId());
                    long currentStreak = completionService.getCurrentDayStreak(user.getId());

                    log.debug("Updated statistics for user {}: workoutsThisWeek={}, totalMinutesThisWeek={}, currentStreak={}",
                             user.getUsername(), workoutsThisWeek, totalMinutesThisWeek, currentStreak);
                } catch (Exception e) {
                    log.warn("Failed to update statistics for user {}", user.getUsername(), e);
                }
            });

            log.info("Hourly user statistics update completed successfully");
        } catch (Exception e) {
            log.error("Error during hourly user statistics update", e);
        }
    }
}

