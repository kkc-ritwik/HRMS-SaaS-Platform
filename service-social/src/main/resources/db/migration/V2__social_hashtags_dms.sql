CREATE TABLE IF NOT EXISTS social_hashtags (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id      VARCHAR(100) NOT NULL,
    tag            VARCHAR(100) NOT NULL,
    usage_count    BIGINT NOT NULL DEFAULT 0,
    last_used_at   TIMESTAMPTZ,
    created_by     VARCHAR(100),
    updated_by     VARCHAR(100),
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted        BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE (tenant_id, tag)
);
CREATE INDEX IF NOT EXISTS ix_hashtag_trending ON social_hashtags (tenant_id, usage_count DESC);

CREATE TABLE IF NOT EXISTS social_direct_messages (
    id                       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id                VARCHAR(100) NOT NULL,
    thread_key               VARCHAR(80) NOT NULL,
    sender_id                UUID NOT NULL,
    recipient_id             UUID NOT NULL,
    content                  VARCHAR(5000) NOT NULL,
    attachment_uri           VARCHAR(1000),
    read_at                  TIMESTAMPTZ,
    is_deleted_by_sender     BOOLEAN,
    is_deleted_by_recipient  BOOLEAN,
    created_by               VARCHAR(100),
    updated_by               VARCHAR(100),
    created_at               TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at               TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted                  BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX IF NOT EXISTS ix_dm_thread ON social_direct_messages (tenant_id, thread_key, created_at);
