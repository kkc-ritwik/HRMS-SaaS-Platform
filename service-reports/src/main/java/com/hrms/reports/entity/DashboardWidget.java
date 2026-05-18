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
@Table(name = "dashboard_widgets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Auditable("DashboardWidget")
@EntityListeners(AuditEntityListener.class)
public class DashboardWidget extends BaseEntity {

    public enum WidgetType {
        CHART, TABLE, KPI, CALENDAR, HEATMAP
    }

    @Column(name = "dashboard_id", nullable = false)
    private UUID dashboardId;

    @Enumerated(EnumType.STRING)
    @Column(name = "widget_type", nullable = false, length = 30)
    private WidgetType widgetType;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "data_source", length = 200)
    private String dataSource;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "query_config", columnDefinition = "jsonb")
    private List<String> queryConfig;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "display_config", columnDefinition = "jsonb")
    private List<String> displayConfig;

    @Column(name = "position_x", nullable = false)
    private int positionX = 0;

    @Column(name = "position_y", nullable = false)
    private int positionY = 0;

    @Column(name = "width", nullable = false)
    private int width = 4;

    @Column(name = "height", nullable = false)
    private int height = 3;

    @Column(name = "refresh_seconds")
    private Integer refreshSeconds;
}
