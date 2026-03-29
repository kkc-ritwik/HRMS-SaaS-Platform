package com.hrms.compliance.repository;

import com.hrms.compliance.entity.ComplianceItem;
import com.hrms.compliance.entity.ComplianceItem.ComplianceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ComplianceItemRepository extends JpaRepository<ComplianceItem, UUID> {

    Optional<ComplianceItem> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Page<ComplianceItem> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId, Pageable pageable);

    List<ComplianceItem> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId);

    Page<ComplianceItem> findByTenantIdAndStatusAndDeletedFalse(String tenantId, ComplianceStatus status, Pageable pageable);

    List<ComplianceItem> findByTenantIdAndOwnerIdAndDeletedFalse(String tenantId, UUID ownerId);
}
