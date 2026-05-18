package com.hrms.social.dm;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/** Private 1:1 message between two employees. Backed by WebSocket on delivery (notification svc). */
@Entity
@Table(name = "social_direct_messages",
        indexes = @Index(name = "ix_dm_thread", columnList = "tenant_id,thread_key,created_at"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class DirectMessage extends BaseEntity {

    /** Canonical thread key = sorted(senderId,recipientId).join("-") so both sides see one thread. */
    @Column(name = "thread_key", length = 80, nullable = false)
    private String threadKey;

    @Column(name = "sender_id", nullable = false) private UUID senderId;
    @Column(name = "recipient_id", nullable = false) private UUID recipientId;
    @Column(name = "content", length = 5000, nullable = false) private String content;

    @Column(name = "attachment_uri", length = 1000) private String attachmentUri;
    @Column(name = "read_at") private Instant readAt;
    @Column(name = "is_deleted_by_sender") private Boolean deletedBySender;
    @Column(name = "is_deleted_by_recipient") private Boolean deletedByRecipient;

    /** Helper â€” call before save() to compute the canonical thread key. */
    public static String threadKey(UUID a, UUID b) {
        String x = a.toString(), y = b.toString();
        return x.compareTo(y) < 0 ? x + "-" + y : y + "-" + x;
    }
}
