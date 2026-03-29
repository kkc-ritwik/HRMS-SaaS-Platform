package com.hrms.leave.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class LeaveTypeDto {

    @Getter @Setter
    public static class CreateRequest {
        @NotBlank @Size(max = 150) private String name;
        @NotBlank @Size(max = 50)  private String code;
        private boolean paid = true;
        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$") private String color;
        private String description;
        private String appliesToGender;
        private String appliesToEmploymentType;
        private BigDecimal maxDaysPerYear;
        private BigDecimal requiresAttachmentAfterDays;
    }

    @Getter @Setter
    public static class UpdateRequest {
        @Size(max = 150) private String name;
        private Boolean paid;
        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$") private String color;
        private String description;
        private String appliesToGender;
        private String appliesToEmploymentType;
        private BigDecimal maxDaysPerYear;
        private BigDecimal requiresAttachmentAfterDays;
        private Boolean active;
    }

    @Getter @Setter @Builder
    public static class Response {
        private UUID id;
        private String tenantId;
        private String name;
        private String code;
        private boolean paid;
        private String color;
        private String description;
        private String appliesToGender;
        private String appliesToEmploymentType;
        private BigDecimal maxDaysPerYear;
        private BigDecimal requiresAttachmentAfterDays;
        private boolean active;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
