package com.hrms.payroll.service;

import com.hrms.payroll.entity.*;
import com.hrms.payroll.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Pure calculation engine.  No @Transactional — callers own the transaction.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PayrollCalculationService {

    private final EmployeeSalaryComponentRepository salaryCompRepo;
    private final SalaryComponentRepository         componentRepo;
    private final PfConfigRepository                pfConfigRepo;
    private final EsiConfigRepository               esiConfigRepo;
    private final TaxDeclarationRepository          declarationRepo;
    private final PayslipRepository                 payslipRepo;

    // ── Main entrypoint ───────────────────────────────────────────────────────

    /**
     * Calculate a payslip for one employee.
     *
     * @param lopDays  Loss-of-pay days (0 if attendance data unavailable)
     * @param state    Employee's work state for PT lookup (null = skip PT)
     */
    public Payslip calculatePayslip(String tenantId, EmployeeSalary salary,
                                    int month, int year, UUID payrollRunId,
                                    int lopDays, String state, String currentUser) {

        YearMonth ym        = YearMonth.of(year, month);
        int daysInMonth     = ym.lengthOfMonth();
        int daysPayable     = daysInMonth - lopDays;
        UUID employeeId     = salary.getEmployeeId();

        // ── Component metadata ────────────────────────────────────────────────
        List<EmployeeSalaryComponent> escs = salaryCompRepo.findByEmployeeSalaryId(salary.getId());
        List<UUID> compIds = escs.stream().map(EmployeeSalaryComponent::getComponentId)
                .collect(Collectors.toList());
        Map<UUID, SalaryComponent> compMap = compIds.isEmpty() ? Map.of()
                : componentRepo.findAllByIdInAndTenantId(compIds, tenantId)
                        .stream().collect(Collectors.toMap(SalaryComponent::getId, c -> c));

        // ── Statutory configs ─────────────────────────────────────────────────
        PfConfig  pf  = pfConfigRepo.findFirstByTenantIdAndDeletedFalseOrderByEffectiveFromDesc(tenantId)
                .orElse(defaultPfConfig());
        EsiConfig esi = esiConfigRepo.findFirstByTenantIdAndDeletedFalseOrderByEffectiveFromDesc(tenantId)
                .orElse(defaultEsiConfig());

        // ── Calculate earnings ────────────────────────────────────────────────
        List<PayslipLineItem> lines    = new ArrayList<>();
        BigDecimal grossEarnings       = BigDecimal.ZERO;
        BigDecimal pfWage              = BigDecimal.ZERO;   // Basic (or as per policy)

        for (EmployeeSalaryComponent esc : escs) {
            SalaryComponent comp = compMap.get(esc.getComponentId());
            if (comp == null || comp.getType() != SalaryComponent.ComponentType.EARNING) continue;
            if (!comp.isActive()) continue;

            BigDecimal amount = esc.getMonthlyAmount();
            // Apply LOP for pro-rata components
            if (comp.isProRata() && lopDays > 0) {
                amount = amount.multiply(BigDecimal.valueOf(daysPayable))
                               .divide(BigDecimal.valueOf(daysInMonth), 2, RoundingMode.HALF_UP);
            }
            // Track Basic for PF wage
            if ("BASIC".equalsIgnoreCase(comp.getCode())) pfWage = amount;

            lines.add(PayslipLineItem.builder()
                    .componentId(comp.getId().toString())
                    .code(comp.getCode()).name(comp.getName())
                    .type("EARNING").amount(amount).build());

            if (comp.isPartOfGross()) grossEarnings = grossEarnings.add(amount);
        }

        // ── PF (Employee + Employer) ──────────────────────────────────────────
        BigDecimal pfBase      = pfWage.min(pf.getBasicWageCeiling());
        BigDecimal pfRateE     = pf.getPfRateEmployee().divide(BD_100, 6, RoundingMode.HALF_UP);
        BigDecimal pfRateEr    = pf.getPfRateEmployer().divide(BD_100, 6, RoundingMode.HALF_UP);
        BigDecimal employeePf  = pfBase.multiply(pfRateE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal employerPf  = pfBase.multiply(pfRateEr).setScale(2, RoundingMode.HALF_UP);

        // ── ESI (Employee + Employer) ─────────────────────────────────────────
        BigDecimal employeeEsi = BigDecimal.ZERO;
        BigDecimal employerEsi = BigDecimal.ZERO;
        if (grossEarnings.compareTo(esi.getWageCeiling()) <= 0 &&
            grossEarnings.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal esiRateE  = esi.getEmployeeRate().divide(BD_100, 6, RoundingMode.HALF_UP);
            BigDecimal esiRateEr = esi.getEmployerRate().divide(BD_100, 6, RoundingMode.HALF_UP);
            employeeEsi = grossEarnings.multiply(esiRateE).setScale(2, RoundingMode.HALF_UP);
            employerEsi = grossEarnings.multiply(esiRateEr).setScale(2, RoundingMode.HALF_UP);
        }

        // ── PT (skipped here — caller sets state; TaxConfigService.calculatePt is used) ──
        BigDecimal employeePt = BigDecimal.ZERO;
        // Note: PT lookup requires TaxConfigService; injecting it would create a circular dep.
        // The PayrollRunService resolves PT and passes it in via lopDays/state and updates the payslip.

        // ── TDS ───────────────────────────────────────────────────────────────
        BigDecimal tds = calculateMonthlyTds(tenantId, employeeId, grossEarnings,
                month, year, employeePf);

        // ── Loan EMIs deducted this month ─────────────────────────────────────
        // Loan deductions are applied by PayrollRunService after this method returns.

        // ── Reimbursements (informational lines, not in deductions total) ─────
        for (EmployeeSalaryComponent esc : escs) {
            SalaryComponent comp = compMap.get(esc.getComponentId());
            if (comp == null || comp.getType() != SalaryComponent.ComponentType.REIMBURSEMENT) continue;
            lines.add(PayslipLineItem.builder()
                    .componentId(comp.getId().toString())
                    .code(comp.getCode()).name(comp.getName())
                    .type("REIMBURSEMENT").amount(esc.getMonthlyAmount()).build());
        }

        // ── Employer contributions (informational) ────────────────────────────
        lines.add(PayslipLineItem.builder().code("PF_ER").name("PF Employer Contribution")
                .type("EMPLOYER_CONTRIBUTION").amount(employerPf).build());
        if (employerEsi.compareTo(BigDecimal.ZERO) > 0) {
            lines.add(PayslipLineItem.builder().code("ESI_ER").name("ESI Employer Contribution")
                    .type("EMPLOYER_CONTRIBUTION").amount(employerEsi).build());
        }

        // ── Deduction lines ───────────────────────────────────────────────────
        if (employeePf.compareTo(BigDecimal.ZERO) > 0)
            lines.add(PayslipLineItem.builder().code("PF_EMP").name("PF Employee Contribution")
                    .type("DEDUCTION").amount(employeePf).build());
        if (employeeEsi.compareTo(BigDecimal.ZERO) > 0)
            lines.add(PayslipLineItem.builder().code("ESI_EMP").name("ESI Employee Contribution")
                    .type("DEDUCTION").amount(employeeEsi).build());
        if (tds.compareTo(BigDecimal.ZERO) > 0)
            lines.add(PayslipLineItem.builder().code("TDS").name("Income Tax (TDS)")
                    .type("DEDUCTION").amount(tds).build());

        // ── Totals ────────────────────────────────────────────────────────────
        BigDecimal totalDeductions = employeePf.add(employeeEsi).add(employeePt).add(tds);
        BigDecimal netPay          = grossEarnings.subtract(totalDeductions);

        // ── Build Payslip entity ──────────────────────────────────────────────
        Payslip payslip = new Payslip();
        payslip.setTenantId(tenantId);
        payslip.setPayrollRunId(payrollRunId);
        payslip.setEmployeeId(employeeId);
        payslip.setMonth(month);
        payslip.setYear(year);
        payslip.setDaysInMonth(daysInMonth);
        payslip.setDaysPayable(daysPayable);
        payslip.setDaysWorked(daysPayable);
        payslip.setLopDays(BigDecimal.valueOf(lopDays));
        payslip.setGrossEarnings(grossEarnings);
        payslip.setTotalDeductions(totalDeductions);
        payslip.setNetPay(netPay);
        payslip.setEmployerPf(employerPf);
        payslip.setEmployerEsi(employerEsi);
        payslip.setEmployerLwf(BigDecimal.ZERO);
        payslip.setEmployeePf(employeePf);
        payslip.setEmployeeEsi(employeeEsi);
        payslip.setEmployeePt(employeePt);
        payslip.setTds(tds);
        payslip.setComponentsJson(lines);
        payslip.setStatus(Payslip.PayslipStatus.PROCESSED);
        payslip.setCreatedBy(currentUser);

        log.debug("Calculated payslip: employee={} {}/{} gross={} net={}",
                employeeId, month, year, grossEarnings, netPay);
        return payslip;
    }

    // ── TDS Estimation ────────────────────────────────────────────────────────

    BigDecimal calculateMonthlyTds(String tenantId, UUID employeeId,
                                   BigDecimal grossMonthly, int month, int year,
                                   BigDecimal employeePfMonthly) {
        String fy = financialYear(month, year);

        TaxDeclaration decl = declarationRepo
                .findByEmployeeIdAndTenantIdAndFinancialYearAndDeletedFalse(employeeId, tenantId, fy)
                .orElse(null);

        TaxDeclaration.TaxRegime regime = decl != null ? decl.getRegime() : TaxDeclaration.TaxRegime.NEW;

        // Projected annual gross
        BigDecimal annualGross = grossMonthly.multiply(BigDecimal.valueOf(12));

        // Add previous employer income
        BigDecimal prevIncome = decl != null && decl.getPreviousEmployerIncome() != null
                ? decl.getPreviousEmployerIncome() : BigDecimal.ZERO;
        BigDecimal otherIncome = decl != null && decl.getOtherIncome() != null
                ? decl.getOtherIncome() : BigDecimal.ZERO;
        BigDecimal totalAnnualIncome = annualGross.add(prevIncome).add(otherIncome);

        // Standard deduction
        BigDecimal stdDeduction = regime == TaxDeclaration.TaxRegime.NEW
                ? BigDecimal.valueOf(75_000)   // new regime FY 2024-25
                : BigDecimal.valueOf(50_000);

        BigDecimal taxableIncome = totalAnnualIncome.subtract(stdDeduction);

        if (regime == TaxDeclaration.TaxRegime.OLD && decl != null) {
            // 80C: PF + declared (capped at 1.5L)
            BigDecimal annualPf   = employeePfMonthly.multiply(BigDecimal.valueOf(12));
            BigDecimal decl80c    = nvl(decl.getSection80c());
            BigDecimal total80c   = annualPf.add(decl80c).min(BD_1_5L);
            // 80D (capped at 25,000)
            BigDecimal total80d   = nvl(decl.getSection80d()).min(BD_25K);
            // 80E no cap
            BigDecimal total80e   = nvl(decl.getSection80e());
            // 24B capped at 2L
            BigDecimal total24b   = nvl(decl.getSection24b()).min(BD_2L);
            // HRA + LTA
            BigDecimal hraLta     = nvl(decl.getHraExemptionClaimed()).add(nvl(decl.getLtaClaimed()));

            taxableIncome = taxableIncome
                    .subtract(total80c).subtract(total80d).subtract(total80e)
                    .subtract(total24b).subtract(hraLta);
        }
        taxableIncome = taxableIncome.max(BigDecimal.ZERO);

        // Tax on taxable income
        BigDecimal tax = regime == TaxDeclaration.TaxRegime.NEW
                ? newRegimeTax(taxableIncome)
                : oldRegimeTax(taxableIncome);

        // Rebate u/s 87A (income ≤ 5L for old regime, ≤ 7L for new regime)
        tax = applyRebate87A(tax, taxableIncome, regime);

        // 4% Health & Education cess
        BigDecimal annualTax = tax.multiply(BigDecimal.valueOf(1.04)).setScale(2, RoundingMode.HALF_UP);

        // Subtract previous employer TDS
        BigDecimal prevTds = decl != null && decl.getPreviousEmployerTds() != null
                ? decl.getPreviousEmployerTds() : BigDecimal.ZERO;
        annualTax = annualTax.subtract(prevTds).max(BigDecimal.ZERO);

        // Subtract TDS already deducted in the FY
        int fyStart = month >= 4 ? year : year - 1;
        BigDecimal tdsAlreadyDeducted = payslipRepo.sumTdsForFY(tenantId, employeeId, fyStart, fyStart + 1);
        annualTax = annualTax.subtract(tdsAlreadyDeducted).max(BigDecimal.ZERO);

        // Remaining months in FY (including current month)
        int monthInFy    = month >= 4 ? month - 3 : month + 9;
        int remaining    = 13 - monthInFy;

        BigDecimal monthly = annualTax.divide(BigDecimal.valueOf(remaining), 2, RoundingMode.CEILING);
        return monthly.max(BigDecimal.ZERO);
    }

    // ── Tax Slab Calculations ─────────────────────────────────────────────────

    /** Old regime slabs — FY 2024-25. */
    private BigDecimal oldRegimeTax(BigDecimal income) {
        BigDecimal tax = BigDecimal.ZERO;
        BigDecimal slab1 = BigDecimal.valueOf(250_000);
        BigDecimal slab2 = BigDecimal.valueOf(500_000);
        BigDecimal slab3 = BigDecimal.valueOf(1_000_000);

        if (income.compareTo(slab1) <= 0) return tax;
        tax = tax.add(income.min(slab2).subtract(slab1).multiply(BigDecimal.valueOf(0.05)));
        if (income.compareTo(slab2) <= 0) return tax;
        tax = tax.add(income.min(slab3).subtract(slab2).multiply(BigDecimal.valueOf(0.20)));
        if (income.compareTo(slab3) <= 0) return tax;
        tax = tax.add(income.subtract(slab3).multiply(BigDecimal.valueOf(0.30)));
        return tax.setScale(2, RoundingMode.HALF_UP);
    }

    /** New regime slabs — FY 2024-25 (Budget 2024). */
    private BigDecimal newRegimeTax(BigDecimal income) {
        BigDecimal tax  = BigDecimal.ZERO;
        BigDecimal s1   = BigDecimal.valueOf(300_000);
        BigDecimal s2   = BigDecimal.valueOf(700_000);
        BigDecimal s3   = BigDecimal.valueOf(1_000_000);
        BigDecimal s4   = BigDecimal.valueOf(1_200_000);
        BigDecimal s5   = BigDecimal.valueOf(1_500_000);

        if (income.compareTo(s1) <= 0) return tax;
        tax = tax.add(income.min(s2).subtract(s1).multiply(BigDecimal.valueOf(0.05)));
        if (income.compareTo(s2) <= 0) return tax;
        tax = tax.add(income.min(s3).subtract(s2).multiply(BigDecimal.valueOf(0.10)));
        if (income.compareTo(s3) <= 0) return tax;
        tax = tax.add(income.min(s4).subtract(s3).multiply(BigDecimal.valueOf(0.15)));
        if (income.compareTo(s4) <= 0) return tax;
        tax = tax.add(income.min(s5).subtract(s4).multiply(BigDecimal.valueOf(0.20)));
        if (income.compareTo(s5) <= 0) return tax;
        tax = tax.add(income.subtract(s5).multiply(BigDecimal.valueOf(0.30)));
        return tax.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal applyRebate87A(BigDecimal tax, BigDecimal taxableIncome,
                                       TaxDeclaration.TaxRegime regime) {
        BigDecimal ceiling = regime == TaxDeclaration.TaxRegime.NEW
                ? BigDecimal.valueOf(700_000) : BigDecimal.valueOf(500_000);
        BigDecimal maxRebate = regime == TaxDeclaration.TaxRegime.NEW
                ? BigDecimal.valueOf(25_000) : BigDecimal.valueOf(12_500);
        if (taxableIncome.compareTo(ceiling) <= 0) {
            return tax.subtract(maxRebate.min(tax)).max(BigDecimal.ZERO);
        }
        return tax;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Financial year string for a given payroll month/year. */
    static String financialYear(int month, int year) {
        int fyStartYear = month >= 4 ? year : year - 1;
        return fyStartYear + "-" + String.format("%02d", (fyStartYear + 1) % 100);
    }

    private PfConfig defaultPfConfig() {
        PfConfig c = new PfConfig();
        c.setBasicWageCeiling(BigDecimal.valueOf(15_000));
        c.setPfRateEmployee(BigDecimal.valueOf(12));
        c.setPfRateEmployer(BigDecimal.valueOf(12));
        c.setEffectiveFrom(LocalDate.of(2024, 4, 1));
        return c;
    }

    private EsiConfig defaultEsiConfig() {
        EsiConfig c = new EsiConfig();
        c.setWageCeiling(BigDecimal.valueOf(21_000));
        c.setEmployeeRate(BigDecimal.valueOf(0.75));
        c.setEmployerRate(BigDecimal.valueOf(3.25));
        c.setEffectiveFrom(LocalDate.of(2024, 4, 1));
        return c;
    }

    private BigDecimal nvl(BigDecimal v) { return v != null ? v : BigDecimal.ZERO; }

    private static final BigDecimal BD_100  = BigDecimal.valueOf(100);
    private static final BigDecimal BD_25K  = BigDecimal.valueOf(25_000);
    private static final BigDecimal BD_1_5L = BigDecimal.valueOf(150_000);
    private static final BigDecimal BD_2L   = BigDecimal.valueOf(200_000);
}
