package com.hrms.tenant.featureflag;

import com.hrms.security.model.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resolve a feature flag for the current tenant + optional principal.
 *
 *   if (flags.isEnabled("payroll.new-engine")) { ... }
 *
 * Per-tenant override beats global. If {@code rolloutPercent} is set, employees are
 * bucketed deterministically by hash so a single user gets a stable answer.
 *
 * In-memory cache TTL = 60s; restart or call {@link #invalidateAll()} to refresh after
 * editing flags. For multi-pod invalidation, emit an event on the {@code hrms.config}
 * topic and listen across replicas.
 */
@Service
@ConditionalOnProperty(name = "hrms.tenant.platform.enabled", havingValue = "true")
@RequiredArgsConstructor
public class FeatureFlagService {

    public interface Repo extends JpaRepository<FeatureFlag, UUID> {
        Optional<FeatureFlag> findByKey(String key);
    }

    private final Repo repo;
    private final Map<String, CachedFlag> cache = new ConcurrentHashMap<>();
    private static final long TTL_MS = 60_000;

    public boolean isEnabled(String key) {
        return isEnabled(key, null);
    }

    public boolean isEnabled(String key, String bucketKey) {
        FeatureFlag flag = load(key);
        if (flag == null) return false;
        String tenant = TenantContext.get();
        if (flag.getEnabledTenants() != null && tenant != null
                && flag.getEnabledTenants().containsKey(tenant)) {
            return Boolean.TRUE.equals(flag.getEnabledTenants().get(tenant));
        }
        if (!Boolean.TRUE.equals(flag.getEnabled())) return false;
        if (flag.getRolloutPercent() == null || flag.getRolloutPercent() >= 100) return true;
        if (bucketKey == null) bucketKey = tenant == null ? "anon" : tenant;
        int bucket = Math.abs(bucketKey.hashCode()) % 100;
        return bucket < flag.getRolloutPercent();
    }

    public Map<String, Object> variant(String key) {
        FeatureFlag flag = load(key);
        return flag == null ? Map.of() : (flag.getVariantPayload() == null ? Map.of() : flag.getVariantPayload());
    }

    public void invalidateAll() { cache.clear(); }
    public void invalidate(String key) { cache.remove(key); }

    private FeatureFlag load(String key) {
        CachedFlag cached = cache.get(key);
        long now = System.currentTimeMillis();
        if (cached != null && (now - cached.timestamp) < TTL_MS) return cached.flag;
        FeatureFlag fresh = repo.findByKey(key).orElse(null);
        cache.put(key, new CachedFlag(fresh, now));
        return fresh;
    }

    private record CachedFlag(FeatureFlag flag, long timestamp) {}
}
