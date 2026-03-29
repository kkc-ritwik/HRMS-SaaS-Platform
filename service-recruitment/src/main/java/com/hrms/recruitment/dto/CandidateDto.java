package com.hrms.recruitment.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class CandidateDto {

    @Getter @Setter
    public static class CreateRequest {
        @NotBlank private String  firstName;
        @NotBlank private String  lastName;
        @NotBlank @Email private String email;
        private String            phone;
        private String            currentCompany;
        private String            currentTitle;
        private BigDecimal        totalExperienceYears;
        private String            resumeUrl;
        private String            linkedinUrl;
        private String            source;
        private UUID              agencyId;
        private UUID              referredBy;
        private List<String>      tags;
    }

    @Getter @Setter
    public static class UpdateRequest {
        private String            firstName;
        private String            lastName;
        private String            phone;
        private String            currentCompany;
        private String            currentTitle;
        private BigDecimal        totalExperienceYears;
        private String            resumeUrl;
        private String            linkedinUrl;
        private String            source;
        private UUID              agencyId;
        private UUID              referredBy;
        private List<String>      tags;
    }

    @Getter @Setter @Builder
    public static class Response {
        private UUID              id;
        private String            firstName;
        private String            lastName;
        private String            fullName;
        private String            email;
        private String            phone;
        private String            currentCompany;
        private String            currentTitle;
        private BigDecimal        totalExperienceYears;
        private String            resumeUrl;
        private String            linkedinUrl;
        private String            source;
        private UUID              agencyId;
        private UUID              referredBy;
        private List<String>      tags;
        private int               applicationCount;
        private Instant           createdAt;
        private Instant           updatedAt;
    }

    /** Lightweight summary used when embedding in application responses. */
    @Getter @Setter @Builder
    public static class Summary {
        private UUID   id;
        private String fullName;
        private String email;
        private String phone;
        private String currentTitle;
        private String currentCompany;
        private BigDecimal totalExperienceYears;
        private String resumeUrl;
    }
}
