package com.hrms.leave.dto;

import com.hrms.leave.entity.AttendancePunch;
import com.hrms.leave.entity.AttendanceRecord;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class AttendanceDto {

    @Getter @Setter
    public static class PunchRequest {
        private String source = "WEB";
        private String ipAddress;
        private BigDecimal latitude;
        private BigDecimal longitude;
    }

    @Getter @Setter @Builder
    public static class PunchResponse {
        private UUID punchId;
        private UUID employeeId;
        private Instant punchTime;
        private AttendancePunch.PunchType type;
        private String source;
        private AttendanceRecord.AttendanceStatus currentStatus;
        private Instant firstCheckIn;
        private Instant lastCheckOut;
        private BigDecimal effectiveHoursToday;
        private int lateByMins;
        private String message;
    }

    @Getter @Setter @Builder
    public static class RecordResponse {
        private UUID id;
        private UUID employeeId;
        private LocalDate attendanceDate;
        private String shiftName;
        private Instant firstCheckIn;
        private Instant lastCheckOut;
        private BigDecimal totalHours;
        private BigDecimal effectiveHours;
        private BigDecimal breakHours;
        private BigDecimal overtimeHours;
        private AttendanceRecord.AttendanceStatus status;
        private int lateByMins;
        private int earlyLeavingMins;
        private String source;
        private boolean regularized;
        private List<PunchInfo> punches;
    }

    @Getter @Setter @Builder
    public static class PunchInfo {
        private UUID id;
        private Instant punchTime;
        private AttendancePunch.PunchType type;
        private String source;
        private String ipAddress;
        private BigDecimal latitude;
        private BigDecimal longitude;
    }

    @Getter @Setter @Builder
    public static class TeamAttendanceItem {
        private UUID employeeId;
        private String employeeName;
        private AttendanceRecord.AttendanceStatus status;
        private Instant firstCheckIn;
        private Instant lastCheckOut;
        private BigDecimal effectiveHours;
        private int lateByMins;
        private String shiftName;
    }

    @Getter @Setter @Builder
    public static class DashboardResponse {
        private LocalDate date;
        private long totalExpected;      // employees who should have attended
        private long presentCount;       // checked in (PRESENT or HALF_DAY)
        private long absentCount;        // no punch at all
        private long lateCount;          // present but late
        private long onLeaveCount;       // on approved leave
        private long halfDayCount;
        private List<TeamAttendanceItem> presentEmployees;
        private List<UUID> absentEmployeeIds;
        private List<TeamAttendanceItem> lateEmployees;
    }

    @Getter @Setter @Builder
    public static class MonthSummary {
        private int year;
        private int month;
        private UUID employeeId;
        private int presentDays;
        private int absentDays;
        private int halfDays;
        private int leaveDays;
        private int holidayDays;
        private int weekOffDays;
        private BigDecimal totalOvertimeHours;
        private List<RecordResponse> records;
    }
}
