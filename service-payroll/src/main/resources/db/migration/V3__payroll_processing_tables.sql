-- ─────────────────────────────────────────────────────────────────────────────
-- service-payroll  ·  V3  ·  Pay runs, payslips, TDS, loans
-- ─────────────────────────────────────────────────────────────────────────────

-- 1. payroll_runs ──────────────────────────────────────────────────────────────
CREATE TABLE payroll_runs (
    id               UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id        VARCHAR(100)  NOT NULL,
    month            INT           NOT NULL CHECK (month BETWEEN 1 AND 12),
    year             INT           NOT NULL,
    status           VARCHAR(20)   NOT NULL DEFAULT 'DRAFT'
        CHECK (status IN ('DRAFT','PROCESSING','PROCESSED','LOCKED','PAID')),
    run_type         VARCHAR(20)   NOT NULL DEFAULT 'REGULAR'
        CHECK (run_type IN ('REGULAR','SUPPLEMENTARY','ARREARS','FNF')),
    total_gross      NUMERIC(16,2) DEFAULT 0,
    total_deductions NUMERIC(16,2) DEFAULT 0,
    total_net        NUMERIC(16,2) DEFAULT 0,
    total_employer_pf  NUMERIC(14,2) DEFAULT 0,
    total_employer_esi NUMERIC(14,2) DEFAULT 0,
    employee_count   INT           DEFAULT 0,
    processed_by     VARCHAR(100),
    processed_at     TIMESTAMPTZ,
    locked_by        VARCHAR(100),
    locked_at        TIMESTAMPTZ,
    created_by       VARCHAR(100),
    updated_by       VARCHAR(100),
    created_at       TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ   NOT NULL DEFAULT now(),
    is_deleted       BOOLEAN       NOT NULL DEFAULT false
);

CREATE INDEX idx_payroll_runs_tenant ON payroll_runs(tenant_id, year, month, is_deleted);

-- 2. payslips ──────────────────────────────────────────────────────────────────
CREATE TABLE payslips (
    id               UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id        VARCHAR(100)  NOT NULL,
    payroll_run_id   UUID          NOT NULL REFERENCES payroll_runs(id),
    employee_id      UUID          NOT NULL,
    month            INT           NOT NULL CHECK (month BETWEEN 1 AND 12),
    year             INT           NOT NULL,
    days_in_month    INT           NOT NULL DEFAULT 30,
    days_payable     INT           NOT NULL DEFAULT 30,
    days_worked      INT           NOT NULL DEFAULT 30,
    lop_days         NUMERIC(5,2)  NOT NULL DEFAULT 0,
    gross_earnings   NUMERIC(14,2) NOT NULL DEFAULT 0,
    total_deductions NUMERIC(14,2) NOT NULL DEFAULT 0,
    net_pay          NUMERIC(14,2) NOT NULL DEFAULT 0,
    employer_pf      NUMERIC(12,2) NOT NULL DEFAULT 0,
    employer_esi     NUMERIC(12,2) NOT NULL DEFAULT 0,
    employer_lwf     NUMERIC(10,2) NOT NULL DEFAULT 0,
    employee_pf      NUMERIC(12,2) NOT NULL DEFAULT 0,
    employee_esi     NUMERIC(12,2) NOT NULL DEFAULT 0,
    employee_pt      NUMERIC(10,2) NOT NULL DEFAULT 0,
    tds              NUMERIC(12,2) NOT NULL DEFAULT 0,
    components_json  JSONB,
    status           VARCHAR(20)   NOT NULL DEFAULT 'DRAFT'
        CHECK (status IN ('DRAFT','PROCESSED','LOCKED','PUBLISHED')),
    pdf_url          VARCHAR(1000),
    emailed_at       TIMESTAMPTZ,
    created_by       VARCHAR(100),
    updated_by       VARCHAR(100),
    created_at       TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ   NOT NULL DEFAULT now(),
    is_deleted       BOOLEAN       NOT NULL DEFAULT false,
    CONSTRAINT uq_payslip_employee_run UNIQUE (tenant_id, employee_id, month, year, payroll_run_id)
);

CREATE INDEX idx_payslips_employee ON payslips(tenant_id, employee_id, year, month, is_deleted);
CREATE INDEX idx_payslips_run      ON payslips(payroll_run_id);

