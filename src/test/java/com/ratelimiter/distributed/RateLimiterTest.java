package com.ratelimiter.distributed;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ratelimiter.distributed.service.RateLimiterService;

public class RateLimiterTest {

    RateLimiterService limiter;

    @BeforeEach
    void setup() {
        limiter = new RateLimiterService(Clock.systemUTC());
    }

    @Test
    void shouldAllowUptoCapacity() {
        // allow 5 requests
        for (int i = 0; i < 5; i++) {
            assertTrue(limiter.isAllowed("user1"));
        }

        assertFalse(limiter.isAllowed("user1"));
    }

    @Test
    void shouldRefillTokensAfterInterval() throws InterruptedException {
        // consume all tokens
        for (int i = 0; i < 5; i++) {
            assertTrue(limiter.isAllowed("user2"));
        }   

        // should not allow again as 12 seconds not yet passed
        assertTrue(!limiter.isAllowed("user2"));
    }

    @Test
    void measureTrafficAndThroughput() throws InterruptedException {
        long start = System.nanoTime();
        // 1000 requests over 100 threads
        ExecutorService executor = Executors.newFixedThreadPool(100);
        for (int i = 0; i < 1000; i++) {
            executor.submit(() -> limiter.isAllowed("user1"));
        }

        // stop taking new tasks
        executor.shutdown();

        // wait current tasks to finish, atleast 1 min
        executor.awaitTermination(1, TimeUnit.MINUTES);

        // calculate time
        long end = System.nanoTime();
        System.out.println("Time " + (end - start));
        System.out.println("Total allowed: " + limiter.getAllowed());
        System.out.println("Total blocked: " + limiter.getBlocked());
    }
}
