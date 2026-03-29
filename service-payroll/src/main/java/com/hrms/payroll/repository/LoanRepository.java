package com.hrms.payroll.repository;

import com.hrms.payroll.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoanRepository extends JpaRepository<Loan, UUID> {

    Optional<Loan> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    List<Loan> findByEmployeeIdAndTenantIdAndDeletedFalseOrderByDisbursementDateDesc(
            UUID employeeId, String tenantId);

    /** Active loans whose deduction should start at or before the given payroll month. */
    List<Loan> findByTenantIdAndStatusAndStartDeductionMonthLessThanEqualAndDeletedFalse(
            String tenantId, Loan.LoanStatus status, LocalDate payrollMonth);

    List<Loan> findByEmployeeIdAndTenantIdAndStatusAndDeletedFalse(
            UUID employeeId, String tenantId, Loan.LoanStatus status);
}
