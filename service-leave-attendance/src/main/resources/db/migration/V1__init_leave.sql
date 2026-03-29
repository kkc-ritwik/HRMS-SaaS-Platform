-- ============================================================================
-- V1__init_leave.sql
-- Leave Management schema: types, policies, balances, applications,
-- approvals, holidays, comp-off and adjustments.
-- ============================================================================

-- ── 1. Leave Types ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS leave_types (
    id                              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id                       VARCHAR(50) NOT NULL,
    name                            VARCHAR(150) NOT NULL,
    code                            VARCHAR(50)  NOT NULL,
    is_paid                         BOOLEAN     NOT NULL DEFAULT TRUE,
    color                           VARCHAR(7),           -- hex e.g. #FF5733
    description                     TEXT,
    applies_to_gender               VARCHAR(20),          -- MALE / FEMALE / OTHER / NULL = all
    applies_to_employment_type      VARCHAR(30),          -- FULL_TIME / etc. / NULL = all
    max_days_per_year               NUMERIC(6,2),
    requires_attachment_after_days  NUMERIC(6,2),
    is_active                       BOOLEAN     NOT NULL DEFAULT TRUE,
    created_by                      VARCHAR(100),
    updated_by                      VARCHAR(100),
    created_at                      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at                      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_deleted                      BOOLEAN     NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_leave_type_code_tenant UNIQUE (code, tenant_id)
);
CREATE INDEX IF NOT EXISTS idx_lt_tenant ON leave_types(tenant_id);


-- ── 2. Leave Policies ─────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS leave_policies (
    id                          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id                   VARCHAR(50) NOT NULL,
    name                        VARCHAR(200) NOT NULL,
    leave_type_id               UUID        NOT NULL REFERENCES leave_types(id) ON DELETE CASCADE,
    accrual_type                VARCHAR(20) NOT NULL DEFAULT 'YEARLY',
    accrual_amount              NUMERIC(6,2) NOT NULL DEFAULT 0,
    max_accrual                 NUMERIC(6,2),
    carry_forward_enabled       BOOLEAN     NOT NULL DEFAULT FALSE,
    max_carry_forward           NUMERIC(6,2),
    carry_forward_expiry_months INTEGER,
    encashment_enabled          BOOLEAN     NOT NULL DEFAULT FALSE,
    max_encashment_days         NUMERIC(6,2),
    pro_rata_on_joining         BOOLEAN     NOT NULL DEFAULT FALSE,
    pro_rata_on_exit            BOOLEAN     NOT NULL DEFAULT FALSE,
    negative_balance_allowed    BOOLEAN     NOT NULL DEFAULT FALSE,
    max_negative_days           NUMERIC(6,2),
    sandwich_rule_enabled       BOOLEAN     NOT NULL DEFAULT FALSE,
    min_notice_days             INTEGER     NOT NULL DEFAULT 0,
    allow_half_day              BOOLEAN     NOT NULL DEFAULT TRUE,
    allow_short_leave           BOOLEAN     NOT NULL DEFAULT FALSE,
    effective_from              DATE        NOT NULL,
    effective_to                DATE,
    is_active                   BOOLEAN     NOT NULL DEFAULT TRUE,
    created_by                  VARCHAR(100),
    updated_by                  VARCHAR(100),
    created_at                  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_deleted                  BOOLEAN     NOT NULL DEFAULT FALSE
);
CREATE INDEX IF NOT EXISTS idx_lp_tenant     ON leave_policies(tenant_id);
CREATE INDEX IF NOT EXISTS idx_lp_leave_type ON leave_policies(leave_type_id);


-- ── 3. Leave Balances ─────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS leave_balances (
    id               UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id        VARCHAR(50) NOT NULL,
    employee_id      UUID        NOT NULL,
    leave_type_id    UUID        NOT NULL REFERENCES leave_types(id) ON DELETE CASCADE,
    year             INTEGER     NOT NULL,
    opening_balance  NUMERIC(8,2) NOT NULL DEFAULT 0,
    accrued          NUMERIC(8,2) NOT NULL DEFAULT 0,
    used             NUMERIC(8,2) NOT NULL DEFAULT 0,
    carry_forward    NUMERIC(8,2) NOT NULL DEFAULT 0,
    encashed         NUMERIC(8,2) NOT NULL DEFAULT 0,
    adjusted         NUMERIC(8,2) NOT NULL DEFAULT 0,
    available        NUMERIC(8,2) GENERATED ALWAYS AS
                     (opening_balance + accrued + carry_forward + adjusted - used - encashed)
                     STORED,
    created_by       VARCHAR(100),
    updated_by       VARCHAR(100),
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_deleted       BOOLEAN     NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_balance_emp_type_year UNIQUE (tenant_id, employee_id, leave_type_id, year)
);
CREATE INDEX IF NOT EXISTS idx_lb_employee    ON leave_balances(employee_id);
CREATE INDEX IF NOT EXISTS idx_lb_tenant_year ON leave_balances(tenant_id, year);
CREATE INDEX IF NOT EXISTS idx_lb_leave_type  ON leave_balances(leave_type_id);


