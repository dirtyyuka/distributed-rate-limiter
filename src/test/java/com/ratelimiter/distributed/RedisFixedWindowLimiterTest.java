package com.ratelimiter.distributed;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.ratelimiter.distributed.limiter.RedisFixedWindowLimiter;

@ExtendWith(MockitoExtension.class)
public class RedisFixedWindowLimiterTest {
    
    RedisFixedWindowLimiter limiter;

    @Mock
    StringRedisTemplate redisTemplate;

    @Mock
    ValueOperations<String, String> operations;

    @BeforeEach
    void setup() {
        when(redisTemplate.opsForValue()).thenReturn(operations);
        limiter = new RedisFixedWindowLimiter(redisTemplate);
    }

    @Test
    void shouldAllowUptoCapacity() {
        when(operations.increment("rate_limit:user1"))
            .thenReturn(1L, 2L, 3L, 4L, 5L, 6L);
        
        for (int i = 0; i < 5; i++) {
            assertTrue(limiter.isAllowed("user1"));
        }

        assertFalse(limiter.isAllowed("user1"));
    }
}
