package com.hrms.notification.repository;

import com.hrms.notification.entity.EmailTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, UUID> {

    Optional<EmailTemplate> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Page<EmailTemplate> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId, Pageable pageable);

    List<EmailTemplate> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId);

    Optional<EmailTemplate> findByCodeAndTenantIdAndDeletedFalse(String code, String tenantId);
}
