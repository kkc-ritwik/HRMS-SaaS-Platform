package com.hrms.audit.service;

import com.hrms.audit.context.AuditContext;
import com.hrms.audit.entity.AuditAction;
import com.hrms.audit.entity.AuditLog;
import com.hrms.audit.hashchain.AuditHashChain;
import com.hrms.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository repo;
    private final ObjectProvider<AuditHashChain> hashChainProvider;

    /** Explicit log call from a service method (use when you need before/after diff). */
    public AuditLog log(String tenantId, String entityName, String entityId, AuditAction action,
                        Map<String, Object> before, Map<String, Object> after) {
        Map<String, Object> diff = diff(before, after);
        AuditContext.Snapshot s = AuditContext.get();
        AuditLog row = AuditLog.builder()
                .tenantId(tenantId).entityName(entityName).entityId(entityId).action(action)
                .actorId(s == null ? null : s.actorId())
                .actorEmail(s == null ? null : s.actorEmail())
                .ipAddress(s == null ? null : s.ipAddress())
                .userAgent(s == null ? null : s.userAgent())
                .requestId(s == null ? null : s.requestId())
                .beforeValue(before).afterValue(after).changedFields(diff)
                .createdAt(OffsetDateTime.now()).build();
        AuditHashChain chain = hashChainProvider.getIfAvailable();
        if (chain != null) chain.seal(row);
        return repo.save(row);
    }

    public AuditLog logEvent(String tenantId, String entityName, String entityId, AuditAction action) {
        return log(tenantId, entityName, entityId, action, null, null);
    }

    private Map<String, Object> diff(Map<String, Object> before, Map<String, Object> after) {
        if (before == null || after == null) return null;
        Map<String, Object> out = new HashMap<>();
        for (var e : after.entrySet()) {
            Object b = before.get(e.getKey());
            if (b == null && e.getValue() == null) continue;
            if (b == null || !b.equals(e.getValue())) {
                out.put(e.getKey(), Map.of("before", b, "after", e.getValue()));
            }
        }
        return out.isEmpty() ? null : out;
    }
}
