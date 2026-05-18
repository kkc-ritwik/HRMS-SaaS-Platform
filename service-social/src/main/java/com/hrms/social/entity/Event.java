package com.hrms.social.entity;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Auditable("Event")
@EntityListeners(AuditEntityListener.class)
public class Event extends BaseEntity {

    public enum EventType {
        MEETING, CELEBRATION, TRAINING, SOCIAL
    }

    @Column(name = "title", nullable = false, length = 300)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "organizer_id", nullable = false)
    private UUID organizerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 30)
    private EventType eventType = EventType.SOCIAL;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time")
    private Instant endTime;

    @Column(name = "location", length = 300)
    private String location;

    @Column(name = "virtual_link", length = 500)
    private String virtualLink;

    @Column(name = "rsvp_count", nullable = false)
    private int rsvpCount = 0;

    @Column(name = "group_id")
    private UUID groupId;
}
