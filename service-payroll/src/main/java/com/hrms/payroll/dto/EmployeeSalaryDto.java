package com.hrms.payroll.dto;

import com.hrms.payroll.entity.EmployeeSalary;
import com.hrms.payroll.entity.SalaryComponent;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class EmployeeSalaryDto {

    @Getter @Setter
    public static class AssignRequest {
        private UUID    salaryStructureId;     // optional

        @NotNull
        private BigDecimal ctcAnnual;

        @NotNull
        private LocalDate effectiveFrom;

        private String  revisionLetterUrl;
        private UUID    approvedBy;

        /** Explicit monthly breakdown; if empty the service auto-splits from CTC. */
        private List<ComponentItem> components;
    }

    /** Per-component amount in an assignment request. */
    @Getter @Setter
    public static class ComponentItem {
        @NotNull private UUID componentId;
        @NotNull private BigDecimal monthlyAmount;
        private BigDecimal employerContribution;   // for EMPLOYER_CONTRIBUTION type
    }

    @Getter @Setter @Builder
    public static class Response {
        private UUID      id;
        private UUID      employeeId;
        private UUID      salaryStructureId;
        private String    salaryStructureName;
        private BigDecimal ctcAnnual;
        private BigDecimal grossMonthly;
        private BigDecimal netMonthly;
        private LocalDate effectiveFrom;
        private LocalDate effectiveTo;
        private String    revisionLetterUrl;
        private UUID      approvedBy;
        private EmployeeSalary.SalaryStatus status;
        private List<ComponentBreakdown> components;
        private Instant   createdAt;
        private Instant   updatedAt;
    }

    @Getter @Setter @Builder
    public static class ComponentBreakdown {
        private UUID       componentId;
        private String     componentName;
        private String     componentCode;
        private SalaryComponent.ComponentType componentType;
        private BigDecimal monthlyAmount;
        private BigDecimal annualAmount;
        private BigDecimal employerContribution;
        private boolean    taxable;
        private boolean    partOfGross;
    }
}
