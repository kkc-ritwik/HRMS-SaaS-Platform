package com.hrms.performance.entity;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "goal_updates")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Auditable("GoalUpdate")
@EntityListeners(AuditEntityListener.class)
public class GoalUpdate extends BaseEntity {

    @Column(name = "goal_id", nullable = false)
    private UUID goalId;

    @Column(name = "progress_value", nullable = false, precision = 5, scale = 2)
    private BigDecimal progressValue;

    @Column(name = "current_value", precision = 12, scale = 2)
    private BigDecimal currentValue;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;
}
