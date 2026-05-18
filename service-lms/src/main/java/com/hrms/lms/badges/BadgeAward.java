package com.hrms.lms.badges;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "lms_badge_awards",
        uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id","badge_id","employee_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class BadgeAward extends BaseEntity {
    @Column(name = "badge_id", nullable = false) private UUID badgeId;
    @Column(name = "employee_id", nullable = false) private UUID employeeId;
    @Column(name = "awarded_at", nullable = false) private Instant awardedAt;
    @Column(name = "trigger_event", length = 100) private String triggerEvent;
    @Column(name = "is_visible_publicly") private Boolean isVisiblePublicly;
}
