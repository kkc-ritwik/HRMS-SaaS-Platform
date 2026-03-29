package com.hrms.expense.repository;

import com.hrms.expense.entity.ExpenseReport;
import com.hrms.expense.entity.ExpenseReport.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExpenseReportRepository extends JpaRepository<ExpenseReport, UUID> {

    Optional<ExpenseReport> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Page<ExpenseReport> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId, Pageable pageable);

    List<ExpenseReport> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId);

    Page<ExpenseReport> findByTenantIdAndEmployeeIdAndDeletedFalseOrderByCreatedAtDesc(
            String tenantId, UUID employeeId, Pageable pageable);

    List<ExpenseReport> findByTenantIdAndStatusAndDeletedFalse(String tenantId, ReportStatus status);
}
