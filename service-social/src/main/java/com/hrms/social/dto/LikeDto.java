package com.hrms.social.dto;

import com.hrms.social.entity.Like;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

public class LikeDto {

    @Getter
    @Setter
    public static class CreateRequest {
        @NotNull
        private UUID employeeId;

        private UUID postId;

        private UUID commentId;

        private Like.ReactionType reactionType = Like.ReactionType.LIKE;
    }

    @Getter
    @Setter
    public static class UpdateRequest {
        private Like.ReactionType reactionType;
    }

    @Getter
    @Setter
    @Builder
    public static class Response {
        private UUID id;
        private String tenantId;
        private UUID postId;
        private UUID commentId;
        private UUID employeeId;
        private Like.ReactionType reactionType;
        private String createdBy;
        private String updatedBy;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
