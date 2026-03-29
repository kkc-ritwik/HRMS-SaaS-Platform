package com.hrms.social.dto;

import com.hrms.social.entity.GroupMember;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

public class GroupMemberDto {

    @Getter
    @Setter
    public static class CreateRequest {
        @NotNull
        private UUID groupId;

        @NotNull
        private UUID employeeId;

        private GroupMember.MemberRole role = GroupMember.MemberRole.MEMBER;
    }

    @Getter
    @Setter
    public static class UpdateRequest {
        private GroupMember.MemberRole role;
    }

    @Getter
    @Setter
    @Builder
    public static class Response {
        private UUID id;
        private String tenantId;
        private UUID groupId;
        private UUID employeeId;
        private GroupMember.MemberRole role;
        private Instant joinedAt;
        private String createdBy;
        private String updatedBy;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
