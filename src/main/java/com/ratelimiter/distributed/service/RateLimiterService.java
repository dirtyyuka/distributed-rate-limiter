package com.ratelimiter.distributed.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.ratelimiter.distributed.util.RequestCounter;

@Service
public class RateLimiterService {

    // in-memory store for request counts, handling concurrency with AtomicInteger
    Map<String, RequestCounter> requestCounts = new HashMap<>();

    public boolean isAllowed(String userId) {
        long now = System.currentTimeMillis();
        // request check
        RequestCounter counter = requestCounts.computeIfAbsent(userId, id -> new RequestCounter());

        if (now - counter.getTimestamp() > 60000) {
            // lock
            synchronized (counter) {
                // check updated
                if (now - counter.getTimestamp() > 60000) {
                    counter.timestamp = now;
                    counter.count.set(1);
                }
            }
        }

        int current = counter.count.incrementAndGet();
        return current <= 5; // allow max 5 requests per minute
    }
    
}