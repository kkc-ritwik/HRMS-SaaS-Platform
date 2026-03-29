-- ─────────────────────────────────────────────────────────────────────────────
-- service-payroll  ·  V1  ·  Salary structures & statutory config
-- ─────────────────────────────────────────────────────────────────────────────

-- 1. salary_structures ────────────────────────────────────────────────────────
CREATE TABLE salary_structures (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       VARCHAR(100) NOT NULL,
    name            VARCHAR(200) NOT NULL,
    description     TEXT,
    is_default      BOOLEAN      NOT NULL DEFAULT false,
    is_active       BOOLEAN      NOT NULL DEFAULT true,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    is_deleted      BOOLEAN      NOT NULL DEFAULT false,
    CONSTRAINT uq_structure_name_tenant UNIQUE (name, tenant_id)
);

-- 2. salary_components ────────────────────────────────────────────────────────
CREATE TABLE salary_components (
    id                          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id                   VARCHAR(100) NOT NULL,
    name                        VARCHAR(200) NOT NULL,
    code                        VARCHAR(50)  NOT NULL,
    type                        VARCHAR(30)  NOT NULL
        CHECK (type IN ('EARNING','DEDUCTION','REIMBURSEMENT','EMPLOYER_CONTRIBUTION')),
    calculation_type            VARCHAR(20)  NOT NULL DEFAULT 'FIXED'
        CHECK (calculation_type IN ('FIXED','PERCENTAGE','FORMULA')),
    percentage_of_component_id  UUID         REFERENCES salary_components(id),
    percentage_value            NUMERIC(10,4),
    formula_expression          VARCHAR(500),
    is_taxable                  BOOLEAN      NOT NULL DEFAULT true,
    is_part_of_ctc              BOOLEAN      NOT NULL DEFAULT true,
    is_part_of_gross            BOOLEAN      NOT NULL DEFAULT true,
    is_pro_rata                 BOOLEAN      NOT NULL DEFAULT true,
    display_order               INT          NOT NULL DEFAULT 0,
    is_active                   BOOLEAN      NOT NULL DEFAULT true,
    created_by                  VARCHAR(100),
    updated_by                  VARCHAR(100),
    created_at                  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at                  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    is_deleted                  BOOLEAN      NOT NULL DEFAULT false,
    CONSTRAINT uq_component_code_tenant UNIQUE (code, tenant_id)
);

CREATE INDEX idx_salary_components_tenant ON salary_components(tenant_id, is_deleted);

-- 3. salary_structure_components (join) ───────────────────────────────────────
CREATE TABLE salary_structure_components (
    id                   UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    salary_structure_id  UUID         NOT NULL REFERENCES salary_structures(id),
    component_id         UUID         NOT NULL REFERENCES salary_components(id),
    default_amount       NUMERIC(12,2),
    default_percentage   NUMERIC(10,4),
    is_mandatory         BOOLEAN      NOT NULL DEFAULT true,
    CONSTRAINT uq_structure_component UNIQUE (salary_structure_id, component_id)
);

-- 4. employee_salary ───────────────────────────────────────────────────────────
CREATE TABLE employee_salary (
    id                   UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id            VARCHAR(100)  NOT NULL,
    employee_id          UUID          NOT NULL,
    salary_structure_id  UUID          REFERENCES salary_structures(id),
    ctc_annual           NUMERIC(14,2) NOT NULL,
    gross_monthly        NUMERIC(12,2),
    net_monthly          NUMERIC(12,2),
    effective_from       DATE          NOT NULL,
    effective_to         DATE,
    revision_letter_url  VARCHAR(1000),
    approved_by          UUID,
    status               VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE'
        CHECK (status IN ('ACTIVE','INACTIVE')),
    created_by           VARCHAR(100),
    updated_by           VARCHAR(100),
    created_at           TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ   NOT NULL DEFAULT now(),
    is_deleted           BOOLEAN       NOT NULL DEFAULT false
);

CREATE INDEX idx_employee_salary_employee ON employee_salary(tenant_id, employee_id, is_deleted);
CREATE INDEX idx_employee_salary_active   ON employee_salary(tenant_id, employee_id, status) WHERE is_deleted = false;

-- 5. employee_salary_components (join) ────────────────────────────────────────
CREATE TABLE employee_salary_components (
    id                    UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_salary_id    UUID          NOT NULL REFERENCES employee_salary(id),
    component_id          UUID          NOT NULL REFERENCES salary_components(id),
    monthly_amount        NUMERIC(12,2) NOT NULL DEFAULT 0,
    annual_amount         NUMERIC(14,2) NOT NULL DEFAULT 0,
    employer_contribution NUMERIC(12,2) NOT NULL DEFAULT 0,
    CONSTRAINT uq_employee_salary_component UNIQUE (employee_salary_id, component_id)
);

-- 6. pf_config ─────────────────────────────────────────────────────────────────
CREATE TABLE pf_config (
    id                        UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id                 VARCHAR(100)  NOT NULL,
    pf_number                 VARCHAR(50),
    basic_wage_ceiling        NUMERIC(10,2) NOT NULL DEFAULT 15000,
    pf_rate_employee          NUMERIC(6,4)  NOT NULL DEFAULT 12,
    pf_rate_employer          NUMERIC(6,4)  NOT NULL DEFAULT 12,
    eps_rate                  NUMERIC(6,4)  NOT NULL DEFAULT 8.33,
    edli_rate                 NUMERIC(6,4)  NOT NULL DEFAULT 0.5,
    admin_charge_rate         NUMERIC(6,4)  NOT NULL DEFAULT 0.5,
    include_employer_pf_in_ctc BOOLEAN      NOT NULL DEFAULT true,
    effective_from            DATE          NOT NULL,
    created_by                VARCHAR(100),
    updated_by                VARCHAR(100),
    created_at                TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at                TIMESTAMPTZ   NOT NULL DEFAULT now(),
    is_deleted                BOOLEAN       NOT NULL DEFAULT false
);

-- 7. esi_config ────────────────────────────────────────────────────────────────
CREATE TABLE esi_config (
    id              UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       VARCHAR(100)  NOT NULL,
    esi_number      VARCHAR(50),
    wage_ceiling    NUMERIC(10,2) NOT NULL DEFAULT 21000,
    employee_rate   NUMERIC(6,4)  NOT NULL DEFAULT 0.75,
    employer_rate   NUMERIC(6,4)  NOT NULL DEFAULT 3.25,
    effective_from  DATE          NOT NULL,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ   NOT NULL DEFAULT now(),
    is_deleted      BOOLEAN       NOT NULL DEFAULT false
);

-- 8. pt_slabs ──────────────────────────────────────────────────────────────────
CREATE TABLE pt_slabs (
    id              UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       VARCHAR(100)  NOT NULL,
    state           VARCHAR(50)   NOT NULL,
    slab_from       NUMERIC(10,2) NOT NULL DEFAULT 0,
    slab_to         NUMERIC(10,2),           -- NULL means no upper limit
    monthly_tax     NUMERIC(8,2)  NOT NULL,
    gender          VARCHAR(10),             -- NULL means applicable to all
    effective_from  DATE          NOT NULL,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ   NOT NULL DEFAULT now(),
    is_deleted      BOOLEAN       NOT NULL DEFAULT false
);

CREATE INDEX idx_pt_slabs_tenant_state ON pt_slabs(tenant_id, state, is_deleted);
