package com.ratelimiter.distributed.util;

import java.util.concurrent.atomic.AtomicInteger;

import lombok.Getter;

@Getter
public class RequestCounter {
    public AtomicInteger count;
    public long timestamp;

    public RequestCounter() {
        this.count = new AtomicInteger(0);
        this.timestamp = System.currentTimeMillis();
    }
}
