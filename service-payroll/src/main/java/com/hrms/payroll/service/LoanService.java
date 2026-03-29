package com.hrms.payroll.service;

import com.hrms.common.exception.BusinessException;
import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.payroll.dto.LoanDto;
import com.hrms.payroll.entity.Loan;
import com.hrms.payroll.entity.LoanRepayment;
import com.hrms.payroll.repository.LoanRepaymentRepository;
import com.hrms.payroll.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository           loanRepository;
    private final LoanRepaymentRepository  repaymentRepository;

    // ── Create ────────────────────────────────────────────────────────────────

    @Transactional
    public LoanDto.Response create(String tenantId, UUID employeeId,
                                   LoanDto.CreateRequest req, String currentUser) {
        Loan loan = new Loan();
        loan.setTenantId(tenantId);
        loan.setEmployeeId(employeeId);
        loan.setLoanType(req.getLoanType());
        loan.setPrincipalAmount(req.getPrincipalAmount());
        loan.setInterestRate(req.getInterestRate() != null ? req.getInterestRate() : BigDecimal.ZERO);
        loan.setTenureMonths(req.getTenureMonths());
        loan.setDisbursementDate(req.getDisbursementDate());

        LocalDate startMonth = req.getStartDeductionMonth() != null
                ? req.getStartDeductionMonth().withDayOfMonth(1)
                : req.getDisbursementDate().plusMonths(1).withDayOfMonth(1);
        loan.setStartDeductionMonth(startMonth);

        BigDecimal emi = calculateEmi(req.getPrincipalAmount(), loan.getInterestRate(), req.getTenureMonths());
        loan.setEmiAmount(emi);
        loan.setOutstandingBalance(req.getPrincipalAmount());
        loan.setStatus(Loan.LoanStatus.ACTIVE);
        loan.setCreatedBy(currentUser);

        return toResponse(loanRepository.save(loan), 0);
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<LoanDto.Response> getMyLoans(String tenantId, UUID employeeId) {
        return loanRepository
                .findByEmployeeIdAndTenantIdAndDeletedFalseOrderByDisbursementDateDesc(employeeId, tenantId)
                .stream()
                .map(l -> toResponse(l, repaymentRepository.countByLoanId(l.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public LoanDto.Response getLoan(String tenantId, UUID loanId) {
        Loan loan = getEntity(tenantId, loanId);
        return toResponse(loan, repaymentRepository.countByLoanId(loanId));
    }

    // ── Amortization schedule ────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<LoanDto.RepaymentScheduleItem> getRepaymentSchedule(String tenantId, UUID loanId) {
        Loan loan = getEntity(tenantId, loanId);
        List<LoanRepayment> paid = repaymentRepository.findByLoanIdOrderByEmiNumber(loanId);

        List<LoanDto.RepaymentScheduleItem> schedule = new ArrayList<>();
        BigDecimal balance    = loan.getPrincipalAmount();
        BigDecimal annualRate = loan.getInterestRate();
        boolean    interestFree = annualRate.compareTo(BigDecimal.ZERO) == 0;

        for (int i = 1; i <= loan.getTenureMonths(); i++) {
            BigDecimal interest;
            BigDecimal principal;
            BigDecimal emi = loan.getEmiAmount();

            if (interestFree) {
                interest  = BigDecimal.ZERO;
                principal = emi.min(balance);
            } else {
                BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(1200), 10, RoundingMode.HALF_UP);
                interest  = balance.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
                principal = emi.subtract(interest).min(balance);
            }

            BigDecimal closing = balance.subtract(principal).max(BigDecimal.ZERO);
            final int emiNum   = i;
            boolean   isPaid   = paid.stream().anyMatch(r -> r.getEmiNumber() == emiNum);

            schedule.add(LoanDto.RepaymentScheduleItem.builder()
                    .emiNumber(i)
                    .openingBalance(balance)
                    .emiAmount(emi)
                    .principalPart(principal)
                    .interestPart(interest)
                    .closingBalance(closing)
                    .paid(isPaid)
                    .build());

            balance = closing;
            if (balance.compareTo(BigDecimal.ZERO) == 0) break;
        }
        return schedule;
    }

    // ── Close loan ────────────────────────────────────────────────────────────

    @Transactional
    public LoanDto.Response closeLoan(String tenantId, UUID loanId, String currentUser) {
        Loan loan = getEntity(tenantId, loanId);
        if (loan.getStatus() != Loan.LoanStatus.ACTIVE) {
            throw new BusinessException("LOAN_NOT_ACTIVE",
                    "Only ACTIVE loans can be closed. Current status: " + loan.getStatus());
        }
        loan.setStatus(Loan.LoanStatus.CLOSED);
        loan.setOutstandingBalance(BigDecimal.ZERO);
        loan.setUpdatedBy(currentUser);
        return toResponse(loanRepository.save(loan), repaymentRepository.countByLoanId(loanId));
    }

    // ── EMI calculation ───────────────────────────────────────────────────────

    /**
     * Standard reducing-balance EMI formula:
     *   EMI = P × r × (1 + r)^n / ((1 + r)^n − 1)
     * For interest-free loans: EMI = P / n
     */
    public static BigDecimal calculateEmi(BigDecimal principal, BigDecimal annualRatePercent, int months) {
        if (annualRatePercent.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(BigDecimal.valueOf(months), 2, RoundingMode.HALF_UP);
        }
        BigDecimal r    = annualRatePercent.divide(BigDecimal.valueOf(1200), 10, RoundingMode.HALF_UP);
        BigDecimal onePlusR = BigDecimal.ONE.add(r);
        BigDecimal pow  = onePlusR.pow(months, new MathContext(15, RoundingMode.HALF_UP));
        BigDecimal emi  = principal.multiply(r).multiply(pow)
                .divide(pow.subtract(BigDecimal.ONE), 2, RoundingMode.HALF_UP);
        return emi;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Loan getEntity(String tenantId, UUID id) {
        return loanRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan", "id", id));
    }

    private LoanDto.Response toResponse(Loan l, int repaymentsMade) {
        return LoanDto.Response.builder()
                .id(l.getId())
                .employeeId(l.getEmployeeId())
                .loanType(l.getLoanType())
                .principalAmount(l.getPrincipalAmount())
                .interestRate(l.getInterestRate())
                .tenureMonths(l.getTenureMonths())
                .emiAmount(l.getEmiAmount())
                .disbursementDate(l.getDisbursementDate())
                .startDeductionMonth(l.getStartDeductionMonth())
                .outstandingBalance(l.getOutstandingBalance())
                .status(l.getStatus())
                .repaymentsMade(repaymentsMade)
                .createdAt(l.getCreatedAt())
                .updatedAt(l.getUpdatedAt())
                .build();
    }
}
