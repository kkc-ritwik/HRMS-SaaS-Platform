package com.hrms.leave.dto;

import com.hrms.leave.entity.Holiday;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class HolidayDto {

    @Getter @Setter
    public static class CreateRequest {
        @NotBlank private String name;
        @NotNull  private LocalDate date;
        private Holiday.HolidayType type = Holiday.HolidayType.NATIONAL;
        private List<UUID> locationIds;
    }

    @Getter @Setter
    public static class UpdateRequest {
        private String name;
        private LocalDate date;
        private Holiday.HolidayType type;
        private List<UUID> locationIds;
        private Boolean active;
    }

    @Getter @Setter @Builder
    public static class Response {
        private UUID id;
        private String name;
        private LocalDate date;
        private Holiday.HolidayType type;
        private List<UUID> locationIds;
        private boolean active;
        private int year;
        private Instant createdAt;
    }
}
