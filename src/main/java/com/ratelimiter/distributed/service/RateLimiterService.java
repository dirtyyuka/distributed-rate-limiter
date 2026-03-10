package com.ratelimiter.distributed.service;
import org.springframework.stereotype.Service;

import com.ratelimiter.distributed.limiter.InMemoryTokenBucketLimiter;

@Service
public class RateLimiterService {

    private final InMemoryTokenBucketLimiter rateLimiter1;

    public RateLimiterService(InMemoryTokenBucketLimiter rateLimiter1) {
        this.rateLimiter1 = rateLimiter1;
    }

    public boolean validateRequest(String userId) {
        return rateLimiter1.isAllowed(userId);
    }
}