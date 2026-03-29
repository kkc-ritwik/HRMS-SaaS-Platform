package com.hrms.social.dto;

import com.hrms.social.entity.SocialGroup;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

public class SocialGroupDto {

    @Getter
    @Setter
    public static class CreateRequest {
        @NotBlank
        private String name;

        private String description;

        private SocialGroup.GroupType groupType = SocialGroup.GroupType.PUBLIC;

        @NotNull
        private UUID ownerId;

        private String coverImageUrl;
    }

    @Getter
    @Setter
    public static class UpdateRequest {
        private String name;
        private String description;
        private SocialGroup.GroupType groupType;
        private String coverImageUrl;
    }

    @Getter
    @Setter
    @Builder
    public static class Response {
        private UUID id;
        private String tenantId;
        private String name;
        private String description;
        private SocialGroup.GroupType groupType;
        private UUID ownerId;
        private String coverImageUrl;
        private int memberCount;
        private String createdBy;
        private String updatedBy;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
