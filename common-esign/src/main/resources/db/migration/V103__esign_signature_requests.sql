CREATE TABLE IF NOT EXISTS signature_requests (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id               VARCHAR(100) NOT NULL,
    subject_type            VARCHAR(50) NOT NULL,
    subject_id              UUID NOT NULL,
    document_storage_uri    VARCHAR(1000) NOT NULL,
    title                   VARCHAR(300) NOT NULL,
    provider                VARCHAR(30) NOT NULL,
    status                  VARCHAR(30) NOT NULL,
    external_envelope_id    VARCHAR(200),
    signed_document_uri     VARCHAR(1000),
    signers                 JSONB NOT NULL,
    expires_at              TIMESTAMPTZ,
    sent_at                 TIMESTAMPTZ,
    completed_at            TIMESTAMPTZ,
    cancelled_at            TIMESTAMPTZ,
    last_event              VARCHAR(500),
    created_by              VARCHAR(100),
    updated_by              VARCHAR(100),
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted                 BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX IF NOT EXISTS ix_sigreq_status  ON signature_requests (status);
CREATE INDEX IF NOT EXISTS ix_sigreq_subject ON signature_requests (subject_type, subject_id);
