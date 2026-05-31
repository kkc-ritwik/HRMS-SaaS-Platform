package com.hrms.tenant.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Opt-in registration for the tenant platform features (async operations, feature flags,
 * tenant quotas). Activates only when {@code hrms.tenant.platform.enabled=true} so
 * services that don't use these features don't need to create the underlying tables.
 *
 * To enable for a service:
 *   1. Set hrms.tenant.platform.enabled=true in its application-prod.yml.
 *   2. Run the Flyway migration that adds async_operations, feature_flags, tenant_quotas
 *      (see common-tenant/src/main/resources/db/migration/V_tenant_platform.sql).
 */
@Configuration
@ConditionalOnProperty(name = "hrms.tenant.platform.enabled", havingValue = "true")
@ComponentScan(basePackages = {"com.hrms.tenant.async", "com.hrms.tenant.featureflag", "com.hrms.tenant.quota"})
@EntityScan(basePackages = {"com.hrms.tenant.async", "com.hrms.tenant.featureflag", "com.hrms.tenant.quota"})
@EnableJpaRepositories(basePackages = {"com.hrms.tenant.async", "com.hrms.tenant.featureflag", "com.hrms.tenant.quota"})
public class TenantPlatformAutoConfiguration {
}
