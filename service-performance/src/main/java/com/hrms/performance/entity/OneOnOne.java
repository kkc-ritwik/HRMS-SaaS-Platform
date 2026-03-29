package com.hrms.performance.entity;

import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "one_on_ones")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class OneOnOne extends BaseEntity {

    @Column(name = "manager_id", nullable = false)
    private UUID managerId;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "scheduled_at", nullable = false)
    private Instant scheduledAt;

    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes = 30;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MeetingStatus status = MeetingStatus.SCHEDULED;

    @Column(name = "agenda", columnDefinition = "TEXT")
    private String agenda;

    @Column(name = "manager_notes", columnDefinition = "TEXT")
    private String managerNotes;

    @Column(name = "employee_notes", columnDefinition = "TEXT")
    private String employeeNotes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "action_items", columnDefinition = "jsonb")
    private List<ActionItem> actionItems;

    @Column(name = "next_meeting_date")
    private LocalDate nextMeetingDate;

    // ── Enum ──────────────────────────────────────────────────────────────────

    public enum MeetingStatus { SCHEDULED, COMPLETED, CANCELLED, RESCHEDULED }
}
