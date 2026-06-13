package com.hrms.auth.controller;

import com.hrms.auth.dto.AuthDto;
import com.hrms.auth.entity.User;
import com.hrms.auth.repository.UserRepository;
import com.hrms.common.dto.ApiResponse;
import com.hrms.common.dto.PaginationMeta;
import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.security.model.TenantContext;
import com.hrms.security.model.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User profile and administration")
public class UserController {

    private final UserRepository userRepository;

    // ── Current user ──────────────────────────────────────────────────────────────

    @GetMapping("/me")
    @Operation(summary = "Return the authenticated user's profile from JWT claims")
    public ResponseEntity<ApiResponse<AuthDto.UserInfo>> me() {
        UserPrincipal principal = currentUser();

        AuthDto.UserInfo info = AuthDto.UserInfo.builder()
                .id(UUID.fromString(principal.getId()))
                .email(principal.getEmail())
                .fullName(principal.getFullName())
                .roles(principal.getRoles())
                .permissions(principal.getPermissions())
                .build();

        return ResponseEntity.ok(ApiResponse.ok(info));
    }

    // ── Admin: list & fetch ───────────────────────────────────────────────────────

    @GetMapping
    @PreAuthorize("hasAnyRole('HR_ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "List all users for the current tenant (paginated)")
    public ResponseEntity<ApiResponse<List<AuthDto.UserInfo>>> listUsers(
            @PageableDefault(size = 20) Pageable pageable) {

        String tenantId = TenantContext.get();
        Page<User> page = userRepository.findByTenantIdAndDeletedFalse(tenantId, pageable);

        List<AuthDto.UserInfo> users = page.getContent().stream()
                .map(this::toUserInfo)
                .toList();

        PaginationMeta meta = PaginationMeta.builder()
                .page(page.getNumber())
                .size(page.getSize())
                .total(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();

        return ResponseEntity.ok(ApiResponse.ok(users, meta));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a specific user by ID (must belong to the same tenant)")
    public ResponseEntity<ApiResponse<AuthDto.UserInfo>> getUser(@PathVariable UUID id) {
        User user = userRepository.findByIdAndTenantIdAndDeletedFalse(id, TenantContext.get())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        return ResponseEntity.ok(ApiResponse.ok(toUserInfo(user)));
    }

    // ── Admin: status management ──────────────────────────────────────────────────

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('HR_ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Activate or deactivate a user account")
    public ResponseEntity<ApiResponse<String>> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody StatusUpdateRequest req) {

        User user = userRepository.findByIdAndTenantIdAndDeletedFalse(id, TenantContext.get())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        User.UserStatus newStatus = User.UserStatus.valueOf(req.getStatus().toUpperCase());
        user.setStatus(newStatus);
        userRepository.save(user);

        log.info("User {} status changed to {} by admin", id, newStatus);
        return ResponseEntity.ok(ApiResponse.ok("User status updated to " + newStatus));
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('HR_ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Deactivate a user account")
    public ResponseEntity<ApiResponse<String>> deactivate(@PathVariable UUID id) {
        User user = userRepository.findByIdAndTenantIdAndDeletedFalse(id, TenantContext.get())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        user.setStatus(User.UserStatus.INACTIVE);
        userRepository.save(user);
        log.info("User {} deactivated by admin", id);
        return ResponseEntity.ok(ApiResponse.ok("User deactivated"));
    }

    // ── Inner DTO ─────────────────────────────────────────────────────────────────

    @Data
    @NoArgsConstructor
    public static class StatusUpdateRequest {
        @NotBlank(message = "Status must not be blank")
        private String status;
    }

    // ── Private helpers ───────────────────────────────────────────────────────────

    private AuthDto.UserInfo toUserInfo(User user) {
        return AuthDto.UserInfo.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }

    private UserPrincipal currentUser() {
        return (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
    }
}
