package com.hrms.helpdesk.entity;


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
@Table(name = "kb_articles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Auditable("KbArticle")
@EntityListeners(AuditEntityListener.class)
public class KbArticle extends BaseEntity {

    public enum ArticleStatus {
        DRAFT, PUBLISHED, ARCHIVED
    }

    @Column(name = "title", nullable = false, length = 300)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "category_id")
    private UUID categoryId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tags", columnDefinition = "jsonb")
    private List<String> tags;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ArticleStatus status = ArticleStatus.DRAFT;

    @Column(name = "views", nullable = false)
    private int views = 0;

    @Column(name = "helpful_votes", nullable = false)
    private int helpfulVotes = 0;

    @Column(name = "author_id")
    private UUID authorId;

    @Column(name = "published_at")
    private Instant publishedAt;
}
