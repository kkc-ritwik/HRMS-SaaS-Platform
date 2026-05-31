-- Tenant platform tables — only required when hrms.tenant.platform.enabled=true.
-- Each service that opts in must copy this file into its own db/migration folder with
-- the next available version number, OR reference it via spring.flyway.locations.

CREATE TABLE IF NOT EXISTS async_operations (
    id                 UUID PRIMARY KEY,
    tenant_id          VARCHAR(100) NOT NULL,
    actor_id           VARCHAR(100),
    operation_type     VARCHAR(80) NOT NULL,
    description        VARCHAR(500),
    status             VARCHAR(20) NOT NULL DEFAULT 'QUEUED',
    progress_percent   INTEGER DEFAULT 0,
    items_total        BIGINT,
    items_processed    BIGINT DEFAULT 0,
    items_failed       BIGINT DEFAULT 0,
    started_at         TIMESTAMP WITH TIME ZONE,
    finished_at        TIMESTAMP WITH TIME ZONE,
    parameters         JSONB,
    result_metadata    JSONB,
    result_location    VARCHAR(1000),
    error_code         VARCHAR(80),
    error_message      VARCHAR(4000),
    retention_until    TIMESTAMP WITH TIME ZONE,
    version            BIGINT
);
CREATE INDEX IF NOT EXISTS ix_op_tenant_status ON async_operations(tenant_id, status);
CREATE INDEX IF NOT EXISTS ix_op_actor         ON async_operations(tenant_id, actor_id);

CREATE TABLE IF NOT EXISTS feature_flags (
    id                  UUID PRIMARY KEY,
    key                 VARCHAR(100) NOT NULL,
    description         VARCHAR(500),
    enabled             BOOLEAN NOT NULL DEFAULT FALSE,
    enabled_tenants     JSONB,
    rollout_percent     INTEGER,
    variant_payload     JSONB,
    created_at          TIMESTAMP WITH TIME ZONE,
    updated_at          TIMESTAMP WITH TIME ZONE,
    CONSTRAINT uq_flag_key UNIQUE (key)
);
CREATE INDEX IF NOT EXISTS ix_flag_key ON feature_flags(key);

CREATE TABLE IF NOT EXISTS tenant_quotas (
    id                UUID PRIMARY KEY,
    tenant_id         VARCHAR(100) NOT NULL,
    quota_key         VARCHAR(100) NOT NULL,
    soft_limit        BIGINT,
    hard_limit        BIGINT,
    current_usage     BIGINT DEFAULT 0,
    reset_at          TIMESTAMP WITH TIME ZONE,
    reset_interval    VARCHAR(30),
    status            VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    updated_at        TIMESTAMP WITH TIME ZONE,
    version           BIGINT,
    CONSTRAINT uq_quota_key UNIQUE (tenant_id, quota_key)
);
CREATE INDEX IF NOT EXISTS ix_quota_tenant ON tenant_quotas(tenant_id);

CREATE TABLE IF NOT EXISTS saga_instances (
    id              UUID PRIMARY KEY,
    tenant_id       VARCHAR(100) NOT NULL,
    name            VARCHAR(100) NOT NULL,
    subject_id      VARCHAR(100),
    status          VARCHAR(20) NOT NULL DEFAULT 'STARTED',
    current_step    INTEGER DEFAULT 0,
    compensating    BOOLEAN DEFAULT FALSE,
    steps           JSONB,
    context         JSONB,
    trace           JSONB,
    started_at      TIMESTAMP WITH TIME ZONE,
    finished_at     TIMESTAMP WITH TIME ZONE,
    error_message   VARCHAR(2000),
    version         BIGINT
);
CREATE INDEX IF NOT EXISTS ix_saga_status ON saga_instances(tenant_id, status);
CREATE INDEX IF NOT EXISTS ix_saga_name   ON saga_instances(tenant_id, name);
