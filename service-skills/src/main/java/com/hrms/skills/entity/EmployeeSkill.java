package com.hrms.skills.entity;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "employee_skills",
        uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id","employee_id","skill_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EmployeeSkill {
    @Id @GeneratedValue private UUID id;
    @Column(name = "tenant_id", length = 100, nullable = false) private String tenantId;
    @Column(name = "employee_id", nullable = false) private UUID employeeId;
    @Column(name = "skill_id", nullable = false) private UUID skillId;
    /** 1 = Beginner, 2 = Intermediate, 3 = Advanced, 4 = Expert, 5 = Master */
    @Column(nullable = false) private Integer proficiency;
    @Column(name = "years_of_experience") private Double yearsOfExperience;
    @Column(name = "self_rated") private Boolean selfRated;
    @Column(name = "manager_endorsed") private Boolean managerEndorsed;
    @Column(name = "last_used") private LocalDate lastUsed;
}
