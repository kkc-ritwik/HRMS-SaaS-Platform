-- ============================================================
-- Compensation Service Schema
-- ============================================================

-- pay_grades
CREATE TABLE pay_grades (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id   VARCHAR(50)  NOT NULL,
    name        VARCHAR(150) NOT NULL,
    code        VARCHAR(50)  NOT NULL,
    description TEXT,
    min_salary  DECIMAL(15,2) NOT NULL,
    max_salary  DECIMAL(15,2) NOT NULL,
    currency    VARCHAR(10)  NOT NULL DEFAULT 'USD',
    level       INT          NOT NULL DEFAULT 1,
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_by  VARCHAR(100),
    updated_by  VARCHAR(100),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    is_deleted  BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_pay_grade_code_tenant UNIQUE (code, tenant_id)
);

CREATE INDEX idx_pay_grades_tenant_id ON pay_grades (tenant_id);

-- benefits
CREATE TABLE benefits (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id             VARCHAR(50)  NOT NULL,
    name                  VARCHAR(200) NOT NULL,
    code                  VARCHAR(50)  NOT NULL,
    description           TEXT,
    benefit_type          VARCHAR(50)  NOT NULL DEFAULT 'OTHER',
    provider              VARCHAR(200),
    coverage_amount       DECIMAL(15,2),
    employee_contribution DECIMAL(15,2),
    employer_contribution DECIMAL(15,2),
    currency              VARCHAR(10)  NOT NULL DEFAULT 'USD',
    active                BOOLEAN      NOT NULL DEFAULT TRUE,
    created_by            VARCHAR(100),
    updated_by            VARCHAR(100),
    created_at            TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    is_deleted            BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_benefit_code_tenant UNIQUE (code, tenant_id)
);

CREATE INDEX idx_benefits_tenant_id ON benefits (tenant_id);

-- employee_benefits
CREATE TABLE employee_benefits (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id        VARCHAR(50) NOT NULL,
    employee_id      UUID        NOT NULL,
    benefit_id       UUID        NOT NULL REFERENCES benefits(id) ON DELETE CASCADE,
    enrollment_date  DATE        NOT NULL,
    termination_date DATE,
    status           VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    notes            TEXT,
    created_by       VARCHAR(100),
    updated_by       VARCHAR(100),
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_deleted       BOOLEAN     NOT NULL DEFAULT FALSE
);

CREATE UNIQUE INDEX uq_employee_benefit ON employee_benefits (tenant_id, employee_id, benefit_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_employee_benefits_tenant_id   ON employee_benefits (tenant_id);
CREATE INDEX idx_employee_benefits_emp_tenant  ON employee_benefits (tenant_id, employee_id);

-- compensation_plans
CREATE TABLE compensation_plans (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id        VARCHAR(50)   NOT NULL,
    employee_id      UUID          NOT NULL,
    pay_grade_id     UUID          REFERENCES pay_grades(id) ON DELETE SET NULL,
    base_salary      DECIMAL(15,2) NOT NULL,
    currency         VARCHAR(10)   NOT NULL DEFAULT 'USD',
    effective_date   DATE          NOT NULL,
    end_date         DATE,
    allowances       DECIMAL(15,2) DEFAULT 0,
    bonus_percentage DECIMAL(5,2)  DEFAULT 0,
    notes            TEXT,
    status           VARCHAR(20)   NOT NULL DEFAULT 'DRAFT',
    created_by       VARCHAR(100),
    updated_by       VARCHAR(100),
    created_at       TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    is_deleted       BOOLEAN       NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_compensation_plans_tenant_id  ON compensation_plans (tenant_id);
CREATE INDEX idx_compensation_plans_emp_tenant ON compensation_plans (tenant_id, employee_id);
