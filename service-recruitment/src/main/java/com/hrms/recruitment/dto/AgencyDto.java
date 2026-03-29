package com.hrms.recruitment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class AgencyDto {

    @Getter @Setter
    public static class CreateRequest {
        @NotBlank private String  name;
        private String            contactPerson;
        private String            contactEmail;
        private String            contactPhone;
        private BigDecimal        commissionPercent;
    }

    @Getter @Setter
    public static class UpdateRequest {
        private String            name;
        private String            contactPerson;
        private String            contactEmail;
        private String            contactPhone;
        private BigDecimal        commissionPercent;
        private Boolean           active;
    }

    @Getter @Setter @Builder
    public static class Response {
        private UUID              id;
        private String            name;
        private String            contactPerson;
        private String            contactEmail;
        private String            contactPhone;
        private BigDecimal        commissionPercent;
        private boolean           active;
        private Instant           createdAt;
        private Instant           updatedAt;
    }
}
