CREATE TABLE form_definitions (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id             VARCHAR(100) NOT NULL,
    code                  VARCHAR(100) NOT NULL,
    title                 VARCHAR(200) NOT NULL,
    description           VARCHAR(1000),
    version               INTEGER NOT NULL,
    status                VARCHAR(30),
    fields                JSONB,
    workflow_config       JSONB,
    is_anonymous_allowed  BOOLEAN,
    is_active             BOOLEAN,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (tenant_id, code, version)
);

CREATE TABLE form_submissions (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id             VARCHAR(100) NOT NULL,
    form_id               UUID NOT NULL REFERENCES form_definitions(id) ON DELETE CASCADE,
    submitter_id          UUID,
    is_anonymous          BOOLEAN,
    answers               JSONB,
    workflow_instance_id  UUID,
    submitted_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX ix_sub_form      ON form_submissions (form_id);
CREATE INDEX ix_sub_submitter ON form_submissions (tenant_id, submitter_id);
