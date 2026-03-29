package com.hrms.workflow.entity;

import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@Table(name = "workflow_definitions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowDefinition extends BaseEntity {

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "entity_type", nullable = false, length = 100)
    private String entityType;

    @Column(name = "trigger_event", length = 100)
    private String triggerEvent;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "steps_config", columnDefinition = "jsonb")
    private List<String> stepsConfig;

    @Column(name = "active", nullable = false)
    private boolean active = true;
}
