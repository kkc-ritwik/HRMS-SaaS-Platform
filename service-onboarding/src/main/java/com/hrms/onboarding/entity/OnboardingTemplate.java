package com.hrms.onboarding.entity;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "onboarding_templates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Auditable("OnboardingTemplate")
@EntityListeners(AuditEntityListener.class)
public class OnboardingTemplate extends BaseEntity {

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "role_id")
    private UUID roleId;

    @Column(name = "department_id")
    private UUID departmentId;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;
}
