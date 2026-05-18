package com.hrms.workplace.meetingroom;


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

/** Bookable meeting / conference room within a location. */
@Entity
@Table(name = "meeting_rooms")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Auditable("MeetingRoom")
@EntityListeners(AuditEntityListener.class)
public class MeetingRoom extends BaseEntity {

    @Column(name = "name", length = 200, nullable = false) private String name;
    @Column(name = "code", length = 50, nullable = false) private String code;
    @Column(name = "location_id", nullable = false) private UUID locationId;
    @Column(name = "floor", length = 50) private String floor;
    @Column(name = "capacity", nullable = false) private Integer capacity;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "amenities", columnDefinition = "jsonb")
    private List<String> amenities;        // ["projector","whiteboard","video-conf","phone","wheelchair-access"]

    @Column(name = "is_bookable") private Boolean bookable;
    @Column(name = "requires_approval") private Boolean requiresApproval;
    @Column(name = "approver_employee_id") private UUID approverEmployeeId;
    @Column(name = "photo_uri", length = 1000) private String photoUri;
    @Column(name = "is_active", nullable = false) private boolean active = true;
}
