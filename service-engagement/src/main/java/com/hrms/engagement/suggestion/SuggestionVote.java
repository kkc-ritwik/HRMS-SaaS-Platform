package com.hrms.engagement.suggestion;

import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * One vote per employee per suggestion. employee_id stored as raw UUID — the suggestion's
 * anonymity guarantee covers the suggestion content, not the act of voting.
 */
@Entity
@Table(name = "engagement_suggestion_votes",
        uniqueConstraints = @UniqueConstraint(name = "uq_sugg_vote", columnNames = {"suggestion_id","voter_employee_id"}),
        indexes = @Index(name = "ix_vote_sugg", columnList = "suggestion_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class SuggestionVote extends BaseEntity {
    @Column(name = "suggestion_id", nullable = false) private UUID suggestionId;
    @Column(name = "voter_employee_id", nullable = false) private UUID voterEmployeeId;
    @Column(name = "direction", length = 4, nullable = false) private String direction; // UP / DOWN
}
