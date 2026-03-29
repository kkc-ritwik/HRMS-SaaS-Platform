package com.hrms.leave.repository;

import com.hrms.leave.entity.OvertimeRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OvertimeRecordRepository extends JpaRepository<OvertimeRecord, UUID> {

    Optional<OvertimeRecord> findByEmployeeIdAndOvertimeDateAndTenantIdAndDeletedFalse(
            UUID employeeId, LocalDate date, String tenantId);

    Page<OvertimeRecord> findByEmployeeIdAndTenantIdAndDeletedFalse(
            UUID employeeId, String tenantId, Pageable pageable);

    List<OvertimeRecord> findByTenantIdAndStatusAndDeletedFalse(
            String tenantId, OvertimeRecord.OvertimeStatus status);
}
