CREATE TABLE IF NOT EXISTS document_versions (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id         VARCHAR(100) NOT NULL,
    document_id       UUID NOT NULL,
    version_number    INTEGER NOT NULL,
    storage_uri       VARCHAR(1000) NOT NULL,
    size_bytes        BIGINT,
    checksum_sha256   VARCHAR(64),
    content_type      VARCHAR(100),
    change_summary    VARCHAR(1000),
    uploaded_by       UUID,
    is_current        BOOLEAN NOT NULL DEFAULT FALSE,
    created_by        VARCHAR(100),
    updated_by        VARCHAR(100),
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted           BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE (tenant_id, document_id, version_number)
);
CREATE INDEX IF NOT EXISTS ix_docver_doc ON document_versions (document_id);
