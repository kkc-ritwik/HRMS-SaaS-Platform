package com.hrms.gateway;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

/**
 * Edge configuration:
 *   1. KeyResolver — buckets the rate-limiter by tenant+user (falls back to client IP)
 *      so a noisy tenant doesn't starve everyone else.
 *   2. Security headers and structured response headers are applied via gateway
 *      default-filters in application.yml.
 */
@Configuration
public class GatewaySupport {

    @Bean
    public KeyResolver tenantUserKeyResolver() {
        return exchange -> {
            var headers = exchange.getRequest().getHeaders();
            String tenant = headers.getFirst("X-Tenant-Id");
            String user = headers.getFirst("X-User-Id");
            if (user != null && !user.isBlank()) {
                return Mono.just("u:" + (tenant == null ? "_" : tenant) + ":" + user);
            }
            String ip = exchange.getRequest().getRemoteAddress() == null
                    ? "unknown"
                    : exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
            return Mono.just("ip:" + ip);
        };
    }
}
