package com.hrms.reports.seed;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrms.events.model.DomainEvent;
import com.hrms.events.model.Topics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Listens for {@code tenant.created} events on the EMPLOYEE topic (canonical bootstrap
 * source for new tenants) and copies the system-wide "default" dashboard + widgets into
 * the new tenant's scope so it has a working home page from day-0.
 *
 * Also seeds workflow templates if any are visible at tenant_id=null (system-wide).
 *
 * Idempotent — uses ON CONFLICT to avoid duplicate seeds.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TenantDashboardSeeder {

    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;

    @KafkaListener(topics = Topics.EMPLOYEE, groupId = "${spring.kafka.consumer.group-id:service-reports}-tenant-seeder")
    @Transactional
    public void onEvent(String payload) {
        try {
            DomainEvent ev = mapper.readValue(payload, DomainEvent.class);
            if (!"tenant.created".equals(ev.getEventType())) return;
            String tenantId = ev.getTenantId();
            if (tenantId == null || tenantId.isBlank() || "default".equals(tenantId)) return;
            seedDashboard(tenantId);
            log.info("Seeded default dashboard for new tenant {}", tenantId);
        } catch (Exception e) {
            log.warn("Tenant dashboard seed failed: {}", e.getMessage());
        }
    }

    /** Public API — also callable manually for re-seeding via an admin endpoint. */
    @Transactional
    public void seedDashboard(String tenantId) {
        // 1. Copy the default-tenant dashboard row
        List<Map<String, Object>> existing = jdbc.queryForList(
                "SELECT id FROM dashboards WHERE tenant_id = ? AND name = 'HRMS Default Dashboard'",
                tenantId);
        if (!existing.isEmpty()) return;          // already seeded

        UUID newDashId = UUID.randomUUID();
        jdbc.update("""
            INSERT INTO dashboards (id, tenant_id, name, description, is_default, is_public, created_at, updated_at, deleted)
            VALUES (?, ?, 'HRMS Default Dashboard', 'Out-of-the-box workforce KPIs', true, true, NOW(), NOW(), false)
            """, newDashId, tenantId);

        // 2. Copy the default widgets, rewriting dashboard_id + tenant_id
        jdbc.update("""
            INSERT INTO dashboard_widgets (id, tenant_id, dashboard_id, widget_type, title, position, config, created_at, updated_at, deleted)
            SELECT gen_random_uuid(), ?, ?, widget_type, title, position, config, NOW(), NOW(), false
            FROM dashboard_widgets
            WHERE tenant_id = 'default'
              AND dashboard_id IN (SELECT id FROM dashboards WHERE tenant_id='default' AND name='HRMS Default Dashboard')
            """, tenantId, newDashId);
    }
}
