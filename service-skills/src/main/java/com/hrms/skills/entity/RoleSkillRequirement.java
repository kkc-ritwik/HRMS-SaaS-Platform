package com.hrms.skills.entity;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "role_skill_requirements",
        uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id","designation_id","skill_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RoleSkillRequirement {
    @Id @GeneratedValue private UUID id;
    @Column(name = "tenant_id", length = 100, nullable = false) private String tenantId;
    @Column(name = "designation_id", nullable = false) private UUID designationId;
    @Column(name = "skill_id", nullable = false) private UUID skillId;
    @Column(name = "min_proficiency", nullable = false) private Integer minProficiency;
    @Column(name = "is_mandatory") private Boolean isMandatory;
}
