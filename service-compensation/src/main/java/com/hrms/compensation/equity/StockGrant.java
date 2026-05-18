package com.hrms.compensation.equity;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.events.annotation.PublishEvents;
import com.hrms.events.listener.EntityLifecyclePublisher;
import com.hrms.events.model.Topics;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Equity grant â€” ESOP / RSU / Performance share. Vesting schedule is computed at runtime. */
@Entity
@Table(name = "stock_grants",
        uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id","grant_number"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@PublishEvents(topic = Topics.PAYROLL, namespace = "compensation.stock_grant")
@EntityListeners(EntityLifecyclePublisher.class)
public class StockGrant extends BaseEntity {

    @Column(name = "grant_number", length = 50, nullable = false)
    private String grantNumber;

    @Column(name = "employee_id", nullable = false) private UUID employeeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "grant_type", length = 20, nullable = false)
    private GrantType grantType;

    @Column(name = "total_units", nullable = false) private Integer totalUnits;
    @Column(name = "strike_price", precision = 14, scale = 4) private BigDecimal strikePrice;
    @Column(name = "fair_market_value_at_grant", precision = 14, scale = 4) private BigDecimal fmvAtGrant;

    @Column(name = "grant_date", nullable = false) private LocalDate grantDate;
    @Column(name = "vesting_start_date", nullable = false) private LocalDate vestingStartDate;

    /** "FOUR_YEAR_MONTHLY_WITH_ONE_YEAR_CLIFF" (standard) / "FOUR_YEAR_QUARTERLY" / "CUSTOM" */
    @Column(name = "vesting_schedule_code", length = 50, nullable = false) private String vestingScheduleCode;
    @Column(name = "cliff_months") private Integer cliffMonths;
    @Column(name = "total_vesting_months") private Integer totalVestingMonths;
    @Column(name = "vest_frequency_months") private Integer vestFrequencyMonths;

    @Column(name = "exercise_window_days_after_termination") private Integer exerciseWindowDays;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private Status status = Status.GRANTED;

    public enum GrantType { ESOP, RSU, PSU, SAR, PHANTOM }
    public enum Status { DRAFT, GRANTED, ACTIVE, EXERCISED, EXPIRED, FORFEITED, CANCELLED }
}
