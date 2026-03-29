package com.hrms.social.dto;

import com.hrms.social.entity.Event;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

public class EventDto {

    @Getter
    @Setter
    public static class CreateRequest {
        @NotBlank
        private String title;

        private String description;

        @NotNull
        private UUID organizerId;

        private Event.EventType eventType = Event.EventType.SOCIAL;

        @NotNull
        private Instant startTime;

        private Instant endTime;

        private String location;

        private String virtualLink;

        private UUID groupId;
    }

    @Getter
    @Setter
    public static class UpdateRequest {
        private String title;
        private String description;
        private Event.EventType eventType;
        private Instant startTime;
        private Instant endTime;
        private String location;
        private String virtualLink;
        private UUID groupId;
    }

    @Getter
    @Setter
    @Builder
    public static class Response {
        private UUID id;
        private String tenantId;
        private String title;
        private String description;
        private UUID organizerId;
        private Event.EventType eventType;
        private Instant startTime;
        private Instant endTime;
        private String location;
        private String virtualLink;
        private int rsvpCount;
        private UUID groupId;
        private String createdBy;
        private String updatedBy;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
