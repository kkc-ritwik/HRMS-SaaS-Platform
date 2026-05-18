package com.hrms.social.engagement;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/** Hashtag usage counter â€” incremented as posts containing the tag are created. */
@Entity
@Table(name = "social_hashtags", uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id","tag"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Auditable("Hashtag")
@EntityListeners(AuditEntityListener.class)
public class Hashtag extends BaseEntity {

    @Column(name = "tag", length = 100, nullable = false)
    private String tag;

    @Column(name = "usage_count", nullable = false)
    private Long usageCount = 0L;

    @Column(name = "last_used_at")
    private Instant lastUsedAt;
}
