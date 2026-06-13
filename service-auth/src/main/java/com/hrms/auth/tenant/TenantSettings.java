package com.hrms.auth.tenant;

import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashMap;
import java.util.Map;

/** Per-tenant branding (logo, colours) + feature-flag overrides. One row per tenant. */
@Entity
@Table(name = "tenant_settings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class TenantSettings extends BaseEntity {

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "branding", columnDefinition = "jsonb")
    private Map<String, Object> branding = new HashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "feature_flags", columnDefinition = "jsonb")
    private Map<String, Object> featureFlags = new HashMap<>();
}
