package com.hrms.payroll.repository;

import com.hrms.payroll.entity.LoanRepayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface LoanRepaymentRepository extends JpaRepository<LoanRepayment, UUID> {

    List<LoanRepayment> findByLoanIdOrderByEmiNumber(UUID loanId);

    int countByLoanId(UUID loanId);

    @Query("SELECT COALESCE(SUM(lr.principalPart), 0) FROM LoanRepayment lr WHERE lr.loanId = :loanId")
    java.math.BigDecimal sumPrincipalRepaid(@Param("loanId") UUID loanId);
}
