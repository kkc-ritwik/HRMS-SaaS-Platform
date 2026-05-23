package com.hrms.resilience.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

/**
 * Redis-backed mutex for scheduled jobs and one-shot critical sections. Uses
 * {@code SET key value NX EX ttl} for atomic acquire and a Lua check-and-delete
 * for safe release (only the holder can release its own token).
 *
 * Pattern for @Scheduled methods that should run on exactly one replica:
 *
 *   public void run() {
 *       lock.runIfLeader("payroll-eod-scan", Duration.ofMinutes(10), () -&gt; doWork());
 *   }
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DistributedLock {

    private static final String RELEASE_LUA = """
            if redis.call("get", KEYS[1]) == ARGV[1] then
              return redis.call("del", KEYS[1])
            else
              return 0
            end
            """;

    private final StringRedisTemplate redis;

    /** Try to acquire the lock; returns the release token if successful, otherwise null. */
    public String tryAcquire(String name, Duration ttl) {
        String token = UUID.randomUUID().toString();
        Boolean ok = redis.opsForValue().setIfAbsent("lock:" + name, token, ttl);
        return Boolean.TRUE.equals(ok) ? token : null;
    }

    public boolean release(String name, String token) {
        if (token == null) return false;
        org.springframework.data.redis.core.script.DefaultRedisScript<Long> script =
                new org.springframework.data.redis.core.script.DefaultRedisScript<>(RELEASE_LUA, Long.class);
        Long result = redis.execute(script, java.util.List.of("lock:" + name), token);
        return result != null && result > 0;
    }

    /** Convenience: acquire-run-release. Returns true if the work executed. */
    public boolean runIfLeader(String name, Duration ttl, Runnable work) {
        String token = tryAcquire(name, ttl);
        if (token == null) {
            log.debug("Lock {} already held — skipping", name);
            return false;
        }
        try {
            work.run();
            return true;
        } finally {
            release(name, token);
        }
    }
}
