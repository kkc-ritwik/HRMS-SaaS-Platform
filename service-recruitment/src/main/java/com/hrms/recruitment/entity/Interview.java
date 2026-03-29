package com.hrms.recruitment.entity;

import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "interviews")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Interview extends BaseEntity {

    @Column(name = "application_id", nullable = false)
    private UUID applicationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "interview_type", nullable = false, length = 30)
    private InterviewType interviewType;

    @Column(name = "round_number", nullable = false)
    private int roundNumber = 1;

    @Column(name = "scheduled_at", nullable = false)
    private Instant scheduledAt;

    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes = 60;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode", nullable = false, length = 20)
    private InterviewMode mode = InterviewMode.VIDEO;

    @Column(name = "meeting_link", length = 1000)
    private String meetingLink;

    @Column(name = "venue", length = 500)
    private String venue;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private InterviewStatus status = InterviewStatus.SCHEDULED;

    @Column(name = "overall_rating")
    private Integer overallRating;

    @Enumerated(EnumType.STRING)
    @Column(name = "recommendation", length = 20)
    private Recommendation recommendation;

    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;

    // ── Enums ──────────────────────────────────────────────────────────────────

    public enum InterviewType { PHONE_SCREEN, TECHNICAL, HR, PANEL, FINAL, CASE_STUDY }

    public enum InterviewMode { IN_PERSON, VIDEO, PHONE }

    public enum InterviewStatus { SCHEDULED, COMPLETED, CANCELLED, NO_SHOW }

    public enum Recommendation { STRONGLY_HIRE, HIRE, NEUTRAL, NO_HIRE, STRONG_NO_HIRE }
}
