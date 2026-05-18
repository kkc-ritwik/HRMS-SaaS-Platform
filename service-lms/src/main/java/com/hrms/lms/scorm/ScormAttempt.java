package com.hrms.lms.scorm;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * One per learner-per-package attempt. cmi data model values arrive from the SCORM
 * runtime API (LMSCommit calls); we store everything as a JSONB map. Standard fields
 * (status, score, time) get dedicated columns for fast querying / reporting.
 */
@Entity
@Table(name = "lms_scorm_attempts",
        indexes = @Index(name = "ix_scorm_attempt_emp", columnList = "tenant_id,employee_id,package_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ScormAttempt extends BaseEntity {

    @Column(name = "package_id", nullable = false) private UUID packageId;
    @Column(name = "employee_id", nullable = false) private UUID employeeId;
    @Column(name = "attempt_number", nullable = false) private int attemptNumber = 1;

    @Column(name = "lesson_status", length = 30) private String lessonStatus;       // passed/failed/completed/incomplete/browsed/not_attempted
    @Column(name = "completion_status", length = 30) private String completionStatus;  // SCORM 2004
    @Column(name = "success_status", length = 30) private String successStatus;        // SCORM 2004

    @Column(name = "score_raw", precision = 6, scale = 2) private java.math.BigDecimal scoreRaw;
    @Column(name = "score_min", precision = 6, scale = 2) private java.math.BigDecimal scoreMin;
    @Column(name = "score_max", precision = 6, scale = 2) private java.math.BigDecimal scoreMax;
    @Column(name = "score_scaled", precision = 5, scale = 4) private java.math.BigDecimal scoreScaled;

    @Column(name = "total_time_seconds") private Long totalTimeSeconds;
    @Column(name = "session_time_seconds") private Long sessionTimeSeconds;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "cmi_data", columnDefinition = "jsonb")
    private Map<String, Object> cmiData;     // raw key/value snapshot

    @Column(name = "started_at") private Instant startedAt;
    @Column(name = "last_committed_at") private Instant lastCommittedAt;
    @Column(name = "completed_at") private Instant completedAt;
}
