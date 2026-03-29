-- ============================================================
-- Expense Service — initial schema
-- ============================================================

-- 1. expense_categories
CREATE TABLE expense_categories (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id        VARCHAR(50)     NOT NULL,
    created_by       VARCHAR(100),
    updated_by       VARCHAR(100),
    created_at       TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    is_deleted       BOOLEAN         NOT NULL DEFAULT FALSE,

    name             VARCHAR(150)    NOT NULL,
    code             VARCHAR(50)     NOT NULL,
    description      TEXT,
    max_amount       DECIMAL(15,2),
    requires_receipt BOOLEAN         NOT NULL DEFAULT TRUE,
    active           BOOLEAN         NOT NULL DEFAULT TRUE,

    CONSTRAINT uq_expense_category_code_tenant UNIQUE (code, tenant_id)
);

-- 2. expense_policies
CREATE TABLE expense_policies (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id         VARCHAR(50)     NOT NULL,
    created_by        VARCHAR(100),
    updated_by        VARCHAR(100),
    created_at        TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    is_deleted        BOOLEAN         NOT NULL DEFAULT FALSE,

    name              VARCHAR(200)    NOT NULL,
    description       TEXT,
    category_id       UUID            REFERENCES expense_categories(id) ON DELETE SET NULL,
    employee_level    VARCHAR(100),
    max_amount        DECIMAL(15,2),
    currency          VARCHAR(10)     NOT NULL DEFAULT 'USD',
    approval_required BOOLEAN         NOT NULL DEFAULT TRUE,
    active            BOOLEAN         NOT NULL DEFAULT TRUE
);

-- 3. expense_reports
CREATE TABLE expense_reports (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       VARCHAR(50)     NOT NULL,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    is_deleted      BOOLEAN         NOT NULL DEFAULT FALSE,

    employee_id     UUID            NOT NULL,
    title           VARCHAR(300)    NOT NULL,
    description     TEXT,
    period_start    DATE,
    period_end      DATE,
    total_amount    DECIMAL(15,2)   NOT NULL DEFAULT 0,
    currency        VARCHAR(10)     NOT NULL DEFAULT 'USD',
    status          VARCHAR(30)     NOT NULL DEFAULT 'DRAFT'
                        CHECK (status IN ('DRAFT','SUBMITTED','APPROVED','REJECTED','PAID')),
    submitted_at    TIMESTAMPTZ,
    approved_by     UUID,
    approved_at     TIMESTAMPTZ,
    rejected_reason TEXT,
    paid_at         TIMESTAMPTZ
);

CREATE INDEX idx_expense_reports_tenant_employee ON expense_reports (tenant_id, employee_id);

-- 4. expense_items
CREATE TABLE expense_items (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id    VARCHAR(50)     NOT NULL,
    created_by   VARCHAR(100),
    updated_by   VARCHAR(100),
    created_at   TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    is_deleted   BOOLEAN         NOT NULL DEFAULT FALSE,

    report_id    UUID            NOT NULL REFERENCES expense_reports(id) ON DELETE CASCADE,
    category_id  UUID            REFERENCES expense_categories(id) ON DELETE SET NULL,
    description  VARCHAR(500)    NOT NULL,
    amount       DECIMAL(15,2)   NOT NULL,
    currency     VARCHAR(10)     NOT NULL DEFAULT 'USD',
    expense_date DATE            NOT NULL,
    receipt_url  VARCHAR(500),
    merchant     VARCHAR(200),
    notes        TEXT
);

CREATE INDEX idx_expense_items_report ON expense_items (report_id);

-- 5. advances
CREATE TABLE advances (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id      VARCHAR(50)     NOT NULL,
    created_by     VARCHAR(100),
    updated_by     VARCHAR(100),
    created_at     TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    is_deleted     BOOLEAN         NOT NULL DEFAULT FALSE,

    employee_id    UUID            NOT NULL,
    amount         DECIMAL(15,2)   NOT NULL,
    currency       VARCHAR(10)     NOT NULL DEFAULT 'USD',
    purpose        TEXT,
    status         VARCHAR(30)     NOT NULL DEFAULT 'REQUESTED'
                       CHECK (status IN ('REQUESTED','APPROVED','DISBURSED','SETTLED','REJECTED')),
    requested_at   TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    approved_by    UUID,
    approved_at    TIMESTAMPTZ,
    disbursed_at   TIMESTAMPTZ,
    due_date       DATE,
    settled_amount DECIMAL(15,2)
);

CREATE INDEX idx_advances_tenant_employee ON advances (tenant_id, employee_id);
