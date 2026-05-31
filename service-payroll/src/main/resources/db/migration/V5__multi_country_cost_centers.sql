-- Country payroll profiles for multi-country payroll runs.

CREATE TABLE IF NOT EXISTS payroll_country_profiles (
    id                            UUID PRIMARY KEY,
    tenant_id                     VARCHAR(100) NOT NULL,
    created_by                    VARCHAR(100),
    updated_by                    VARCHAR(100),
    created_at                    TIMESTAMP WITH TIME ZONE,
    updated_at                    TIMESTAMP WITH TIME ZONE,
    is_deleted                    BOOLEAN NOT NULL DEFAULT FALSE,

    country                       VARCHAR(2) NOT NULL,
    country_name                  VARCHAR(100),
    financial_year                VARCHAR(10) NOT NULL,
    fy_start                      DATE,
    fy_end                        DATE,
    currency                      VARCHAR(3) NOT NULL,
    standard_deduction            NUMERIC(12,2),
    pf_employer_percent           NUMERIC(5,2),
    pf_employee_percent           NUMERIC(5,2),
    pf_wage_ceiling               NUMERIC(12,2),
    esi_employer_percent          NUMERIC(5,2),
    esi_employee_percent          NUMERIC(5,2),
    esi_wage_ceiling              NUMERIC(12,2),
    gratuity_eligibility_years    INTEGER,
    tax_brackets                  JSONB,
    additional_components         JSONB,
    active                        BOOLEAN DEFAULT TRUE,
    CONSTRAINT uq_country_fy UNIQUE (country, financial_year)
);
CREATE INDEX IF NOT EXISTS ix_country_fy ON payroll_country_profiles(country, financial_year);
