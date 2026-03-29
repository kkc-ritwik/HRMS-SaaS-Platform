package com.hrms.payroll.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class SalaryStructureDto {

    @Getter @Setter
    public static class CreateRequest {
        @NotBlank private String  name;
        private String  description;
        private boolean defaultStructure = false;
    }

    @Getter @Setter
    public static class UpdateRequest {
        private String  name;
        private String  description;
        private Boolean defaultStructure;
        private Boolean active;
    }

    @Getter @Setter @Builder
    public static class Response {
        private UUID    id;
        private String  name;
        private String  description;
        private boolean defaultStructure;
        private boolean active;
        private List<ComponentEntry> components;
        private Instant createdAt;
        private Instant updatedAt;
    }

    /** A component entry within a salary structure (default amounts / percentages). */
    @Getter @Setter @Builder
    public static class ComponentEntry {
        private UUID       id;                // SalaryStructureComponent id
        private UUID       componentId;
        private String     componentName;
        private String     componentCode;
        private String     componentType;
        private BigDecimal defaultAmount;
        private BigDecimal defaultPercentage;
        private boolean    mandatory;
    }

    /** Request to add / update a component in a structure. */
    @Getter @Setter
    public static class ComponentAssignRequest {
        @NotBlank(message = "componentId is required")
        private String componentId;            // UUID as string for easy JSON binding
        private BigDecimal defaultAmount;
        private BigDecimal defaultPercentage;
        private boolean    mandatory = true;
    }
}
