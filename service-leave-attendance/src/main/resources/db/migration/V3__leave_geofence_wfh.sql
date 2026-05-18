CREATE TABLE IF NOT EXISTS geofences (
    id                         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id                  VARCHAR(100) NOT NULL,
    name                       VARCHAR(100) NOT NULL,
    location_id                UUID,
    center_latitude            NUMERIC(10,7) NOT NULL,
    center_longitude           NUMERIC(10,7) NOT NULL,
    radius_meters              INTEGER NOT NULL,
    is_active                  BOOLEAN NOT NULL DEFAULT TRUE,
    applies_to_department_id   UUID,
    created_by                 VARCHAR(100),
    updated_by                 VARCHAR(100),
    created_at                 TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at                 TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted                    BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX IF NOT EXISTS ix_geofence_location ON geofences (location_id);

CREATE TABLE IF NOT EXISTS wfh_requests (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           VARCHAR(100) NOT NULL,
    employee_id         UUID NOT NULL,
    from_date           DATE NOT NULL,
    to_date             DATE NOT NULL,
    duration_days       NUMERIC(6,2),
    reason              VARCHAR(1000),
    status              VARCHAR(30) NOT NULL,
    approver_id         UUID,
    approved_at         TIMESTAMPTZ,
    approver_comments   VARCHAR(500),
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted             BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX IF NOT EXISTS ix_wfh_emp    ON wfh_requests (tenant_id, employee_id);
CREATE INDEX IF NOT EXISTS ix_wfh_status ON wfh_requests (tenant_id, status);

CREATE TABLE IF NOT EXISTS location_holidays (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       VARCHAR(100) NOT NULL,
    location_id     UUID NOT NULL,
    holiday_date    DATE NOT NULL,
    name            VARCHAR(200) NOT NULL,
    is_optional     BOOLEAN,
    description     VARCHAR(500),
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted         BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE (tenant_id, location_id, holiday_date)
);
