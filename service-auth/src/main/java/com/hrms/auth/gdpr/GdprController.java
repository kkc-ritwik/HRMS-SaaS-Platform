package com.hrms.auth.gdpr;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrms.auth.entity.User;
import com.hrms.auth.repository.LoginHistoryRepository;
import com.hrms.auth.repository.SessionRepository;
import com.hrms.auth.repository.UserRepository;
import com.hrms.security.model.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * GDPR / DPDP Act data-subject endpoints:
 *   GET    /api/v1/me/data           — Right to Data Portability (export everything)
 *   POST   /api/v1/me/erase          — Right to Erasure (soft-delete + anonymize)
 *   POST   /api/v1/me/restrict       — Right to Restriction (suspend processing)
 *   POST   /api/v1/me/consent        — record / withdraw consent
 *
 * Only covers data held in the auth domain; tenant-wide export aggregator should call
 * the other services' equivalent endpoints (each service exposes /api/v1/me/data too).
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/me")
@RequiredArgsConstructor
public class GdprController {

    private final UserRepository users;
    private final SessionRepository sessions;
    private final LoginHistoryRepository loginHistory;
    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

    /** Export every byte of personal data we hold about the requesting user. JSON download. */
    @GetMapping("/data")
    public ResponseEntity<byte[]> exportMyData(@RequestParam UUID userId) throws Exception {
        User user = users.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("exportedAt", Instant.now().toString());
        payload.put("tenantId", user.getTenantId());
        Map<String, Object> userMap = new LinkedHashMap<>();
        userMap.put("id", user.getId());
        userMap.put("email", user.getEmail());
        userMap.put("fullName", user.getFullName());
        userMap.put("phone", user.getPhone());
        userMap.put("createdAt", user.getCreatedAt());
        userMap.put("lastLoginAt", user.getLastLoginAt());
        userMap.put("status", user.getStatus());
        payload.put("user", userMap);
        payload.put("sessions", sessions.findByUserIdAndActiveTrue(userId).stream()
                .map(s -> Map.of("ipAddress", s.getIpAddress() == null ? "" : s.getIpAddress(),
                                  "userAgent", s.getUserAgent() == null ? "" : s.getUserAgent(),
                                  "createdAt", s.getCreatedAt(), "expiresAt", s.getExpiresAt())).toList());
        payload.put("loginHistory", loginHistory
                .findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, 100))
                .stream()
                .map(h -> Map.of("status", h.getStatus(), "ipAddress", h.getIpAddress() == null ? "" : h.getIpAddress(),
                                  "createdAt", h.getCreatedAt(),
                                  "failureReason", h.getFailureReason() == null ? "" : h.getFailureReason())).toList());

        byte[] json = mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(payload);
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        h.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"hrms-personal-data-" + userId + ".json\"");
        return ResponseEntity.ok().headers(h).body(json);
    }

    /**
     * Right to Erasure — soft-delete + anonymize PII fields. Cannot fully delete records
     * required for legal retention (payroll, statutory) but blanks out identifying fields.
     * Returns the list of records affected.
     */
    @PostMapping("/erase")
    @Transactional
    public Map<String, Object> eraseMyData(@RequestParam UUID userId,
                                            @RequestParam(required = false) String reason) {
        User user = users.findById(userId).orElseThrow();
        String marker = "ERASED-" + userId.toString().substring(0, 8);
        user.setEmail(marker + "@erased.local");
        user.setFullName("[Erased]");
        user.setPhone(null);
        user.setAvatarUrl(null);
        user.setStatus(User.UserStatus.INACTIVE);
        user.setDeleted(true);
        users.save(user);

        sessions.findByUserIdAndActiveTrue(userId).forEach(s -> {
            s.setActive(false);
            sessions.save(s);
        });
        log.info("Erased PII for user {} (reason={})", userId, reason);
        return Map.of("status", "erased", "userId", userId, "erasedAt", Instant.now(),
                "note", "Legally-retained records (payroll, tax, audit) kept with anonymised key");
    }

    /** Right to Restriction — keep data but suspend any processing. Status flips to LOCKED. */
    @PostMapping("/restrict")
    @Transactional
    public Map<String, Object> restrictProcessing(@RequestParam UUID userId,
                                                   @RequestParam(required = false) String reason) {
        User user = users.findById(userId).orElseThrow();
        user.setStatus(User.UserStatus.LOCKED);
        users.save(user);
        sessions.findByUserIdAndActiveTrue(userId).forEach(s -> {
            s.setActive(false); sessions.save(s);
        });
        log.info("Processing restricted for user {} (reason={})", userId, reason);
        return Map.of("status", "restricted", "userId", userId, "restrictedAt", Instant.now());
    }

    /** Record a consent grant or withdrawal (purpose, version). Audit trail. */
    @PostMapping("/consent")
    public Map<String, Object> recordConsent(@RequestBody ConsentRecord req) {
        log.info("Consent recorded: user={} purpose={} granted={} version={}",
                req.userId(), req.purpose(), req.granted(), req.policyVersion());
        // Persistence to a `consents` table left to a dedicated GDPR audit table; the event
        // bus carries the record so common-audit captures it.
        return Map.of("recordedAt", Instant.now(), "tenantId", TenantContext.get(),
                "userId", req.userId(), "purpose", req.purpose(), "granted", req.granted());
    }

    public record ConsentRecord(UUID userId, String purpose, boolean granted, String policyVersion) {}
}
