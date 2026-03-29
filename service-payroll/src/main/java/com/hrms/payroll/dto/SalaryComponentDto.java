package com.hrms.payroll.dto;

import com.hrms.payroll.entity.SalaryComponent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class SalaryComponentDto {

    @Getter @Setter
    public static class CreateRequest {
        @NotBlank private String name;
        @NotBlank private String code;
        @NotNull  private SalaryComponent.ComponentType type;
        @NotNull  private SalaryComponent.CalculationType calculationType;

        /** Required when calculationType = PERCENTAGE. */
        private UUID   percentageOfComponentId;
        private BigDecimal percentageValue;

        /** Required when calculationType = FORMULA. */
        private String formulaExpression;

        private boolean taxable       = true;
        private boolean partOfCtc     = true;
        private boolean partOfGross   = true;
        private boolean proRata       = true;
        private int     displayOrder  = 0;
    }

    @Getter @Setter
    public static class UpdateRequest {
        private String  name;
        private SalaryComponent.ComponentType type;
        private SalaryComponent.CalculationType calculationType;
        private UUID    percentageOfComponentId;
        private BigDecimal percentageValue;
        private String  formulaExpression;
        private Boolean taxable;
        private Boolean partOfCtc;
        private Boolean partOfGross;
        private Boolean proRata;
        private Integer displayOrder;
        private Boolean active;
    }

    @Getter @Setter @Builder
    public static class Response {
        private UUID   id;
        private String name;
        private String code;
        private SalaryComponent.ComponentType     type;
        private SalaryComponent.CalculationType   calculationType;
        private UUID   percentageOfComponentId;
        private String percentageOfComponentName;
        private BigDecimal percentageValue;
        private String formulaExpression;
        private boolean taxable;
        private boolean partOfCtc;
        private boolean partOfGross;
        private boolean proRata;
        private int    displayOrder;
        private boolean active;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
