CREATE TABLE IF NOT EXISTS calibration_sessions (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id             VARCHAR(100) NOT NULL,
    cycle_id              UUID NOT NULL,
    name                  VARCHAR(200) NOT NULL,
    department_id         UUID,
    facilitator_id        UUID NOT NULL,
    participant_ids       JSONB,
    scheduled_at          TIMESTAMPTZ,
    completed_at          TIMESTAMPTZ,
    status                VARCHAR(30) NOT NULL,
    target_distribution   JSONB,
    notes                 VARCHAR(4000),
    created_by            VARCHAR(100),
    updated_by            VARCHAR(100),
    created_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted               BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS calibration_adjustments (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id        VARCHAR(100) NOT NULL,
    session_id       UUID NOT NULL REFERENCES calibration_sessions(id) ON DELETE CASCADE,
    review_id        UUID NOT NULL,
    employee_id      UUID NOT NULL,
    original_rating  NUMERIC(4,2),
    adjusted_rating  NUMERIC(4,2) NOT NULL,
    justification    VARCHAR(2000),
    adjusted_by      UUID,
    created_by       VARCHAR(100),
    updated_by       VARCHAR(100),
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted          BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE (session_id, review_id)
);

CREATE TABLE IF NOT EXISTS succession_plans (
    id                       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id                VARCHAR(100) NOT NULL,
    role_id                  UUID NOT NULL,
    incumbent_employee_id    UUID,
    criticality              VARCHAR(20),
    risk_of_loss             VARCHAR(20),
    impact_of_loss           VARCHAR(20),
    candidates               JSONB,
    notes                    VARCHAR(4000),
    created_by               VARCHAR(100),
    updated_by               VARCHAR(100),
    created_at               TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at               TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted                  BOOLEAN NOT NULL DEFAULT FALSE
);
