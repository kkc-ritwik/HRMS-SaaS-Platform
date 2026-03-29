package com.hrms.leave.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public class ShiftDto {

    @Getter @Setter
    public static class CreateRequest {
        @NotBlank private String name;
        @NotBlank private String code;
        @NotNull  private LocalTime startTime;
        @NotNull  private LocalTime endTime;
        private int breakDurationMins = 0;
        private BigDecimal minHoursFullDay = BigDecimal.valueOf(8.0);
        private BigDecimal minHoursHalfDay = BigDecimal.valueOf(4.0);
        private int gracePeriodMins = 0;
        private int overtimeThresholdMins = 0;
        private boolean flexible = false;
        private LocalTime flexStartTime;
        private LocalTime flexEndTime;
        private LocalTime coreStartTime;
        private LocalTime coreEndTime;
        private boolean nightShift = false;
        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$") private String color;
    }

    @Getter @Setter
    public static class UpdateRequest {
        private String name;
        private LocalTime startTime;
        private LocalTime endTime;
        private Integer breakDurationMins;
        private BigDecimal minHoursFullDay;
        private BigDecimal minHoursHalfDay;
        private Integer gracePeriodMins;
        private Integer overtimeThresholdMins;
        private Boolean flexible;
        private LocalTime flexStartTime;
        private LocalTime flexEndTime;
        private LocalTime coreStartTime;
        private LocalTime coreEndTime;
        private Boolean nightShift;
        private String color;
        private Boolean active;
    }

    @Getter @Setter @Builder
    public static class Response {
        private UUID id;
        private String name;
        private String code;
        private LocalTime startTime;
        private LocalTime endTime;
        private int breakDurationMins;
        private BigDecimal minHoursFullDay;
        private BigDecimal minHoursHalfDay;
        private int gracePeriodMins;
        private int overtimeThresholdMins;
        private boolean flexible;
        private LocalTime flexStartTime;
        private LocalTime flexEndTime;
        private LocalTime coreStartTime;
        private LocalTime coreEndTime;
        private boolean nightShift;
        private String color;
        private boolean active;
        private Instant createdAt;
        private Instant updatedAt;
    }

    @Getter @Setter
    public static class AssignRequest {
        @NotNull private UUID employeeId;
        @NotNull private LocalDate fromDate;
        private LocalDate toDate;       // null = single day
        private boolean weekOff = false;
        private boolean holiday = false;
    }
}
