package com.hrms.performance.dto;

import com.hrms.performance.entity.Competency;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class CompetencyDto {

    @Getter @Setter
    public static class CreateRequest {
        @NotBlank private String name;
        private String           description;
        @NotNull  private Competency.CompetencyCategory category;
        /** One behavior description per proficiency level (up to 5 items). */
        private List<String>     behaviors;
    }

    @Getter @Setter
    public static class UpdateRequest {
        private String           name;
        private String           description;
        private Competency.CompetencyCategory category;
        private List<String>     behaviors;
        private Boolean          active;
    }

    @Getter @Setter @Builder
    public static class Response {
        private UUID                          id;
        private String                        name;
        private String                        description;
        private Competency.CompetencyCategory category;
        private List<String>                  behaviors;
        private boolean                       active;
        private Instant                       createdAt;
        private Instant                       updatedAt;
    }

    // ── Role mapping DTOs ─────────────────────────────────────────────────────

    @Getter @Setter
    public static class RoleMappingRequest {
        @NotNull private UUID competencyId;
        private UUID          roleId;
        private UUID          departmentId;
        @Min(1) @Max(5)
        private int           expectedLevel = 3;
        private BigDecimal    weightage = BigDecimal.valueOf(20);
    }

    @Getter @Setter @Builder
    public static class RoleMappingResponse {
        private UUID          id;
        private UUID          competencyId;
        private String        competencyName;
        private UUID          roleId;
        private UUID          departmentId;
        private int           expectedLevel;
        private BigDecimal    weightage;
        private Instant       createdAt;
    }
}
