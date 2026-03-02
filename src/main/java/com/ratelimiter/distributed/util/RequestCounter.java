package com.ratelimiter.distributed.util;

import java.util.concurrent.atomic.AtomicInteger;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@AllArgsConstructor
public class RequestCounter {
    volatile long lastRefillTimestamp;
    volatile long lastAccessTimestamp;
    
    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    AtomicInteger tokens = new AtomicInteger();
    
    // manual getter, setters
    public void setTokens(int token) {
        tokens.set(token);
    }

    public int getTokens() {
        return tokens.get();
    }

    public void decrementTokens() {
        tokens.decrementAndGet();
    }
}
