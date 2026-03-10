package com.ratelimiter.distributed.limiter;

import java.time.Clock;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;


@Component
public class RedisSlidingWindowLimiter extends AbstractLimiter implements RateLimiter {

    private static final DefaultRedisScript<Long> script = new DefaultRedisScript<>();
    private final int capacity;
    AtomicLong allowedRequests = new AtomicLong();
    AtomicLong blockedRequests = new AtomicLong();

    public RedisSlidingWindowLimiter(
            Clock clock,
            StringRedisTemplate redisTemplate,
            @Value("${ratelimit.capacity}") int capacity,
            @Value("${ratelimit.redis-sliding-window-script-path}") String scriptPath) {
        super(redisTemplate, clock);
        this.capacity = capacity;
        script.setLocation(new ClassPathResource(scriptPath));
        script.setResultType(Long.class);
    }

    @Override
    public boolean isAllowed(String userId) {
        String key = buildKey(userId);

        Long result = redisTemplate.execute(
                script,
                List.of(key),
                String.valueOf(capacity)
        );

        if (result == 1) {
            allowedRequests.incrementAndGet();
            return true;
        }

        blockedRequests.incrementAndGet();
        return false;
    }
}
