package com.hrms.resilience.ratelimit;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Sliding-window rate limiter backed by Redis. Use for per-tenant or per-user throttling.
 */
@Service
@RequiredArgsConstructor
public class RedisRateLimiter {

    private final StringRedisTemplate redis;

    /** Returns true if the request is allowed; false if it should be rejected. */
    public boolean allow(String key, int maxRequests, Duration window) {
        String bucket = "ratelimit:" + key + ":" + (System.currentTimeMillis() / window.toMillis());
        Long count = redis.opsForValue().increment(bucket);
        if (count != null && count == 1L) {
            redis.expire(bucket, window);
        }
        return count != null && count <= maxRequests;
    }
}
