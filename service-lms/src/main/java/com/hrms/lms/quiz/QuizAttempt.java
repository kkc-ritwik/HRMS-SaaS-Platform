package com.hrms.lms.quiz;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "lms_quiz_attempts",
        indexes = @Index(name = "ix_attempt_emp_assess", columnList = "tenant_id,employee_id,assessment_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class QuizAttempt extends BaseEntity {

    @Column(name = "assessment_id", nullable = false) private UUID assessmentId;
    @Column(name = "employee_id", nullable = false) private UUID employeeId;
    @Column(name = "attempt_number", nullable = false) private Integer attemptNumber;

    @Column(name = "started_at") private Instant startedAt;
    @Column(name = "submitted_at") private Instant submittedAt;
    @Column(name = "time_spent_seconds") private Integer timeSpentSeconds;

    @Column(name = "score", precision = 6, scale = 2) private BigDecimal score;
    @Column(name = "max_score", precision = 6, scale = 2) private BigDecimal maxScore;
    @Column(name = "score_percent", precision = 5, scale = 2) private BigDecimal scorePercent;
    @Column(name = "passed") private Boolean passed;

    /** Map of { questionId -> answer payload (option ids or text) }. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "answers", columnDefinition = "jsonb")
    private Map<String, Object> answers;

    /** Map of { questionId -> { earned, max, correct } } from auto-grading. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "grading", columnDefinition = "jsonb")
    private Map<String, Object> grading;
}
