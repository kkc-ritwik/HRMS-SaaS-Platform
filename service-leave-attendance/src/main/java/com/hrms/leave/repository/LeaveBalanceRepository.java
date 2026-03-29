package com.hrms.leave.repository;

import com.hrms.leave.entity.LeaveBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, UUID> {

    List<LeaveBalance> findByEmployeeIdAndTenantIdAndYearAndDeletedFalse(
            UUID employeeId, String tenantId, int year);

    Optional<LeaveBalance> findByEmployeeIdAndLeaveTypeIdAndYearAndTenantIdAndDeletedFalse(
            UUID employeeId, UUID leaveTypeId, int year, String tenantId);

    List<LeaveBalance> findByTenantIdAndYearAndDeletedFalse(String tenantId, int year);

    @Modifying
    @Query("UPDATE LeaveBalance lb SET lb.used = lb.used + :days, lb.updatedBy = :updatedBy " +
           "WHERE lb.employeeId = :employeeId AND lb.leaveTypeId = :leaveTypeId " +
           "AND lb.year = :year AND lb.tenantId = :tenantId AND lb.deleted = false")
    int incrementUsed(@Param("employeeId") UUID employeeId,
                      @Param("leaveTypeId") UUID leaveTypeId,
                      @Param("year") int year,
                      @Param("tenantId") String tenantId,
                      @Param("days") BigDecimal days,
                      @Param("updatedBy") String updatedBy);

    @Modifying
    @Query("UPDATE LeaveBalance lb SET lb.used = GREATEST(lb.used - :days, 0), lb.updatedBy = :updatedBy " +
           "WHERE lb.employeeId = :employeeId AND lb.leaveTypeId = :leaveTypeId " +
           "AND lb.year = :year AND lb.tenantId = :tenantId AND lb.deleted = false")
    int decrementUsed(@Param("employeeId") UUID employeeId,
                      @Param("leaveTypeId") UUID leaveTypeId,
                      @Param("year") int year,
                      @Param("tenantId") String tenantId,
                      @Param("days") BigDecimal days,
                      @Param("updatedBy") String updatedBy);

    @Modifying
    @Query("UPDATE LeaveBalance lb SET lb.accrued = lb.accrued + :amount, lb.updatedBy = :updatedBy " +
           "WHERE lb.employeeId = :employeeId AND lb.leaveTypeId = :leaveTypeId " +
           "AND lb.year = :year AND lb.tenantId = :tenantId AND lb.deleted = false")
    int addAccrual(@Param("employeeId") UUID employeeId,
                   @Param("leaveTypeId") UUID leaveTypeId,
                   @Param("year") int year,
                   @Param("tenantId") String tenantId,
                   @Param("amount") BigDecimal amount,
                   @Param("updatedBy") String updatedBy);
}
