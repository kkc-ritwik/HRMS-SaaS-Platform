-- Insurance dependents + market salary benchmarks.

CREATE TABLE IF NOT EXISTS benefit_dependents (
    id              UUID PRIMARY KEY,
    tenant_id       VARCHAR(100) NOT NULL,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    created_at      TIMESTAMP WITH TIME ZONE,
    updated_at      TIMESTAMP WITH TIME ZONE,
    is_deleted      BOOLEAN NOT NULL DEFAULT FALSE,

    employee_id     UUID NOT NULL,
    relationship    VARCHAR(30) NOT NULL,
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100),
    date_of_birth   DATE NOT NULL,
    gender          VARCHAR(10),
    national_id     VARCHAR(500),
    is_primary      BOOLEAN,
    is_disabled     BOOLEAN,
    is_student      BOOLEAN,
    address_line1   VARCHAR(300),
    city            VARCHAR(100),
    state           VARCHAR(100),
    postal_code     VARCHAR(20),
    country         VARCHAR(2),
    phone           VARCHAR(30),
    email           VARCHAR(200),
    enrolled_on     DATE,
    removed_on      DATE,
    removal_reason  VARCHAR(200)
);
CREATE INDEX IF NOT EXISTS ix_dependent_emp ON benefit_dependents(tenant_id, employee_id);

CREATE TABLE IF NOT EXISTS comp_market_benchmarks (
    id                      UUID PRIMARY KEY,
    tenant_id               VARCHAR(100) NOT NULL,
    created_by              VARCHAR(100),
    updated_by              VARCHAR(100),
    created_at              TIMESTAMP WITH TIME ZONE,
    updated_at              TIMESTAMP WITH TIME ZONE,
    is_deleted              BOOLEAN NOT NULL DEFAULT FALSE,

    provider                VARCHAR(50) NOT NULL,
    role_code               VARCHAR(100) NOT NULL,
    role_title              VARCHAR(200),
    job_family              VARCHAR(100),
    level                   VARCHAR(30),
    country                 VARCHAR(2) NOT NULL,
    city                    VARCHAR(100),
    industry                VARCHAR(100),
    company_size_band       VARCHAR(30),
    currency                VARCHAR(3) NOT NULL,
    p10_total_cash          NUMERIC(14,2),
    p25_total_cash          NUMERIC(14,2),
    p50_total_cash          NUMERIC(14,2),
    p75_total_cash          NUMERIC(14,2),
    p90_total_cash          NUMERIC(14,2),
    p50_base                NUMERIC(14,2),
    p50_variable            NUMERIC(14,2),
    p50_equity              NUMERIC(14,2),
    sample_size             INTEGER,
    survey_date             DATE NOT NULL,
    source_document_uri     VARCHAR(500),
    internal_pay_grade_id   UUID
);
CREATE INDEX IF NOT EXISTS ix_bench_role_geo ON comp_market_benchmarks(tenant_id, role_code, country);
CREATE INDEX IF NOT EXISTS ix_bench_date     ON comp_market_benchmarks(survey_date);
