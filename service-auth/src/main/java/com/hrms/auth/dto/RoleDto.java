package com.hrms.auth.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class RoleDto {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CreateRoleRequest {
        @NotBlank private String name;
        @NotBlank private String code;
        private String description;
        private Set<UUID> permissionIds;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class UpdateRoleRequest {
        private String name;
        private String description;
        private Set<UUID> permissionIds;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class RoleResponse {
        private UUID id;
        private String name;
        private String code;
        private String description;
        private boolean systemRole;
        private boolean active;
        private List<PermissionResponse> permissions;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class PermissionResponse {
        private UUID id;
        private String module;
        private String action;
        private String scope;
        private String description;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class AssignRoleRequest {
        private UUID userId;
        private UUID roleId;
    }
}
