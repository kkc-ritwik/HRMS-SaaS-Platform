package com.hrms.leave.service;

import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.leave.dto.LeaveBalanceDto;
import com.hrms.leave.entity.*;
import com.hrms.leave.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaveBalanceService {

    private final LeaveBalanceRepository    balanceRepository;
    private final LeaveTypeRepository       leaveTypeRepository;
    private final LeavePolicyRepository     policyRepository;
    private final LeaveAdjustmentRepository adjustmentRepository;

    // ── Query ─────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<LeaveBalanceDto.Response> getBalances(String tenantId, UUID employeeId, int year) {
        return balanceRepository
                .findByEmployeeIdAndTenantIdAndYearAndDeletedFalse(employeeId, tenantId, year)
                .stream().map(b -> toResponse(b, tenantId)).toList();
    }

    @Transactional(readOnly = true)
    public Optional<LeaveBalanceDto.Response> getBalance(String tenantId, UUID employeeId,
                                                          UUID leaveTypeId, int year) {
        return balanceRepository
                .findByEmployeeIdAndLeaveTypeIdAndYearAndTenantIdAndDeletedFalse(
                        employeeId, leaveTypeId, year, tenantId)
                .map(b -> toResponse(b, tenantId));
    }

    // ── Init yearly balances for one or all employees ─────────────────────────

    @Transactional
    public int initYearlyBalances(String tenantId, int year, UUID employeeId,
                                   String currentUser) {
        List<LeavePolicy> policies = policyRepository
                .findByTenantIdAndActiveAndDeletedFalse(tenantId, true);
        int created = 0;
        for (LeavePolicy policy : policies) {
            if (!isPolicyActiveForYear(policy, year)) continue;
            UUID empId = employeeId; // single employee init — for bulk, caller loops
            Optional<LeaveBalance> existing = balanceRepository
                    .findByEmployeeIdAndLeaveTypeIdAndYearAndTenantIdAndDeletedFalse(
                            empId, policy.getLeaveTypeId(), year, tenantId);
            if (existing.isEmpty()) {
                LeaveBalance balance = new LeaveBalance();
                balance.setTenantId(tenantId);
                balance.setEmployeeId(empId);
                balance.setLeaveTypeId(policy.getLeaveTypeId());
                balance.setYear(year);
                // For YEARLY accrual type: credit full amount on init
                if (policy.getAccrualType() == LeavePolicy.AccrualType.YEARLY) {
                    balance.setOpeningBalance(policy.getAccrualAmount());
                } else if (policy.getAccrualType() == LeavePolicy.AccrualType.ON_HIRE_DATE) {
                    balance.setOpeningBalance(policy.getAccrualAmount());
                } else {
                    balance.setOpeningBalance(BigDecimal.ZERO);
                }
                balance.setAccrued(BigDecimal.ZERO);
                balance.setUsed(BigDecimal.ZERO);
                balance.setCarryForward(BigDecimal.ZERO);
                balance.setEncashed(BigDecimal.ZERO);
                balance.setAdjusted(BigDecimal.ZERO);
                balance.setCreatedBy(currentUser);
                balance.setUpdatedBy(currentUser);
                balanceRepository.save(balance);
                created++;
            }
        }
        return created;
    }

    // ── Monthly accrual (called by scheduler) ─────────────────────────────────

    @Transactional
    public void accrueLeaves(String tenantId, UUID employeeId, int year, String performedBy) {
        List<LeavePolicy> policies = policyRepository
                .findByTenantIdAndActiveAndDeletedFalse(tenantId, true)
                .stream()
                .filter(p -> p.getAccrualType() == LeavePolicy.AccrualType.MONTHLY
                          || p.getAccrualType() == LeavePolicy.AccrualType.QUARTERLY)
                .toList();

        for (LeavePolicy policy : policies) {
            Optional<LeaveBalance> balOpt = balanceRepository
                    .findByEmployeeIdAndLeaveTypeIdAndYearAndTenantIdAndDeletedFalse(
                            employeeId, policy.getLeaveTypeId(), year, tenantId);
            if (balOpt.isEmpty()) continue;

            LeaveBalance balance = balOpt.get();
            BigDecimal currentAccrued = balance.getAccrued() != null
                    ? balance.getAccrued() : BigDecimal.ZERO;
            BigDecimal newAccrued = currentAccrued.add(policy.getAccrualAmount());

            // Cap at max accrual
            if (policy.getMaxAccrual() != null
                    && newAccrued.compareTo(policy.getMaxAccrual()) > 0) {
                newAccrued = policy.getMaxAccrual();
            }

            BigDecimal delta = newAccrued.subtract(currentAccrued);
            if (delta.compareTo(BigDecimal.ZERO) > 0) {
                balanceRepository.addAccrual(employeeId, policy.getLeaveTypeId(),
                        year, tenantId, delta, performedBy);
                log.debug("[LeaveBalance] Accrued {} days for employee {} leave type {}",
                        delta, employeeId, policy.getLeaveTypeId());
            }
        }
    }

    // ── Manual adjustment ─────────────────────────────────────────────────────

    @Transactional
    public LeaveBalanceDto.Response adjustBalance(String tenantId,
                                                   LeaveBalanceDto.AdjustRequest req,
                                                   String currentUser) {
        int year = req.getEffectiveDate().getYear();
        LeaveBalance balance = balanceRepository
                .findByEmployeeIdAndLeaveTypeIdAndYearAndTenantIdAndDeletedFalse(
                        req.getEmployeeId(), req.getLeaveTypeId(), year, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "LeaveBalance", "employee+leaveType+year",
                        req.getEmployeeId() + "/" + req.getLeaveTypeId() + "/" + year));

        BigDecimal days = req.getAdjustmentType() == LeaveAdjustment.AdjustmentType.CREDIT
                ? req.getDays()
                : req.getDays().negate();
        balance.setAdjusted(balance.getAdjusted().add(days));
        balance.setUpdatedBy(currentUser);
        balanceRepository.save(balance);

        // Record audit trail
        LeaveAdjustment adj = new LeaveAdjustment();
        adj.setTenantId(tenantId);
        adj.setEmployeeId(req.getEmployeeId());
        adj.setLeaveTypeId(req.getLeaveTypeId());
        adj.setAdjustmentType(req.getAdjustmentType());
        adj.setDays(req.getDays());
        adj.setReason(req.getReason());
        adj.setAdjustedBy(currentUser);
        adj.setEffectiveDate(req.getEffectiveDate());
        adj.setCreatedBy(currentUser);
        adj.setUpdatedBy(currentUser);
        adjustmentRepository.save(adj);

        return toResponse(balanceRepository.findById(balance.getId()).orElse(balance), tenantId);
    }

    // ── Comp-off credit ───────────────────────────────────────────────────────

    @Transactional
    public void creditCompOff(String tenantId, UUID employeeId, UUID leaveTypeId,
                               BigDecimal days, String performedBy) {
        int year = LocalDate.now().getYear();
        Optional<LeaveBalance> balOpt = balanceRepository
                .findByEmployeeIdAndLeaveTypeIdAndYearAndTenantIdAndDeletedFalse(
                        employeeId, leaveTypeId, year, tenantId);

        LeaveBalance balance = balOpt.orElseGet(() -> {
            LeaveBalance b = new LeaveBalance();
            b.setTenantId(tenantId);
            b.setEmployeeId(employeeId);
            b.setLeaveTypeId(leaveTypeId);
            b.setYear(year);
            b.setOpeningBalance(BigDecimal.ZERO);
            b.setAccrued(BigDecimal.ZERO);
            b.setUsed(BigDecimal.ZERO);
            b.setCarryForward(BigDecimal.ZERO);
            b.setEncashed(BigDecimal.ZERO);
            b.setAdjusted(BigDecimal.ZERO);
            b.setCreatedBy(performedBy);
            b.setUpdatedBy(performedBy);
            return balanceRepository.save(b);
        });

        balance.setAdjusted(balance.getAdjusted().add(days));
        balance.setUpdatedBy(performedBy);
        balanceRepository.save(balance);
    }

    // ── Package-visible: deduct / credit used (called by LeaveApplicationService) ──

    @Transactional
    void deductUsed(String tenantId, UUID employeeId, UUID leaveTypeId,
                    int year, BigDecimal days, String performedBy) {
        balanceRepository.incrementUsed(employeeId, leaveTypeId, year, tenantId, days, performedBy);
    }

    @Transactional
    void creditBack(String tenantId, UUID employeeId, UUID leaveTypeId,
                    int year, BigDecimal days, String performedBy) {
        balanceRepository.decrementUsed(employeeId, leaveTypeId, year, tenantId, days, performedBy);
    }

    // ── private ───────────────────────────────────────────────────────────────

    private boolean isPolicyActiveForYear(LeavePolicy policy, int year) {
        LocalDate jan1 = LocalDate.of(year, 1, 1);
        LocalDate dec31 = LocalDate.of(year, 12, 31);
        return !policy.getEffectiveFrom().isAfter(dec31)
                && (policy.getEffectiveTo() == null || !policy.getEffectiveTo().isBefore(jan1));
    }

    private LeaveBalanceDto.Response toResponse(LeaveBalance b, String tenantId) {
        String ltName = leaveTypeRepository.findById(b.getLeaveTypeId())
                .map(LeaveType::getName).orElse(null);
        String ltColor = leaveTypeRepository.findById(b.getLeaveTypeId())
                .map(LeaveType::getColor).orElse(null);
        boolean ltPaid = leaveTypeRepository.findById(b.getLeaveTypeId())
                .map(LeaveType::isPaid).orElse(true);
        return LeaveBalanceDto.Response.builder()
                .id(b.getId()).employeeId(b.getEmployeeId()).leaveTypeId(b.getLeaveTypeId())
                .leaveTypeName(ltName).leaveTypeColor(ltColor).leaveTypePaid(ltPaid)
                .year(b.getYear())
                .openingBalance(b.getOpeningBalance()).accrued(b.getAccrued())
                .used(b.getUsed()).carryForward(b.getCarryForward())
                .encashed(b.getEncashed()).adjusted(b.getAdjusted())
                .available(b.getAvailable())
                .build();
    }
}
