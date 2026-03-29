package com.hrms.social.entity;

import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Post extends BaseEntity {

    public enum Visibility {
        PUBLIC, TEAM, PRIVATE
    }

    public enum PostType {
        GENERAL, ACHIEVEMENT, BIRTHDAY, WORK_ANNIVERSARY, ANNOUNCEMENT
    }

    @Column(name = "author_id", nullable = false)
    private UUID authorId;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "media_urls", columnDefinition = "jsonb")
    private List<String> mediaUrls;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false, length = 20)
    private Visibility visibility = Visibility.PUBLIC;

    @Enumerated(EnumType.STRING)
    @Column(name = "post_type", nullable = false, length = 30)
    private PostType postType = PostType.GENERAL;

    @Column(name = "likes_count", nullable = false)
    private int likesCount = 0;

    @Column(name = "comments_count", nullable = false)
    private int commentsCount = 0;

    @Column(name = "pinned", nullable = false)
    private boolean pinned = false;
}
