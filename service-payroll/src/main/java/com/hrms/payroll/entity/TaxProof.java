package com.hrms.payroll.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Proof documents uploaded against a tax declaration section.
 * Not multi-tenanted at row level — tenant is implied via declaration.
 */
@Entity
@Table(name = "tax_proofs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class TaxProof {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "declaration_id", nullable = false)
    private UUID declarationId;

    /** e.g. "80C", "80D", "HRA", "24B". */
    @Column(name = "section", nullable = false, length = 20)
    private String section;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "declared_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal declaredAmount = BigDecimal.ZERO;

    /** Amount verified from the proof document. */
    @Column(name = "proof_amount", precision = 12, scale = 2)
    private BigDecimal proofAmount;

    @Column(name = "proof_url", length = 1000)
    private String proofUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ProofStatus status = ProofStatus.PENDING;

    @Column(name = "verified_by", length = 100)
    private String verifiedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // ── Enum ──────────────────────────────────────────────────────────────────

    public enum ProofStatus { PENDING, APPROVED, REJECTED }
}
