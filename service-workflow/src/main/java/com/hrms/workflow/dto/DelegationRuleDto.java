package com.hrms.workflow.dto;

import com.hrms.workflow.entity.DelegationRule.DelegationScope;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class DelegationRuleDto {

    @Getter
    @Setter
    public static class CreateRequest {

        @NotNull
        private UUID delegatorId;

        @NotNull
        private UUID delegateId;

        private DelegationScope scope;

        private List<String> entityTypes;

        @NotNull
        private LocalDate startDate;

        @NotNull
        private LocalDate endDate;

        private String reason;

        private boolean active = true;
    }

    @Getter
    @Setter
    public static class UpdateRequest {

        private UUID delegatorId;

        private UUID delegateId;

        private DelegationScope scope;

        private List<String> entityTypes;

        private LocalDate startDate;

        private LocalDate endDate;

        private String reason;

        private Boolean active;
    }

    @Getter
    @Setter
    @Builder
    public static class Response {

        private UUID id;
        private String tenantId;
        private UUID delegatorId;
        private UUID delegateId;
        private DelegationScope scope;
        private List<String> entityTypes;
        private LocalDate startDate;
        private LocalDate endDate;
        private boolean active;
        private String reason;
        private String createdBy;
        private String updatedBy;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
