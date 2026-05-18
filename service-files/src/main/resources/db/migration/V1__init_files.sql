CREATE TABLE file_folders (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id   VARCHAR(100) NOT NULL,
    owner_id    UUID NOT NULL,
    parent_id   UUID,
    name        VARCHAR(200) NOT NULL,
    is_shared   BOOLEAN,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE file_records (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       VARCHAR(100) NOT NULL,
    owner_id        UUID NOT NULL,
    folder_id       UUID,
    filename        VARCHAR(500) NOT NULL,
    content_type    VARCHAR(100),
    size_bytes      BIGINT,
    storage_uri     VARCHAR(1000) NOT NULL,
    checksum_sha256 VARCHAR(64),
    is_public       BOOLEAN,
    description     VARCHAR(1000),
    tags            VARCHAR(500),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX ix_file_owner  ON file_records (tenant_id, owner_id);
CREATE INDEX ix_file_folder ON file_records (tenant_id, folder_id);

CREATE TABLE file_shares (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id             VARCHAR(100) NOT NULL,
    file_id               UUID NOT NULL REFERENCES file_records(id) ON DELETE CASCADE,
    shared_with_user_id   UUID,
    shared_with_group_id  UUID,
    permission            VARCHAR(20),
    share_token           VARCHAR(100) UNIQUE,
    expires_at            TIMESTAMPTZ,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX ix_share_file ON file_shares (file_id);
