package com.ratelimiter.distributed.limiter;

import java.time.Clock;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

@Component
@Primary
public class RedisTokenBucketLimiter extends AbstractLimiter implements RateLimiter {

    // configs
    private static final DefaultRedisScript<Long> script = new DefaultRedisScript<>();
    private final int capacity;
    private final long refillRate;
    AtomicLong allowedRequests = new AtomicLong();
    AtomicLong blockedRequests = new AtomicLong();

    public RedisTokenBucketLimiter(
            Clock clock,
            StringRedisTemplate redisTemplate,
            @Value("${ratelimit.capacity}") int capacity,
            @Value("${ratelimit.refill-rate}") long refillRate,
            @Value("${ratelimit.redis-token-bucket-script-path}") String scriptPath) {
        super(redisTemplate, clock);
        this.capacity = capacity;
        this.refillRate = refillRate;
        script.setLocation(new ClassPathResource(scriptPath));
        script.setResultType(Long.class);
    }

    @Override
    public boolean isAllowed(String userId) {
        String key = buildKey(userId);

        Long result = redisTemplate.execute(
                script,
                List.of(key),
                String.valueOf(this.capacity), // capacity
                String.valueOf(this.refillRate), // refillRate (ms)
                String.valueOf(clock.millis()));

        if (result == 1) {
            allowedRequests.incrementAndGet();
            return true;
        }

        blockedRequests.incrementAndGet();
        return false;
    }

}
