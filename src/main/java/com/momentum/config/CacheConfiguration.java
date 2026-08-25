package com.momentum.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
@Slf4j
public class CacheConfiguration {

    @Bean
    public CacheManager cacheManager() {
        log.info("Initializing cache manager with caches: userStats, completions, workouts, userProfiles");
        CacheManager cacheManager = new ConcurrentMapCacheManager(
            "userStats",
            "completions",
            "workouts",
            "userProfiles"
        );
        log.info("Cache manager initialized successfully");
        return cacheManager;
    }
}

