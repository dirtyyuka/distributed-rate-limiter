package com.ratelimiter.distributed.limiter;

public interface RateLimiter {
    boolean isAllowed(String key);
}
