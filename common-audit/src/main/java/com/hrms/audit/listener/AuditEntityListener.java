package com.hrms.audit.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.context.AuditContext;
import com.hrms.audit.entity.AuditAction;
import com.hrms.audit.entity.AuditLog;
import com.hrms.audit.repository.AuditLogRepository;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * Listener that captures CREATE / UPDATE / DELETE on @Auditable entities.
 * Apply to entities with {@code @EntityListeners(AuditEntityListener.class)}.
 *
 * Note: PostUpdate sees only the AFTER state (Hibernate doesn't expose the BEFORE snapshot
 * to listeners). For BEFORE/AFTER diffing, use Hibernate Envers OR call AuditService.logChange()
 * manually from the service layer. This listener captures the AFTER snapshot reliably.
 */
@Slf4j
@Component
public class AuditEntityListener {

    private static ApplicationContext CTX;
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    @Autowired
    public void init(ApplicationContext ctx) { CTX = ctx; }

    @PostPersist
    public void onPersist(Object entity) { write(entity, AuditAction.CREATE); }

    @PostUpdate
    public void onUpdate(Object entity) { write(entity, AuditAction.UPDATE); }

    @PostRemove
    public void onRemove(Object entity) { write(entity, AuditAction.DELETE); }

    private void write(Object entity, AuditAction action) {
        try {
            if (CTX == null) return;
            Auditable ann = entity.getClass().getAnnotation(Auditable.class);
            if (ann == null) return;

            Set<String> redact = new HashSet<>(Arrays.asList(ann.redactFields().split(",")));
            Map<String, Object> snapshot = serializeEntity(entity, redact);
            String entityId = readField(entity, "id");
            String tenantId = readField(entity, "tenantId");
            String name = ann.value().isBlank() ? entity.getClass().getSimpleName() : ann.value();
            AuditContext.Snapshot s = AuditContext.get();

            AuditLog logEntry = AuditLog.builder()
                    .tenantId(tenantId).entityName(name).entityId(entityId).action(action)
                    .actorId(s == null ? null : s.actorId())
                    .actorEmail(s == null ? null : s.actorEmail())
                    .ipAddress(s == null ? null : s.ipAddress())
                    .userAgent(s == null ? null : s.userAgent())
                    .requestId(s == null ? null : s.requestId())
                    .afterValue(snapshot)
                    .createdAt(OffsetDateTime.now())
                    .build();

            // Persisting an AuditLog here (mid-flush, inside @PostPersist/@PostUpdate) would mutate
            // Hibernate's in-flight ActionQueue and throw ConcurrentModificationException. Defer the
            // write to AFTER the business transaction commits, in its own transaction.
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        try {
                            CTX.getBean(AuditLogWriter.class).write(logEntry);
                        } catch (Exception ex) {
                            log.warn("Deferred audit write failed for {}: {}", name, ex.getMessage());
                        }
                    }
                });
            } else {
                CTX.getBean(AuditLogWriter.class).write(logEntry);
            }
        } catch (Exception e) {
            log.warn("Audit log write failed for {}: {}", entity.getClass().getSimpleName(), e.getMessage());
        }
    }

    private String readField(Object entity, String field) {
        try {
            Field f = findField(entity.getClass(), field);
            if (f == null) return null;
            f.setAccessible(true);
            Object v = f.get(entity);
            return v == null ? null : v.toString();
        } catch (Exception e) { return null; }
    }

    private Field findField(Class<?> c, String name) {
        while (c != null && c != Object.class) {
            try { return c.getDeclaredField(name); } catch (NoSuchFieldException e) { c = c.getSuperclass(); }
        }
        return null;
    }

    private Map<String, Object> serializeEntity(Object e, Set<String> redact) {
        try {
            Map<String, Object> raw = MAPPER.convertValue(e, Map.class);
            raw.replaceAll((k, v) -> redact.contains(k) ? "***" : v);
            return raw;
        } catch (Exception ex) {
            return Map.of("_error", "serialization failed: " + ex.getMessage());
        }
    }
}
