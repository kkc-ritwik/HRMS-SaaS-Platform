package com.hrms.corehr.entity;

import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "employees")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Employee extends BaseEntity {

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "employee_code", nullable = false, length = 30)
    private String employeeCode;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "middle_name", length = 100)
    private String middleName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "work_email", length = 255)
    private String workEmail;

    @Column(name = "phone", length = 30)
    private String phone;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 20)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "marital_status", length = 20)
    private MaritalStatus maritalStatus;

    @Column(name = "nationality", length = 100)
    private String nationality;

    @Column(name = "profile_picture_url", columnDefinition = "TEXT")
    private String profilePictureUrl;

    @Column(name = "department_id")
    private UUID departmentId;

    @Column(name = "designation_id")
    private UUID designationId;

    @Column(name = "location_id")
    private UUID locationId;

    @Column(name = "manager_id")
    private UUID managerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type", length = 30)
    private EmploymentType employmentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_status", nullable = false, length = 30)
    private EmploymentStatus employmentStatus = EmploymentStatus.ACTIVE;

    @Column(name = "join_date", nullable = false)
    private LocalDate joinDate;

    @Column(name = "confirmation_date")
    private LocalDate confirmationDate;

    @Column(name = "exit_date")
    private LocalDate exitDate;

    @Column(name = "notice_period_days")
    private Integer noticePeriodDays;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "custom_fields", columnDefinition = "jsonb")
    private Map<String, Object> customFields;

    // ── Statutory / compliance ─────────────────────────────────────────────────

    @Column(name = "pan_number", length = 10)
    private String panNumber;

    @Column(name = "aadhar_number", length = 12)
    private String aadharNumber;

    @Column(name = "uan_number", length = 12)
    private String uanNumber;

    @Column(name = "esi_number", length = 17)
    private String esiNumber;

    // ── Banking ───────────────────────────────────────────────────────────────

    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(name = "bank_account_number", length = 30)
    private String bankAccountNumber;

    @Column(name = "ifsc_code", length = 11)
    private String ifscCode;

    @Column(name = "bank_branch", length = 100)
    private String bankBranch;

    // ── Supplemental profile ──────────────────────────────────────────────────

    @Column(name = "blood_group", length = 5)
    private String bloodGroup;

    @Column(name = "display_name", length = 255)
    private String displayName;

    @Column(name = "phone_secondary", length = 20)
    private String phoneSecondary;

    @Column(name = "about_me", length = 1000)
    private String aboutMe;

    // ── HR references ─────────────────────────────────────────────────────────

    @Column(name = "secondary_manager_id")
    private UUID secondaryManagerId;

    @Column(name = "shift_id")
    private UUID shiftId;

    @Column(name = "pay_grade_id")
    private UUID payGradeId;

    // ── Key dates ─────────────────────────────────────────────────────────────

    @Column(name = "probation_end_date")
    private LocalDate probationEndDate;

    @Column(name = "resignation_date")
    private LocalDate resignationDate;

    @Column(name = "last_working_date")
    private LocalDate lastWorkingDate;

    // ── Miscellaneous ─────────────────────────────────────────────────────────

    @Column(name = "cost_center_code", length = 50)
    private String costCenterCode;

    @Column(name = "tags", columnDefinition = "TEXT")
    private String tags;

    // ── Enums ─────────────────────────────────────────────────────────────────

    public enum Gender {
        MALE, FEMALE, OTHER, PREFER_NOT_TO_SAY
    }

    public enum MaritalStatus {
        SINGLE, MARRIED, DIVORCED, WIDOWED
    }

    public enum EmploymentType {
        FULL_TIME, PART_TIME, CONTRACT, INTERN
    }

    public enum EmploymentStatus {
        ACTIVE, ON_LEAVE, PROBATION, TERMINATED, RESIGNED
    }
}
