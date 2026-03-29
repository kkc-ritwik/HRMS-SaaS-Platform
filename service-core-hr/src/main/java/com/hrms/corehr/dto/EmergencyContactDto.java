package com.hrms.corehr.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

public class EmergencyContactDto {

    @Getter @Setter
    public static class CreateRequest {
        @NotBlank
        private String  name;
        private String  relationship;
        @NotBlank
        private String  phone;
        private String  email;
        private Boolean isPrimary;
    }

    @Getter @Setter
    public static class Response {
        private UUID    id;
        private String  name;
        private String  relationship;
        private String  phone;
        private String  email;
        private Boolean isPrimary;
    }
}
