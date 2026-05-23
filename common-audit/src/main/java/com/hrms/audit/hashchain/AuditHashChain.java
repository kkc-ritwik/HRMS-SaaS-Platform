package com.hrms.audit.hashchain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrms.audit.entity.AuditLog;
import com.hrms.audit.repository.AuditLogRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

/**
 * Tamper-evident hash chain over the audit_logs table.
 *
 *   currentHash = sha256( prevHash || canonical(row[entityName, entityId, action, actorId,
 *                                                     ipAddress, requestId, beforeValue,
 *                                                     afterValue, createdAt]) )
 *
 * Each new row is committed with both prevHash (← previous row's currentHash for the same
 * tenant) and its own currentHash. A {@link #verify} sweep recomputes hashes and reports
 * any breaks — used by the scheduled {@link #scheduledVerify()} job + an admin endpoint.
 *
 * If tampering is detected, an alert is emitted via the structured log → AlertManager.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuditHashChain {

    private final AuditLogRepository repo;
    private final ObjectMapper mapper = new ObjectMapper();
    @PersistenceContext private EntityManager em;

    /** Compute the current hash for a row, given the chain's prev hash. */
    public String computeHash(AuditLog log, String prevHash) {
        try {
            Map<String, Object> canonical = new LinkedHashMap<>();
            canonical.put("entityName", log.getEntityName());
            canonical.put("entityId", log.getEntityId());
            canonical.put("action", log.getAction() == null ? null : log.getAction().name());
            canonical.put("actorId", log.getActorId());
            canonical.put("ipAddress", log.getIpAddress());
            canonical.put("requestId", log.getRequestId());
            canonical.put("beforeValue", log.getBeforeValue());
            canonical.put("afterValue", log.getAfterValue());
            canonical.put("createdAt", log.getCreatedAt() == null ? null : log.getCreatedAt().toString());

            String payload = (prevHash == null ? "GENESIS" : prevHash) + "|"
                    + mapper.writeValueAsString(canonical);
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(payload.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to compute audit hash", e);
        }
    }

    /** Set prev/current hash on a brand-new AuditLog row before persist. */
    @Transactional
    public void seal(AuditLog freshRow) {
        String tenant = freshRow.getTenantId() == null ? "_" : freshRow.getTenantId();
        Object lastHashRaw = em.createNativeQuery(
                "SELECT current_hash FROM audit_logs " +
                "WHERE COALESCE(tenant_id,'_') = :t AND current_hash IS NOT NULL " +
                "ORDER BY created_at DESC LIMIT 1")
                .setParameter("t", tenant)
                .getResultStream().findFirst().orElse(null);
        String prev = lastHashRaw == null ? null : lastHashRaw.toString();
        freshRow.setPrevHash(prev);
        freshRow.setCurrentHash(computeHash(freshRow, prev));
    }

    /**
     * Verify the full chain for one tenant. Returns list of broken row ids (empty = clean).
     * O(N) walk — meant to run during off-peak windows.
     */
    @Transactional(readOnly = true)
    public List<UUID> verify(String tenantId) {
        List<UUID> bad = new ArrayList<>();
        String prev = null;
        List<AuditLog> rows = repo.findByTenantIdOrderByCreatedAtAsc(tenantId);
        for (AuditLog r : rows) {
            String expected = computeHash(r, prev);
            if (!expected.equals(r.getCurrentHash()) || !Objects.equals(prev, r.getPrevHash())) {
                bad.add(r.getId());
                log.error("[AUDIT-TAMPER] tenant={} row={} expected={} actual={}",
                        tenantId, r.getId(), expected, r.getCurrentHash());
            }
            prev = r.getCurrentHash();
        }
        return bad;
    }

    private static String bytesToHex(byte[] in) {
        StringBuilder sb = new StringBuilder(in.length * 2);
        for (byte b : in) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
