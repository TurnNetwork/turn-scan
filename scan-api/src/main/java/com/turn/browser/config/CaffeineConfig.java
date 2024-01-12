package com.turn.browser.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;


@Configuration
public class CaffeineConfig {

    /**
     * caffeine cache management
     */
    @Bean
    public CacheManager caffeineCacheManager() {
        Caffeine caffeine = Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(1000)
                .expireAfterWrite(5, TimeUnit.SECONDS);
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setAllowNullValues(true);
        cacheManager.setCaffeine(caffeine);
        return cacheManager;
    }

}
