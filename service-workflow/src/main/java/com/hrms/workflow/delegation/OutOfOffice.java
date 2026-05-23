package com.hrms.workflow.delegation;

import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Out-of-office responder. While active, every approval request that would normally
 * land in the user's inbox is auto-forwarded to delegateUserId (if set) AND an
 * auto-reply is posted on inbound HR-facing messages.
 *
 * Implemented as a sibling to {@link com.hrms.workflow.entity.DelegationRule} — that
 * one is scoped per workflow type, this one is global and time-bounded.
 */
@Entity
@Table(name = "out_of_office")
@Auditable("OutOfOffice")
@EntityListeners(AuditEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class OutOfOffice extends BaseEntity {

    @Column(name = "user_id", nullable = false) private UUID userId;

    /** Whom should approvals be routed to during the absence. Null = approvals just queue. */
    @Column(name = "delegate_user_id") private UUID delegateUserId;

    @Column(name = "starts_at", nullable = false) private Instant startsAt;
    @Column(name = "ends_at", nullable = false) private Instant endsAt;

    @Column(name = "auto_reply_message", length = 2000) private String autoReplyMessage;

    /** If true, urgent/CRITICAL items still notify the user despite OOO. */
    @Column(name = "notify_urgent") private Boolean notifyUrgent;

    @Column(name = "is_active", nullable = false) private boolean active = true;

    public boolean appliesNow() {
        Instant now = Instant.now();
        return active && !now.isBefore(startsAt) && !now.isAfter(endsAt);
    }
}
