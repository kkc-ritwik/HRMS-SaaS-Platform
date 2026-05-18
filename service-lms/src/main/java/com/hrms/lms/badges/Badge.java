package com.hrms.lms.badges;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/** Badge that can be awarded for completing a course, path, or hitting a milestone. */
@Entity
@Table(name = "lms_badges", uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id","code"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Auditable("Badge")
@EntityListeners(AuditEntityListener.class)
public class Badge extends BaseEntity {
    @Column(name = "code", length = 50, nullable = false) private String code;
    @Column(name = "name", length = 200, nullable = false) private String name;
    @Column(name = "description", length = 1000) private String description;
    @Column(name = "icon_url", length = 1000) private String iconUrl;
    @Column(name = "color_hex", length = 9) private String colorHex;
    @Column(name = "points") private Integer points;
    @Column(name = "trigger_type", length = 50) private String triggerType;  // COURSE_COMPLETE / PATH_COMPLETE / MILESTONE
    @Column(name = "trigger_ref_id") private UUID triggerRefId;
    @Column(name = "is_active") private Boolean isActive;
}
