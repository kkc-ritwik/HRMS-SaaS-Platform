package com.hrms.corehr.views;


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
import java.util.UUID;

/**
 * User-saved filter / view for any list page. Filters JSON drives the query;
 * columns JSON controls visible fields + order; sort drives ordering.
 * Can be shared with org / department / specific users.
 */
@Entity
@Table(name = "smart_views",
        indexes = @Index(name = "ix_smartview_owner_entity", columnList = "tenant_id,entity_name,owner_user_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class SmartView extends BaseEntity {

    @Column(name = "entity_name", length = 100, nullable = false) private String entityName;
    @Column(name = "name", length = 200, nullable = false) private String name;
    @Column(name = "owner_user_id", nullable = false) private UUID ownerUserId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "filters", columnDefinition = "jsonb")
    private Map<String, Object> filters;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "columns", columnDefinition = "jsonb")
    private List<String> columns;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "sort", columnDefinition = "jsonb")
    private List<Map<String, String>> sort;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", length = 20, nullable = false)
    private Visibility visibility = Visibility.PRIVATE;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "shared_with_user_ids", columnDefinition = "jsonb")
    private List<UUID> sharedWithUserIds;

    @Column(name = "is_default") private Boolean isDefault;

    public enum Visibility { PRIVATE, SHARED_WITH_USERS, DEPARTMENT, TENANT }
}
