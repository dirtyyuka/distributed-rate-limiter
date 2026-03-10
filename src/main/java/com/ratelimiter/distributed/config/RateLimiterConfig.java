package com.ratelimiter.distributed.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class RateLimiterConfig {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
    
}