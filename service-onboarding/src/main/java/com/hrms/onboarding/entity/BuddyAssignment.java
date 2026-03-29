package com.hrms.onboarding.entity;

import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "buddy_assignments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuddyAssignment extends BaseEntity {

    public enum AssignmentStatus {
        ACTIVE, COMPLETED, CANCELLED
    }

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "buddy_id", nullable = false)
    private UUID buddyId;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private AssignmentStatus status = AssignmentStatus.ACTIVE;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
