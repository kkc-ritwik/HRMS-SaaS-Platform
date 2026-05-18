package com.hrms.engagement.entity;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "poll_votes", uniqueConstraints = @UniqueConstraint(columnNames = {"poll_id","voter_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Auditable("PollVote")
@EntityListeners(AuditEntityListener.class)
public class PollVote {
    @Id @GeneratedValue private UUID id;
    @Column(name = "tenant_id", length = 100, nullable = false) private String tenantId;
    @Column(name = "poll_id", nullable = false) private UUID pollId;
    @Column(name = "voter_id") private UUID voterId;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "selected_options", columnDefinition = "jsonb")
    private List<Integer> selectedOptions;
    @Column(name = "voted_at") private OffsetDateTime votedAt;
    @PrePersist void prePersist() { votedAt = OffsetDateTime.now(); }
}
