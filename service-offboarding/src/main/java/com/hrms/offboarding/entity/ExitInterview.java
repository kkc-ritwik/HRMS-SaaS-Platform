package com.hrms.offboarding.entity;

import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "exit_interviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExitInterview extends BaseEntity {

    public enum InterviewStatus {
        SCHEDULED, COMPLETED, SKIPPED
    }

    @Column(name = "separation_id", nullable = false)
    private UUID separationId;

    @Column(name = "interviewer_id")
    private UUID interviewerId;

    @Column(name = "scheduled_at")
    private Instant scheduledAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "satisfaction_rating")
    private Integer satisfactionRating;

    @Column(name = "reason_for_leaving", columnDefinition = "TEXT")
    private String reasonForLeaving;

    @Column(name = "would_rejoin")
    private Boolean wouldRejoin;

    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private InterviewStatus status;
}
