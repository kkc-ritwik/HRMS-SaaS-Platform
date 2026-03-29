package com.hrms.payroll.service;

import com.hrms.common.dto.PaginationMeta;
import com.hrms.common.exception.BusinessException;
import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.payroll.dto.PayrollRunDto;
import com.hrms.payroll.entity.*;
import com.hrms.payroll.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayrollRunService {

    private final PayrollRunRepository        runRepository;
    private final PayslipRepository           payslipRepository;
    private final EmployeeSalaryRepository    salaryRepository;
    private final PayrollCalculationService   calculationService;

    // ── Create ────────────────────────────────────────────────────────────────

    @Transactional
    public PayrollRunDto.Response createPayRun(String tenantId, PayrollRunDto.CreateRequest req,
                                                String currentUser) {
        // Prevent duplicate REGULAR runs for same month/year
        if (req.getRunType() == PayrollRun.RunType.REGULAR &&
            runRepository.existsByTenantIdAndYearAndMonthAndRunTypeAndDeletedFalse(
                    tenantId, req.getYear(), req.getMonth(), PayrollRun.RunType.REGULAR)) {
            throw new BusinessException("DUPLICATE_PAY_RUN",
                    "A REGULAR pay run for " + req.getMonth() + "/" + req.getYear() + " already exists");
        }

        PayrollRun run = new PayrollRun();
        run.setTenantId(tenantId);
        run.setMonth(req.getMonth());
        run.setYear(req.getYear());
        run.setRunType(req.getRunType());
        run.setStatus(PayrollRun.RunStatus.DRAFT);
        run.setCreatedBy(currentUser);
        return toResponse(runRepository.save(run));
    }

    // ── Process ───────────────────────────────────────────────────────────────

    /**
     * Process all active employees. Loops, calculates each payslip, saves, then
     * updates run totals. Errors per-employee are logged but don't abort the run.
     */
    @Transactional
    public PayrollRunDto.Response processPayRun(String tenantId, UUID runId, String currentUser) {
        PayrollRun run = getEntity(tenantId, runId);
        if (run.getStatus() != PayrollRun.RunStatus.DRAFT) {
            throw new BusinessException("INVALID_RUN_STATUS",
                    "Only DRAFT runs can be processed. Current status: " + run.getStatus());
        }

        run.setStatus(PayrollRun.RunStatus.PROCESSING);
        runRepository.save(run);

        List<EmployeeSalary> activeSalaries = salaryRepository
                .findByTenantIdAndStatusAndDeletedFalse(tenantId, EmployeeSalary.SalaryStatus.ACTIVE);

        BigDecimal totalGross      = BigDecimal.ZERO;
        BigDecimal totalDeductions = BigDecimal.ZERO;
        BigDecimal totalNet        = BigDecimal.ZERO;
        BigDecimal totalEPf        = BigDecimal.ZERO;
        BigDecimal totalEEsi       = BigDecimal.ZERO;
        int        processed       = 0;

        for (EmployeeSalary salary : activeSalaries) {
            try {
                // Skip if a payslip already exists for this employee in this run
                if (payslipRepository.findByTenantIdAndEmployeeIdAndMonthAndYearAndPayrollRunIdAndDeletedFalse(
                        tenantId, salary.getEmployeeId(), run.getMonth(), run.getYear(), runId).isPresent()) {
                    continue;
                }
                // LOP days = 0 (attendance integration is done externally; can override via reprocess)
                Payslip payslip = Objects.requireNonNull(
                        calculationService.calculatePayslip(
                                tenantId, salary, run.getMonth(), run.getYear(), runId,
                                0, null, currentUser));

                Payslip saved = payslipRepository.save(payslip);

                totalGross      = totalGross.add(orZero(saved.getGrossEarnings()));
                totalDeductions = totalDeductions.add(orZero(saved.getTotalDeductions()));
                totalNet        = totalNet.add(orZero(saved.getNetPay()));
                totalEPf        = totalEPf.add(orZero(saved.getEmployerPf()));
                totalEEsi       = totalEEsi.add(orZero(saved.getEmployerEsi()));
                processed++;

            } catch (Exception e) {
                log.error("Error calculating payslip for employee={} run={}",
                        salary.getEmployeeId(), runId, e);
            }
        }

        run.setTotalGross(totalGross);
        run.setTotalDeductions(totalDeductions);
        run.setTotalNet(totalNet);
        run.setTotalEmployerPf(totalEPf);
        run.setTotalEmployerEsi(totalEEsi);
        run.setEmployeeCount(processed);
        run.setStatus(PayrollRun.RunStatus.PROCESSED);
        run.setProcessedBy(currentUser);
        run.setProcessedAt(Instant.now());
        run.setUpdatedBy(currentUser);
        runRepository.save(run);

        log.info("PayRun processed: id={} employees={} gross={}", runId, processed, totalGross);
        return toResponse(run);
    }

    // ── Lock ──────────────────────────────────────────────────────────────────

    @Transactional
    public PayrollRunDto.Response lockPayRun(String tenantId, UUID runId, String currentUser) {
        PayrollRun run = getEntity(tenantId, runId);
        if (run.getStatus() != PayrollRun.RunStatus.PROCESSED) {
            throw new BusinessException("INVALID_RUN_STATUS",
                    "Only PROCESSED runs can be locked. Current status: " + run.getStatus());
        }
        run.setStatus(PayrollRun.RunStatus.LOCKED);
        run.setLockedBy(currentUser);
        run.setLockedAt(Instant.now());
        run.setUpdatedBy(currentUser);
        runRepository.save(run);

        // Lock all payslips in this run
        payslipRepository.updateStatusByRun(runId, tenantId, Payslip.PayslipStatus.LOCKED, currentUser);
        return toResponse(run);
    }

    // ── Mark Paid ────────────────────────────────────────────────────────────

    @Transactional
    public PayrollRunDto.Response markAsPaid(String tenantId, UUID runId, String currentUser) {
        PayrollRun run = getEntity(tenantId, runId);
        if (run.getStatus() != PayrollRun.RunStatus.LOCKED) {
            throw new BusinessException("INVALID_RUN_STATUS",
                    "Only LOCKED runs can be marked PAID. Current status: " + run.getStatus());
        }
        run.setStatus(PayrollRun.RunStatus.PAID);
        run.setUpdatedBy(currentUser);
        runRepository.save(run);

        // Publish payslips so employees can view them
        payslipRepository.updateStatusByRun(runId, tenantId, Payslip.PayslipStatus.PUBLISHED, currentUser);
        return toResponse(run);
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PayrollRunDto.Response getPayRun(String tenantId, UUID runId) {
        return toResponse(getEntity(tenantId, runId));
    }

    @Transactional(readOnly = true)
    public Page<PayrollRunDto.Response> listPayRuns(String tenantId, Pageable pageable) {
        return runRepository
                .findByTenantIdAndDeletedFalseOrderByYearDescMonthDesc(tenantId, pageable)
                .map(this::toResponse);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    public PaginationMeta buildMeta(Page<?> page) {
        return PaginationMeta.builder()
                .page(page.getNumber()).size(page.getSize())
                .total(page.getTotalElements()).totalPages(page.getTotalPages())
                .hasNext(page.hasNext()).hasPrevious(page.hasPrevious())
                .build();
    }

    private PayrollRun getEntity(String tenantId, UUID id) {
        return runRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("PayrollRun", "id", id));
    }

    private static BigDecimal orZero(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    private PayrollRunDto.Response toResponse(PayrollRun r) {
        return PayrollRunDto.Response.builder()
                .id(r.getId()).month(r.getMonth()).year(r.getYear())
                .status(r.getStatus()).runType(r.getRunType())
                .totalGross(r.getTotalGross()).totalDeductions(r.getTotalDeductions())
                .totalNet(r.getTotalNet()).totalEmployerPf(r.getTotalEmployerPf())
                .totalEmployerEsi(r.getTotalEmployerEsi()).employeeCount(r.getEmployeeCount())
                .processedBy(r.getProcessedBy()).processedAt(r.getProcessedAt())
                .lockedBy(r.getLockedBy()).lockedAt(r.getLockedAt())
                .createdAt(r.getCreatedAt()).updatedAt(r.getUpdatedAt())
                .build();
    }
}
