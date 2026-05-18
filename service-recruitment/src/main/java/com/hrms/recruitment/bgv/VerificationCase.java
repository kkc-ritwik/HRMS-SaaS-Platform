package com.hrms.recruitment.bgv;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/** Persistent record of a candidate BGV case + final result for compliance/audit. */
@Entity
@Table(name = "bgv_verification_cases",
        indexes = @Index(name = "ix_bgv_candidate", columnList = "candidate_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class VerificationCase extends BaseEntity {

    @Column(name = "candidate_id", nullable = false) private UUID candidateId;
    @Column(name = "external_verification_id", length = 200) private String externalVerificationId;
    @Column(name = "provider", length = 30) private String provider;
    @Column(name = "package_type", length = 30) private String packageType;
    @Column(name = "status", length = 30) private String status;
    @Column(name = "overall_result", length = 30) private String overallResult;
    @Column(name = "tracking_url", length = 1000) private String trackingUrl;
    @Column(name = "report_url", length = 1000) private String reportUrl;
    @Column(name = "initiated_at") private Instant initiatedAt;
    @Column(name = "completed_at") private Instant completedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "vendor_response", columnDefinition = "jsonb")
    private Map<String, Object> vendorResponse;
}
