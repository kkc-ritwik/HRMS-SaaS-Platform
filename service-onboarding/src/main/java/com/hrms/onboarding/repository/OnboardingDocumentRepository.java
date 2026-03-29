package com.hrms.onboarding.repository;

import com.hrms.onboarding.entity.OnboardingDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OnboardingDocumentRepository extends JpaRepository<OnboardingDocument, UUID> {

    Page<OnboardingDocument> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId, Pageable pageable);

    List<OnboardingDocument> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId);

    Optional<OnboardingDocument> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    List<OnboardingDocument> findByTenantIdAndEmployeeIdAndDeletedFalse(String tenantId, UUID employeeId);
}
