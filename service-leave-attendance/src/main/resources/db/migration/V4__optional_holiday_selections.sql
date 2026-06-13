-- Employee selections of optional (restricted) holidays, subject to an annual quota.
CREATE TABLE IF NOT EXISTS optional_holiday_selections (
    id           UUID PRIMARY KEY,
    tenant_id    VARCHAR(100) NOT NULL,
    created_by   VARCHAR(100),
    updated_by   VARCHAR(100),
    created_at   TIMESTAMPTZ,
    updated_at   TIMESTAMPTZ,
    is_deleted   BOOLEAN NOT NULL DEFAULT FALSE,
    employee_id  UUID NOT NULL,
    holiday_id   UUID NOT NULL,
    year         INTEGER NOT NULL
);
CREATE INDEX IF NOT EXISTS ix_opt_holiday_emp ON optional_holiday_selections (tenant_id, employee_id, year);
CREATE UNIQUE INDEX IF NOT EXISTS uq_opt_holiday_pick ON optional_holiday_selections (tenant_id, employee_id, holiday_id);
