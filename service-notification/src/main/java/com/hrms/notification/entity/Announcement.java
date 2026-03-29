package com.hrms.notification.entity;

import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "announcements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Announcement extends BaseEntity {

    public enum AudienceType {
        ALL, DEPARTMENT, ROLE, EMPLOYEE
    }

    public enum Priority {
        LOW, MEDIUM, HIGH, URGENT
    }

    @Column(name = "title", nullable = false, length = 300)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "audience_type", nullable = false, length = 30)
    private AudienceType audienceType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "target_ids", columnDefinition = "jsonb")
    private List<String> targetIds;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    private Priority priority;

    @Column(name = "start_date")
    private Instant startDate;

    @Column(name = "end_date")
    private Instant endDate;

    @Column(name = "author_id")
    private UUID authorId;

    @Column(name = "pinned", nullable = false)
    private boolean pinned = false;
}
