package com.hrms.payroll.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class TaxConfigDto {

    // ── PF ────────────────────────────────────────────────────────────────────

    @Getter @Setter
    public static class PfConfigRequest {
        private String pfNumber;
        private BigDecimal basicWageCeiling;
        private BigDecimal pfRateEmployee;
        private BigDecimal pfRateEmployer;
        private BigDecimal epsRate;
        private BigDecimal edliRate;
        private BigDecimal adminChargeRate;
        private Boolean    includeEmployerPfInCtc;
        @NotNull private LocalDate effectiveFrom;
    }

    @Getter @Setter @Builder
    public static class PfConfigResponse {
        private UUID       id;
        private String     pfNumber;
        private BigDecimal basicWageCeiling;
        private BigDecimal pfRateEmployee;
        private BigDecimal pfRateEmployer;
        private BigDecimal epsRate;
        private BigDecimal edliRate;
        private BigDecimal adminChargeRate;
        private boolean    includeEmployerPfInCtc;
        private LocalDate  effectiveFrom;
        private Instant    createdAt;
        private Instant    updatedAt;
    }

    // ── ESI ───────────────────────────────────────────────────────────────────

    @Getter @Setter
    public static class EsiConfigRequest {
        private String     esiNumber;
        private BigDecimal wageCeiling;
        private BigDecimal employeeRate;
        private BigDecimal employerRate;
        @NotNull private LocalDate effectiveFrom;
    }

    @Getter @Setter @Builder
    public static class EsiConfigResponse {
        private UUID       id;
        private String     esiNumber;
        private BigDecimal wageCeiling;
        private BigDecimal employeeRate;
        private BigDecimal employerRate;
        private LocalDate  effectiveFrom;
        private Instant    createdAt;
        private Instant    updatedAt;
    }

    // ── PT Slabs ──────────────────────────────────────────────────────────────

    @Getter @Setter
    public static class PtSlabRequest {
        @NotNull private String     state;
        @NotNull private BigDecimal slabFrom;
        private BigDecimal slabTo;        // null = no ceiling
        @NotNull private BigDecimal monthlyTax;
        private String     gender;        // 'M', 'F', or null
        @NotNull private LocalDate effectiveFrom;
    }

    @Getter @Setter @Builder
    public static class PtSlabResponse {
        private UUID       id;
        private String     state;
        private BigDecimal slabFrom;
        private BigDecimal slabTo;
        private BigDecimal monthlyTax;
        private String     gender;
        private LocalDate  effectiveFrom;
        private Instant    createdAt;
    }
}
