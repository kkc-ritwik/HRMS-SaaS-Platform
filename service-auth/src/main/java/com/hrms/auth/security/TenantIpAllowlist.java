package com.hrms.auth.security;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/** One row per tenant per allowed CIDR. Login/API access from outside the union is denied. */
@Entity
@Table(name = "tenant_ip_allowlist",
        indexes = @Index(name = "ix_iplist_tenant", columnList = "tenant_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class TenantIpAllowlist extends BaseEntity {

    @Column(name = "cidr", length = 50, nullable = false) private String cidr;
    @Column(name = "label", length = 200) private String label;
    @Enumerated(EnumType.STRING)
    @Column(name = "applies_to", length = 30, nullable = false)
    private AppliesTo appliesTo = AppliesTo.LOGIN;
    @Column(name = "is_active", nullable = false) private boolean active = true;

    public enum AppliesTo { LOGIN, API, BOTH }
}
