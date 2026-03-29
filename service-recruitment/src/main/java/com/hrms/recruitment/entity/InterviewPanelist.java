package com.hrms.recruitment.entity;

import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "interview_panelists")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class InterviewPanelist extends BaseEntity {

    @Column(name = "interview_id", nullable = false)
    private UUID interviewId;

    /** Employee who will conduct / has conducted the interview. */
    @Column(name = "interviewer_id", nullable = false)
    private UUID interviewerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private PanelistRole role = PanelistRole.PANELIST;

    /** Rating given by this panelist (1–5). */
    @Column(name = "rating")
    private Integer rating;

    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;

    /** When this panelist submitted their scorecard. */
    @Column(name = "submitted_at")
    private Instant submittedAt;

    // ── Enum ──────────────────────────────────────────────────────────────────

    public enum PanelistRole { LEAD, PANELIST, SHADOW }
}
