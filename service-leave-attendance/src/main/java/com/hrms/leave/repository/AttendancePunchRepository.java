package com.hrms.leave.repository;

import com.hrms.leave.entity.AttendancePunch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttendancePunchRepository extends JpaRepository<AttendancePunch, UUID> {

    List<AttendancePunch> findByEmployeeIdAndTenantIdAndPunchTimeBetweenOrderByPunchTimeAsc(
            UUID employeeId, String tenantId, Instant from, Instant to);

    /** Last valid punch of a given type for an employee today. */
    Optional<AttendancePunch> findFirstByEmployeeIdAndTenantIdAndTypeAndValidTrueOrderByPunchTimeDesc(
            UUID employeeId, String tenantId, AttendancePunch.PunchType type);

    /** All today's punches for an employee, ordered ascending. */
    @Query("SELECT p FROM AttendancePunch p " +
           "WHERE p.tenantId = :tenantId AND p.employeeId = :employeeId " +
           "AND p.deleted = false AND p.valid = true " +
           "AND p.punchTime >= :dayStart AND p.punchTime < :dayEnd " +
           "ORDER BY p.punchTime ASC")
    List<AttendancePunch> findDayPunches(@Param("tenantId") String tenantId,
                                          @Param("employeeId") UUID employeeId,
                                          @Param("dayStart") Instant dayStart,
                                          @Param("dayEnd") Instant dayEnd);
}
