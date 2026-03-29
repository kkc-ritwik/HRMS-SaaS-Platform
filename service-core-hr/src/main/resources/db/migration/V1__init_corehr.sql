-- ============================================================================
-- V1__init_corehr.sql
-- Core HR schema: departments, designations, locations, employees,
-- and all related sub-tables.
-- ============================================================================

-- ── 1. Departments ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS departments (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       VARCHAR(50) NOT NULL,
    name            VARCHAR(150) NOT NULL,
    code            VARCHAR(50)  NOT NULL,
    description     TEXT,
    parent_id       UUID        REFERENCES departments(id) ON DELETE SET NULL,
    manager_id      UUID,
    head_count      INTEGER     NOT NULL DEFAULT 0,
    active          BOOLEAN     NOT NULL DEFAULT TRUE,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_deleted      BOOLEAN     NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_dept_code_tenant UNIQUE (code, tenant_id)
);
CREATE INDEX IF NOT EXISTS idx_dept_tenant   ON departments(tenant_id);
CREATE INDEX IF NOT EXISTS idx_dept_parent   ON departments(parent_id);
CREATE INDEX IF NOT EXISTS idx_dept_manager  ON departments(manager_id);


-- ── 2. Designations ───────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS designations (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       VARCHAR(50) NOT NULL,
    name            VARCHAR(150) NOT NULL,
    code            VARCHAR(50)  NOT NULL,
    description     TEXT,
    grade           VARCHAR(20),
    active          BOOLEAN     NOT NULL DEFAULT TRUE,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_deleted      BOOLEAN     NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_desig_code_tenant UNIQUE (code, tenant_id)
);
CREATE INDEX IF NOT EXISTS idx_desig_tenant ON designations(tenant_id);


-- ── 3. Locations ──────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS locations (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       VARCHAR(50) NOT NULL,
    name            VARCHAR(150) NOT NULL,
    code            VARCHAR(50)  NOT NULL,
    address_line1   VARCHAR(255),
    address_line2   VARCHAR(255),
    city            VARCHAR(100),
    state           VARCHAR(100),
    country         VARCHAR(100),
    postal_code     VARCHAR(20),
    phone           VARCHAR(30),
    time_zone       VARCHAR(60),
    active          BOOLEAN     NOT NULL DEFAULT TRUE,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_deleted      BOOLEAN     NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_loc_code_tenant UNIQUE (code, tenant_id)
);
CREATE INDEX IF NOT EXISTS idx_loc_tenant ON locations(tenant_id);


-- ── 4. Employees ──────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS employees (
    id                  UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           VARCHAR(50) NOT NULL,
    user_id             UUID,
    employee_code       VARCHAR(30)  NOT NULL,
    first_name          VARCHAR(100) NOT NULL,
    middle_name         VARCHAR(100),
    last_name           VARCHAR(100) NOT NULL,
    email               VARCHAR(255) NOT NULL,
    work_email          VARCHAR(255),
    phone               VARCHAR(30),
    date_of_birth       DATE,
    gender              VARCHAR(20),
    marital_status      VARCHAR(20),
    nationality         VARCHAR(100),
    profile_picture_url TEXT,
    department_id       UUID        REFERENCES departments(id)  ON DELETE SET NULL,
    designation_id      UUID        REFERENCES designations(id) ON DELETE SET NULL,
    location_id         UUID        REFERENCES locations(id)    ON DELETE SET NULL,
    manager_id          UUID        REFERENCES employees(id)    ON DELETE SET NULL,
    employment_type     VARCHAR(30),
    employment_status   VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    join_date           DATE        NOT NULL,
    confirmation_date   DATE,
    exit_date           DATE,
    notice_period_days  INTEGER,
    custom_fields       JSONB,
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_deleted          BOOLEAN     NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_emp_code_tenant  UNIQUE (employee_code, tenant_id),
    CONSTRAINT uq_emp_email_tenant UNIQUE (email, tenant_id)
);
CREATE INDEX IF NOT EXISTS idx_emp_tenant      ON employees(tenant_id);
CREATE INDEX IF NOT EXISTS idx_emp_department  ON employees(department_id);
CREATE INDEX IF NOT EXISTS idx_emp_designation ON employees(designation_id);
CREATE INDEX IF NOT EXISTS idx_emp_location    ON employees(location_id);
CREATE INDEX IF NOT EXISTS idx_emp_manager     ON employees(manager_id);
CREATE INDEX IF NOT EXISTS idx_emp_status      ON employees(employment_status);
CREATE INDEX IF NOT EXISTS idx_emp_user        ON employees(user_id);


