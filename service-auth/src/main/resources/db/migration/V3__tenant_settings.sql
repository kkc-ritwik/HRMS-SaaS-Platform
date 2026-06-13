-- Per-tenant branding (logo, colours) and feature-flag overrides.
CREATE TABLE IF NOT EXISTS tenant_settings (
    id             UUID PRIMARY KEY,
    tenant_id      VARCHAR(100) NOT NULL,
    created_by     VARCHAR(100),
    updated_by     VARCHAR(100),
    created_at     TIMESTAMPTZ,
    updated_at     TIMESTAMPTZ,
    is_deleted     BOOLEAN NOT NULL DEFAULT FALSE,
    branding       JSONB NOT NULL DEFAULT '{}',
    feature_flags  JSONB NOT NULL DEFAULT '{}'
);
CREATE UNIQUE INDEX IF NOT EXISTS uq_tenant_settings_tenant ON tenant_settings (tenant_id);
