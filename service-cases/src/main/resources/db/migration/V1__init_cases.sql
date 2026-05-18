CREATE TABLE hr_cases (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       VARCHAR(100) NOT NULL,
    case_number     VARCHAR(30)  NOT NULL UNIQUE,
    type            VARCHAR(30),
    title           VARCHAR(200),
    description     VARCHAR(5000),
    complainant_employee_id UUID,
    respondent_employee_id  UUID,
    is_anonymous    BOOLEAN,
    severity        VARCHAR(30),
    status          VARCHAR(30) NOT NULL,
    assigned_to     UUID,
    icc_committee_id UUID,
    sla_due_date    TIMESTAMPTZ,
    resolved_at     TIMESTAMPTZ,
    resolution      VARCHAR(5000),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX ix_hrcase_tenant      ON hr_cases (tenant_id);
CREATE INDEX ix_hrcase_complainant ON hr_cases (complainant_employee_id);
CREATE INDEX ix_hrcase_status      ON hr_cases (status);

CREATE TABLE case_notes (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       VARCHAR(100) NOT NULL,
    case_id         UUID NOT NULL REFERENCES hr_cases(id) ON DELETE CASCADE,
    author_id       UUID NOT NULL,
    content         VARCHAR(5000) NOT NULL,
    is_internal     BOOLEAN,
    attachment_uri  VARCHAR(1000),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX ix_note_case ON case_notes (case_id);

CREATE TABLE icc_committees (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id             VARCHAR(100) NOT NULL,
    name                  VARCHAR(200) NOT NULL,
    presiding_officer_id  UUID NOT NULL,
    description           VARCHAR(500),
    is_active             BOOLEAN DEFAULT TRUE
);
