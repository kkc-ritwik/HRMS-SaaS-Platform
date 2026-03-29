package com.hrms.onboarding.repository;

import com.hrms.onboarding.entity.OnboardingTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OnboardingTemplateRepository extends JpaRepository<OnboardingTemplate, UUID> {

    Page<OnboardingTemplate> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId, Pageable pageable);

    List<OnboardingTemplate> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId);

    Optional<OnboardingTemplate> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);
}
