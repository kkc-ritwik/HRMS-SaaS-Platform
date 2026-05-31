-- Immigration: passport, visa, work permit, OCI/PIO, etc. PII column document_number is
-- AES-256-GCM encrypted by the JPA converter.

CREATE TABLE IF NOT EXISTS travel_documents (
    id                  UUID PRIMARY KEY,
    tenant_id           VARCHAR(100) NOT NULL,
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100),
    created_at          TIMESTAMP WITH TIME ZONE,
    updated_at          TIMESTAMP WITH TIME ZONE,
    is_deleted          BOOLEAN NOT NULL DEFAULT FALSE,

    employee_id         UUID NOT NULL,
    document_type       VARCHAR(30) NOT NULL,
    document_number     VARCHAR(500) NOT NULL,
    issuing_country     VARCHAR(2) NOT NULL,
    issuing_authority   VARCHAR(200),
    issue_date          DATE,
    expiry_date         DATE NOT NULL,
    destination_country VARCHAR(2),
    visa_category       VARCHAR(50),
    entries_allowed     VARCHAR(20),
    stored_copy_uri     VARCHAR(1000),
    is_active           BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS ix_traveldoc_employee ON travel_documents(tenant_id, employee_id);
CREATE INDEX IF NOT EXISTS ix_traveldoc_expiry   ON travel_documents(expiry_date);
