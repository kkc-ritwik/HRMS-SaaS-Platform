-- Investment proof documents that finance verifies against an employee's tax declaration.

CREATE TABLE IF NOT EXISTS tax_investment_proofs (
    id                    UUID PRIMARY KEY,
    tenant_id             VARCHAR(100) NOT NULL,
    created_by            VARCHAR(100),
    updated_by            VARCHAR(100),
    created_at            TIMESTAMP WITH TIME ZONE,
    updated_at            TIMESTAMP WITH TIME ZONE,
    is_deleted            BOOLEAN NOT NULL DEFAULT FALSE,

    employee_id           UUID NOT NULL,
    declaration_id        UUID,
    financial_year        VARCHAR(10) NOT NULL,
    section               VARCHAR(20) NOT NULL,
    sub_section           VARCHAR(50),
    instrument_type       VARCHAR(100),
    claimed_amount        NUMERIC(12,2) NOT NULL,
    verified_amount       NUMERIC(12,2),
    document_uri          VARCHAR(1000) NOT NULL,
    document_type         VARCHAR(100),
    document_date         DATE,
    policy_number         VARCHAR(500),
    pan_number            VARCHAR(500),
    bank_account          VARCHAR(500),
    notes                 VARCHAR(2000),
    status                VARCHAR(20) NOT NULL DEFAULT 'UPLOADED',
    verified_by           UUID,
    verified_at           TIMESTAMP WITH TIME ZONE,
    reviewer_comment      VARCHAR(2000)
);
CREATE INDEX IF NOT EXISTS ix_proof_emp_fy ON tax_investment_proofs(tenant_id, employee_id, financial_year);
CREATE INDEX IF NOT EXISTS ix_proof_status ON tax_investment_proofs(tenant_id, status);
