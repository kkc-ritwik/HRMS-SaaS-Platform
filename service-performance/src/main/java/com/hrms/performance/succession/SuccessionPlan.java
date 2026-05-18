package com.hrms.performance.succession;


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

/**
 * Succession plan for a critical role / position. Identifies internal candidates
 * with readiness levels (Ready Now / 1-2y / 3+y), plus optional external benchmarks.
 */
@Entity
@Table(name = "succession_plans")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Auditable("SuccessionPlan")
@EntityListeners(AuditEntityListener.class)
public class SuccessionPlan extends BaseEntity {

    /** The critical role being planned for (typically a Designation UUID). */
    @Column(name = "role_id", nullable = false)
    private UUID roleId;

    /** Current incumbent (if any). */
    @Column(name = "incumbent_employee_id")
    private UUID incumbentEmployeeId;

    @Column(name = "criticality", length = 20)
    private String criticality;          // LOW / MEDIUM / HIGH / MISSION_CRITICAL

    @Column(name = "risk_of_loss", length = 20)
    private String riskOfLoss;           // LOW / MEDIUM / HIGH

    @Column(name = "impact_of_loss", length = 20)
    private String impactOfLoss;         // LOW / MEDIUM / HIGH

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "candidates", columnDefinition = "jsonb")
    private List<SuccessorCandidate> candidates;

    @Column(name = "notes", length = 4000)
    private String notes;

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class SuccessorCandidate {
        private UUID employeeId;
        private String readiness;        // READY_NOW / READY_1_2_YEARS / READY_3_PLUS_YEARS
        private String developmentNotes;
    }
}
