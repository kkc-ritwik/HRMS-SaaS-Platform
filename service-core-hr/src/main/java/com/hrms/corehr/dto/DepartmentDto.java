package com.hrms.corehr.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class DepartmentDto {

    @Getter @Setter
    public static class CreateRequest {
        @NotBlank @Size(max = 150) private String name;
        @NotBlank @Size(max = 50)  private String code;
        @Size(max = 2000)          private String description;
        private UUID parentId;
        private UUID managerId;
    }

    @Getter @Setter
    public static class UpdateRequest {
        @Size(max = 150) private String name;
        @Size(max = 2000) private String description;
        private UUID parentId;
        private UUID managerId;
        private Boolean active;
    }

    @Getter @Setter @Builder
    public static class Response {
        private UUID id;
        private String tenantId;
        private String name;
        private String code;
        private String description;
        private UUID parentId;
        private String parentName;
        private UUID managerId;
        private String managerName;
        private int headCount;
        private boolean active;
        private List<Response> children;
        private Instant createdAt;
        private Instant updatedAt;
    }

    @Getter @Setter @Builder
    public static class ListItem {
        private UUID id;
        private String name;
        private String code;
        private UUID parentId;
        private int headCount;
        private boolean active;
    }
}
