package com.hrms.reports.repository;

import com.hrms.reports.entity.SavedReport;
import com.hrms.reports.entity.SavedReport.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SavedReportRepository extends JpaRepository<SavedReport, UUID> {

    Optional<SavedReport> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Page<SavedReport> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId, Pageable pageable);

    List<SavedReport> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId);

    Page<SavedReport> findByTenantIdAndDefinitionIdAndDeletedFalseOrderByCreatedAtDesc(
            String tenantId, UUID definitionId, Pageable pageable);

    Page<SavedReport> findByTenantIdAndGeneratedByAndDeletedFalseOrderByCreatedAtDesc(
            String tenantId, UUID generatedBy, Pageable pageable);

    List<SavedReport> findByTenantIdAndStatusAndDeletedFalse(String tenantId, ReportStatus status);
}
