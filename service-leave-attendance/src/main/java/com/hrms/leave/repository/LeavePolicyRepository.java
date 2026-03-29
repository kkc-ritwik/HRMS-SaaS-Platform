package com.hrms.leave.repository;

import com.hrms.leave.entity.LeavePolicy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeavePolicyRepository extends JpaRepository<LeavePolicy, UUID> {

    Optional<LeavePolicy> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Page<LeavePolicy> findByTenantIdAndDeletedFalse(String tenantId, Pageable pageable);

    List<LeavePolicy> findByLeaveTypeIdAndTenantIdAndActiveAndDeletedFalse(
            UUID leaveTypeId, String tenantId, boolean active);

    /** Find the active policy for a leave type that is effective today. */
    Optional<LeavePolicy> findFirstByLeaveTypeIdAndTenantIdAndActiveAndDeletedFalseAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(
            UUID leaveTypeId, String tenantId, boolean active, LocalDate today);

    List<LeavePolicy> findByTenantIdAndActiveAndDeletedFalse(String tenantId, boolean active);
}
