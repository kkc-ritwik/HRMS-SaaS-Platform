package com.hrms.social.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

public class CommentDto {

    @Getter
    @Setter
    public static class CreateRequest {
        @NotNull
        private UUID postId;

        @NotNull
        private UUID authorId;

        @NotBlank
        private String content;

        private UUID parentCommentId;
    }

    @Getter
    @Setter
    public static class UpdateRequest {
        private String content;
    }

    @Getter
    @Setter
    @Builder
    public static class Response {
        private UUID id;
        private String tenantId;
        private UUID postId;
        private UUID authorId;
        private String content;
        private UUID parentCommentId;
        private int likesCount;
        private String createdBy;
        private String updatedBy;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
