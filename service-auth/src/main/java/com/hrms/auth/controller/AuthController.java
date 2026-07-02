package com.hrms.auth.controller;

import com.hrms.auth.dto.AuthDto;
import com.hrms.auth.dto.OtpDto;
import com.hrms.auth.service.AuthService;
import com.hrms.auth.service.OtpService;
import com.hrms.common.dto.ApiResponse;
import com.hrms.common.exception.UnauthorizedException;
import com.hrms.security.model.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Authentication, session management and OTP")
public class AuthController {

    private final AuthService authService;
    private final OtpService otpService;

    // ── Session management ────────────────────────────────────────────────────────

    @PostMapping("/login")
    @Operation(summary = "Authenticate with email + password")
    public ResponseEntity<ApiResponse<AuthDto.LoginResponse>> login(
            @Valid @RequestBody AuthDto.LoginRequest req,
            HttpServletRequest httpReq) {

        AuthDto.LoginResponse response = authService.login(
                req, extractIp(httpReq), httpReq.getHeader("User-Agent"));
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/signup")
    @Operation(summary = "Register a new user account")
    public ResponseEntity<ApiResponse<AuthDto.LoginResponse>> signup(
            @Valid @RequestBody AuthDto.SignupRequest req) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(authService.signup(req)));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Exchange a refresh token for a new access token")
    public ResponseEntity<ApiResponse<AuthDto.LoginResponse>> refresh(
            @Valid @RequestBody AuthDto.RefreshRequest req) {

        return ResponseEntity.ok(ApiResponse.ok(
                authService.refreshToken(req.getRefreshToken())));
    }

    @PostMapping("/logout")
    @Operation(summary = "Invalidate the current session")
    public ResponseEntity<Void> logout(@RequestBody AuthDto.RefreshRequest req) {
        UUID userId = UUID.fromString(currentUser().getId());
        // Hash the raw refresh token the client provides, then match against DB
        authService.logout(userId, sha256(req.getRefreshToken()));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout-all")
    @Operation(summary = "Invalidate all active sessions for the current user")
    public ResponseEntity<Void> logoutAll() {
        authService.logoutAll(UUID.fromString(currentUser().getId()));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change the authenticated user's password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @Valid @RequestBody AuthDto.ChangePasswordRequest req) {

        authService.changePassword(UUID.fromString(currentUser().getId()), req);
        return ResponseEntity.ok(ApiResponse.ok("Password changed successfully"));
    }

    @GetMapping("/me")
    @Operation(summary = "Get the currently authenticated user")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> me() {
        UserPrincipal u = currentUser();
        java.util.Map<String, Object> user = new java.util.LinkedHashMap<>();
        user.put("id", u.getId());
        user.put("email", u.getEmail());
        user.put("fullName", u.getFullName());
        user.put("employeeId", u.getEmployeeId());
        user.put("tenantId", u.getTenantId());
        user.put("roles", u.getRoles());
        user.put("permissions", u.getPermissions());
        return ResponseEntity.ok(ApiResponse.ok(user));
    }

    // ── OTP ───────────────────────────────────────────────────────────────────────

    @PostMapping("/otp/send")
    @Operation(summary = "Generate and send a one-time password")
    public ResponseEntity<ApiResponse<String>> sendOtp(
            @Valid @RequestBody OtpDto.SendOtpRequest req) {

        // generateOtp stores in Redis and calls sendOtpEmail internally
        otpService.generateOtp(req.getEmail(), req.getPurpose());
        return ResponseEntity.ok(ApiResponse.ok("OTP sent to " + req.getEmail()));
    }

    @PostMapping("/otp/verify")
    @Operation(summary = "Verify an OTP and receive JWT tokens on success")
    public ResponseEntity<ApiResponse<AuthDto.LoginResponse>> verifyOtp(
            @Valid @RequestBody OtpDto.VerifyOtpRequest req,
            HttpServletRequest httpReq) {

        boolean valid = otpService.verifyOtp(req.getEmail(), req.getOtp(), req.getPurpose());
        if (!valid) {
            throw new UnauthorizedException("Invalid or expired OTP");
        }

        AuthDto.LoginResponse response = authService.generateTokensForVerifiedUser(
                req.getEmail(), req.getTenantId(),
                extractIp(httpReq), httpReq.getHeader("User-Agent"));

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // ── Private helpers ───────────────────────────────────────────────────────────

    private UserPrincipal currentUser() {
        return (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
    }

    private String extractIp(HttpServletRequest req) {
        String forwarded = req.getHeader("X-Forwarded-For");
        return (forwarded != null && !forwarded.isBlank())
                ? forwarded.split(",")[0].trim()
                : req.getRemoteAddr();
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
