-- Tracks filed statutory returns (TDS 24Q/26Q, PT, PF ECR, ESI, etc.).
CREATE TABLE IF NOT EXISTS statutory_returns (
    id                      UUID PRIMARY KEY,
    tenant_id               VARCHAR(100) NOT NULL,
    created_by              VARCHAR(100),
    updated_by              VARCHAR(100),
    created_at              TIMESTAMPTZ,
    updated_at              TIMESTAMPTZ,
    is_deleted              BOOLEAN NOT NULL DEFAULT FALSE,
    return_type             VARCHAR(20)  NOT NULL,
    financial_year          VARCHAR(10)  NOT NULL,
    period                  VARCHAR(20),
    status                  VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',
    storage_uri             VARCHAR(1000),
    acknowledgement_number  VARCHAR(100),
    total_amount            NUMERIC(16,2),
    submitted_at            TIMESTAMPTZ,
    notes                   VARCHAR(2000)
);
CREATE INDEX IF NOT EXISTS ix_statutory_tenant ON statutory_returns (tenant_id, return_type);
CREATE INDEX IF NOT EXISTS ix_statutory_status ON statutory_returns (tenant_id, status);
