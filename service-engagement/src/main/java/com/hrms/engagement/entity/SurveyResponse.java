package com.hrms.engagement.entity;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "survey_responses", indexes = @Index(name = "ix_resp_survey", columnList = "survey_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Auditable("SurveyResponse")
@EntityListeners(AuditEntityListener.class)
public class SurveyResponse {
    @Id @GeneratedValue private UUID id;
    @Column(name = "tenant_id", length = 100, nullable = false) private String tenantId;
    @Column(name = "survey_id", nullable = false) private UUID surveyId;
    @Column(name = "respondent_id") private UUID respondentId;
    @JdbcTypeCode(SqlTypes.JSON) @Column(columnDefinition = "jsonb")
    private Map<String, Object> answers;
    @Column(name = "submitted_at") private OffsetDateTime submittedAt;
    @PrePersist void prePersist() { if (submittedAt == null) submittedAt = OffsetDateTime.now(); }
}
