package com.hrms.engagement.stay;

import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Question bank for stay interviews. Tenants can override default questions, add their own,
 * and mark questions as required / optional per trigger type.
 *
 * Seeded defaults (HBR's "Stay Interview" canon):
 *  · What keeps you here?
 *  · What might tempt you to leave?
 *  · What can I do to make your job better?
 *  · When did you last consider leaving us?
 *  · What can we do to help you grow?
 */
@Entity
@Table(name = "engagement_stay_questions",
        indexes = @Index(name = "ix_stayq_tenant_active", columnList = "tenant_id,is_active"))
@Auditable("StayQuestion")
@EntityListeners(AuditEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class StayQuestion extends BaseEntity {

    @Column(name = "code", length = 60, nullable = false) private String code;
    @Column(name = "question_text", length = 1000, nullable = false) private String questionText;

    @Enumerated(EnumType.STRING)
    @Column(name = "response_type", length = 20, nullable = false)
    private ResponseType responseType;

    @Column(name = "is_required") private Boolean required;
    @Column(name = "is_active") private Boolean active = true;
    @Column(name = "display_order") private Integer displayOrder;
    @Column(name = "applies_to_triggers", length = 200) private String appliesToTriggers; // CSV of StayInterview.Trigger names

    public enum ResponseType { FREE_TEXT, RATING_1_5, YES_NO, MULTI_SELECT }
}
