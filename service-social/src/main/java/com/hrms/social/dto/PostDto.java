package com.hrms.social.dto;

import com.hrms.social.entity.Post;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class PostDto {

    @Getter
    @Setter
    public static class CreateRequest {
        @NotNull
        private UUID authorId;

        @NotBlank
        private String content;

        private List<String> mediaUrls;

        private Post.Visibility visibility = Post.Visibility.PUBLIC;

        private Post.PostType postType = Post.PostType.GENERAL;

        private boolean pinned = false;
    }

    @Getter
    @Setter
    public static class UpdateRequest {
        private String content;
        private List<String> mediaUrls;
        private Post.Visibility visibility;
        private Post.PostType postType;
        private Boolean pinned;
    }

    @Getter
    @Setter
    @Builder
    public static class Response {
        private UUID id;
        private String tenantId;
        private UUID authorId;
        private String content;
        private List<String> mediaUrls;
        private Post.Visibility visibility;
        private Post.PostType postType;
        private int likesCount;
        private int commentsCount;
        private boolean pinned;
        private String createdBy;
        private String updatedBy;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
