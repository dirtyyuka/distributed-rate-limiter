package com.ratelimiter.distributed;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.ratelimiter.distributed.limiter.RedisSlidingWindowLimiter;

@SpringBootTest
@Testcontainers
public class RedisTokenBucketLimiterTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7.0-alpine"))
            .withExposedPorts(6379);
    
    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));    
    }
    
    @Autowired
    private RedisSlidingWindowLimiter limiter;

    @Autowired 
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    void clearRedis() {
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
    }

    @Test
    void shouldAllowInitialRequestsAndBlockFollowing() {
        String userId = "user1";

        // rate limit
        for (int i = 0; i < 5; i++) {
            assertTrue(limiter.isAllowed(userId), "Request" + (i + 1) + "should be allowed");
        }

        assertFalse(limiter.isAllowed(userId), "6th request should be blocked");
    }

    @Test
    void shouldNotExceedCapacityUnderConcurrency() throws InterruptedException {
        int threads = 100;  
        int requests = 1000;

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        AtomicInteger allowed = new AtomicInteger();

        for (int i = 0; i < requests; i++) {
            executor.submit(() -> {
                if (limiter.isAllowed("user1")) {
                    allowed.incrementAndGet();
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        assertTrue(allowed.get() <= 5);
    }
    
}
