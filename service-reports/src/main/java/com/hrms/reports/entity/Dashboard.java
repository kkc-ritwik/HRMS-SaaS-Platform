package com.hrms.reports.entity;

import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "dashboards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Dashboard extends BaseEntity {

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "shared", nullable = false)
    private boolean shared = false;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "shared_with", columnDefinition = "jsonb")
    private List<String> sharedWith;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "layout_config", columnDefinition = "jsonb")
    private List<String> layoutConfig;

    @Column(name = "theme", length = 50)
    private String theme;

    @Column(name = "default_dashboard", nullable = false)
    private boolean defaultDashboard = false;
}
