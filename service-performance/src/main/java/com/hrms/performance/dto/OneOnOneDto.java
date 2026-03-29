package com.hrms.performance.dto;

import com.hrms.performance.entity.ActionItem;
import com.hrms.performance.entity.OneOnOne;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class OneOnOneDto {

    @Getter @Setter
    public static class ScheduleRequest {
        @NotNull private UUID    employeeId;
        @NotNull private Instant scheduledAt;
        private int              durationMinutes = 30;
        private String           agenda;
        private LocalDate        nextMeetingDate;
    }

    @Getter @Setter
    public static class UpdateRequest {
        private Instant          scheduledAt;
        private int              durationMinutes;
        private OneOnOne.MeetingStatus status;
        private String           agenda;
        private String           managerNotes;
        private String           employeeNotes;
        private List<ActionItem> actionItems;
        private LocalDate        nextMeetingDate;
    }

    @Getter @Setter @Builder
    public static class Response {
        private UUID                   id;
        private UUID                   managerId;
        private UUID                   employeeId;
        private Instant                scheduledAt;
        private int                    durationMinutes;
        private OneOnOne.MeetingStatus status;
        private String                 agenda;
        private String                 managerNotes;
        private String                 employeeNotes;
        private List<ActionItem>       actionItems;
        private LocalDate              nextMeetingDate;
        private Instant                createdAt;
        private Instant                updatedAt;
    }
}
