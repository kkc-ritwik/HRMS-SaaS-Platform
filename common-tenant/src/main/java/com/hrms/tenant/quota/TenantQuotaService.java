package com.hrms.tenant.quota;

import com.hrms.security.model.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantQuotaService {

    public interface Repo extends JpaRepository<TenantQuota, UUID> {
        Optional<TenantQuota> findByTenantIdAndQuotaKey(String tenantId, String quotaKey);

        @Modifying
        @Query("UPDATE TenantQuota q SET q.currentUsage = q.currentUsage + :delta WHERE q.tenantId = :t AND q.quotaKey = :k")
        int bump(@Param("t") String tenant, @Param("k") String key, @Param("delta") long delta);
    }

    private final Repo repo;

    /** Throws if the action would exceed hard limit; returns the row otherwise. */
    @Transactional
    public TenantQuota checkAndIncrement(String quotaKey, long delta) {
        String tenant = TenantContext.get();
        TenantQuota q = repo.findByTenantIdAndQuotaKey(tenant, quotaKey)
                .orElseGet(() -> defaultsFor(tenant, quotaKey));
        if (q.getStatus() == TenantQuota.Status.SUSPENDED) {
            throw new QuotaException("TENANT_SUSPENDED", "Tenant is suspended");
        }
        long projected = (q.getCurrentUsage() == null ? 0 : q.getCurrentUsage()) + delta;
        if (q.getHardLimit() != null && projected > q.getHardLimit()) {
            q.setStatus(TenantQuota.Status.THROTTLED);
            repo.save(q);
            throw new QuotaException("QUOTA_EXCEEDED",
                    "Quota '" + quotaKey + "' exceeded: " + projected + " > " + q.getHardLimit());
        }
        q.setCurrentUsage(projected);
        if (q.getSoftLimit() != null && projected >= q.getSoftLimit()) {
            q.setStatus(TenantQuota.Status.WARNING);
        }
        return repo.save(q);
    }

    @Transactional
    public void suspend(String tenant, String reason) {
        log.warn("Suspending tenant {} - {}", tenant, reason);
        repo.findAll().stream()
                .filter(q -> tenant.equals(q.getTenantId()))
                .forEach(q -> { q.setStatus(TenantQuota.Status.SUSPENDED); repo.save(q); });
    }

    @Transactional
    public void unsuspend(String tenant) {
        repo.findAll().stream()
                .filter(q -> tenant.equals(q.getTenantId()))
                .forEach(q -> { q.setStatus(TenantQuota.Status.ACTIVE); repo.save(q); });
    }

    private TenantQuota defaultsFor(String tenant, String key) {
        TenantQuota q = new TenantQuota();
        q.setTenantId(tenant);
        q.setQuotaKey(key);
        switch (key) {
            case "max_employees"          -> { q.setSoftLimit(900L);  q.setHardLimit(1000L); }
            case "max_storage_bytes"      -> { q.setSoftLimit(90L * 1024 * 1024 * 1024); q.setHardLimit(100L * 1024 * 1024 * 1024); }
            case "max_api_requests_per_day" -> { q.setSoftLimit(180000L); q.setHardLimit(200000L); q.setResetInterval("DAY"); }
            case "max_payroll_runs_per_month" -> { q.setSoftLimit(8L); q.setHardLimit(12L); q.setResetInterval("MONTH"); }
            case "max_open_requisitions"  -> { q.setSoftLimit(45L);   q.setHardLimit(50L); }
            default                       -> { q.setSoftLimit(null);  q.setHardLimit(null); }
        }
        return repo.save(q);
    }

    public static class QuotaException extends RuntimeException {
        private final String code;
        public QuotaException(String code, String msg) { super(msg); this.code = code; }
        public String getCode() { return code; }
    }
}
