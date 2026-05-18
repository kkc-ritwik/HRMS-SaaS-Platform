package com.hrms.onboarding.preboard;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

/**
 * Workspace given to a candidate AFTER offer acceptance and BEFORE day-0.
 * Lets them upload joining documents (PAN, Aadhar, BGV consent, previous-employer letter),
 * fill personal details, see their welcome kit, and complete the company-handbook acknowledgement.
 *
 * Access token is a single-use short URL â†’ no full login required pre-joining.
 */
@Entity
@Table(name = "pre_onboarding_portals",
        uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id","candidate_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class PreOnboardingPortal extends BaseEntity {

    @Column(name = "candidate_id", nullable = false)
    private UUID candidateId;

    @Column(name = "offer_id")
    private UUID offerId;

    @Column(name = "access_token", length = 100, unique = true, nullable = false)
    private String accessToken;

    @Column(name = "proposed_join_date")
    private LocalDate proposedJoinDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private Status status = Status.INVITED;

    /** Map of { documentType -> storageUri } the candidate has uploaded. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "uploaded_documents", columnDefinition = "jsonb")
    private Map<String, String> uploadedDocuments;

    /** Free-form personal details captured pre-joining. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "personal_details", columnDefinition = "jsonb")
    private Map<String, Object> personalDetails;

    @Column(name = "handbook_acknowledged_at")
    private java.time.Instant handbookAcknowledgedAt;

    @Column(name = "invitation_sent_at")
    private java.time.Instant invitationSentAt;

    @Column(name = "completed_at")
    private java.time.Instant completedAt;

    public enum Status { INVITED, IN_PROGRESS, COMPLETED, EXPIRED, CANCELLED }
}
