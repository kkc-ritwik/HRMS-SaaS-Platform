package com.hrms.compliance.statutory;

import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * A filed (or in-progress) statutory return — TDS 24Q/26Q, Professional Tax,
 * PF ECR, ESI, etc. Tracks generation + submission lifecycle and the FVU/challan URI.
 */
@Entity
@Table(name = "statutory_returns")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Auditable("StatutoryReturn")
@EntityListeners(AuditEntityListener.class)
public class StatutoryReturn extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "return_type", length = 20, nullable = false)
    private ReturnType returnType;

    @Column(name = "financial_year", length = 10, nullable = false) private String financialYear;
    @Column(name = "period", length = 20) private String period;     // Q1..Q4 / month

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private Status status = Status.DRAFT;

    @Column(name = "storage_uri", length = 1000) private String storageUri;
    @Column(name = "acknowledgement_number", length = 100) private String acknowledgementNumber;
    @Column(name = "total_amount", precision = 16, scale = 2) private BigDecimal totalAmount;
    @Column(name = "submitted_at") private OffsetDateTime submittedAt;
    @Column(name = "notes", length = 2000) private String notes;

    public enum ReturnType { FORM_24Q, FORM_26Q, FORM_16, PT, PF_ECR, ESI, GSTR1, GSTR3B, OTHER }
    public enum Status { DRAFT, GENERATED, SUBMITTED, ACKNOWLEDGED, REJECTED }
}
