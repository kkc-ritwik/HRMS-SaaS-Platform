package com.hrms.document.dto;

import com.hrms.document.entity.GeneratedLetter.LetterStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

public class GeneratedLetterDto {

    @Getter
    @Setter
    public static class CreateRequest {

        @NotNull
        private UUID employeeId;

        private UUID templateId;

        private String letterType;

        @NotBlank
        private String subject;

        private String content;
    }

    @Getter
    @Setter
    public static class UpdateRequest {

        private UUID employeeId;

        private UUID templateId;

        private String letterType;

        private String subject;

        private String content;

        private LetterStatus status;

        private Instant sentAt;
    }

    @Getter
    @Setter
    @Builder
    public static class Response {

        private UUID id;
        private UUID employeeId;
        private UUID templateId;
        private String letterType;
        private String subject;
        private String content;
        private UUID generatedBy;
        private Instant sentAt;
        private LetterStatus status;
        private String tenantId;
        private String createdBy;
        private String updatedBy;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