-- 3. tax_declarations ──────────────────────────────────────────────────────────
CREATE TABLE tax_declarations (
    id                      UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id               VARCHAR(100)  NOT NULL,
    employee_id             UUID          NOT NULL,
    financial_year          VARCHAR(10)   NOT NULL,   -- e.g. '2025-26'
    regime                  VARCHAR(5)    NOT NULL DEFAULT 'NEW'
        CHECK (regime IN ('OLD','NEW')),
    status                  VARCHAR(20)   NOT NULL DEFAULT 'DRAFT'
        CHECK (status IN ('DRAFT','SUBMITTED','VERIFIED')),
    section_80c             NUMERIC(12,2) NOT NULL DEFAULT 0,
    section_80d             NUMERIC(12,2) NOT NULL DEFAULT 0,
    section_80e             NUMERIC(12,2) NOT NULL DEFAULT 0,
    section_80g             NUMERIC(12,2) NOT NULL DEFAULT 0,
    section_24b             NUMERIC(12,2) NOT NULL DEFAULT 0,
    hra_exemption_claimed   NUMERIC(12,2) NOT NULL DEFAULT 0,
    lta_claimed             NUMERIC(12,2) NOT NULL DEFAULT 0,
    other_income            NUMERIC(12,2) NOT NULL DEFAULT 0,
    previous_employer_income NUMERIC(14,2) NOT NULL DEFAULT 0,
    previous_employer_tds   NUMERIC(12,2) NOT NULL DEFAULT 0,
    created_by              VARCHAR(100),
    updated_by              VARCHAR(100),
    created_at              TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ   NOT NULL DEFAULT now(),
    is_deleted              BOOLEAN       NOT NULL DEFAULT false,
    CONSTRAINT uq_declaration_employee_fy UNIQUE (tenant_id, employee_id, financial_year)
);

CREATE INDEX idx_tax_declarations_employee ON tax_declarations(tenant_id, employee_id, is_deleted);

-- 4. tax_proofs ────────────────────────────────────────────────────────────────
CREATE TABLE tax_proofs (
    id               UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    declaration_id   UUID          NOT NULL REFERENCES tax_declarations(id),
    section          VARCHAR(20)   NOT NULL,           -- '80C', '80D', 'HRA', etc.
    description      VARCHAR(500),
    declared_amount  NUMERIC(12,2) NOT NULL DEFAULT 0,
    proof_amount     NUMERIC(12,2),
    proof_url        VARCHAR(1000),
    status           VARCHAR(20)   NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING','APPROVED','REJECTED')),
    verified_by      VARCHAR(100),
    created_at       TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX idx_tax_proofs_declaration ON tax_proofs(declaration_id);

-- 5. loans ─────────────────────────────────────────────────────────────────────
CREATE TABLE loans (
    id                     UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id              VARCHAR(100)  NOT NULL,
    employee_id            UUID          NOT NULL,
    loan_type              VARCHAR(50)   NOT NULL,    -- HOME, PERSONAL, VEHICLE, ADVANCE, etc.
    principal_amount       NUMERIC(14,2) NOT NULL,
    interest_rate          NUMERIC(6,4)  NOT NULL DEFAULT 0,   -- annual %
    tenure_months          INT           NOT NULL,
    emi_amount             NUMERIC(12,2) NOT NULL,
    disbursement_date      DATE          NOT NULL,
    start_deduction_month  DATE          NOT NULL,   -- first payroll month to deduct EMI
    outstanding_balance    NUMERIC(14,2) NOT NULL,
    status                 VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE'
        CHECK (status IN ('ACTIVE','CLOSED','DEFAULTED')),
    created_by             VARCHAR(100),
    updated_by             VARCHAR(100),
    created_at             TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at             TIMESTAMPTZ   NOT NULL DEFAULT now(),
    is_deleted             BOOLEAN       NOT NULL DEFAULT false
);

CREATE INDEX idx_loans_employee ON loans(tenant_id, employee_id, is_deleted);

-- 6. loan_repayments ───────────────────────────────────────────────────────────
CREATE TABLE loan_repayments (
    id             UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    loan_id        UUID          NOT NULL REFERENCES loans(id),
    payslip_id     UUID          REFERENCES payslips(id),   -- null for manual payments
    emi_number     INT           NOT NULL,
    principal_part NUMERIC(12,2) NOT NULL DEFAULT 0,
    interest_part  NUMERIC(12,2) NOT NULL DEFAULT 0,
    total_emi      NUMERIC(12,2) NOT NULL DEFAULT 0,
    paid_at        TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX idx_loan_repayments_loan ON loan_repayments(loan_id);
