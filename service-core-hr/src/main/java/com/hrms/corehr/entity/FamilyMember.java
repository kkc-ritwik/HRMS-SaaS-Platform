package com.hrms.corehr.entity;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "family_members")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Auditable("FamilyMember")
@EntityListeners(AuditEntityListener.class)
public class FamilyMember extends BaseEntity {

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "relationship", nullable = false, length = 50)
    private String relationship;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "occupation", length = 150)
    private String occupation;

    @Column(name = "phone", length = 30)
    private String phone;

    @Column(name = "gender", length = 20)
    private String gender;

    @Column(name = "is_dependent")
    private boolean dependent;
}
