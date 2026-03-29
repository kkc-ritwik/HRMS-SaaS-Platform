package com.hrms.performance.repository;

import com.hrms.performance.entity.PipPlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PipPlanRepository extends JpaRepository<PipPlan, UUID> {

    Optional<PipPlan> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Page<PipPlan> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId, Pageable pageable);

    List<PipPlan> findByTenantIdAndEmployeeIdAndDeletedFalseOrderByCreatedAtDesc(
            String tenantId, UUID employeeId);

    List<PipPlan> findByTenantIdAndManagerIdAndDeletedFalse(String tenantId, UUID managerId);

    List<PipPlan> findByTenantIdAndStatusAndDeletedFalse(String tenantId, PipPlan.PipStatus status);
}
