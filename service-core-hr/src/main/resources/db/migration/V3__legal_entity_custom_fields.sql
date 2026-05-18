CREATE TABLE IF NOT EXISTS legal_entities (
    id                              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id                       VARCHAR(100) NOT NULL,
    code                            VARCHAR(50) NOT NULL,
    name                            VARCHAR(200) NOT NULL,
    registered_name                 VARCHAR(300),
    parent_id                       UUID,
    country                         VARCHAR(2) NOT NULL,
    base_currency                   VARCHAR(3) NOT NULL,
    time_zone                       VARCHAR(50),
    registered_address              VARCHAR(1000),
    city                            VARCHAR(100),
    state                           VARCHAR(100),
    postal_code                     VARCHAR(20),
    tax_id                          VARCHAR(50),
    tan_number                      VARCHAR(20),
    pan_number                      VARCHAR(20),
    cin_number                      VARCHAR(30),
    epfo_establishment_id           VARCHAR(30),
    esic_employer_code              VARCHAR(30),
    lwf_registration_number         VARCHAR(50),
    default_pay_frequency           VARCHAR(20),
    fiscal_year_start_month         INTEGER,
    bank_account_for_disbursement   VARCHAR(40),
    bank_ifsc                       VARCHAR(20),
    logo_url                        VARCHAR(1000),
    letterhead_url                  VARCHAR(1000),
    is_active                       BOOLEAN NOT NULL DEFAULT TRUE,
    created_by                      VARCHAR(100),
    updated_by                      VARCHAR(100),
    created_at                      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at                      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted                         BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE (tenant_id, code)
);
CREATE INDEX IF NOT EXISTS ix_legal_entity_parent ON legal_entities (parent_id);

-- Link existing employees to a legal entity (nullable for backward compatibility).
ALTER TABLE employees
    ADD COLUMN IF NOT EXISTS legal_entity_id UUID;
CREATE INDEX IF NOT EXISTS ix_employee_legal_entity ON employees (legal_entity_id);

CREATE TABLE IF NOT EXISTS custom_field_definitions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       VARCHAR(100) NOT NULL,
    entity_name     VARCHAR(100) NOT NULL,
    field_key       VARCHAR(100) NOT NULL,
    label           VARCHAR(200) NOT NULL,
    data_type       VARCHAR(30) NOT NULL,
    is_required     BOOLEAN NOT NULL DEFAULT FALSE,
    is_searchable   BOOLEAN NOT NULL DEFAULT FALSE,
    is_sensitive    BOOLEAN NOT NULL DEFAULT FALSE,
    options         JSONB,
    validation      JSONB,
    default_value   VARCHAR(1000),
    help_text       VARCHAR(500),
    display_order   INTEGER,
    section         VARCHAR(100),
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted         BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE (tenant_id, entity_name, field_key)
);
