package com.hrms.compensation.benefits;

import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Dependent enrolled under an employee's insurance benefit (mediclaim, life, accident).
 * Required for ACA (US), India group-mediclaim, and most insurance providers.
 */
@Entity
@Table(name = "benefit_dependents",
        indexes = @Index(name = "ix_dependent_emp", columnList = "tenant_id,employee_id"))
@Auditable(value = "BenefitDependent", redactFields = "nationalId,dateOfBirth")
@EntityListeners(AuditEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class BenefitDependent extends BaseEntity {

    @Column(name = "employee_id", nullable = false) private UUID employeeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "relationship", length = 30, nullable = false)
    private Relationship relationship;

    @Column(name = "first_name", length = 100, nullable = false) private String firstName;
    @Column(name = "last_name", length = 100) private String lastName;
    @Column(name = "date_of_birth", nullable = false) private LocalDate dateOfBirth;
    @Column(name = "gender", length = 10) private String gender;

    @Column(name = "national_id", length = 200)
    @jakarta.persistence.Convert(converter = com.hrms.security.crypto.PiiEncryptedConverter.class)
    private String nationalId;

    @Column(name = "is_primary") private Boolean primary;
    @Column(name = "is_disabled") private Boolean disabled;
    @Column(name = "is_student") private Boolean student;

    @Column(name = "address_line1", length = 300) private String addressLine1;
    @Column(name = "city", length = 100) private String city;
    @Column(name = "state", length = 100) private String state;
    @Column(name = "postal_code", length = 20) private String postalCode;
    @Column(name = "country", length = 2) private String country;

    @Column(name = "phone", length = 30) private String phone;
    @Column(name = "email", length = 200) private String email;

    @Column(name = "enrolled_on") private LocalDate enrolledOn;
    @Column(name = "removed_on") private LocalDate removedOn;
    @Column(name = "removal_reason", length = 200) private String removalReason;

    public enum Relationship {
        SPOUSE, CHILD, DOMESTIC_PARTNER, FATHER, MOTHER,
        FATHER_IN_LAW, MOTHER_IN_LAW, SIBLING, OTHER
    }
}
