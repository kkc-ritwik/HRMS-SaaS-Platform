package com.hrms.recruitment.dto;

import com.hrms.recruitment.entity.JobRequisition;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class JobRequisitionDto {

    @Getter @Setter
    public static class CreateRequest {
        @NotBlank  private String  title;
        private UUID               departmentId;
        private String             location;
        @NotNull   private JobRequisition.EmploymentType employmentType;
        @Min(1)    private int     positionsCount = 1;
        private String             description;
        private String             requirements;
        private BigDecimal         salaryMin;
        private BigDecimal         salaryMax;
        @NotNull   private JobRequisition.Priority priority;
        private LocalDate          targetDate;
        private String             source;
        private UUID               agencyId;
    }

    @Getter @Setter
    public static class UpdateRequest {
        private String             title;
        private UUID               departmentId;
        private String             location;
        private JobRequisition.EmploymentType employmentType;
        private Integer            positionsCount;
        private String             description;
        private String             requirements;
        private BigDecimal         salaryMin;
        private BigDecimal         salaryMax;
        private JobRequisition.Priority priority;
        private LocalDate          targetDate;
        private String             source;
        private UUID               agencyId;
    }

    @Getter @Setter @Builder
    public static class Response {
        private UUID                          id;
        private String                        title;
        private UUID                          departmentId;
        private String                        location;
        private JobRequisition.EmploymentType employmentType;
        private int                           positionsCount;
        private int                           positionsFilled;
        private int                           openPositions;
        private String                        description;
        private String                        requirements;
        private BigDecimal                    salaryMin;
        private BigDecimal                    salaryMax;
        private JobRequisition.RequisitionStatus status;
        private JobRequisition.Priority       priority;
        private LocalDate                     targetDate;
        private String                        source;
        private UUID                          agencyId;
        private String                        requestedBy;
        private String                        approvedBy;
        private Instant                       approvedAt;
        private Instant                       createdAt;
        private Instant                       updatedAt;
    }
}
