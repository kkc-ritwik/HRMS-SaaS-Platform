package com.hrms.corehr.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

public class LocationDto {

    @Getter @Setter
    public static class CreateRequest {
        @NotBlank @Size(max = 150) private String name;
        @NotBlank @Size(max = 50)  private String code;
        @Size(max = 255) private String addressLine1;
        @Size(max = 255) private String addressLine2;
        @Size(max = 100) private String city;
        @Size(max = 100) private String state;
        @Size(max = 100) private String country;
        @Size(max = 20)  private String postalCode;
        @Size(max = 30)  private String phone;
        @Size(max = 60)  private String timeZone;
    }

    @Getter @Setter
    public static class UpdateRequest {
        @Size(max = 150) private String name;
        @Size(max = 255) private String addressLine1;
        @Size(max = 255) private String addressLine2;
        @Size(max = 100) private String city;
        @Size(max = 100) private String state;
        @Size(max = 100) private String country;
        @Size(max = 20)  private String postalCode;
        @Size(max = 30)  private String phone;
        @Size(max = 60)  private String timeZone;
        private Boolean active;
    }

    @Getter @Setter @Builder
    public static class Response {
        private UUID id;
        private String tenantId;
        private String name;
        private String code;
        private String addressLine1;
        private String addressLine2;
        private String city;
        private String state;
        private String country;
        private String postalCode;
        private String phone;
        private String timeZone;
        private boolean active;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
