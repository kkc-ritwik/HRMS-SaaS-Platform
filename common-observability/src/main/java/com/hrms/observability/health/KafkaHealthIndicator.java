package com.hrms.observability.health;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Liveness probe for Kafka. The Spring Boot starter only ships a Kafka health indicator
 * when the consumer is auto-configured; this one works for any service that has a
 * bootstrap-servers property even if it only produces.
 */
@Configuration
@ConditionalOnClass(AdminClient.class)
class KafkaHealthIndicatorConfig {

    @Component("kafka")
    @ConditionalOnEnabledHealthIndicator("kafka")
    public static class Indicator implements HealthIndicator {

        @Value("${spring.kafka.bootstrap-servers:}")
        private String bootstrap;

        @Override
        public Health health() {
            if (bootstrap == null || bootstrap.isBlank()) {
                return Health.unknown().withDetail("reason", "bootstrap-servers not configured").build();
            }
            Properties props = new Properties();
            props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
            props.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, "3000");
            props.put(AdminClientConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG, "5000");
            try (AdminClient admin = AdminClient.create(props)) {
                DescribeClusterResult r = admin.describeCluster();
                String clusterId = r.clusterId().get(3, TimeUnit.SECONDS);
                int nodes = r.nodes().get(3, TimeUnit.SECONDS).size();
                return Health.up()
                        .withDetail("clusterId", clusterId)
                        .withDetail("nodes", nodes)
                        .withDetail("bootstrap", bootstrap)
                        .build();
            } catch (Exception e) {
                return Health.down(e).withDetail("bootstrap", bootstrap).build();
            }
        }
    }
}
