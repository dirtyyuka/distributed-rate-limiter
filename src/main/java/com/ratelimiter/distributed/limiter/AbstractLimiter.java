package com.ratelimiter.distributed.limiter;

import java.time.Clock;

import org.springframework.data.redis.core.StringRedisTemplate;

public class AbstractLimiter {
    
    protected final StringRedisTemplate redisTemplate;
    protected final Clock clock;

    public AbstractLimiter(StringRedisTemplate redisTemplate, Clock clock) {
        this.redisTemplate = redisTemplate;
        this.clock = clock;
    }

    protected String buildKey(String userId) {
        return "rate_limit:" + userId;
    }
    
}
