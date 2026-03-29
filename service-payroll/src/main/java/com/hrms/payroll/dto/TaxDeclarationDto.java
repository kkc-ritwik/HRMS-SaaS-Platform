package com.hrms.payroll.dto;

import com.hrms.payroll.entity.TaxDeclaration;
import com.hrms.payroll.entity.TaxProof;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class TaxDeclarationDto {

    @Getter @Setter
    public static class SubmitRequest {
        @NotBlank private String financialYear;         // e.g. "2025-26"
        @NotNull  private TaxDeclaration.TaxRegime regime;

        private BigDecimal section80c             = BigDecimal.ZERO;
        private BigDecimal section80d             = BigDecimal.ZERO;
        private BigDecimal section80e             = BigDecimal.ZERO;
        private BigDecimal section80g             = BigDecimal.ZERO;
        private BigDecimal section24b             = BigDecimal.ZERO;
        private BigDecimal hraExemptionClaimed    = BigDecimal.ZERO;
        private BigDecimal ltaClaimed             = BigDecimal.ZERO;
        private BigDecimal otherIncome            = BigDecimal.ZERO;
        private BigDecimal previousEmployerIncome = BigDecimal.ZERO;
        private BigDecimal previousEmployerTds    = BigDecimal.ZERO;
    }

    @Getter @Setter @Builder
    public static class Response {
        private UUID       id;
        private UUID       employeeId;
        private String     financialYear;
        private TaxDeclaration.TaxRegime regime;
        private TaxDeclaration.DeclarationStatus status;
        private BigDecimal section80c;
        private BigDecimal section80d;
        private BigDecimal section80e;
        private BigDecimal section80g;
        private BigDecimal section24b;
        private BigDecimal hraExemptionClaimed;
        private BigDecimal ltaClaimed;
        private BigDecimal otherIncome;
        private BigDecimal previousEmployerIncome;
        private BigDecimal previousEmployerTds;
        private List<ProofSummary> proofs;
        private Instant    createdAt;
        private Instant    updatedAt;
    }

    // ── Proof sub-DTOs ────────────────────────────────────────────────────────

    @Getter @Setter
    public static class AddProofRequest {
        @NotBlank private String     section;
        private String               description;
        @NotNull  private BigDecimal declaredAmount;
        private BigDecimal           proofAmount;
        private String               proofUrl;
    }

    @Getter @Setter @Builder
    public static class ProofSummary {
        private UUID       id;
        private String     section;
        private String     description;
        private BigDecimal declaredAmount;
        private BigDecimal proofAmount;
        private String     proofUrl;
        private TaxProof.ProofStatus status;
        private String     verifiedBy;
        private Instant    createdAt;
    }
}
