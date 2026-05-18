package com.hrms.skills.entity;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "skills", uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id","code"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Auditable("Skill")
@EntityListeners(AuditEntityListener.class)
public class Skill {
    @Id @GeneratedValue private UUID id;
    @Column(name = "tenant_id", length = 100, nullable = false) private String tenantId;
    @Column(length = 50, nullable = false) private String code;
    @Column(length = 200, nullable = false) private String name;
    @Column(length = 500) private String description;
    @Column(length = 50) private String category;          // TECHNICAL/SOFT/DOMAIN/CERTIFICATION
    @Column(name = "is_active") private Boolean isActive;
}
