package com.example.analytics.service;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class RateLimiter {
    private final int CAPACITY = 100; // 100 req/sec
    private final AtomicInteger tokens = new AtomicInteger(CAPACITY);
    private final AtomicLong lastRefillTime = new AtomicLong(System.currentTimeMillis());

    public boolean tryConsume() {
        refill();
        if (tokens.get() > 0) {
            tokens.decrementAndGet();
            return true;
        }
        return false;
    }

    private void refill() {
        long now = System.currentTimeMillis();
        long last = lastRefillTime.get();
        if (now - last > 1000) {
            if (lastRefillTime.compareAndSet(last, now)) {
                tokens.set(CAPACITY);
            }
        }
    }
}
