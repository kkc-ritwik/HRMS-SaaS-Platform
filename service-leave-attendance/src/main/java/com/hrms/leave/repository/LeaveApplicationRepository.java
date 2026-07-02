package com.hrms.leave.repository;

import com.hrms.leave.entity.LeaveApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeaveApplicationRepository extends JpaRepository<LeaveApplication, UUID> {

    Optional<LeaveApplication> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Page<LeaveApplication> findByEmployeeIdAndTenantIdAndDeletedFalse(
            UUID employeeId, String tenantId, Pageable pageable);

    @Query("SELECT la FROM LeaveApplication la " +
           "WHERE la.tenantId = :tenantId AND la.employeeId = :employeeId " +
           "AND la.deleted = false " +
           "AND (:status IS NULL OR la.status = :status) " +
           "AND (:year IS NULL OR EXTRACT(YEAR FROM la.fromDate) = :year)")
    Page<LeaveApplication> findByEmployeeFiltered(
            @Param("tenantId") String tenantId,
            @Param("employeeId") UUID employeeId,
            @Param("status") LeaveApplication.LeaveStatus status,
            @Param("year") Integer year,
            Pageable pageable);

    /** Check for overlapping approved/pending leaves (conflict check). */
    @Query("SELECT la FROM LeaveApplication la " +
           "WHERE la.tenantId = :tenantId AND la.employeeId = :employeeId " +
           "AND la.deleted = false " +
           "AND la.status IN ('PENDING', 'APPROVED') " +
           "AND la.fromDate <= :toDate AND la.toDate >= :fromDate")
    List<LeaveApplication> findConflicting(
            @Param("tenantId") String tenantId,
            @Param("employeeId") UUID employeeId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);

    /** Team leaves: leaves for a list of employee IDs in a date range. */
    @Query("SELECT la FROM LeaveApplication la " +
           "WHERE la.tenantId = :tenantId " +
           "AND la.employeeId IN :employeeIds " +
           "AND la.deleted = false " +
           "AND la.status IN ('PENDING', 'APPROVED') " +
           "AND la.fromDate <= :toDate AND la.toDate >= :fromDate " +
           "ORDER BY la.fromDate ASC")
    List<LeaveApplication> findTeamLeaves(
            @Param("tenantId") String tenantId,
            @Param("employeeIds") List<UUID> employeeIds,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);

    Page<LeaveApplication> findByTenantIdAndEmployeeIdInAndDeletedFalse(
            String tenantId, List<UUID> employeeIds, Pageable pageable);

    /** Tenant-wide leaves (HR/admin view), optionally filtered by status. */
    @Query("SELECT la FROM LeaveApplication la WHERE la.tenantId = :tenantId AND la.deleted = false " +
           "AND (:status IS NULL OR la.status = :status) ORDER BY la.fromDate DESC")
    Page<LeaveApplication> findByTenantFiltered(
            @Param("tenantId") String tenantId,
            @Param("status") LeaveApplication.LeaveStatus status,
            Pageable pageable);
}
