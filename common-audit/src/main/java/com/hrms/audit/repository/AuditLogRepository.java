package com.hrms.audit.repository;

import com.hrms.audit.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    Page<AuditLog> findByTenantIdAndEntityNameAndEntityIdOrderByCreatedAtDesc(
            String tenantId, String entityName, String entityId, Pageable pageable);

    Page<AuditLog> findByTenantIdAndActorIdOrderByCreatedAtDesc(
            String tenantId, String actorId, Pageable pageable);

    Page<AuditLog> findByTenantIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            String tenantId, OffsetDateTime from, OffsetDateTime to, Pageable pageable);
}
