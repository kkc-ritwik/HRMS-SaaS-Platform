package com.hrms.auth.tenant;

import com.hrms.events.model.DomainEvent;
import com.hrms.events.model.Topics;
import com.hrms.events.publisher.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Creates a new tenant + emits {@code tenant.created} on Topics.EMPLOYEE so downstream
 * services (TenantDashboardSeeder, NotificationPreferencesSeeder, etc.) can seed defaults.
 *
 * The event payload carries tenant identity + the admin user id so listeners can
 * impersonate or attribute the bootstrap actions.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TenantOnboardingService {

    private final EventPublisher events;

    @Transactional
    public TenantCreated provisionTenant(NewTenantRequest req) {
        String tenantId = req.tenantId() == null || req.tenantId().isBlank()
                ? UUID.randomUUID().toString() : req.tenantId();

        events.publishDirect(Topics.EMPLOYEE, DomainEvent.of(
                "tenant.created", "auth",
                tenantId, tenantId, "Tenant",
                Map.of(
                        "tenantId", tenantId,
                        "displayName", req.displayName() == null ? "" : req.displayName(),
                        "adminEmail", req.adminEmail() == null ? "" : req.adminEmail(),
                        "country", req.country() == null ? "IN" : req.country(),
                        "baseCurrency", req.baseCurrency() == null ? "INR" : req.baseCurrency(),
                        "timeZone", req.timeZone() == null ? "Asia/Kolkata" : req.timeZone(),
                        "industry", req.industry() == null ? "" : req.industry(),
                        "createdAt", Instant.now().toString())));

        log.info("Tenant provisioned: {} ({})", tenantId, req.displayName());
        return new TenantCreated(tenantId, Instant.now());
    }

    public record NewTenantRequest(String tenantId, String displayName, String adminEmail,
                                    String country, String baseCurrency, String timeZone,
                                    String industry) {}
    public record TenantCreated(String tenantId, Instant createdAt) {}
}
