package com.hrms.asset.entity;

import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "asset_assignments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class AssetAssignment extends BaseEntity {

    public enum AssignmentStatus {
        ACTIVE, RETURNED, LOST
    }

    @Column(name = "asset_id", nullable = false)
    private UUID assetId;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "assigned_by")
    private UUID assignedBy;

    @Column(name = "assigned_date", nullable = false)
    private LocalDate assignedDate;

    @Column(name = "expected_return_date")
    private LocalDate expectedReturnDate;

    @Column(name = "actual_return_date")
    private LocalDate actualReturnDate;

    @Column(name = "condition_at_assignment", length = 100)
    private String conditionAtAssignment;

    @Column(name = "condition_at_return", length = 100)
    private String conditionAtReturn;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private AssignmentStatus status = AssignmentStatus.ACTIVE;
}
