package com.hrms.corehr.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

public class FamilyMemberDto {

    @Getter @Setter
    public static class CreateRequest {
        @NotBlank
        private String    name;
        private String    relationship;
        private LocalDate dateOfBirth;
        private String    gender;
        private String    occupation;
        private Boolean   isDependent;
        private String    phone;
    }

    @Getter @Setter
    public static class Response {
        private UUID      id;
        private String    name;
        private String    relationship;
        private LocalDate dateOfBirth;
        private String    gender;
        private String    occupation;
        private Boolean   isDependent;
        private String    phone;
    }
}
