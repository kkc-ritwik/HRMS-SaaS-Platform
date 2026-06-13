package com.hrms.offboarding.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.offboarding.entity.Separation;
import com.hrms.offboarding.repository.SeparationRepository;
import com.hrms.security.model.TenantContext;
import com.hrms.security.model.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Offboarding extras — notice-period buy-out calculator and separation withdrawal.
 */
@RestController
@RequestMapping("/api/v1/offboarding")
@RequiredArgsConstructor
@Tag(name = "Offboarding Extras", description = "Notice buy-out calculation and separation withdrawal")
public class OffboardingExtrasController {

    private final SeparationRepository separationRepository;

    /**
     * Compute the notice-period buy-out amount when an employee leaves before serving
     * the full notice period. {@code perDay = monthlySalary * 12 / 365}.
     */
    @PostMapping("/notice-buyout/calc")
    @PreAuthorize("hasAuthority('OFFBOARDING:READ')")
    @Operation(summary = "Calculate notice-period buy-out amount")
    public ResponseEntity<ApiResponse<Map<String, Object>>> calcBuyout(@RequestBody BuyoutRequest req) {
        BigDecimal monthly = req.monthlySalary() == null ? BigDecimal.ZERO : req.monthlySalary();
        int noticeDays = req.noticePeriodDays() == null ? 0 : req.noticePeriodDays();
        int servedDays = req.servedDays() == null ? 0 : req.servedDays();
        int shortfall = Math.max(0, noticeDays - servedDays);

        BigDecimal perDay = monthly.multiply(BigDecimal.valueOf(12))
                .divide(BigDecimal.valueOf(365), 2, RoundingMode.HALF_UP);
        BigDecimal buyout = perDay.multiply(BigDecimal.valueOf(shortfall)).setScale(2, RoundingMode.HALF_UP);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("monthlySalary", monthly);
        out.put("noticePeriodDays", noticeDays);
        out.put("servedDays", servedDays);
        out.put("shortfallDays", shortfall);
        out.put("perDayRate", perDay);
        out.put("buyoutAmount", buyout);
        out.put("payableBy", "EMPLOYEE");
        return ResponseEntity.ok(ApiResponse.ok(out));
    }

    @PostMapping("/separations/{id}/withdraw")
    @PreAuthorize("hasAuthority('OFFBOARDING:WRITE')")
    @Operation(summary = "Withdraw (cancel) a separation request")
    public ResponseEntity<ApiResponse<Separation>> withdraw(
            @PathVariable UUID id, @RequestBody(required = false) Map<String, String> body) {
        Separation sep = separationRepository.findByIdAndTenantIdAndDeletedFalse(id, TenantContext.get())
                .orElseThrow(() -> new IllegalArgumentException("Separation not found: " + id));
        sep.setStatus(Separation.SeparationStatus.CANCELLED);
        sep.setUpdatedBy(currentUserId());
        return ResponseEntity.ok(ApiResponse.ok(separationRepository.save(sep)));
    }

    private String currentUserId() {
        Object p = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return p instanceof UserPrincipal up ? up.getId() : null;
    }

    public record BuyoutRequest(BigDecimal monthlySalary, Integer noticePeriodDays, Integer servedDays) {}
}
