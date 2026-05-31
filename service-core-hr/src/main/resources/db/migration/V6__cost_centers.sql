-- Cost-centre master + split allocations (employee → cost-centre weighted).

CREATE TABLE IF NOT EXISTS cost_centers (
    id                    UUID PRIMARY KEY,
    tenant_id             VARCHAR(100) NOT NULL,
    created_by            VARCHAR(100),
    updated_by            VARCHAR(100),
    created_at            TIMESTAMP WITH TIME ZONE,
    updated_at            TIMESTAMP WITH TIME ZONE,
    is_deleted            BOOLEAN NOT NULL DEFAULT FALSE,

    code                  VARCHAR(50) NOT NULL,
    name                  VARCHAR(200) NOT NULL,
    parent_id             UUID,
    department_id         UUID,
    location_id           UUID,
    legal_entity_id       UUID,
    manager_employee_id   UUID,
    currency              VARCHAR(3),
    annual_budget         NUMERIC(14,2),
    active                BOOLEAN DEFAULT TRUE,
    CONSTRAINT uq_cc_code UNIQUE (tenant_id, code)
);
CREATE INDEX IF NOT EXISTS ix_cc_parent ON cost_centers(tenant_id, parent_id);

CREATE TABLE IF NOT EXISTS cost_center_allocations (
    id                UUID PRIMARY KEY,
    tenant_id         VARCHAR(100) NOT NULL,
    created_by        VARCHAR(100),
    updated_by        VARCHAR(100),
    created_at        TIMESTAMP WITH TIME ZONE,
    updated_at        TIMESTAMP WITH TIME ZONE,
    is_deleted        BOOLEAN NOT NULL DEFAULT FALSE,

    employee_id       UUID NOT NULL,
    cost_center_id    UUID NOT NULL,
    percentage        NUMERIC(5,2) NOT NULL,
    effective_from    DATE NOT NULL,
    effective_to      DATE,
    approved_by       UUID,
    notes             VARCHAR(500)
);
CREATE INDEX IF NOT EXISTS ix_cca_employee ON cost_center_allocations(tenant_id, employee_id);
CREATE INDEX IF NOT EXISTS ix_cca_cc       ON cost_center_allocations(tenant_id, cost_center_id);
