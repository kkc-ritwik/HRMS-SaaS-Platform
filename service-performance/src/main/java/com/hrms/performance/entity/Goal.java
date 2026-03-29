package com.hrms.performance.entity;

import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "goals")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Goal extends BaseEntity {

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "manager_id")
    private UUID managerId;

    @Column(name = "cycle_id")
    private UUID cycleId;

    /** Parent Objective UUID when this goal is a Key Result. */
    @Column(name = "parent_goal_id")
    private UUID parentGoalId;

    @Enumerated(EnumType.STRING)
    @Column(name = "goal_type", nullable = false, length = 20)
    private GoalType goalType = GoalType.INDIVIDUAL;

    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "weightage", nullable = false, precision = 5, scale = 2)
    private BigDecimal weightage = BigDecimal.valueOf(100);

    @Column(name = "target_value", precision = 12, scale = 2)
    private BigDecimal targetValue;

    @Column(name = "current_value", precision = 12, scale = 2)
    private BigDecimal currentValue = BigDecimal.ZERO;

    @Column(name = "unit", length = 50)
    private String unit;

    @Column(name = "progress", nullable = false, precision = 5, scale = 2)
    private BigDecimal progress = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private GoalStatus status = GoalStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 10)
    private Priority priority = Priority.MEDIUM;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    // ── Enums ──────────────────────────────────────────────────────────────────

    public enum GoalType { OBJECTIVE, KEY_RESULT, INDIVIDUAL, TEAM, DEPARTMENT, COMPANY }

    public enum GoalStatus { DRAFT, ACTIVE, COMPLETED, CANCELLED, OVERDUE }

    public enum Priority { LOW, MEDIUM, HIGH }
}
