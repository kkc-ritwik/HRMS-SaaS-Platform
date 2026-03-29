package com.hrms.payroll.dto;

import com.hrms.payroll.entity.PayrollRun;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class PayrollRunDto {

    @Getter @Setter
    public static class CreateRequest {
        @NotNull @Min(1) @Max(12)
        private Integer month;

        @NotNull @Min(2000)
        private Integer year;

        private PayrollRun.RunType runType = PayrollRun.RunType.REGULAR;
    }

    @Getter @Setter @Builder
    public static class Response {
        private UUID             id;
        private int              month;
        private int              year;
        private PayrollRun.RunStatus status;
        private PayrollRun.RunType   runType;
        private BigDecimal       totalGross;
        private BigDecimal       totalDeductions;
        private BigDecimal       totalNet;
        private BigDecimal       totalEmployerPf;
        private BigDecimal       totalEmployerEsi;
        private int              employeeCount;
        private String           processedBy;
        private Instant          processedAt;
        private String           lockedBy;
        private Instant          lockedAt;
        private Instant          createdAt;
        private Instant          updatedAt;
    }
}
