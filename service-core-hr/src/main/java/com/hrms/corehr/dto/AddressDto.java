package com.hrms.corehr.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

public class AddressDto {

    @Getter @Setter
    public static class CreateRequest {
        @NotBlank
        private String type;          // "CURRENT" or "PERMANENT"
        @NotBlank
        private String addressLine1;
        private String addressLine2;
        private String city;
        private String state;
        private String country;
        private String postalCode;
    }

    @Getter @Setter
    public static class Response {
        private UUID   id;
        private String type;
        private String addressLine1;
        private String addressLine2;
        private String city;
        private String state;
        private String country;
        private String postalCode;
    }
}
