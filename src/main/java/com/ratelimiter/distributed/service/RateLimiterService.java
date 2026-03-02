package com.ratelimiter.distributed.service;

import java.time.Clock;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.ratelimiter.distributed.util.RequestCounter;

@Service
public class RateLimiterService {

    // in-memory store for request counts, handling concurrency with AtomicInteger
    ConcurrentHashMap<String, RequestCounter> requestCounts = new ConcurrentHashMap<>();
    private final Clock clock;
    private final AtomicLong totalAllowed = new AtomicLong(0);
    private final AtomicLong totalBlocked = new AtomicLong(0);

    public RateLimiterService(Clock clock) {
        this.clock = clock;
    }

    private final long refillIntervalMs = 12000;
    private final int capacity = 5;

    // getters
    public long getAllowed() {
        return totalAllowed.get();
    }

    public long getBlocked() {
        return totalBlocked.get();
    }

    public boolean isAllowed(String userId) {
        RequestCounter counter = requestCounts
                .computeIfAbsent(userId,
                        id -> new RequestCounter(clock.millis(), clock.millis(), new AtomicInteger(capacity)));
        // bucket sort preventing burst requests
        synchronized (counter) {
            long now = clock.millis();
            long elapsedTime = now - counter.getLastRefillTimestamp();

            long tokensToAdd = (elapsedTime / refillIntervalMs);
            if (tokensToAdd > 0) {
                int newTokens = Math.min(capacity, counter.getTokens() + (int) tokensToAdd);
                counter.setTokens(newTokens);

                // update timestamps
                long timeConsumed = tokensToAdd * refillIntervalMs;
                counter.setLastRefillTimestamp(counter.getLastRefillTimestamp() + timeConsumed);
            }

            if (counter.getTokens() > 0) {
                counter.decrementTokens();
                counter.setLastAccessTimestamp(now);
                totalAllowed.incrementAndGet();
                return true;
            }

            totalBlocked.incrementAndGet();
            return false;
        }
    }

    @Scheduled(fixedRate = 60000) // every 60 secs 
    public void cleanupInactiveUsers() {
        long now = clock.millis();
        long inactivityLimit = 10 * 60_000; // 10 minutes

        requestCounts.forEach((userId, counter) -> {
            if (now - counter.getLastAccessTimestamp() > inactivityLimit) {
                requestCounts.remove(userId, counter);
            }
        });
    }

}