package com.hrms.social.entity;

import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "groups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SocialGroup extends BaseEntity {

    public enum GroupType {
        PUBLIC, PRIVATE, SECRET
    }

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "group_type", nullable = false, length = 20)
    private GroupType groupType = GroupType.PUBLIC;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    @Column(name = "member_count", nullable = false)
    private int memberCount = 0;
}
