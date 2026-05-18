-- Shared audit table. Each service applies this migration by referencing it from its own Flyway path
-- (e.g. spring.flyway.locations=classpath:db/migration,classpath:/db/migration/shared).
-- Alternatively, copy into the service's own migration with a unique version prefix.

CREATE TABLE IF NOT EXISTS audit_logs (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       VARCHAR(100),
    entity_name     VARCHAR(100) NOT NULL,
    entity_id       VARCHAR(100),
    action          VARCHAR(20)  NOT NULL,
    actor_id        VARCHAR(100),
    actor_email     VARCHAR(255),
    ip_address      VARCHAR(64),
    user_agent      VARCHAR(500),
    request_id      VARCHAR(100),
    before_value    JSONB,
    after_value     JSONB,
    changed_fields  JSONB,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS ix_audit_tenant_entity ON audit_logs (tenant_id, entity_name, entity_id);
CREATE INDEX IF NOT EXISTS ix_audit_actor         ON audit_logs (actor_id);
CREATE INDEX IF NOT EXISTS ix_audit_created_at    ON audit_logs (created_at DESC);
