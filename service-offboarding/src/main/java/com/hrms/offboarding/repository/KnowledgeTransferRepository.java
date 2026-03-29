package com.hrms.offboarding.repository;

import com.hrms.offboarding.entity.KnowledgeTransfer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface KnowledgeTransferRepository extends JpaRepository<KnowledgeTransfer, UUID> {

    Optional<KnowledgeTransfer> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Page<KnowledgeTransfer> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId, Pageable pageable);

    List<KnowledgeTransfer> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId);

    List<KnowledgeTransfer> findByTenantIdAndSeparationIdAndDeletedFalse(String tenantId, UUID separationId);

    List<KnowledgeTransfer> findByTenantIdAndFromEmployeeIdAndDeletedFalse(String tenantId, UUID fromEmployeeId);

    List<KnowledgeTransfer> findByTenantIdAndToEmployeeIdAndDeletedFalse(String tenantId, UUID toEmployeeId);
}
