package com.hrms.lms.ilt;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "lms_session_attendees",
        uniqueConstraints = @UniqueConstraint(columnNames = {"session_id","employee_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class SessionAttendee extends BaseEntity {

    @Column(name = "session_id", nullable = false) private UUID sessionId;
    @Column(name = "employee_id", nullable = false) private UUID employeeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "registration_status", length = 30, nullable = false)
    private RegStatus registrationStatus = RegStatus.REGISTERED;

    @Column(name = "registered_at") private Instant registeredAt;
    @Column(name = "checked_in_at") private Instant checkedInAt;
    @Column(name = "completed") private Boolean completed;
    @Column(name = "completion_score", precision = 5, scale = 2) private java.math.BigDecimal completionScore;
    @Column(name = "feedback_rating") private Integer feedbackRating;
    @Column(name = "feedback_comment", length = 2000) private String feedbackComment;

    public enum RegStatus { REGISTERED, WAITLISTED, ATTENDED, NO_SHOW, CANCELLED }
}
