package com.hrms.auth.controller;

import com.hrms.auth.dto.RoleDto;
import com.hrms.auth.service.RoleService;
import com.hrms.common.dto.ApiResponse;
import com.hrms.security.model.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@Tag(name = "Roles", description = "Role and permission management")
public class RoleController {

    private final RoleService roleService;

    // ── Read (accessible to HR admins and above) ──────────────────────────────────

    @GetMapping
    @Operation(summary = "List all active roles for the current tenant")
    public ResponseEntity<ApiResponse<List<RoleDto.RoleResponse>>> listRoles() {
        return ResponseEntity.ok(ApiResponse.ok(
                roleService.listRoles(TenantContext.get())));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single role by ID")
    public ResponseEntity<ApiResponse<RoleDto.RoleResponse>> getRole(
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                roleService.getRole(TenantContext.get(), id)));
    }

    @GetMapping("/users/{userId}")
    @Operation(summary = "Get all roles assigned to a user")
    public ResponseEntity<ApiResponse<List<RoleDto.RoleResponse>>> getUserRoles(
            @PathVariable UUID userId) {
        return ResponseEntity.ok(ApiResponse.ok(
                roleService.getUserRoles(userId)));
    }

    // ── Write (requires role-management authority) ────────────────────────────────

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN:MANAGE_USERS:ALL')")
    @Operation(summary = "Create a new role for the current tenant")
    public ResponseEntity<ApiResponse<RoleDto.RoleResponse>> createRole(
            @Valid @RequestBody RoleDto.CreateRoleRequest req) {

        RoleDto.RoleResponse created = roleService.createRole(TenantContext.get(), req);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN:MANAGE_USERS:ALL')")
    @Operation(summary = "Update an existing role")
    public ResponseEntity<ApiResponse<RoleDto.RoleResponse>> updateRole(
            @PathVariable UUID id,
            @Valid @RequestBody RoleDto.UpdateRoleRequest req) {

        return ResponseEntity.ok(ApiResponse.ok(
                roleService.updateRole(TenantContext.get(), id, req)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN:MANAGE_USERS:ALL')")
    @Operation(summary = "Soft-delete a non-system role")
    public ResponseEntity<Void> deleteRole(@PathVariable UUID id) {
        roleService.deleteRole(TenantContext.get(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/assign")
    @PreAuthorize("hasAuthority('ADMIN:MANAGE_USERS:ALL')")
    @Operation(summary = "Assign a role to a user")
    public ResponseEntity<ApiResponse<String>> assignRole(
            @Valid @RequestBody RoleDto.AssignRoleRequest req) {

        roleService.assignRoleToUser(TenantContext.get(), req);
        return ResponseEntity.ok(ApiResponse.ok("Role assigned successfully"));
    }

    @DeleteMapping("/users/{userId}/roles/{roleId}")
    @PreAuthorize("hasAuthority('ADMIN:MANAGE_USERS:ALL')")
    @Operation(summary = "Remove a role from a user")
    public ResponseEntity<Void> removeRole(
            @PathVariable UUID userId,
            @PathVariable UUID roleId) {

        roleService.removeRoleFromUser(TenantContext.get(), userId, roleId);
        return ResponseEntity.noContent().build();
    }
}
