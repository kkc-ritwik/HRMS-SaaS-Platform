package com.hrms.auth.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.util.Set;
import java.util.UUID;

public class AuthDto {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class LoginRequest {
        @NotBlank private String email;
        @NotBlank private String password;
        @NotBlank private String tenantId;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class SignupRequest {
        @NotBlank @Email  private String email;
        @NotBlank @Size(min = 8) private String password;
        @NotBlank private String fullName;
        @NotBlank private String tenantId;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class LoginResponse {
        private String accessToken;
        private String refreshToken;
        private long expiresIn;
        private UserInfo user;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class UserInfo {
        private UUID id;
        private String email;
        private String fullName;
        private String avatarUrl;
        private Set<String> roles;
        private Set<String> permissions;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class RefreshRequest {
        @NotBlank private String refreshToken;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ChangePasswordRequest {
        private String currentPassword;
        @Size(min = 8) private String newPassword;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ForgotPasswordRequest {
        private String email;
        private String tenantId;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ResetPasswordRequest {
        private String token;
        private String newPassword;
    }
}
