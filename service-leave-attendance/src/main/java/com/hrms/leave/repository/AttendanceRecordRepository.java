package com.hrms.leave.repository;

import com.hrms.leave.entity.AttendanceRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, UUID> {

    Optional<AttendanceRecord> findByEmployeeIdAndAttendanceDateAndTenantIdAndDeletedFalse(
            UUID employeeId, LocalDate date, String tenantId);

    @Query("SELECT ar FROM AttendanceRecord ar " +
           "WHERE ar.tenantId = :tenantId AND ar.employeeId = :employeeId " +
           "AND ar.deleted = false " +
           "AND EXTRACT(YEAR FROM ar.attendanceDate) = :year " +
           "AND EXTRACT(MONTH FROM ar.attendanceDate) = :month " +
           "ORDER BY ar.attendanceDate ASC")
    List<AttendanceRecord> findByEmployeeAndMonth(@Param("tenantId") String tenantId,
                                                   @Param("employeeId") UUID employeeId,
                                                   @Param("year") int year,
                                                   @Param("month") int month);

    List<AttendanceRecord> findByTenantIdAndAttendanceDateAndDeletedFalse(
            String tenantId, LocalDate date);

    @Query("SELECT ar FROM AttendanceRecord ar " +
           "WHERE ar.tenantId = :tenantId AND ar.employeeId IN :employeeIds " +
           "AND ar.deleted = false AND ar.attendanceDate = :date")
    List<AttendanceRecord> findTeamAttendance(@Param("tenantId") String tenantId,
                                               @Param("employeeIds") List<UUID> employeeIds,
                                               @Param("date") LocalDate date);

    long countByTenantIdAndAttendanceDateAndStatusAndDeletedFalse(
            String tenantId, LocalDate date, AttendanceRecord.AttendanceStatus status);

    /** Employees who have punched in today (firstCheckIn is not null). */
    @Query("SELECT ar FROM AttendanceRecord ar " +
           "WHERE ar.tenantId = :tenantId AND ar.attendanceDate = :date " +
           "AND ar.deleted = false AND ar.firstCheckIn IS NOT NULL")
    List<AttendanceRecord> findPresentToday(@Param("tenantId") String tenantId,
                                             @Param("date") LocalDate date);

    /** Monthly attendance summary by employee. */
    @Query("SELECT ar FROM AttendanceRecord ar " +
           "WHERE ar.tenantId = :tenantId AND ar.employeeId IN :employeeIds " +
           "AND ar.deleted = false " +
           "AND ar.attendanceDate BETWEEN :from AND :to " +
           "ORDER BY ar.employeeId, ar.attendanceDate")
    List<AttendanceRecord> findTeamMonthly(@Param("tenantId") String tenantId,
                                            @Param("employeeIds") List<UUID> employeeIds,
                                            @Param("from") LocalDate from,
                                            @Param("to") LocalDate to);

    Page<AttendanceRecord> findByEmployeeIdAndTenantIdAndDeletedFalse(
            UUID employeeId, String tenantId, Pageable pageable);
}
