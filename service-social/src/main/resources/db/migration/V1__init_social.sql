-- ============================================================
-- Social Service Schema
-- ============================================================

-- posts
CREATE TABLE posts (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id        VARCHAR(50)  NOT NULL,
    created_by       VARCHAR(100),
    updated_by       VARCHAR(100),
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    is_deleted       BOOLEAN      NOT NULL DEFAULT FALSE,
    author_id        UUID         NOT NULL,
    content          TEXT         NOT NULL,
    media_urls       JSONB        DEFAULT '[]',
    visibility       VARCHAR(20)  NOT NULL DEFAULT 'PUBLIC',
    post_type        VARCHAR(30)  NOT NULL DEFAULT 'GENERAL',
    likes_count      INT          NOT NULL DEFAULT 0,
    comments_count   INT          NOT NULL DEFAULT 0,
    pinned           BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_posts_tenant_id     ON posts (tenant_id);
CREATE INDEX idx_posts_tenant_author ON posts (tenant_id, author_id);

-- comments
CREATE TABLE comments (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id         VARCHAR(50)  NOT NULL,
    created_by        VARCHAR(100),
    updated_by        VARCHAR(100),
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    is_deleted        BOOLEAN      NOT NULL DEFAULT FALSE,
    post_id           UUID         NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    author_id         UUID         NOT NULL,
    content           TEXT         NOT NULL,
    parent_comment_id UUID         REFERENCES comments(id) ON DELETE CASCADE,
    likes_count       INT          NOT NULL DEFAULT 0
);

CREATE INDEX idx_comments_tenant_id ON comments (tenant_id);
CREATE INDEX idx_comments_post_id   ON comments (post_id);

-- likes
CREATE TABLE likes (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id     VARCHAR(50)  NOT NULL,
    created_by    VARCHAR(100),
    updated_by    VARCHAR(100),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    is_deleted    BOOLEAN      NOT NULL DEFAULT FALSE,
    post_id       UUID         REFERENCES posts(id) ON DELETE CASCADE,
    comment_id    UUID         REFERENCES comments(id) ON DELETE CASCADE,
    employee_id   UUID         NOT NULL,
    reaction_type VARCHAR(20)  NOT NULL DEFAULT 'LIKE'
);

CREATE UNIQUE INDEX uq_likes_post_employee
    ON likes (tenant_id, post_id, employee_id)
    WHERE post_id IS NOT NULL;

CREATE UNIQUE INDEX uq_likes_comment_employee
    ON likes (tenant_id, comment_id, employee_id)
    WHERE comment_id IS NOT NULL;

CREATE INDEX idx_likes_tenant_id      ON likes (tenant_id);
CREATE INDEX idx_likes_tenant_employee ON likes (tenant_id, employee_id);

-- groups
CREATE TABLE groups (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       VARCHAR(50)  NOT NULL,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    is_deleted      BOOLEAN      NOT NULL DEFAULT FALSE,
    name            VARCHAR(200) NOT NULL,
    description     TEXT,
    group_type      VARCHAR(20)  NOT NULL DEFAULT 'PUBLIC',
    owner_id        UUID         NOT NULL,
    cover_image_url VARCHAR(500),
    member_count    INT          NOT NULL DEFAULT 0
);

CREATE INDEX idx_groups_tenant_id ON groups (tenant_id);

-- group_members
CREATE TABLE group_members (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id   VARCHAR(50)  NOT NULL,
    created_by  VARCHAR(100),
    updated_by  VARCHAR(100),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    is_deleted  BOOLEAN      NOT NULL DEFAULT FALSE,
    group_id    UUID         NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
    employee_id UUID         NOT NULL,
    role        VARCHAR(20)  NOT NULL DEFAULT 'MEMBER',
    joined_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX uq_group_members ON group_members (group_id, employee_id);
CREATE INDEX idx_group_members_tenant_id      ON group_members (tenant_id);
CREATE INDEX idx_group_members_tenant_employee ON group_members (tenant_id, employee_id);

-- events
CREATE TABLE events (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id    VARCHAR(50)  NOT NULL,
    created_by   VARCHAR(100),
    updated_by   VARCHAR(100),
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    is_deleted   BOOLEAN      NOT NULL DEFAULT FALSE,
    title        VARCHAR(300) NOT NULL,
    description  TEXT,
    organizer_id UUID         NOT NULL,
    event_type   VARCHAR(30)  NOT NULL DEFAULT 'SOCIAL',
    start_time   TIMESTAMPTZ  NOT NULL,
    end_time     TIMESTAMPTZ,
    location     VARCHAR(300),
    virtual_link VARCHAR(500),
    rsvp_count   INT          NOT NULL DEFAULT 0,
    group_id     UUID         REFERENCES groups(id) ON DELETE SET NULL
);

CREATE INDEX idx_events_tenant_id ON events (tenant_id);
