-- ============================================================
-- Compliance Service — Initial Schema
-- ============================================================

-- compliance_items
CREATE TABLE compliance_items (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       VARCHAR(50)  NOT NULL,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    is_deleted      BOOLEAN      NOT NULL DEFAULT FALSE,

    title           VARCHAR(300) NOT NULL,
    description     TEXT,
    category        VARCHAR(100),
    regulation      VARCHAR(200),
    jurisdiction    VARCHAR(200),
    due_date        DATE,
    recurring       BOOLEAN      NOT NULL DEFAULT FALSE,
    recurrence_period VARCHAR(50),
    status          VARCHAR(30)  NOT NULL DEFAULT 'PENDING',
    owner_id        UUID,
    notes           TEXT
);

CREATE INDEX idx_compliance_items_tenant_id ON compliance_items (tenant_id);

-- compliance_tasks
CREATE TABLE compliance_tasks (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id            VARCHAR(50)  NOT NULL,
    created_by           VARCHAR(100),
    updated_by           VARCHAR(100),
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    is_deleted           BOOLEAN      NOT NULL DEFAULT FALSE,

    compliance_item_id   UUID         NOT NULL REFERENCES compliance_items (id) ON DELETE CASCADE,
    title                VARCHAR(300) NOT NULL,
    description          TEXT,
    assignee_id          UUID,
    due_date             DATE,
    status               VARCHAR(30)  NOT NULL DEFAULT 'PENDING',
    completed_at         TIMESTAMPTZ,
    evidence_url         VARCHAR(500),
    notes                TEXT
);

CREATE INDEX idx_compliance_tasks_item_id   ON compliance_tasks (compliance_item_id);
CREATE INDEX idx_compliance_tasks_tenant_id ON compliance_tasks (tenant_id);

-- licenses
CREATE TABLE licenses (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id             VARCHAR(50)  NOT NULL,
    created_by            VARCHAR(100),
    updated_by            VARCHAR(100),
    created_at            TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    is_deleted            BOOLEAN      NOT NULL DEFAULT FALSE,

    employee_id           UUID         NOT NULL,
    license_type          VARCHAR(100) NOT NULL,
    license_number        VARCHAR(100),
    issuing_authority     VARCHAR(200),
    issue_date            DATE,
    expiry_date           DATE,
    status                VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',
    renewal_reminder_days INT          NOT NULL DEFAULT 30,
    document_url          VARCHAR(500),
    notes                 TEXT
);

CREATE INDEX idx_licenses_tenant_employee ON licenses (tenant_id, employee_id);
CREATE INDEX idx_licenses_tenant_id       ON licenses (tenant_id);
