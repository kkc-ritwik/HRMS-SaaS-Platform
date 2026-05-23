package com.hrms.engagement.suggestion;

import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Anonymous suggestion box — employees can drop ideas, complaints or improvement requests
 * without their identity being exposed to anyone except the HR admin who explicitly de-anonymises
 * a record. submittedByEmployeeId is encrypted by PiiEncryptedConverter so the column value on
 * disk is opaque to ops staff.
 */
@Entity
@Table(name = "engagement_suggestions", indexes = {
        @Index(name = "ix_sugg_tenant_status", columnList = "tenant_id,status"),
        @Index(name = "ix_sugg_category", columnList = "tenant_id,category")
})
@Auditable(value = "Suggestion", redactFields = "submittedByEmployeeIdEncrypted,description")
@EntityListeners(AuditEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Suggestion extends BaseEntity {

    @Column(name = "title", length = 200, nullable = false) private String title;
    @Column(name = "description", length = 5000, nullable = false) private String description;

    /** Optional, encrypted — null if truly anonymous. */
    @Column(name = "submitted_by_encrypted", length = 500)
    @jakarta.persistence.Convert(converter = com.hrms.security.crypto.PiiEncryptedConverter.class)
    private String submittedByEmployeeIdEncrypted;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 30, nullable = false)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private Status status = Status.OPEN;

    @Column(name = "votes_up") private Integer votesUp = 0;
    @Column(name = "votes_down") private Integer votesDown = 0;
    @Column(name = "comments_count") private Integer commentsCount = 0;

    @Column(name = "assigned_reviewer_id") private UUID assignedReviewerId;
    @Column(name = "resolution", length = 2000) private String resolution;
    @Column(name = "resolved_at") private OffsetDateTime resolvedAt;

    public enum Category { PROCESS, CULTURE, FACILITY, BENEFITS, TECHNOLOGY, COMPLAINT, IDEA, OTHER }
    public enum Status { OPEN, UNDER_REVIEW, ACCEPTED, REJECTED, IMPLEMENTED, ARCHIVED }
}
