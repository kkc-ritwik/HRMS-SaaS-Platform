package com.hrms.observability.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * Cross-cutting business metrics facade. Provides a typed API on top of Micrometer so
 * services don't repeat tag boilerplate. Metrics names use a {@code hrms.} prefix and
 * always carry {@code service} + {@code tenant} tags so Grafana dashboards can drill down.
 *
 * Examples:
 *   metrics.counter("payroll.runs.completed", "module", "payroll").increment();
 *   metrics.timed("recruitment.application.received", () -&gt; processApplication(...));
 */
@Component
@RequiredArgsConstructor
public class HrmsMetrics {

    private final MeterRegistry registry;

    /** Increment a domain counter — common business event. */
    public Counter counter(String name, String... tagPairs) {
        return Counter.builder("hrms." + name)
                .tags(Tags.of(tagPairs))
                .register(registry);
    }

    /** Time a function and record duration. */
    public <T> T timed(String name, Supplier<T> body, String... tagPairs) {
        Timer timer = Timer.builder("hrms." + name)
                .publishPercentiles(0.5, 0.95, 0.99)
                .publishPercentileHistogram()
                .tags(Tags.of(tagPairs))
                .register(registry);
        return timer.record(body);
    }

    public Timer timer(String name, String... tagPairs) {
        return Timer.builder("hrms." + name)
                .publishPercentiles(0.5, 0.95, 0.99)
                .publishPercentileHistogram()
                .tags(Tags.of(tagPairs))
                .register(registry);
    }

    /** Snapshot a gauge — e.g. open requisitions, pending approvals. */
    public void gauge(String name, Number value, String... tagPairs) {
        registry.gauge("hrms." + name, Tags.of(tagPairs), value);
    }

    public MeterRegistry registry() { return registry; }
}
