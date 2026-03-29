package com.hrms.reports.repository;

import com.hrms.reports.entity.ReportDefinition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReportDefinitionRepository extends JpaRepository<ReportDefinition, UUID> {

    Optional<ReportDefinition> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Page<ReportDefinition> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId, Pageable pageable);

    List<ReportDefinition> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId);

    List<ReportDefinition> findByTenantIdAndActiveAndDeletedFalse(String tenantId, boolean active);

    Page<ReportDefinition> findByTenantIdAndCategoryAndDeletedFalse(String tenantId, String category, Pageable pageable);

    Optional<ReportDefinition> findByCodeAndTenantIdAndDeletedFalse(String code, String tenantId);
}
