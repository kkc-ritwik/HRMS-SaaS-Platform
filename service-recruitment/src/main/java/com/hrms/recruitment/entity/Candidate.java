package com.hrms.recruitment.entity;

import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "candidates")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Candidate extends BaseEntity {

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "email", nullable = false, length = 200)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "current_company", length = 200)
    private String currentCompany;

    @Column(name = "current_title", length = 200)
    private String currentTitle;

    @Column(name = "total_experience_years", precision = 4, scale = 1)
    private BigDecimal totalExperienceYears;

    /** MinIO object key or presigned URL for the resume. */
    @Column(name = "resume_url", length = 1000)
    private String resumeUrl;

    @Column(name = "linkedin_url", length = 500)
    private String linkedinUrl;

    /** AGENCY, REFERRAL, JOB_PORTAL, WALK_IN, CAMPUS */
    @Column(name = "source", length = 50)
    private String source;

    @Column(name = "agency_id")
    private UUID agencyId;

    /** Employee who referred this candidate. */
    @Column(name = "referred_by")
    private UUID referredBy;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tags", columnDefinition = "jsonb")
    private List<String> tags;
}
