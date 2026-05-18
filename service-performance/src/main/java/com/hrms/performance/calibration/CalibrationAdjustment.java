package com.hrms.performance.calibration;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/** A single review's rating adjustment from a calibration session. */
@Entity
@Table(name = "calibration_adjustments",
        uniqueConstraints = @UniqueConstraint(columnNames = {"session_id","review_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CalibrationAdjustment extends BaseEntity {

    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

    @Column(name = "review_id", nullable = false)
    private UUID reviewId;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "original_rating", precision = 4, scale = 2)
    private BigDecimal originalRating;

    @Column(name = "adjusted_rating", precision = 4, scale = 2, nullable = false)
    private BigDecimal adjustedRating;

    @Column(name = "justification", length = 2000)
    private String justification;

    @Column(name = "adjusted_by")
    private UUID adjustedBy;
}
