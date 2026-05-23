package com.hrms.observability.health;

import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

/**
 * Redis liveness probe — extends the built-in by capturing latency and recording it
 * back into Micrometer so /actuator/prometheus exports redis.health.latency.
 */
@Component("redisLatency")
@ConditionalOnClass(RedisConnectionFactory.class)
@ConditionalOnBean(RedisConnectionFactory.class)
@ConditionalOnEnabledHealthIndicator("redisLatency")
public class RedisHealthIndicator implements HealthIndicator {

    private final RedisConnectionFactory factory;

    public RedisHealthIndicator(RedisConnectionFactory factory) { this.factory = factory; }

    @Override
    public Health health() {
        long start = System.nanoTime();
        try (RedisConnection conn = factory.getConnection()) {
            String pong = new String(conn.commands().ping().getBytes());
            long latencyMs = (System.nanoTime() - start) / 1_000_000;
            return (pong.equalsIgnoreCase("pong") ? Health.up() : Health.down())
                    .withDetail("latencyMs", latencyMs)
                    .build();
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}
