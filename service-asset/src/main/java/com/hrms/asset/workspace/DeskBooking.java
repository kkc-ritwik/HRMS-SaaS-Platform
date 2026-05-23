package com.hrms.asset.workspace;

import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Single-day hot-desk booking. Enforces unique-per-desk-per-date via app logic + index. */
@Entity
@Table(name = "workspace_desk_bookings",
        uniqueConstraints = @UniqueConstraint(name = "uq_desk_booking_day",
                columnNames = {"desk_id", "booking_date"}),
        indexes = {
                @Index(name = "ix_db_emp_date", columnList = "tenant_id,employee_id,booking_date"),
                @Index(name = "ix_db_date", columnList = "tenant_id,booking_date")
        })
@Auditable("DeskBooking")
@EntityListeners(AuditEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class DeskBooking extends BaseEntity {

    @Column(name = "desk_id", nullable = false) private UUID deskId;
    @Column(name = "employee_id", nullable = false) private UUID employeeId;
    @Column(name = "booking_date", nullable = false) private LocalDate bookingDate;
    @Column(name = "start_time_offset") private Integer startTimeOffsetMinutes; // null = full day
    @Column(name = "end_time_offset") private Integer endTimeOffsetMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private Status status = Status.BOOKED;

    @Column(name = "checked_in_at") private OffsetDateTime checkedInAt;
    @Column(name = "checked_out_at") private OffsetDateTime checkedOutAt;
    @Column(name = "qr_token", length = 100) private String qrToken;
    @Column(name = "purpose", length = 200) private String purpose;

    public enum Status { BOOKED, CHECKED_IN, COMPLETED, CANCELLED, NO_SHOW }
}
