package com.hrms.onboarding.repository;

import com.hrms.onboarding.entity.OnboardingTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OnboardingTaskRepository extends JpaRepository<OnboardingTask, UUID> {

    Page<OnboardingTask> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId, Pageable pageable);

    List<OnboardingTask> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId);

    Optional<OnboardingTask> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    List<OnboardingTask> findByTenantIdAndTemplateIdAndDeletedFalse(String tenantId, UUID templateId);

    List<OnboardingTask> findByTenantIdAndEmployeeIdAndDeletedFalse(String tenantId, UUID employeeId);
}
