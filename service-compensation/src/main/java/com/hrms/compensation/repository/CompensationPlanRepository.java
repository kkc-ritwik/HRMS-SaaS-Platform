package com.hrms.compensation.repository;

import com.hrms.compensation.entity.CompensationPlan;
import com.hrms.compensation.entity.CompensationPlan.PlanStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CompensationPlanRepository extends JpaRepository<CompensationPlan, UUID> {

    Optional<CompensationPlan> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Page<CompensationPlan> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId, Pageable pageable);

    List<CompensationPlan> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId);

    List<CompensationPlan> findByTenantIdAndEmployeeIdAndDeletedFalseOrderByEffectiveDateDesc(String tenantId, UUID employeeId);

    Optional<CompensationPlan> findByTenantIdAndEmployeeIdAndStatusAndDeletedFalse(String tenantId, UUID employeeId, PlanStatus status);
}
