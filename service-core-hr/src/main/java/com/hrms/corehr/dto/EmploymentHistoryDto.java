package com.hrms.corehr.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class EmploymentHistoryDto {

    @Getter @Setter
    public static class CreateRequest {
        @NotBlank
        private String     companyName;
        private String     designation;
        private LocalDate  fromDate;         // maps to entity.startDate
        private LocalDate  toDate;           // maps to entity.endDate
        private String     reasonForLeaving;
        private BigDecimal ctc;
    }

    @Getter @Setter
    public static class Response {
        private UUID       id;
        private String     companyName;
        private String     designation;
        private LocalDate  fromDate;
        private LocalDate  toDate;
        private String     reasonForLeaving;
        private BigDecimal ctc;
    }
}
