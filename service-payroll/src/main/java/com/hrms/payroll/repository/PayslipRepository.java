package com.hrms.payroll.repository;

import com.hrms.payroll.entity.Payslip;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PayslipRepository extends JpaRepository<Payslip, UUID> {

    Optional<Payslip> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Optional<Payslip> findByTenantIdAndEmployeeIdAndMonthAndYearAndPayrollRunIdAndDeletedFalse(
            String tenantId, UUID employeeId, int month, int year, UUID payrollRunId);

    /** Employee's payslips for a specific year. */
    List<Payslip> findByTenantIdAndEmployeeIdAndYearAndDeletedFalseOrderByMonthDesc(
            String tenantId, UUID employeeId, int year);

    /** Employee's payslips (paginated). */
    Page<Payslip> findByTenantIdAndEmployeeIdAndDeletedFalseOrderByYearDescMonthDesc(
            String tenantId, UUID employeeId, Pageable pageable);

    /** All payslips for a payroll run. */
    List<Payslip> findByPayrollRunIdAndTenantIdAndDeletedFalse(UUID payrollRunId, String tenantId);

    /** TDS deducted so far in the financial year (April onwards). */
    @Query("SELECT COALESCE(SUM(p.tds), 0) FROM Payslip p " +
           "WHERE p.tenantId = :tenantId AND p.employeeId = :employeeId " +
           "AND p.deleted = false " +
           "AND ((p.year = :fyStart AND p.month >= 4) OR (p.year = :fyEnd AND p.month < 4))")
    java.math.BigDecimal sumTdsForFY(@Param("tenantId")  String tenantId,
                                     @Param("employeeId") UUID employeeId,
                                     @Param("fyStart")    int fyStart,
                                     @Param("fyEnd")      int fyEnd);

    /** Bulk status update when a run is locked. */
    @Modifying
    @Query("UPDATE Payslip p SET p.status = :status, p.updatedBy = :updatedBy " +
           "WHERE p.payrollRunId = :runId AND p.tenantId = :tenantId AND p.deleted = false")
    int updateStatusByRun(@Param("runId")     UUID runId,
                          @Param("tenantId")  String tenantId,
                          @Param("status")    Payslip.PayslipStatus status,
                          @Param("updatedBy") String updatedBy);
}
