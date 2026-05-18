package com.hrms.payroll.service.statutory;

import com.hrms.payroll.entity.PtSlab;
import com.hrms.payroll.repository.PtSlabRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Professional Tax (state-specific) calculator. Looks up the active slab for the employee's
 * work state, gender, and gross monthly salary using the existing PtSlabRepository query.
 * Returns ZERO if no matching slab found (no PT applies in that state for that bracket).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProfessionalTaxCalculator {

    private final PtSlabRepository ptSlabRepository;

    public BigDecimal calculate(String tenantId, String state, BigDecimal grossMonthly,
                                 String gender, LocalDate asOf) {
        if (state == null || state.isBlank() || grossMonthly == null) return BigDecimal.ZERO;
        List<PtSlab> match = ptSlabRepository.findApplicableSlab(
                tenantId, state, grossMonthly, gender,
                asOf == null ? LocalDate.now() : asOf);
        return match.isEmpty() ? BigDecimal.ZERO : match.get(0).getMonthlyTax();
    }
}
