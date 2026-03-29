package com.hrms.leave.entity;

import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "leave_approvals")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class LeaveApproval extends BaseEntity {

    @Column(name = "leave_application_id", nullable = false)
    private UUID leaveApplicationId;

    @Column(name = "approver_id", nullable = false)
    private UUID approverId;

    @Column(name = "level", nullable = false)
    private int level = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ApprovalStatus status = ApprovalStatus.PENDING;

    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;

    @Column(name = "acted_at")
    private Instant actedAt;

    public enum ApprovalStatus {
        PENDING, APPROVED, REJECTED
    }
}
