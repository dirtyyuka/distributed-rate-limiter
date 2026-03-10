package com.ratelimiter.distributed.limiter;

import java.time.Duration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisFixedWindowLimiter implements RateLimiter {
    private StringRedisTemplate redisTemplate;

    public RedisFixedWindowLimiter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean isAllowed(String userId) {
        String key = "rate_limit:" + userId;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count == 1) {
            redisTemplate.expire(key, Duration.ofMinutes(1));
        }
        return count <= 5;
    }
}