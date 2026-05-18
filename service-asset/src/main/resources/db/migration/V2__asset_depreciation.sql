CREATE TABLE IF NOT EXISTS asset_depreciation_entries (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id             VARCHAR(100) NOT NULL,
    asset_id              UUID NOT NULL,
    period_year           INTEGER NOT NULL,
    period_month          INTEGER NOT NULL,
    depreciation_amount   NUMERIC(14,2) NOT NULL,
    book_value_after      NUMERIC(14,2),
    method                VARCHAR(30),
    posted_to_gl          BOOLEAN,
    created_by            VARCHAR(100),
    updated_by            VARCHAR(100),
    created_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted               BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE (tenant_id, asset_id, period_year, period_month)
);
