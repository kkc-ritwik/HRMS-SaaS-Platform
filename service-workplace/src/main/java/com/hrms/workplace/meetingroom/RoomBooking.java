package com.hrms.workplace.meetingroom;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "room_bookings",
        indexes = {
            @Index(name = "ix_booking_room_time", columnList = "room_id,starts_at"),
            @Index(name = "ix_booking_organizer", columnList = "tenant_id,organizer_employee_id")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class RoomBooking extends BaseEntity {

    @Column(name = "room_id", nullable = false) private UUID roomId;
    @Column(name = "organizer_employee_id", nullable = false) private UUID organizerEmployeeId;
    @Column(name = "title", length = 200, nullable = false) private String title;
    @Column(name = "description", length = 2000) private String description;
    @Column(name = "starts_at", nullable = false) private Instant startsAt;
    @Column(name = "ends_at", nullable = false) private Instant endsAt;
    @Column(name = "expected_attendee_count") private Integer expectedAttendeeCount;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "attendee_employee_ids", columnDefinition = "jsonb")
    private List<UUID> attendeeEmployeeIds;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "external_attendee_emails", columnDefinition = "jsonb")
    private List<String> externalAttendeeEmails;

    @Column(name = "is_recurring") private Boolean recurring;
    @Column(name = "recurrence_rule", length = 500) private String recurrenceRule;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private Status status = Status.CONFIRMED;

    @Column(name = "cancellation_reason", length = 500) private String cancellationReason;

    public enum Status { TENTATIVE, CONFIRMED, CANCELLED, COMPLETED }
}
