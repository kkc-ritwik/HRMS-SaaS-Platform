package com.hrms.auth.service;

import com.hrms.auth.dto.AuthDto;
import com.hrms.auth.entity.*;
import com.hrms.auth.repository.*;
import com.hrms.common.exception.*;
import com.hrms.security.model.UserPrincipal;
import com.hrms.security.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 30;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserRoleRepository userRoleRepository;
    private final SessionRepository sessionRepository;
    private final LoginHistoryRepository loginHistoryRepository;
    private final PasswordHistoryRepository passwordHistoryRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.access-token-expiry:3600000}")
    private long accessTokenExpiry;

    @Value("${jwt.refresh-token-expiry:604800000}")
    private long refreshTokenExpiry;

    // ── Public API ────────────────────────────────────────────────────────────────

    @Transactional
    public AuthDto.LoginResponse signup(AuthDto.SignupRequest req) {
        if (userRepository.existsByEmailAndTenantId(req.getEmail(), req.getTenantId())) {
            throw new DuplicateResourceException("User", "email", req.getEmail());
        }

        User user = new User();
        user.setTenantId(req.getTenantId());
        user.setEmail(req.getEmail());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setFullName(req.getFullName());
        user.setStatus(User.UserStatus.PENDING_VERIFICATION);
        user.setPasswordChangedAt(Instant.now());
        userRepository.save(user);

        // Assign default EMPLOYEE role, creating it for this tenant if absent
        Role employeeRole = roleRepository.findByCodeAndTenantId("EMPLOYEE", req.getTenantId())
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .tenantId(req.getTenantId())
                        .name("Employee")
                        .code("EMPLOYEE")
                        .description("Default employee role")
                        .systemRole(true)
                        .build()));

        userRoleRepository.save(UserRole.builder()
                .userId(user.getId())
                .roleId(employeeRole.getId())
                .assignedBy("SYSTEM")
                .build());

        passwordHistoryRepository.save(PasswordHistory.builder()
                .userId(user.getId())
                .passwordHash(user.getPasswordHash())
                .build());

        log.info("New user registered: {} in tenant {}", req.getEmail(), req.getTenantId());
        return buildLoginResponse(user, null, null);
    }

    @Transactional
    public AuthDto.LoginResponse login(AuthDto.LoginRequest req, String ipAddress, String userAgent) {
        User user = userRepository.findByEmailAndTenantIdAndDeletedFalse(req.getEmail(), req.getTenantId())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (user.isLocked()) {
            recordLoginHistory(user, req.getTenantId(), ipAddress, userAgent,
                    LoginHistory.LoginStatus.FAILED, "Account locked");
            throw new UnauthorizedException("Account is locked. Please try again later.");
        }

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            handleFailedLogin(user, req.getTenantId(), ipAddress, userAgent);
            throw new UnauthorizedException("Invalid email or password");
        }

        // Successful login — reset counters
        user.setFailedLoginCount(0);
        user.setLockedUntil(null);
        if (user.getStatus() == User.UserStatus.LOCKED) {
            user.setStatus(User.UserStatus.ACTIVE);
        }
        user.setLastLoginAt(Instant.now());
        user.setLastLoginIp(ipAddress);
        userRepository.save(user);

        recordLoginHistory(user, req.getTenantId(), ipAddress, userAgent,
                LoginHistory.LoginStatus.SUCCESS, null);

        log.info("Successful login: {} from {}", req.getEmail(), ipAddress);
        return buildLoginResponse(user, ipAddress, userAgent);
    }

    @Transactional
    public AuthDto.LoginResponse refreshToken(String rawRefreshToken) {
        String tokenHash = sha256(rawRefreshToken);

        Session session = sessionRepository.findByRefreshTokenHashAndActiveTrue(tokenHash)
                .orElseThrow(() -> new UnauthorizedException("Invalid or expired refresh token"));

        if (session.getExpiresAt().isBefore(Instant.now())) {
            session.setActive(false);
            sessionRepository.save(session);
            throw new UnauthorizedException("Refresh token has expired");
        }

        User user = userRepository.findById(session.getUserId())
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        UserPrincipal principal = buildUserPrincipal(user);
        String newAccessToken = jwtService.generateAccessToken(principal);

        return AuthDto.LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(rawRefreshToken)          // client keeps the same refresh token
                .expiresIn(accessTokenExpiry / 1000)
                .user(buildUserInfo(user, principal.getRoles(), principal.getPermissions()))
                .build();
    }

    @Transactional
    public void logout(UUID userId, String refreshTokenHash) {
        sessionRepository.findByRefreshTokenHashAndActiveTrue(refreshTokenHash).ifPresent(session -> {
            if (session.getUserId().equals(userId)) {
                session.setActive(false);
                sessionRepository.save(session);
            }
        });
    }

    @Transactional
    public void logoutAll(UUID userId) {
        List<Session> sessions = sessionRepository.findByUserIdAndActiveTrue(userId);
        sessions.forEach(s -> s.setActive(false));
        sessionRepository.saveAll(sessions);
        log.info("All sessions invalidated for user {}", userId);
    }

    /**
     * Issues tokens for a user whose identity was already verified (e.g. via OTP).
     * Skips password validation — only call after a successful OTP check.
     */
    @Transactional
    public AuthDto.LoginResponse generateTokensForVerifiedUser(String email, String tenantId,
                                                               String ipAddress, String userAgent) {
        User user = userRepository.findByEmailAndTenantIdAndDeletedFalse(email, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return buildLoginResponse(user, ipAddress, userAgent);
    }

    @Transactional
    public void changePassword(UUID userId, AuthDto.ChangePasswordRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPasswordHash())) {
            throw new BusinessException("INVALID_PASSWORD", "Current password is incorrect");
        }

        List<PasswordHistory> history = passwordHistoryRepository.findTop5ByUserIdOrderByCreatedAtDesc(userId);
        boolean reused = history.stream()
                .anyMatch(h -> passwordEncoder.matches(req.getNewPassword(), h.getPasswordHash()));
        if (reused) {
            throw new BusinessException("PASSWORD_REUSED", "New password must not match any of the last 5 passwords");
        }

        String newHash = passwordEncoder.encode(req.getNewPassword());
        user.setPasswordHash(newHash);
        user.setPasswordChangedAt(Instant.now());
        userRepository.save(user);

        passwordHistoryRepository.save(PasswordHistory.builder()
                .userId(userId)
                .passwordHash(newHash)
                .build());

        logoutAll(userId);
        log.info("Password changed for user {}", userId);
    }

    // ── Package-visible helpers (used by RoleService etc.) ────────────────────────

    UserPrincipal buildUserPrincipal(User user) {
        List<UserRole> userRoles = userRoleRepository.findByUserId(user.getId());
        Set<String> roleCodes = new LinkedHashSet<>();
        Set<String> permissions = new LinkedHashSet<>();

        for (UserRole ur : userRoles) {
            roleRepository.findById(ur.getRoleId())
                    .filter(Role::isActive)
                    .ifPresent(role -> {
                        roleCodes.add(role.getCode());
                        rolePermissionRepository.findByRoleId(role.getId()).forEach(rp ->
                                permissionRepository.findById(rp.getPermissionId()).ifPresent(p ->
                                        permissions.add(p.getModule() + ":" + p.getAction())
                                )
                        );
                    });
        }

        return UserPrincipal.builder()
                .id(user.getId().toString())
                .tenantId(user.getTenantId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .employeeId(user.getEmployeeId())
                .roles(roleCodes)
                .permissions(permissions)
                .build();
    }

    // ── Private helpers ───────────────────────────────────────────────────────────

    private AuthDto.LoginResponse buildLoginResponse(User user, String ipAddress, String userAgent) {
        UserPrincipal principal = buildUserPrincipal(user);

        String rawRefreshToken = jwtService.generateRefreshToken(user.getId().toString());
        String refreshTokenHash = sha256(rawRefreshToken);

        sessionRepository.save(Session.builder()
                .userId(user.getId())
                .refreshTokenHash(refreshTokenHash)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .expiresAt(Instant.now().plusMillis(refreshTokenExpiry))
                .build());

        return AuthDto.LoginResponse.builder()
                .accessToken(jwtService.generateAccessToken(principal))
                .refreshToken(rawRefreshToken)
                .expiresIn(accessTokenExpiry / 1000)
                .user(buildUserInfo(user, principal.getRoles(), principal.getPermissions()))
                .build();
    }

    private AuthDto.UserInfo buildUserInfo(User user, Set<String> roles, Set<String> permissions) {
        return AuthDto.UserInfo.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .roles(roles)
                .permissions(permissions)
                .build();
    }

    private void handleFailedLogin(User user, String tenantId,
                                   String ipAddress, String userAgent) {
        int failedCount = user.getFailedLoginCount() + 1;
        user.setFailedLoginCount(failedCount);

        if (failedCount >= MAX_FAILED_ATTEMPTS) {
            user.setStatus(User.UserStatus.LOCKED);
            user.setLockedUntil(Instant.now().plus(LOCK_DURATION_MINUTES, ChronoUnit.MINUTES));
            log.warn("User {} locked after {} failed login attempts", user.getEmail(), failedCount);
        }
        userRepository.save(user);

        recordLoginHistory(user, tenantId, ipAddress, userAgent,
                LoginHistory.LoginStatus.FAILED, "Invalid password");
    }

    private void recordLoginHistory(User user, String tenantId, String ipAddress,
                                    String userAgent, LoginHistory.LoginStatus status,
                                    String failureReason) {
        loginHistoryRepository.save(LoginHistory.builder()
                .tenantId(tenantId)
                .userId(user.getId())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .status(status)
                .failureReason(failureReason)
                .build());
    }

    private String sha256(String input) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
