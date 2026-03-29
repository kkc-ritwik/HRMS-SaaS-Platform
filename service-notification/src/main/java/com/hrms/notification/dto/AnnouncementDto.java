package com.hrms.notification.dto;

import com.hrms.notification.entity.Announcement.AudienceType;
import com.hrms.notification.entity.Announcement.Priority;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class AnnouncementDto {

    @Getter
    @Setter
    public static class CreateRequest {

        @NotBlank(message = "Title is required")
        private String title;

        private String content;

        private AudienceType audienceType;

        private List<String> targetIds;

        private Priority priority;

        private Instant startDate;

        private Instant endDate;

        private UUID authorId;

        private boolean pinned;
    }

    @Getter
    @Setter
    public static class UpdateRequest {

        private String title;

        private String content;

        private AudienceType audienceType;

        private List<String> targetIds;

        private Priority priority;

        private Instant startDate;

        private Instant endDate;

        private UUID authorId;

        private Boolean pinned;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {

        private UUID id;
        private String tenantId;
        private String title;
        private String content;
        private AudienceType audienceType;
        private List<String> targetIds;
        private Priority priority;
        private Instant startDate;
        private Instant endDate;
        private UUID authorId;
        private boolean pinned;
        private String createdBy;
        private String updatedBy;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
