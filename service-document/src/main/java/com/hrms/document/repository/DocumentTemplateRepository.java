package com.hrms.document.repository;

import com.hrms.document.entity.DocumentTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentTemplateRepository extends JpaRepository<DocumentTemplate, UUID> {

    Optional<DocumentTemplate> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Page<DocumentTemplate> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId, Pageable pageable);

    List<DocumentTemplate> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId);
}
