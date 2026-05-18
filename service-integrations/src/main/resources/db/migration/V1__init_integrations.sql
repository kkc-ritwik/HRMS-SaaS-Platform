CREATE TABLE webhook_subscriptions (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id    VARCHAR(100) NOT NULL,
    name         VARCHAR(200) NOT NULL,
    target_url   VARCHAR(1000) NOT NULL,
    secret       VARCHAR(200),
    event_types  JSONB,
    is_active    BOOLEAN,
    retry_max    INTEGER NOT NULL,
    timeout_ms   INTEGER NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE webhook_deliveries (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id         VARCHAR(100) NOT NULL,
    subscription_id   UUID NOT NULL REFERENCES webhook_subscriptions(id) ON DELETE CASCADE,
    event_id          VARCHAR(100) NOT NULL,
    event_type        VARCHAR(100) NOT NULL,
    payload           TEXT NOT NULL,
    status            VARCHAR(30),
    attempts          INTEGER NOT NULL DEFAULT 0,
    next_attempt_at   TIMESTAMPTZ,
    last_response_code INTEGER,
    last_error        VARCHAR(2000),
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    delivered_at      TIMESTAMPTZ
);
CREATE INDEX ix_delivery_sub    ON webhook_deliveries (subscription_id);
CREATE INDEX ix_delivery_status ON webhook_deliveries (status, next_attempt_at);
