package com.hrms.auth.dto;

import jakarta.validation.constraints.*;
import lombok.*;

public class OtpDto {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class SendOtpRequest {
        @NotBlank @Email private String email;
        private String tenantId;
        private OtpPurpose purpose;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class VerifyOtpRequest {
        private String email;
        @Size(min = 6, max = 6) private String otp;
        private String tenantId;
        private OtpPurpose purpose;
    }

    public enum OtpPurpose {
        LOGIN, SIGNUP, RESET_PASSWORD
    }
}
