package com.hrms.leave.repository;

import com.hrms.leave.entity.LeaveType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeaveTypeRepository extends JpaRepository<LeaveType, UUID> {

    Optional<LeaveType> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Page<LeaveType> findByTenantIdAndDeletedFalse(String tenantId, Pageable pageable);

    List<LeaveType> findByTenantIdAndActiveAndDeletedFalse(String tenantId, boolean active);

    boolean existsByCodeAndTenantIdAndDeletedFalse(String code, String tenantId);

    boolean existsByCodeAndTenantIdAndDeletedFalseAndIdNot(String code, String tenantId, UUID id);
}
