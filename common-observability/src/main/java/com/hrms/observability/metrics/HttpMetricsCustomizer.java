package com.hrms.observability.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Enables percentile histograms on http.server.requests so we can compute p95/p99 in
 * Prometheus/Grafana. Default Spring Boot only exposes a Timer summary.
 */
@Configuration
public class HttpMetricsCustomizer {

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> httpHistograms() {
        return r -> r.config().meterFilter(new MeterFilter() {
            @Override
            public DistributionStatisticConfig configure(io.micrometer.core.instrument.Meter.Id id, DistributionStatisticConfig config) {
                String name = id.getName();
                if (name.startsWith("http.server.requests")
                        || name.startsWith("http.client.requests")
                        || name.startsWith("hrms.")) {
                    return DistributionStatisticConfig.builder()
                            .percentilesHistogram(true)
                            .percentiles(0.5, 0.95, 0.99)
                            .serviceLevelObjectives(
                                    Duration.ofMillis(50).toNanos(),
                                    Duration.ofMillis(100).toNanos(),
                                    Duration.ofMillis(250).toNanos(),
                                    Duration.ofMillis(500).toNanos(),
                                    Duration.ofSeconds(1).toNanos(),
                                    Duration.ofSeconds(2).toNanos(),
                                    Duration.ofSeconds(5).toNanos())
                            .build().merge(config);
                }
                return config;
            }
        });
    }
}
