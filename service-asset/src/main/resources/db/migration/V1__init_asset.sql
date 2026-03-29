-- ============================================================
-- Asset Service — Initial Schema
-- ============================================================

-- ── asset_categories ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS asset_categories (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           VARCHAR(50)      NOT NULL,
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100),
    created_at          TIMESTAMPTZ      NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ      NOT NULL DEFAULT NOW(),
    is_deleted          BOOLEAN          NOT NULL DEFAULT FALSE,

    name                VARCHAR(150)     NOT NULL,
    code                VARCHAR(50)      NOT NULL,
    description         TEXT,
    depreciation_rate   DECIMAL(5,2),
    lifespan_years      INT,
    active              BOOLEAN          NOT NULL DEFAULT TRUE,

    CONSTRAINT uq_asset_categories_code_tenant UNIQUE (code, tenant_id)
);

CREATE INDEX IF NOT EXISTS idx_asset_categories_tenant_id ON asset_categories (tenant_id);

-- ── assets ───────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS assets (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           VARCHAR(50)      NOT NULL,
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100),
    created_at          TIMESTAMPTZ      NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ      NOT NULL DEFAULT NOW(),
    is_deleted          BOOLEAN          NOT NULL DEFAULT FALSE,

    category_id         UUID             REFERENCES asset_categories(id) ON DELETE SET NULL,
    name                VARCHAR(200)     NOT NULL,
    code                VARCHAR(50)      NOT NULL,
    serial_number       VARCHAR(100),
    make                VARCHAR(100),
    model               VARCHAR(100),
    purchase_date       DATE,
    purchase_price      DECIMAL(15,2),
    current_value       DECIMAL(15,2),
    status              VARCHAR(30)      NOT NULL DEFAULT 'AVAILABLE',
    location            VARCHAR(200),
    warranty_expiry     DATE,
    notes               TEXT,

    CONSTRAINT uq_assets_code_tenant UNIQUE (code, tenant_id),
    CONSTRAINT chk_assets_status CHECK (status IN ('AVAILABLE','ASSIGNED','UNDER_MAINTENANCE','DISPOSED'))
);

CREATE INDEX IF NOT EXISTS idx_assets_tenant_id ON assets (tenant_id);

-- ── asset_assignments ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS asset_assignments (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id               VARCHAR(50)  NOT NULL,
    created_by              VARCHAR(100),
    updated_by              VARCHAR(100),
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    is_deleted              BOOLEAN      NOT NULL DEFAULT FALSE,

    asset_id                UUID         NOT NULL REFERENCES assets(id) ON DELETE CASCADE,
    employee_id             UUID         NOT NULL,
    assigned_by             UUID,
    assigned_date           DATE         NOT NULL,
    expected_return_date    DATE,
    actual_return_date      DATE,
    condition_at_assignment VARCHAR(100),
    condition_at_return     VARCHAR(100),
    status                  VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',

    CONSTRAINT chk_asset_assignments_status CHECK (status IN ('ACTIVE','RETURNED','LOST'))
);

CREATE INDEX IF NOT EXISTS idx_asset_assignments_tenant_id   ON asset_assignments (tenant_id);
CREATE INDEX IF NOT EXISTS idx_asset_assignments_asset_id    ON asset_assignments (asset_id);
CREATE INDEX IF NOT EXISTS idx_asset_assignments_employee    ON asset_assignments (employee_id, tenant_id);

-- ── asset_requests ────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS asset_requests (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           VARCHAR(50)  NOT NULL,
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100),
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    is_deleted          BOOLEAN      NOT NULL DEFAULT FALSE,

    employee_id         UUID         NOT NULL,
    category_id         UUID         REFERENCES asset_categories(id) ON DELETE SET NULL,
    asset_id            UUID,
    reason              TEXT,
    required_from       DATE,
    required_until      DATE,
    status              VARCHAR(30)  NOT NULL DEFAULT 'PENDING',
    approved_by         UUID,
    notes               TEXT,

    CONSTRAINT chk_asset_requests_status CHECK (status IN ('PENDING','APPROVED','REJECTED','FULFILLED','CANCELLED'))
);

CREATE INDEX IF NOT EXISTS idx_asset_requests_tenant_id          ON asset_requests (tenant_id);
CREATE INDEX IF NOT EXISTS idx_asset_requests_tenant_employee    ON asset_requests (tenant_id, employee_id);

-- ── asset_maintenance ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS asset_maintenance (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           VARCHAR(50)  NOT NULL,
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100),
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    is_deleted          BOOLEAN      NOT NULL DEFAULT FALSE,

    asset_id            UUID         NOT NULL REFERENCES assets(id) ON DELETE CASCADE,
    maintenance_type    VARCHAR(50)  NOT NULL DEFAULT 'PREVENTIVE',
    description         TEXT,
    scheduled_date      DATE,
    completed_date      DATE,
    cost                DECIMAL(15,2),
    vendor              VARCHAR(200),
    status              VARCHAR(30)  NOT NULL DEFAULT 'SCHEDULED',
    notes               TEXT,

    CONSTRAINT chk_asset_maintenance_type   CHECK (maintenance_type IN ('PREVENTIVE','CORRECTIVE','INSPECTION')),
    CONSTRAINT chk_asset_maintenance_status CHECK (status IN ('SCHEDULED','IN_PROGRESS','COMPLETED','CANCELLED'))
);

CREATE INDEX IF NOT EXISTS idx_asset_maintenance_tenant_id ON asset_maintenance (tenant_id);
CREATE INDEX IF NOT EXISTS idx_asset_maintenance_asset_id  ON asset_maintenance (asset_id);
