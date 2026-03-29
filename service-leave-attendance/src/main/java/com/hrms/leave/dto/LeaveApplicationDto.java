package com.hrms.leave.dto;

import com.hrms.leave.entity.LeaveApplication;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class LeaveApplicationDto {

    @Getter @Setter
    public static class ApplyRequest {
        @NotNull private UUID leaveTypeId;
        @NotNull private LocalDate fromDate;
        @NotNull private LocalDate toDate;
        private LeaveApplication.DayType dayType = LeaveApplication.DayType.FULL;
        private String reason;
        private String attachmentUrl;
        private UUID approverId;
    }

    @Getter @Setter
    public static class ApprovalRequest {
        private String comments;
    }

    @Getter @Setter
    public static class CancelRequest {
        private String cancelReason;
    }

    @Getter @Setter @Builder
    public static class Response {
        private UUID id;
        private String tenantId;
        private UUID employeeId;
        private String employeeName;
        private UUID leaveTypeId;
        private String leaveTypeName;
        private String leaveTypeColor;
        private LocalDate fromDate;
        private LocalDate toDate;
        private BigDecimal durationDays;
        private LeaveApplication.DayType dayType;
        private String reason;
        private String attachmentUrl;
        private LeaveApplication.LeaveStatus status;
        private Instant appliedAt;
        private Instant cancelledAt;
        private String cancelReason;
        private List<ApprovalInfo> approvals;
        private Instant createdAt;
        private Instant updatedAt;
    }

    @Getter @Setter @Builder
    public static class ApprovalInfo {
        private UUID id;
        private UUID approverId;
        private String approverName;
        private int level;
        private String status;
        private String comments;
        private Instant actedAt;
    }

    @Getter @Setter @Builder
    public static class TeamCalendarItem {
        private UUID employeeId;
        private String employeeName;
        private UUID leaveTypeId;
        private String leaveTypeName;
        private String leaveTypeColor;
        private LocalDate fromDate;
        private LocalDate toDate;
        private BigDecimal durationDays;
        private String status;
    }
}
