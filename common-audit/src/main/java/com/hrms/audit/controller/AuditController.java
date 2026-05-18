package com.hrms.audit.controller;

import com.hrms.audit.entity.AuditLog;
import com.hrms.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(
        name = "hrms.audit.controller.enabled", havingValue = "true", matchIfMissing = true)
public class AuditController {

    private final AuditLogRepository repo;

    @GetMapping("/entity")
    public Page<AuditLog> byEntity(@RequestParam("tenantId") String tenantId,
                                   @RequestParam("entityName") String entityName,
                                   @RequestParam("entityId") String entityId,
                                   Pageable pageable) {
        return repo.findByTenantIdAndEntityNameAndEntityIdOrderByCreatedAtDesc(tenantId, entityName, entityId, pageable);
    }

    @GetMapping("/actor")
    public Page<AuditLog> byActor(@RequestParam("tenantId") String tenantId,
                                  @RequestParam("actorId") String actorId,
                                  Pageable pageable) {
        return repo.findByTenantIdAndActorIdOrderByCreatedAtDesc(tenantId, actorId, pageable);
    }

    @GetMapping("/range")
    public Page<AuditLog> byRange(@RequestParam("tenantId") String tenantId,
                                  @RequestParam("from") OffsetDateTime from,
                                  @RequestParam("to") OffsetDateTime to,
                                  Pageable pageable) {
        return repo.findByTenantIdAndCreatedAtBetweenOrderByCreatedAtDesc(tenantId, from, to, pageable);
    }
}
