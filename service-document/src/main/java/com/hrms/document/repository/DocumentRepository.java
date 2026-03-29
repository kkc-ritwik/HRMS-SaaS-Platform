package com.hrms.document.repository;

import com.hrms.document.entity.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {

    Optional<Document> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Page<Document> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId, Pageable pageable);

    List<Document> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId);

    List<Document> findByTenantIdAndEmployeeIdAndDeletedFalse(String tenantId, UUID employeeId);

    List<Document> findByTenantIdAndDocumentTypeIdAndDeletedFalse(String tenantId, UUID documentTypeId);
}
