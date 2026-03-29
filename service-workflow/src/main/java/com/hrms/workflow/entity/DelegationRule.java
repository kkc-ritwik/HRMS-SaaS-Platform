package com.hrms.workflow.entity;

import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "delegation_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DelegationRule extends BaseEntity {

    public enum DelegationScope {
        ALL, SPECIFIC
    }

    @Column(name = "delegator_id", nullable = false)
    private UUID delegatorId;

    @Column(name = "delegate_id", nullable = false)
    private UUID delegateId;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope", nullable = false, length = 30)
    private DelegationScope scope = DelegationScope.ALL;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "entity_types", columnDefinition = "jsonb")
    private List<String> entityTypes;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "reason", length = 300)
    private String reason;
}
