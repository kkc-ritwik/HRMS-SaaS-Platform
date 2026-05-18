package com.hrms.reports.entity;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Scheduled-report definition + run-state. Polled by ScheduledReportRunner every minute. */
@Entity
@Table(name = "report_schedules",
        indexes = @Index(name = "ix_report_sched_due", columnList = "next_fire_at"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ReportSchedule extends BaseEntity {

    @Column(name = "report_definition_id", nullable = false) private UUID reportDefinitionId;
    @Column(name = "name", length = 200, nullable = false) private String name;

    /** Parameterized SQL stored at the report-definition level; cached here for quick run. */
    @Column(name = "sql_query", columnDefinition = "TEXT", nullable = false) private String sqlQuery;

    /** JSON bindings for :named parameters (e.g. {"departmentId":"...","year":2026}). */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "bindings", columnDefinition = "jsonb") private Map<String, Object> bindings;

    @Column(name = "max_rows", nullable = false) private int maxRows = 10000;
    @Column(name = "format", length = 10, nullable = false) private String format;     // CSV / XLSX / PDF

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "recipients", columnDefinition = "jsonb", nullable = false)
    private List<String> recipients;

    /** Quartz-style cron. e.g. "0 0 9 * * MON" â€” every Monday 09:00 */
    @Column(name = "cron_expression", length = 80, nullable = false) private String cronExpression;
    @Column(name = "time_zone", length = 50) private String timeZone;

    @Column(name = "next_fire_at") private OffsetDateTime nextFireAt;
    @Column(name = "last_fired_at") private OffsetDateTime lastFiredAt;
    @Column(name = "last_error", length = 2000) private String lastError;
    @Column(name = "consecutive_failures") private Integer consecutiveFailures;

    @Column(name = "is_active", nullable = false) private boolean active = true;
}
