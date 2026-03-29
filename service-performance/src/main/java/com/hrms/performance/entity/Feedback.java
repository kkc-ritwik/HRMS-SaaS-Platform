package com.hrms.performance.entity;

import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "feedback")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Feedback extends BaseEntity {

    @Column(name = "from_employee_id", nullable = false)
    private UUID fromEmployeeId;

    @Column(name = "to_employee_id", nullable = false)
    private UUID toEmployeeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "feedback_type", nullable = false, length = 20)
    private FeedbackType feedbackType = FeedbackType.APPRECIATION;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false, length = 20)
    private Visibility visibility = Visibility.PUBLIC;

    @Column(name = "cycle_id")
    private UUID cycleId;

    @Column(name = "context", length = 200)
    private String context;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tags", columnDefinition = "jsonb")
    private List<String> tags;

    @Column(name = "is_anonymous", nullable = false)
    private boolean anonymous = false;

    // ── Enums ──────────────────────────────────────────────────────────────────

    public enum FeedbackType { APPRECIATION, CONSTRUCTIVE, IMPROVEMENT, NEUTRAL }

    public enum Visibility { PUBLIC, PRIVATE, MANAGER_ONLY }
}
