package com.hrms.document.repository;

import com.hrms.document.entity.DocumentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentTypeRepository extends JpaRepository<DocumentType, UUID> {

    Optional<DocumentType> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Page<DocumentType> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId, Pageable pageable);

    List<DocumentType> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId);
}
