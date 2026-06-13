package com.hrms.auth.controller;

import com.hrms.auth.entity.Session;
import com.hrms.auth.entity.User;
import com.hrms.auth.repository.SessionRepository;
import com.hrms.auth.repository.UserRepository;
import com.hrms.common.dto.ApiResponse;
import com.hrms.common.exception.UnauthorizedException;
import com.hrms.security.mfa.TotpService;
import com.hrms.security.model.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Multi-factor authentication (TOTP) enrolment + active-session management for the
 * currently authenticated user. The {@code mfa_enabled}/{@code mfa_secret} columns and
 * {@code sessions} table already exist — this exposes the self-service REST layer.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "MFA & Sessions", description = "TOTP enrolment and active session management")
public class MfaController {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final TotpService totpService;

    // ── MFA ───────────────────────────────────────────────────────────────────

    @GetMapping("/mfa/status")
    @Operation(summary = "Whether MFA is enabled for the current user")
    public ResponseEntity<ApiResponse<Map<String, Object>>> status() {
        User user = currentUser();
        return ResponseEntity.ok(ApiResponse.ok(Map.of("enabled", user.isMfaEnabled())));
    }

    @PostMapping("/mfa/enroll")
    @Operation(summary = "Start TOTP enrolment — returns a secret + QR code (not yet active)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> enroll() {
        User user = currentUser();
        String secret = totpService.newSecret();
        user.setMfaSecret(secret);
        user.setMfaEnabled(false);
        userRepository.save(user);
        String qr = totpService.qrPngDataUri(secret, "HRMS", user.getEmail());
        return ResponseEntity.ok(ApiResponse.ok(Map.of("secret", secret, "qrCode", qr)));
    }

    @PostMapping("/mfa/verify")
    @Operation(summary = "Confirm TOTP enrolment by verifying a code — activates MFA")
    public ResponseEntity<ApiResponse<Map<String, Object>>> verify(@RequestBody Map<String, String> body) {
        User user = currentUser();
        String code = body.get("code");
        if (user.getMfaSecret() == null || code == null || !totpService.verify(user.getMfaSecret(), code)) {
            throw new UnauthorizedException("Invalid MFA code");
        }
        user.setMfaEnabled(true);
        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("enabled", true)));
    }

    @PostMapping("/mfa/disable")
    @Operation(summary = "Disable MFA (requires a valid current code)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> disable(@RequestBody(required = false) Map<String, String> body) {
        User user = currentUser();
        String code = body == null ? null : body.get("code");
        if (user.isMfaEnabled() && user.getMfaSecret() != null
                && (code == null || !totpService.verify(user.getMfaSecret(), code))) {
            throw new UnauthorizedException("Invalid MFA code");
        }
        user.setMfaEnabled(false);
        user.setMfaSecret(null);
        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("enabled", false)));
    }

    // ── Sessions ──────────────────────────────────────────────────────────────

    @GetMapping("/sessions")
    @Operation(summary = "List the current user's active sessions")
    public ResponseEntity<ApiResponse<List<Session>>> sessions() {
        UUID userId = UUID.fromString(principal().getId());
        return ResponseEntity.ok(ApiResponse.ok(sessionRepository.findByUserIdAndActiveTrue(userId)));
    }

    @DeleteMapping("/sessions/{id}")
    @Operation(summary = "Revoke (sign out) one of the current user's sessions")
    public ResponseEntity<Void> revoke(@PathVariable UUID id) {
        UUID userId = UUID.fromString(principal().getId());
        sessionRepository.findById(id)
                .filter(s -> s.getUserId().equals(userId))
                .ifPresent(s -> { s.setActive(false); sessionRepository.save(s); });
        return ResponseEntity.noContent().build();
    }

    // ── helpers ─────────────────────────────────────────────────────────────────

    private UserPrincipal principal() {
        Object p = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (p instanceof UserPrincipal up) return up;
        throw new UnauthorizedException("Not authenticated");
    }

    private User currentUser() {
        UserPrincipal up = principal();
        return userRepository.findByIdAndTenantIdAndDeletedFalse(UUID.fromString(up.getId()), up.getTenantId())
                .orElseThrow(() -> new UnauthorizedException("User not found"));
    }
}
