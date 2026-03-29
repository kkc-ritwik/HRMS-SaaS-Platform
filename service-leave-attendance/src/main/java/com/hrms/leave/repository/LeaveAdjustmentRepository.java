package com.hrms.leave.repository;

import com.hrms.leave.entity.LeaveAdjustment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LeaveAdjustmentRepository extends JpaRepository<LeaveAdjustment, UUID> {

    Page<LeaveAdjustment> findByEmployeeIdAndTenantIdAndDeletedFalse(
            UUID employeeId, String tenantId, Pageable pageable);

    List<LeaveAdjustment> findByEmployeeIdAndLeaveTypeIdAndTenantIdAndDeletedFalse(
            UUID employeeId, UUID leaveTypeId, String tenantId);
}
