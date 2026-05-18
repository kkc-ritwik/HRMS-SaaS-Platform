package com.hrms.engagement.entity;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import com.hrms.events.annotation.PublishEvents;
import com.hrms.events.listener.EntityLifecyclePublisher;
import com.hrms.events.model.Topics;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "surveys")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Auditable("Survey")
@PublishEvents(topic = Topics.ENGAGEMENT, namespace = "engagement.survey")
@EntityListeners({AuditEntityListener.class, EntityLifecyclePublisher.class})
public class Survey {
    @Id @GeneratedValue private UUID id;
    @Column(name = "tenant_id", length = 100, nullable = false) private String tenantId;
    @Column(nullable = false, length = 200) private String title;
    @Column(length = 1000) private String description;
    @Enumerated(EnumType.STRING) @Column(length = 30) private SurveyType type;
    @Enumerated(EnumType.STRING) @Column(length = 30) private Status status;
    @Column(name = "is_anonymous") private Boolean isAnonymous;
    @Column(name = "starts_on") private LocalDate startsOn;
    @Column(name = "ends_on") private LocalDate endsOn;

    /** Question schema â€” array of { id, text, type, options, required } */
    @JdbcTypeCode(SqlTypes.JSON) @Column(columnDefinition = "jsonb")
    private List<Map<String, Object>> questions;

    /** Optional target audience filter (departments, locations, roles). */
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "audience_filter", columnDefinition = "jsonb")
    private Map<String, Object> audienceFilter;

    public enum SurveyType { ENGAGEMENT, ENPS, PULSE, EXIT, ONBOARDING, CUSTOM }
    public enum Status { DRAFT, ACTIVE, CLOSED, ARCHIVED }
}
