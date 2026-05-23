package com.hrms.resilience.circuit;

import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Programmatic Resilience4j defaults shared across services. Per-service overrides go
 * in application.yml under {@code resilience4j.{circuitbreaker,retry,bulkhead,timelimiter}}.
 *
 * Named instances available everywhere:
 *   · {@code outbound}        — generic outbound HTTP / gRPC
 *   · {@code db}              — Postgres calls (looser thresholds)
 *   · {@code kafka}           — message publishing
 *   · {@code thirdparty}      — DocuSign / Razorpay / SMS providers (tight bulkhead)
 */
@Configuration
public class Resilience4jDefaults {

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig defaultCfg = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .slowCallRateThreshold(80)
                .slowCallDurationThreshold(Duration.ofSeconds(3))
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .permittedNumberOfCallsInHalfOpenState(5)
                .minimumNumberOfCalls(10)
                .slidingWindowSize(20)
                .recordExceptions(java.io.IOException.class,
                        java.util.concurrent.TimeoutException.class,
                        org.springframework.web.client.RestClientException.class)
                .build();

        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(defaultCfg);

        // Tighter for third-party calls — open fast, recover slow
        registry.addConfiguration("thirdparty", CircuitBreakerConfig.from(defaultCfg)
                .failureRateThreshold(40)
                .waitDurationInOpenState(Duration.ofMinutes(1))
                .slidingWindowSize(10)
                .build());

        // Looser for DB — Postgres rarely fails transiently
        registry.addConfiguration("db", CircuitBreakerConfig.from(defaultCfg)
                .failureRateThreshold(70)
                .slowCallDurationThreshold(Duration.ofSeconds(10))
                .waitDurationInOpenState(Duration.ofSeconds(10))
                .build());

        return registry;
    }

    @Bean
    public RetryRegistry retryRegistry() {
        RetryConfig defaultCfg = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(500))
                .retryExceptions(java.io.IOException.class,
                        java.util.concurrent.TimeoutException.class,
                        org.springframework.web.client.ResourceAccessException.class)
                .ignoreExceptions(IllegalArgumentException.class)
                .build();

        RetryRegistry registry = RetryRegistry.of(defaultCfg);
        registry.addConfiguration("kafka", RetryConfig.from(defaultCfg)
                .maxAttempts(5)
                .waitDuration(Duration.ofSeconds(2))
                .build());
        return registry;
    }

    @Bean
    public BulkheadRegistry bulkheadRegistry() {
        BulkheadConfig defaultCfg = BulkheadConfig.custom()
                .maxConcurrentCalls(50)
                .maxWaitDuration(Duration.ofMillis(500))
                .build();
        BulkheadRegistry registry = BulkheadRegistry.of(defaultCfg);
        registry.addConfiguration("thirdparty", BulkheadConfig.from(defaultCfg)
                .maxConcurrentCalls(10)
                .build());
        return registry;
    }

    @Bean
    public TimeLimiterRegistry timeLimiterRegistry() {
        TimeLimiterConfig defaultCfg = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(5))
                .cancelRunningFuture(true)
                .build();
        TimeLimiterRegistry registry = TimeLimiterRegistry.of(defaultCfg);
        registry.addConfiguration("thirdparty", TimeLimiterConfig.from(defaultCfg)
                .timeoutDuration(Duration.ofSeconds(20))
                .build());
        return registry;
    }
}
