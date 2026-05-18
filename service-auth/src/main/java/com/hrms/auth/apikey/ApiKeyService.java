package com.hrms.auth.apikey;

import com.hrms.security.model.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApiKeyService {

    public interface Repo extends JpaRepository<ApiKey, UUID> {
        Optional<ApiKey> findByKeyHash(String keyHash);
        List<ApiKey> findByTenantIdAndActiveTrue(String tenantId);
    }

    private static final String PREFIX = "hrms_pk_";
    private final Repo repo;

    /** Issues a new API key. Returns the plaintext ONCE; the caller MUST show it to the
     *  user immediately because we only persist the hash. */
    @Transactional
    public IssuedKey issue(String name, UUID byUser, Set<String> scopes,
                           Set<String> allowedIps, Integer ttlDays) {
        String raw = PREFIX + randomToken(32);
        ApiKey k = new ApiKey();
        k.setTenantId(TenantContext.get());
        k.setName(name);
        k.setKeyPrefix(raw.substring(0, 12));
        k.setKeyHash(sha256(raw));
        k.setCreatedByUserId(byUser);
        k.setScopes(scopes == null ? Set.of() : scopes);
        k.setAllowedIps(allowedIps == null ? Set.of() : allowedIps);
        if (ttlDays != null && ttlDays > 0) k.setExpiresAt(Instant.now().plusSeconds(ttlDays * 86400L));
        k.setActive(true);
        ApiKey saved = repo.save(k);
        log.info("API key issued: {} (id={}) for tenant {}", k.getKeyPrefix() + "…", saved.getId(), saved.getTenantId());
        return new IssuedKey(saved.getId(), raw, saved.getKeyPrefix(), saved.getExpiresAt());
    }

    /** Look up + validate an incoming key. Updates last_used_at / use_count. */
    @Transactional
    public Optional<ApiKey> validate(String rawKey, String remoteIp) {
        if (rawKey == null || !rawKey.startsWith(PREFIX)) return Optional.empty();
        Optional<ApiKey> opt = repo.findByKeyHash(sha256(rawKey));
        if (opt.isEmpty()) return Optional.empty();
        ApiKey k = opt.get();
        if (!k.isActive() || k.getRevokedAt() != null) return Optional.empty();
        if (k.getExpiresAt() != null && k.getExpiresAt().isBefore(Instant.now())) {
            k.setActive(false); repo.save(k); return Optional.empty();
        }
        if (k.getAllowedIps() != null && !k.getAllowedIps().isEmpty()
                && remoteIp != null && !k.getAllowedIps().contains(remoteIp)) {
            log.warn("API key {} rejected — IP {} not in allowlist", k.getKeyPrefix(), remoteIp);
            return Optional.empty();
        }
        k.setLastUsedAt(Instant.now());
        k.setUseCount(k.getUseCount() + 1);
        repo.save(k);
        return Optional.of(k);
    }

    @Transactional
    public ApiKey revoke(UUID keyId) {
        ApiKey k = repo.findById(keyId).orElseThrow();
        k.setActive(false);
        k.setRevokedAt(Instant.now());
        return repo.save(k);
    }

    /** Rotate: issue a new key + revoke the old one. Caller stores the new plaintext. */
    @Transactional
    public IssuedKey rotate(UUID oldKeyId, UUID byUser) {
        ApiKey old = repo.findById(oldKeyId).orElseThrow();
        IssuedKey replacement = issue(old.getName() + " (rotated)", byUser,
                old.getScopes(), old.getAllowedIps(),
                old.getExpiresAt() == null ? null
                        : (int) ((old.getExpiresAt().getEpochSecond() - Instant.now().getEpochSecond()) / 86400));
        revoke(oldKeyId);
        return replacement;
    }

    public List<ApiKey> listActive() {
        return repo.findByTenantIdAndActiveTrue(TenantContext.get());
    }

    private String randomToken(int bytes) {
        byte[] b = new byte[bytes];
        new SecureRandom().nextBytes(b);
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }

    private String sha256(String input) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }

    public record IssuedKey(UUID id, String plaintextOnce, String prefix, Instant expiresAt) {}
}
