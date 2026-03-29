package com.hrms.recruitment.entity;

import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "offer_letters")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class OfferLetter extends BaseEntity {

    @Column(name = "application_id", nullable = false)
    private UUID applicationId;

    @Column(name = "offered_ctc", nullable = false, precision = 14, scale = 2)
    private BigDecimal offeredCtc;

    @Column(name = "offered_title", nullable = false, length = 200)
    private String offeredTitle;

    @Column(name = "joining_date")
    private LocalDate joiningDate;

    @Column(name = "offer_expiry_date")
    private LocalDate offerExpiryDate;

    /** Name / key of the template used for rendering. */
    @Column(name = "template_used", length = 100)
    private String templateUsed;

    /** Fully merged offer letter content (HTML or Markdown). */
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OfferStatus status = OfferStatus.DRAFT;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(name = "responded_at")
    private Instant respondedAt;

    @Column(name = "response_notes", length = 500)
    private String responseNotes;

    // ── Enum ──────────────────────────────────────────────────────────────────

    public enum OfferStatus { DRAFT, SENT, ACCEPTED, DECLINED, EXPIRED, REVOKED }
}
