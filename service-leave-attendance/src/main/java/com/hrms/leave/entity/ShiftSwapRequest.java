package com.hrms.leave.entity;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.events.annotation.PublishEvents;
import com.hrms.events.listener.EntityLifecyclePublisher;
import com.hrms.events.model.Topics;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Shift swap between two employees on specific dates. Requires acceptance from the
 * swap partner AND manager approval before taking effect on attendance.
 */
@Entity
@Table(name = "shift_swap_requests",
        indexes = @Index(name = "ix_shiftswap_status", columnList = "tenant_id,status"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@PublishEvents(topic = Topics.LEAVE, namespace = "leave.shift_swap")
@EntityListeners(EntityLifecyclePublisher.class)
public class ShiftSwapRequest extends BaseEntity {

    @Column(name = "requester_employee_id", nullable = false) private UUID requesterEmployeeId;
    @Column(name = "swap_with_employee_id", nullable = false) private UUID swapWithEmployeeId;

    @Column(name = "requester_shift_date", nullable = false) private LocalDate requesterShiftDate;
    @Column(name = "requester_shift_id") private UUID requesterShiftId;

    @Column(name = "partner_shift_date", nullable = false) private LocalDate partnerShiftDate;
    @Column(name = "partner_shift_id") private UUID partnerShiftId;

    @Column(name = "reason", length = 1000) private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private Status status = Status.PENDING_PARTNER;

    @Column(name = "partner_responded_at") private Instant partnerRespondedAt;
    @Column(name = "manager_id") private UUID managerId;
    @Column(name = "manager_approved_at") private Instant managerApprovedAt;
    @Column(name = "rejection_reason", length = 500) private String rejectionReason;

    public enum Status { PENDING_PARTNER, PENDING_MANAGER, APPROVED, REJECTED, CANCELLED, COMPLETED }
}
