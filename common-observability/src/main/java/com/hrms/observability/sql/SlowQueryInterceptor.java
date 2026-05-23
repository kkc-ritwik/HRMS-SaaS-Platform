package com.hrms.observability.sql;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lightweight slow-operation recorder. Services call:
 *
 *   slowQueries.record("payrollRun.compute", durationMs, "tenant", t);
 *
 * to flag operations exceeding the configured threshold. Output:
 *   · WARN log line above {@code hrms.sql.slow.threshold-ms} (default 500ms)
 *   · ERROR log line above {@code hrms.sql.slow.error-ms} (default 3000ms)
 *   · Micrometer counter {@code hrms.slow.operations} tagged by operation name
 *   · Micrometer timer   {@code hrms.operation.duration} for all calls
 *
 * For full Hibernate-level statement timing, attach a Hibernate StatementInspector in a
 * service-specific config (it requires the hibernate-core artifact which we keep out of
 * common-observability to stay dependency-light).
 */
@Slf4j
@Component
public class SlowQueryInterceptor {

    @Value("${hrms.sql.slow.threshold-ms:500}")
    private long thresholdMs;

    @Value("${hrms.sql.slow.error-ms:3000}")
    private long errorMs;

    private final MeterRegistry registry;
    private final Map<String, Counter> counters = new ConcurrentHashMap<>();

    public SlowQueryInterceptor(MeterRegistry registry) {
        this.registry = registry;
    }

    /** Record one operation completion; logs + meters automatically. */
    public void record(String operation, long durationMs, String... tagPairs) {
        Timer.builder("hrms.operation.duration")
                .tags(Tags.of(tagPairs).and("operation", operation))
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry)
                .record(Duration.ofMillis(durationMs));

        if (durationMs >= errorMs) {
            log.error("[SLOW-OP critical] {}ms — {}", durationMs, operation);
        } else if (durationMs >= thresholdMs) {
            log.warn("[SLOW-OP] {}ms — {}", durationMs, operation);
        }
        if (durationMs >= thresholdMs) {
            counters.computeIfAbsent(operation, op ->
                    Counter.builder("hrms.slow.operations").tags("operation", op).register(registry))
                    .increment();
        }
    }

    /** Time a Runnable and auto-record. */
    public void time(String operation, Runnable r) {
        long start = System.nanoTime();
        try {
            r.run();
        } finally {
            record(operation, (System.nanoTime() - start) / 1_000_000);
        }
    }
}
