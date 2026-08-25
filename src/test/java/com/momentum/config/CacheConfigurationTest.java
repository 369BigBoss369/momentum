package com.momentum.config;

import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import static org.assertj.core.api.Assertions.assertThat;

class CacheConfigurationTest {

    @Test
    void cacheManager_ShouldCreateConcurrentMapCacheManager() {
        // Given
        CacheConfiguration config = new CacheConfiguration();

        // When
        CacheManager cacheManager = config.cacheManager();

        // Then
        assertThat(cacheManager).isInstanceOf(ConcurrentMapCacheManager.class);
        ConcurrentMapCacheManager concurrentCacheManager = (ConcurrentMapCacheManager) cacheManager;

        // Verify expected cache names are present
        assertThat(concurrentCacheManager.getCacheNames())
                .contains("userStats", "completions", "workouts", "userProfiles");
    }

    @Test
    void cacheManager_ShouldHaveCorrectNumberOfCaches() {
        // Given
        CacheConfiguration config = new CacheConfiguration();

        // When
        CacheManager cacheManager = config.cacheManager();

        // Then
        ConcurrentMapCacheManager concurrentCacheManager = (ConcurrentMapCacheManager) cacheManager;
        assertThat(concurrentCacheManager.getCacheNames()).hasSize(4);
    }
}