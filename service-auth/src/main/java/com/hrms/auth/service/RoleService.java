package com.hrms.auth.service;

import com.hrms.auth.dto.RoleDto;
import com.hrms.auth.entity.*;
import com.hrms.auth.repository.*;
import com.hrms.common.exception.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserRoleRepository userRoleRepository;

    // ── CRUD ─────────────────────────────────────────────────────────────────────

    @Transactional
    public RoleDto.RoleResponse createRole(String tenantId, RoleDto.CreateRoleRequest req) {
        if (roleRepository.findByCodeAndTenantId(req.getCode(), tenantId).isPresent()) {
            throw new DuplicateResourceException("Role", "code", req.getCode());
        }

        Role role = roleRepository.save(Role.builder()
                .tenantId(tenantId)
                .name(req.getName())
                .code(req.getCode())
                .description(req.getDescription())
                .systemRole(false)
                .build());

        if (req.getPermissionIds() != null && !req.getPermissionIds().isEmpty()) {
            persistRolePermissions(role.getId(), req.getPermissionIds());
        }

        log.info("Role created: {} for tenant {}", role.getCode(), tenantId);
        return toRoleResponse(role);
    }

    @Transactional
    public RoleDto.RoleResponse updateRole(String tenantId, UUID roleId, RoleDto.UpdateRoleRequest req) {
        Role role = requireRoleInTenant(tenantId, roleId);

        if (req.getName() != null) role.setName(req.getName());
        if (req.getDescription() != null) role.setDescription(req.getDescription());
        roleRepository.save(role);

        if (req.getPermissionIds() != null) {
            rolePermissionRepository.deleteByRoleId(roleId);
            persistRolePermissions(roleId, req.getPermissionIds());
        }

        return toRoleResponse(role);
    }

    public List<RoleDto.RoleResponse> listRoles(String tenantId) {
        return roleRepository.findByTenantIdAndActiveTrue(tenantId).stream()
                .map(this::toRoleResponse)
                .toList();
    }

    public RoleDto.RoleResponse getRole(String tenantId, UUID roleId) {
        return toRoleResponse(requireRoleInTenant(tenantId, roleId));
    }

    @Transactional
    public void deleteRole(String tenantId, UUID roleId) {
        Role role = requireRoleInTenant(tenantId, roleId);
        if (role.isSystemRole()) {
            throw new BusinessException("SYSTEM_ROLE", "System roles cannot be deleted");
        }
        role.setActive(false);
        roleRepository.save(role);
        log.info("Role soft-deleted: {} in tenant {}", role.getCode(), tenantId);
    }

    // ── User-Role assignment ──────────────────────────────────────────────────────

    @Transactional
    public void assignRoleToUser(String tenantId, RoleDto.AssignRoleRequest req) {
        requireRoleInTenant(tenantId, req.getRoleId());     // validates ownership

        userRoleRepository.save(UserRole.builder()
                .userId(req.getUserId())
                .roleId(req.getRoleId())
                .assignedBy(currentUserId())
                .build());

        log.info("Role {} assigned to user {} by {}",
                req.getRoleId(), req.getUserId(), currentUserId());
    }

    @Transactional
    public void removeRoleFromUser(String tenantId, UUID userId, UUID roleId) {
        requireRoleInTenant(tenantId, roleId);              // validates ownership
        userRoleRepository.deleteByUserIdAndRoleId(userId, roleId);
    }

    public List<RoleDto.RoleResponse> getUserRoles(UUID userId) {
        return userRoleRepository.findByUserId(userId).stream()
                .map(ur -> roleRepository.findById(ur.getRoleId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(this::toRoleResponse)
                .toList();
    }

    // ── Seeding ───────────────────────────────────────────────────────────────────

    /**
     * Idempotently creates the six system roles with their default permission sets.
     * Safe to call on every tenant provisioning event.
     */
    @Transactional
    public void initDefaultRoles(String tenantId) {
        createSystemRoleIfAbsent(tenantId, "SUPER_ADMIN", "Super Administrator",
                List.of()); // all-access enforced at security layer, no per-row grants needed

        createSystemRoleIfAbsent(tenantId, "HR_ADMIN", "HR Administrator", List.of(
                "USERS:READ", "USERS:WRITE",
                "EMPLOYEES:READ", "EMPLOYEES:WRITE", "EMPLOYEES:DELETE",
                "LEAVE:READ", "LEAVE:WRITE", "LEAVE:APPROVE",
                "PAYROLL:READ", "ROLES:READ"
        ));

        createSystemRoleIfAbsent(tenantId, "MANAGER", "Manager", List.of(
                "EMPLOYEES:READ",
                "LEAVE:READ", "LEAVE:APPROVE",
                "REPORTS:READ"
        ));

        createSystemRoleIfAbsent(tenantId, "EMPLOYEE", "Employee", List.of(
                "LEAVE:READ", "LEAVE:WRITE",
                "PROFILE:READ", "PROFILE:WRITE"
        ));

        createSystemRoleIfAbsent(tenantId, "RECRUITER", "Recruiter", List.of(
                "RECRUITMENT:READ", "RECRUITMENT:WRITE", "RECRUITMENT:DELETE"
        ));

        createSystemRoleIfAbsent(tenantId, "PAYROLL_ADMIN", "Payroll Administrator", List.of(
                "PAYROLL:READ", "PAYROLL:WRITE", "PAYROLL:DELETE",
                "EMPLOYEES:READ"
        ));

        log.info("Default roles initialised for tenant {}", tenantId);
    }

    // ── Private helpers ───────────────────────────────────────────────────────────

    private void createSystemRoleIfAbsent(String tenantId, String code,
                                          String name, List<String> permSpecs) {
        Role role = roleRepository.findByCodeAndTenantId(code, tenantId)
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .tenantId(tenantId)
                        .name(name)
                        .code(code)
                        .description("System role – " + name)
                        .systemRole(true)
                        .build()));

        // Only seed permissions when none have been assigned yet (idempotent)
        if (rolePermissionRepository.findByRoleId(role.getId()).isEmpty()) {
            for (String spec : permSpecs) {
                String[] parts = spec.split(":", 2);
                Permission perm = findOrCreatePermission(parts[0], parts[1]);
                rolePermissionRepository.save(RolePermission.builder()
                        .roleId(role.getId())
                        .permissionId(perm.getId())
                        .build());
            }
        }
    }

    private Permission findOrCreatePermission(String module, String action) {
        return permissionRepository.findByModuleAndAction(module, action)
                .orElseGet(() -> permissionRepository.save(Permission.builder()
                        .module(module)
                        .action(action)
                        .build()));
    }

    private void persistRolePermissions(UUID roleId, Set<UUID> permissionIds) {
        permissionIds.forEach(permId ->
                rolePermissionRepository.save(RolePermission.builder()
                        .roleId(roleId)
                        .permissionId(permId)
                        .build()));
    }

    private Role requireRoleInTenant(String tenantId, UUID roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));
        if (!role.getTenantId().equals(tenantId)) {
            // Surface as not-found to avoid leaking cross-tenant existence
            throw new ResourceNotFoundException("Role", "id", roleId);
        }
        return role;
    }

    private RoleDto.RoleResponse toRoleResponse(Role role) {
        List<RoleDto.PermissionResponse> perms = rolePermissionRepository
                .findByRoleId(role.getId()).stream()
                .map(rp -> permissionRepository.findById(rp.getPermissionId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(p -> RoleDto.PermissionResponse.builder()
                        .id(p.getId())
                        .module(p.getModule())
                        .action(p.getAction())
                        .scope(p.getScope())
                        .description(p.getDescription())
                        .build())
                .toList();

        return RoleDto.RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .code(role.getCode())
                .description(role.getDescription())
                .systemRole(role.isSystemRole())
                .active(role.isActive())
                .permissions(perms)
                .build();
    }

    private String currentUserId() {
        try {
            Object principal = SecurityContextHolder.getContext()
                    .getAuthentication().getPrincipal();
            if (principal instanceof com.hrms.security.model.UserPrincipal up) {
                return up.getId();
            }
        } catch (Exception ignored) {
        }
        return "SYSTEM";
    }
}
