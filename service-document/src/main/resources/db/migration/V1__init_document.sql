-- ============================================================
-- V1__init_document.sql
-- Document service schema initialisation
-- ============================================================

-- ── document_types ──────────────────────────────────────────
CREATE TABLE document_types (
    id                        UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id                 VARCHAR(50)   NOT NULL,
    created_by                VARCHAR(100),
    updated_by                VARCHAR(100),
    created_at                TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at                TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    is_deleted                BOOLEAN       NOT NULL DEFAULT FALSE,

    name                      VARCHAR(150)  NOT NULL,
    code                      VARCHAR(50)   NOT NULL,
    description               TEXT,
    required_for_onboarding   BOOLEAN       NOT NULL DEFAULT FALSE,
    active                    BOOLEAN       NOT NULL DEFAULT TRUE,

    CONSTRAINT uq_document_types_code_tenant UNIQUE (code, tenant_id)
);

CREATE INDEX idx_document_types_tenant_id ON document_types (tenant_id);

-- ── documents ───────────────────────────────────────────────
CREATE TABLE documents (
    id                  UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           VARCHAR(50)   NOT NULL,
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100),
    created_at          TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    is_deleted          BOOLEAN       NOT NULL DEFAULT FALSE,

    employee_id         UUID,
    document_type_id    UUID          REFERENCES document_types (id) ON DELETE SET NULL,
    file_name           VARCHAR(300)  NOT NULL,
    file_url            VARCHAR(500),
    file_size_bytes     BIGINT,
    mime_type           VARCHAR(100),
    status              VARCHAR(30)   NOT NULL DEFAULT 'DRAFT',
    expiry_date         DATE,
    remarks             TEXT,
    uploaded_by         UUID
);

CREATE INDEX idx_documents_tenant_id ON documents (tenant_id);

-- ── document_templates ──────────────────────────────────────
CREATE TABLE document_templates (
    id          UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id   VARCHAR(50)   NOT NULL,
    created_by  VARCHAR(100),
    updated_by  VARCHAR(100),
    created_at  TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    is_deleted  BOOLEAN       NOT NULL DEFAULT FALSE,

    name        VARCHAR(200)  NOT NULL,
    code        VARCHAR(50)   NOT NULL,
    description TEXT,
    content     TEXT,
    category    VARCHAR(100),
    active      BOOLEAN       NOT NULL DEFAULT TRUE,

    CONSTRAINT uq_document_templates_code_tenant UNIQUE (code, tenant_id)
);

CREATE INDEX idx_document_templates_tenant_id ON document_templates (tenant_id);

-- ── generated_letters ───────────────────────────────────────
CREATE TABLE generated_letters (
    id            UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id     VARCHAR(50)   NOT NULL,
    created_by    VARCHAR(100),
    updated_by    VARCHAR(100),
    created_at    TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    is_deleted    BOOLEAN       NOT NULL DEFAULT FALSE,

    employee_id   UUID          NOT NULL,
    template_id   UUID          REFERENCES document_templates (id) ON DELETE SET NULL,
    letter_type   VARCHAR(100),
    subject       VARCHAR(300),
    content       TEXT,
    generated_by  UUID,
    sent_at       TIMESTAMPTZ,
    status        VARCHAR(30)   NOT NULL DEFAULT 'DRAFT'
);

CREATE INDEX idx_generated_letters_tenant_id ON generated_letters (tenant_id);

-- ── company_policies ────────────────────────────────────────
CREATE TABLE company_policies (
    id                        UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id                 VARCHAR(50)   NOT NULL,
    created_by                VARCHAR(100),
    updated_by                VARCHAR(100),
    created_at                TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at                TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    is_deleted                BOOLEAN       NOT NULL DEFAULT FALSE,

    title                     VARCHAR(300)  NOT NULL,
    description               TEXT,
    policy_type               VARCHAR(100),
    content                   TEXT,
    version                   VARCHAR(20),
    effective_date            DATE,
    expiry_date               DATE,
    status                    VARCHAR(30)   NOT NULL DEFAULT 'DRAFT',
    requires_acknowledgement  BOOLEAN       NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_company_policies_tenant_id ON company_policies (tenant_id);
