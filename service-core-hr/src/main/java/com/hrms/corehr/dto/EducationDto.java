package com.hrms.corehr.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

public class EducationDto {

    @Getter @Setter
    public static class CreateRequest {
        @NotBlank
        private String     institution;
        private String     degree;
        private String     specialization;   // maps to entity.fieldOfStudy
        private Integer    yearOfPassing;
        private BigDecimal percentage;
        private String     documentUrl;
    }

    @Getter @Setter
    public static class Response {
        private UUID       id;
        private String     institution;
        private String     degree;
        private String     specialization;
        private Integer    yearOfPassing;
        private BigDecimal percentage;
        private String     documentUrl;
    }
}
