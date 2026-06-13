package com.hrms.compensation.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.compensation.entity.Benefit;
import com.hrms.compensation.entity.EmployeeBenefit;
import com.hrms.compensation.repository.BenefitRepository;
import com.hrms.compensation.repository.EmployeeBenefitRepository;
import com.hrms.security.model.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Total-rewards statement (benefits portion) — aggregates an employee's enrolled
 * benefits and the employer's annual contribution. Cash compensation (base/bonus)
 * is owned by service-payroll and reported there.
 */
@RestController
@RequestMapping("/api/v1/compensation/total-rewards")
@RequiredArgsConstructor
@Tag(name = "Total Rewards", description = "Employee total-rewards statement (benefits)")
public class TotalRewardsController {

    private final EmployeeBenefitRepository employeeBenefitRepository;
    private final BenefitRepository benefitRepository;

    @GetMapping("/{employeeId}")
    @PreAuthorize("hasAuthority('COMPENSATION:READ')")
    @Operation(summary = "Total-rewards statement for an employee")
    public ResponseEntity<ApiResponse<Map<String, Object>>> statement(
            @PathVariable UUID employeeId,
            @RequestParam(required = false) String fy) {
        String tenantId = TenantContext.get();
        List<EmployeeBenefit> enrolled = employeeBenefitRepository
                .findByTenantIdAndEmployeeIdAndDeletedFalse(tenantId, employeeId);

        List<Map<String, Object>> lines = new ArrayList<>();
        BigDecimal employerTotal = BigDecimal.ZERO;
        BigDecimal employeeTotal = BigDecimal.ZERO;

        for (EmployeeBenefit eb : enrolled) {
            Benefit b = benefitRepository.findByIdAndTenantIdAndDeletedFalse(eb.getBenefitId(), tenantId).orElse(null);
            BigDecimal employer = b != null && b.getEmployerContribution() != null ? b.getEmployerContribution() : BigDecimal.ZERO;
            BigDecimal employee = b != null && b.getEmployeeContribution() != null ? b.getEmployeeContribution() : BigDecimal.ZERO;
            employerTotal = employerTotal.add(employer);
            employeeTotal = employeeTotal.add(employee);

            Map<String, Object> line = new LinkedHashMap<>();
            line.put("benefitId", eb.getBenefitId());
            line.put("name", b != null ? b.getName() : null);
            line.put("type", b != null ? b.getBenefitType() : null);
            line.put("status", eb.getStatus());
            line.put("employerContribution", employer);
            line.put("employeeContribution", employee);
            lines.add(line);
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("employeeId", employeeId);
        out.put("financialYear", fy);
        out.put("benefitCount", enrolled.size());
        out.put("employerAnnualContribution", employerTotal);
        out.put("employeeAnnualContribution", employeeTotal);
        out.put("totalBenefitsValue", employerTotal.add(employeeTotal));
        out.put("benefits", lines);
        out.put("note", "Cash compensation (base salary, bonuses) is reported by Payroll.");
        return ResponseEntity.ok(ApiResponse.ok(out));
    }
}
