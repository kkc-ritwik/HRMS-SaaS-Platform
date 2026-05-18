CREATE TABLE IF NOT EXISTS outbox_events (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    topic         VARCHAR(100) NOT NULL,
    event_id      VARCHAR(100) NOT NULL UNIQUE,
    event_type    VARCHAR(100) NOT NULL,
    tenant_id     VARCHAR(100),
    aggregate_id  VARCHAR(100),
    payload       TEXT NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    sent_at       TIMESTAMPTZ,
    attempts      INTEGER NOT NULL DEFAULT 0,
    last_error    VARCHAR(1000)
);
CREATE INDEX IF NOT EXISTS ix_outbox_unsent ON outbox_events (sent_at NULLS FIRST, created_at);
CREATE INDEX IF NOT EXISTS ix_outbox_topic  ON outbox_events (topic);