-- ── 4. Leave Applications ─────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS leave_applications (
    id               UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id        VARCHAR(50) NOT NULL,
    employee_id      UUID        NOT NULL,
    leave_type_id    UUID        NOT NULL REFERENCES leave_types(id),
    from_date        DATE        NOT NULL,
    to_date          DATE        NOT NULL,
    duration_days    NUMERIC(6,2) NOT NULL,
    day_type         VARCHAR(20) NOT NULL DEFAULT 'FULL',  -- FULL / FIRST_HALF / SECOND_HALF
    reason           TEXT,
    attachment_url   TEXT,
    status           VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    applied_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    cancelled_at     TIMESTAMPTZ,
    cancel_reason    TEXT,
    created_by       VARCHAR(100),
    updated_by       VARCHAR(100),
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_deleted       BOOLEAN     NOT NULL DEFAULT FALSE
);
CREATE INDEX IF NOT EXISTS idx_la_employee   ON leave_applications(employee_id);
CREATE INDEX IF NOT EXISTS idx_la_tenant     ON leave_applications(tenant_id);
CREATE INDEX IF NOT EXISTS idx_la_dates      ON leave_applications(from_date, to_date);
CREATE INDEX IF NOT EXISTS idx_la_status     ON leave_applications(status);
CREATE INDEX IF NOT EXISTS idx_la_leave_type ON leave_applications(leave_type_id);


-- ── 5. Leave Approvals ────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS leave_approvals (
    id                    UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id             VARCHAR(50) NOT NULL,
    leave_application_id  UUID        NOT NULL REFERENCES leave_applications(id) ON DELETE CASCADE,
    approver_id           UUID        NOT NULL,
    level                 INTEGER     NOT NULL DEFAULT 1,
    status                VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    comments              TEXT,
    acted_at              TIMESTAMPTZ,
    created_by            VARCHAR(100),
    updated_by            VARCHAR(100),
    created_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_deleted            BOOLEAN     NOT NULL DEFAULT FALSE
);
CREATE INDEX IF NOT EXISTS idx_lapprv_application ON leave_approvals(leave_application_id);
CREATE INDEX IF NOT EXISTS idx_lapprv_approver     ON leave_approvals(approver_id);
CREATE INDEX IF NOT EXISTS idx_lapprv_tenant       ON leave_approvals(tenant_id);


-- ── 6. Holidays ───────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS holidays (
    id            UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id     VARCHAR(50) NOT NULL,
    name          VARCHAR(200) NOT NULL,
    date          DATE        NOT NULL,
    type          VARCHAR(20) NOT NULL DEFAULT 'NATIONAL',
    location_ids  JSONB,                -- array of location UUIDs; NULL = applies to all
    is_active     BOOLEAN     NOT NULL DEFAULT TRUE,
    year          INTEGER     NOT NULL,
    created_by    VARCHAR(100),
    updated_by    VARCHAR(100),
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_deleted    BOOLEAN     NOT NULL DEFAULT FALSE
);
CREATE INDEX IF NOT EXISTS idx_hol_tenant      ON holidays(tenant_id);
CREATE INDEX IF NOT EXISTS idx_hol_year        ON holidays(year);
CREATE INDEX IF NOT EXISTS idx_hol_date        ON holidays(date);


-- ── 7. Comp-Off Requests ──────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS comp_off_requests (
    id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id    VARCHAR(50) NOT NULL,
    employee_id  UUID        NOT NULL,
    worked_date  DATE        NOT NULL,
    reason       TEXT,
    expires_at   DATE,
    status       VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    approved_by  UUID,
    created_by   VARCHAR(100),
    updated_by   VARCHAR(100),
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_deleted   BOOLEAN     NOT NULL DEFAULT FALSE
);
CREATE INDEX IF NOT EXISTS idx_cor_employee ON comp_off_requests(employee_id);
CREATE INDEX IF NOT EXISTS idx_cor_tenant   ON comp_off_requests(tenant_id);


-- ── 8. Leave Adjustments ──────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS leave_adjustments (
    id               UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id        VARCHAR(50) NOT NULL,
    employee_id      UUID        NOT NULL,
    leave_type_id    UUID        NOT NULL REFERENCES leave_types(id),
    adjustment_type  VARCHAR(10) NOT NULL,   -- CREDIT / DEBIT
    days             NUMERIC(6,2) NOT NULL,
    reason           TEXT,
    adjusted_by      VARCHAR(100),
    effective_date   DATE        NOT NULL,
    created_by       VARCHAR(100),
    updated_by       VARCHAR(100),
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_deleted       BOOLEAN     NOT NULL DEFAULT FALSE
);
CREATE INDEX IF NOT EXISTS idx_ladj_employee ON leave_adjustments(employee_id);
CREATE INDEX IF NOT EXISTS idx_ladj_tenant   ON leave_adjustments(tenant_id);
