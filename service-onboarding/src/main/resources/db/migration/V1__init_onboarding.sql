-- ============================================================
-- Onboarding Service Schema
-- ============================================================

-- onboarding_templates
CREATE TABLE IF NOT EXISTS onboarding_templates (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       VARCHAR(50) NOT NULL,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_deleted      BOOLEAN     NOT NULL DEFAULT FALSE,

    name            VARCHAR(200) NOT NULL,
    description     TEXT,
    role_id         UUID,
    department_id   UUID,
    active          BOOLEAN     NOT NULL DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_onboarding_templates_tenant_id ON onboarding_templates(tenant_id);

-- onboarding_tasks
CREATE TABLE IF NOT EXISTS onboarding_tasks (
    id                  UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           VARCHAR(50) NOT NULL,
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_deleted          BOOLEAN     NOT NULL DEFAULT FALSE,

    template_id         UUID REFERENCES onboarding_templates(id) ON DELETE SET NULL,
    employee_id         UUID,
    title               VARCHAR(200) NOT NULL,
    description         TEXT,
    task_type           VARCHAR(50)  NOT NULL DEFAULT 'OTHER',
    assigned_to_role    VARCHAR(100),
    due_days_offset     INT          NOT NULL DEFAULT 0,
    required            BOOLEAN      NOT NULL DEFAULT TRUE,
    status              VARCHAR(30)  NOT NULL DEFAULT 'PENDING',
    completed_at        TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_onboarding_tasks_tenant_id    ON onboarding_tasks(tenant_id);
CREATE INDEX IF NOT EXISTS idx_onboarding_tasks_template_id  ON onboarding_tasks(template_id);
CREATE INDEX IF NOT EXISTS idx_onboarding_tasks_employee_id  ON onboarding_tasks(employee_id);

-- onboarding_documents
CREATE TABLE IF NOT EXISTS onboarding_documents (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       VARCHAR(50) NOT NULL,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_deleted      BOOLEAN     NOT NULL DEFAULT FALSE,

    employee_id     UUID        NOT NULL,
    document_name   VARCHAR(200) NOT NULL,
    document_type   VARCHAR(100),
    file_url        VARCHAR(500),
    status          VARCHAR(30)  NOT NULL DEFAULT 'PENDING',
    remarks         TEXT
);

CREATE INDEX IF NOT EXISTS idx_onboarding_documents_tenant_id   ON onboarding_documents(tenant_id);
CREATE INDEX IF NOT EXISTS idx_onboarding_documents_employee_id ON onboarding_documents(employee_id);

-- buddy_assignments
CREATE TABLE IF NOT EXISTS buddy_assignments (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       VARCHAR(50) NOT NULL,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_deleted      BOOLEAN     NOT NULL DEFAULT FALSE,

    employee_id     UUID        NOT NULL,
    buddy_id        UUID        NOT NULL,
    start_date      DATE,
    end_date        DATE,
    status          VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',
    notes           TEXT
);

CREATE INDEX IF NOT EXISTS idx_buddy_assignments_tenant_id   ON buddy_assignments(tenant_id);
CREATE INDEX IF NOT EXISTS idx_buddy_assignments_employee_id ON buddy_assignments(employee_id);

-- probation_reviews
CREATE TABLE IF NOT EXISTS probation_reviews (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       VARCHAR(50) NOT NULL,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_deleted      BOOLEAN     NOT NULL DEFAULT FALSE,

    employee_id     UUID        NOT NULL,
    reviewer_id     UUID,
    review_date     DATE,
    period_months   INT         NOT NULL DEFAULT 3,
    status          VARCHAR(30)  NOT NULL DEFAULT 'SCHEDULED',
    overall_rating  VARCHAR(50),
    comments        TEXT,
    extended_until  DATE
);

CREATE INDEX IF NOT EXISTS idx_probation_reviews_tenant_id   ON probation_reviews(tenant_id);
CREATE INDEX IF NOT EXISTS idx_probation_reviews_employee_id ON probation_reviews(employee_id);
