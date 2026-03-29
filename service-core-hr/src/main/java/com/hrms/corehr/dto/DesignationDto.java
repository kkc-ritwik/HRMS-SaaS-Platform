package com.hrms.corehr.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

public class DesignationDto {

    @Getter @Setter
    public static class CreateRequest {
        @NotBlank @Size(max = 150) private String name;
        @NotBlank @Size(max = 50)  private String code;
        @Size(max = 2000)          private String description;
        @Size(max = 20)            private String grade;
        @Size(max = 20)            private String band;
    }

    @Getter @Setter
    public static class UpdateRequest {
        @Size(max = 150)  private String name;
        @Size(max = 2000) private String description;
        @Size(max = 20)   private String grade;
        @Size(max = 20)   private String band;
        private Boolean active;
    }

    @Getter @Setter @Builder
    public static class Response {
        private UUID id;
        private String tenantId;
        private String name;
        private String code;
        private String description;
        private String grade;
        private String band;
        private boolean active;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
