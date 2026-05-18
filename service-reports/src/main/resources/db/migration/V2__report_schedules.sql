CREATE TABLE IF NOT EXISTS report_schedules (
    id                       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id                VARCHAR(100) NOT NULL,
    report_definition_id     UUID NOT NULL,
    name                     VARCHAR(200) NOT NULL,
    sql_query                TEXT NOT NULL,
    bindings                 JSONB,
    max_rows                 INTEGER NOT NULL DEFAULT 10000,
    format                   VARCHAR(10) NOT NULL,
    recipients               JSONB NOT NULL,
    cron_expression          VARCHAR(80) NOT NULL,
    time_zone                VARCHAR(50),
    next_fire_at             TIMESTAMPTZ,
    last_fired_at            TIMESTAMPTZ,
    last_error               VARCHAR(2000),
    consecutive_failures     INTEGER,
    is_active                BOOLEAN NOT NULL DEFAULT TRUE,
    created_by               VARCHAR(100),
    updated_by               VARCHAR(100),
    created_at               TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at               TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted                  BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX IF NOT EXISTS ix_report_sched_due ON report_schedules (next_fire_at);
