package com.hrms.recruitment.scorecard;

import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.Map;

/**
 * Template defining the questions/competencies a panellist must score for a given
 * interview round (e.g. "Backend SDE-II Behavioural", "Frontend SDE-II Coding"). Each
 * competency has weight, scale (1..5), and a calibration rubric.
 *
 * Bound to {@link com.hrms.recruitment.entity.Interview} via {@code scorecardTemplateId};
 * each panellist fills one {@link InterviewScorecard} per assigned interview.
 */
@Entity
@Table(name = "recruit_scorecard_templates",
        indexes = @Index(name = "ix_sct_tenant_active", columnList = "tenant_id,active"))
@Auditable("InterviewScorecardTemplate")
@EntityListeners(AuditEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class InterviewScorecardTemplate extends BaseEntity {

    @Column(name = "code", length = 100, nullable = false) private String code;
    @Column(name = "name", length = 200, nullable = false) private String name;
    @Column(name = "description", length = 2000) private String description;

    @Column(name = "round_type", length = 50) private String roundType;     // PHONE_SCREEN / TECH / HM / BAR_RAISER / etc.
    @Column(name = "job_family", length = 100) private String jobFamily;
    @Column(name = "level", length = 30) private String level;

    /** [{ id, name, description, weight, scale:[1..5] rubric:{1:"…",5:"…"} }, ...] */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "competencies", columnDefinition = "jsonb")
    private List<Map<String, Object>> competencies;

    @Column(name = "min_overall_to_advance") private Integer minOverallToAdvance;
    @Column(name = "duration_minutes") private Integer durationMinutes;
    @Column(name = "active") private Boolean active = true;
}
