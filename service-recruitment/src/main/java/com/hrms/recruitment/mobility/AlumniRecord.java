package com.hrms.recruitment.mobility;

import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Alumni network — every leaver is automatically enrolled (unless they opt out during exit
 * interview). Personal email + Linked-in stored separately from former employee record so
 * payroll history can be archived/redacted independently. boomerangEligible mirrors the
 * "rehire-recommended" flag set by the last manager on the exit interview.
 */
@Entity
@Table(name = "recruit_alumni",
        indexes = @Index(name = "ix_alumni_email", columnList = "tenant_id,personal_email"))
@Auditable(value = "AlumniRecord", redactFields = "personalEmail,phone")
@EntityListeners(AuditEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class AlumniRecord extends BaseEntity {

    @Column(name = "former_employee_id", nullable = false) private UUID formerEmployeeId;
    @Column(name = "full_name", length = 200, nullable = false) private String fullName;
    @Column(name = "personal_email", length = 200) private String personalEmail;
    @Column(name = "phone", length = 30) private String phone;
    @Column(name = "linkedin_url", length = 500) private String linkedinUrl;

    @Column(name = "last_designation", length = 200) private String lastDesignation;
    @Column(name = "last_department", length = 200) private String lastDepartment;
    @Column(name = "hire_date") private LocalDate hireDate;
    @Column(name = "exit_date") private LocalDate exitDate;
    @Column(name = "tenure_years") private Integer tenureYears;
    @Column(name = "exit_reason", length = 500) private String exitReason;

    @Column(name = "rehire_recommended") private Boolean rehireRecommended;
    @Column(name = "boomerang_eligible") private Boolean boomerangEligible;
    @Column(name = "do_not_rehire") private Boolean doNotRehire;
    @Column(name = "do_not_rehire_reason", length = 1000) private String doNotRehireReason;

    @Column(name = "opt_in_alumni_comms") private Boolean optInAlumniComms = true;
    @Column(name = "opt_in_job_alerts") private Boolean optInJobAlerts;

    @Column(name = "rejoined_on") private LocalDate rejoinedOn;
    @Column(name = "rejoined_employee_id") private UUID rejoinedEmployeeId;
}