-- ── 5. Addresses ──────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS addresses (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       VARCHAR(50) NOT NULL,
    employee_id     UUID        NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    address_type    VARCHAR(20) NOT NULL DEFAULT 'CURRENT',
    address_line1   VARCHAR(255) NOT NULL,
    address_line2   VARCHAR(255),
    city            VARCHAR(100),
    state           VARCHAR(100),
    country         VARCHAR(100),
    postal_code     VARCHAR(20),
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_deleted      BOOLEAN     NOT NULL DEFAULT FALSE
);
CREATE INDEX IF NOT EXISTS idx_addr_employee ON addresses(employee_id);


-- ── 6. Emergency Contacts ─────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS emergency_contacts (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       VARCHAR(50) NOT NULL,
    employee_id     UUID        NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    name            VARCHAR(200) NOT NULL,
    relationship    VARCHAR(50),
    phone           VARCHAR(30)  NOT NULL,
    email           VARCHAR(255),
    is_primary      BOOLEAN     NOT NULL DEFAULT FALSE,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_deleted      BOOLEAN     NOT NULL DEFAULT FALSE
);
CREATE INDEX IF NOT EXISTS idx_ec_employee ON emergency_contacts(employee_id);


-- ── 7. Family Members ─────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS family_members (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       VARCHAR(50) NOT NULL,
    employee_id     UUID        NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    name            VARCHAR(200) NOT NULL,
    relationship    VARCHAR(50)  NOT NULL,
    date_of_birth   DATE,
    occupation      VARCHAR(150),
    phone           VARCHAR(30),
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_deleted      BOOLEAN     NOT NULL DEFAULT FALSE
);
CREATE INDEX IF NOT EXISTS idx_fm_employee ON family_members(employee_id);


-- ── 8. Education ──────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS education (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       VARCHAR(50) NOT NULL,
    employee_id     UUID        NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    institution     VARCHAR(255) NOT NULL,
    degree          VARCHAR(150) NOT NULL,
    field_of_study  VARCHAR(150),
    start_year      INTEGER,
    end_year        INTEGER,
    grade           VARCHAR(50),
    description     TEXT,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_deleted      BOOLEAN     NOT NULL DEFAULT FALSE
);
CREATE INDEX IF NOT EXISTS idx_edu_employee ON education(employee_id);


-- ── 9. Employment History ─────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS employment_history (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       VARCHAR(50) NOT NULL,
    employee_id     UUID        NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    company_name    VARCHAR(255) NOT NULL,
    designation     VARCHAR(150),
    start_date      DATE        NOT NULL,
    end_date        DATE,
    responsibilities TEXT,
    reason_for_leaving VARCHAR(255),
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_deleted      BOOLEAN     NOT NULL DEFAULT FALSE
);
CREATE INDEX IF NOT EXISTS idx_eh_employee ON employment_history(employee_id);


-- ── 10. Employee Lifecycle Events ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS employee_lifecycle_events (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       VARCHAR(50) NOT NULL,
    employee_id     UUID        NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    event_type      VARCHAR(50) NOT NULL,
    event_date      DATE        NOT NULL,
    old_value       TEXT,
    new_value       TEXT,
    remarks         TEXT,
    performed_by    VARCHAR(100),
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_deleted      BOOLEAN     NOT NULL DEFAULT FALSE
);
CREATE INDEX IF NOT EXISTS idx_ele_employee ON employee_lifecycle_events(employee_id);
CREATE INDEX IF NOT EXISTS idx_ele_type     ON employee_lifecycle_events(event_type);
