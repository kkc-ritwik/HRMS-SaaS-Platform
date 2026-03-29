package com.hrms.leave.repository;

import com.hrms.leave.entity.ShiftSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShiftScheduleRepository extends JpaRepository<ShiftSchedule, UUID> {

    Optional<ShiftSchedule> findByEmployeeIdAndScheduleDateAndTenantIdAndDeletedFalse(
            UUID employeeId, LocalDate date, String tenantId);

    List<ShiftSchedule> findByEmployeeIdAndTenantIdAndScheduleDateBetweenAndDeletedFalse(
            UUID employeeId, String tenantId, LocalDate from, LocalDate to);

    List<ShiftSchedule> findByTenantIdAndScheduleDateAndDeletedFalse(
            String tenantId, LocalDate date);

    @Query("SELECT ss FROM ShiftSchedule ss WHERE ss.tenantId = :tenantId " +
           "AND ss.employeeId IN :employeeIds AND ss.deleted = false " +
           "AND ss.scheduleDate BETWEEN :from AND :to")
    List<ShiftSchedule> findTeamSchedules(@Param("tenantId") String tenantId,
                                           @Param("employeeIds") List<UUID> employeeIds,
                                           @Param("from") LocalDate from,
                                           @Param("to") LocalDate to);

    void deleteByEmployeeIdAndScheduleDateAndTenantId(
            UUID employeeId, LocalDate date, String tenantId);
}
