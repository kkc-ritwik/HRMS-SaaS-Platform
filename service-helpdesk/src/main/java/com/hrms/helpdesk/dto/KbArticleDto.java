package com.hrms.helpdesk.dto;

import com.hrms.helpdesk.entity.KbArticle.ArticleStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class KbArticleDto {

    @Getter
    @Setter
    public static class CreateRequest {

        @NotBlank(message = "Title is required")
        private String title;

        private String content;

        private UUID categoryId;

        private List<String> tags;

        private UUID authorId;
    }

    @Getter
    @Setter
    public static class UpdateRequest {

        private String title;

        private String content;

        private UUID categoryId;

        private List<String> tags;

        private ArticleStatus status;

        private Instant publishedAt;
    }

    @Getter
    @Setter
    @Builder
    public static class Response {

        private UUID id;
        private String tenantId;
        private String title;
        private String content;
        private UUID categoryId;
        private List<String> tags;
        private ArticleStatus status;
        private int views;
        private int helpfulVotes;
        private UUID authorId;
        private Instant publishedAt;
        private String createdBy;
        private String updatedBy;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
