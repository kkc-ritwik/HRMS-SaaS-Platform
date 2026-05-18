package com.hrms.reports.entity;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "report_definitions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Auditable("ReportDefinition")
@EntityListeners(AuditEntityListener.class)
public class ReportDefinition extends BaseEntity {

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "category", length = 100)
    private String category;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "query_config", columnDefinition = "jsonb")
    private List<String> queryConfig;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "parameters_config", columnDefinition = "jsonb")
    private List<String> parametersConfig;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "output_formats", columnDefinition = "jsonb")
    private List<String> outputFormats;

    @Column(name = "schedule_cron", length = 100)
    private String scheduleCron;

    @Column(name = "owner_id")
    private UUID ownerId;

    @Column(name = "active", nullable = false)
    private boolean active = true;
}
