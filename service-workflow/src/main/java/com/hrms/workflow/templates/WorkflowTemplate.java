package com.hrms.workflow.templates;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.Map;

/**
 * Pre-built workflow template that tenants can clone into their own workflows
 * (e.g. "Leave Approval â€” Manager + HR", "Travel Approval â€” Manager + Finance + HR",
 * "Expense > â‚¹50k", "Hiring Requisition > L4"). Seeded by V100__seed_workflow_templates.sql.
 */
@Entity
@Table(name = "workflow_templates",
        uniqueConstraints = @UniqueConstraint(columnNames = {"code"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class WorkflowTemplate extends BaseEntity {

    @Column(name = "code", length = 100, nullable = false) private String code;
    @Column(name = "name", length = 200, nullable = false) private String name;
    @Column(name = "description", length = 1000) private String description;
    @Column(name = "category", length = 100) private String category;   // LEAVE / EXPENSE / TRAVEL / HIRING / etc.
    @Column(name = "icon", length = 50) private String icon;

    /** Pre-built step definitions â€” copied into WorkflowStep rows when tenant clones the template. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "steps", columnDefinition = "jsonb", nullable = false)
    private List<Map<String, Object>> steps;

    @Column(name = "is_active", nullable = false) private boolean active = true;
}
