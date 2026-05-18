package com.hrms.lms.ilt;


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

/**
 * Instructor-Led Training (ILT) session â€” scheduled in-person or virtual class.
 * Calendar-aware (start + end in UTC), capacity-managed, with attendee roster +
 * attendance marking at the session-instance level.
 */
@Entity
@Table(name = "lms_training_sessions",
        indexes = @Index(name = "ix_ilt_course_date", columnList = "tenant_id,course_id,starts_at"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class TrainingSession extends BaseEntity {

    @Column(name = "course_id", nullable = false) private UUID courseId;
    @Column(name = "title", nullable = false, length = 200) private String title;
    @Column(name = "description", length = 2000) private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode", length = 20, nullable = false)
    private Mode mode;

    @Column(name = "starts_at", nullable = false) private Instant startsAt;
    @Column(name = "ends_at", nullable = false) private Instant endsAt;
    @Column(name = "time_zone", length = 50) private String timeZone;

    /** For in-person: room/address. For virtual: leave null, use meetingUrl. */
    @Column(name = "location", length = 500) private String location;
    @Column(name = "meeting_url", length = 1000) private String meetingUrl;

    @Column(name = "instructor_employee_id") private UUID instructorEmployeeId;
    @Column(name = "instructor_external_name", length = 200) private String instructorExternalName;

    @Column(name = "capacity") private Integer capacity;
    @Column(name = "registered_count", nullable = false) private int registeredCount = 0;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "materials_uris", columnDefinition = "jsonb")
    private List<String> materialsUris;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private Status status = Status.SCHEDULED;

    public enum Mode { IN_PERSON, VIRTUAL, HYBRID }
    public enum Status { DRAFT, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED }
}
