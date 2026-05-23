package com.hrms.recruitment.scorecard;

import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Coordinated set of interviews for one candidate ("on-site loop"). The recruiter assembles
 * a panel, picks a date, and the system finds a continuous block across panellists'
 * calendars. Each loop has many rounds; each round has 1+ scorecards (multi-panellist).
 *
 *   panel       — list of {employeeId, role:"PHONE_SCREEN"|"HM"|"TECH"|"BAR_RAISER", templateId}
 *   schedule    — list of {startTime, endTime, panellistId, templateId, location, status}
 *   debrief     — final hiring-committee notes after all rounds complete
 */
@Entity
@Table(name = "recruit_hiring_loops",
        indexes = @Index(name = "ix_loop_app", columnList = "tenant_id,application_id"))
@Auditable("HiringLoop")
@EntityListeners(AuditEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class HiringLoop extends BaseEntity {

    @Column(name = "application_id", nullable = false) private UUID applicationId;
    @Column(name = "candidate_id", nullable = false) private UUID candidateId;
    @Column(name = "requisition_id") private UUID requisitionId;
    @Column(name = "loop_date") private LocalDate loopDate;
    @Column(name = "location", length = 200) private String location;
    @Column(name = "format", length = 20) private String format; // ONSITE / REMOTE / HYBRID

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "panel", columnDefinition = "jsonb")
    private List<Map<String, Object>> panel;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "schedule", columnDefinition = "jsonb")
    private List<Map<String, Object>> schedule;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "debrief_notes", columnDefinition = "jsonb")
    private Map<String, Object> debriefNotes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private Status status = Status.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "outcome", length = 20)
    private Outcome outcome;

    public enum Status { DRAFT, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED }
    public enum Outcome { HIRE, NO_HIRE, ADVANCE_TO_OFFER, NEED_ADDITIONAL_ROUND, HOLD }
}
