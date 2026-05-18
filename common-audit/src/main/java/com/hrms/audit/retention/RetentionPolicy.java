package com.hrms.audit.retention;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Per-entity retention windows (in days). Beyond the window, rows are purged by the
 * nightly RetentionScheduler. Defaults respect typical compliance:
 *   - 2555 days (7 years) for audit + payroll-touching records
 *   - 365 days for transactional ops (login history, notifications)
 *   - 90 days for ephemeral data
 *
 * Override via env: HRMS_RETENTION_AUDIT_LOGS_DAYS=3650 etc.
 */
@Getter @Setter
@Component
@ConfigurationProperties(prefix = "hrms.retention")
public class RetentionPolicy {

    private boolean enabled = true;

    private Map<String, Integer> tableDays = new HashMap<>() {{
        put("audit_logs",         2555);   // 7 years
        put("outbox_events",        30);   // 30 days post-sent
        put("login_history",       365);   // 1 year
        put("notifications",       180);   // 6 months
        put("webhook_deliveries",   60);
        put("attendance_punches", 1825);   // 5 years (statutory)
        put("session",              30);
        put("temp_files",            7);
    }};

    /** How many rows to delete per pass (avoid hammering the DB). */
    private int batchSize = 5000;

    /** Cron for the nightly purge — default 02:30 daily. */
    private String cron = "0 30 2 * * *";
}
