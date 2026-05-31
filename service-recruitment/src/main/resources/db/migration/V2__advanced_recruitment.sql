-- Reference checks, alumni network, internal mobility, hiring loops, scorecards,
-- psychometric assessments, and recruitment cost ledger.

CREATE TABLE IF NOT EXISTS reference_checks (
    id                       UUID PRIMARY KEY,
    tenant_id                VARCHAR(100) NOT NULL,
    created_by               VARCHAR(100),
    updated_by               VARCHAR(100),
    created_at               TIMESTAMP WITH TIME ZONE,
    updated_at               TIMESTAMP WITH TIME ZONE,
    is_deleted               BOOLEAN NOT NULL DEFAULT FALSE,

    candidate_id             UUID NOT NULL,
    application_id           UUID,
    referee_name             VARCHAR(200) NOT NULL,
    referee_relationship     VARCHAR(100),
    referee_company          VARCHAR(200),
    referee_email            VARCHAR(300),
    referee_phone            VARCHAR(200),
    invite_token             VARCHAR(200),
    invited_at               TIMESTAMP WITH TIME ZONE,
    responded_at             TIMESTAMP WITH TIME ZONE,
    status                   VARCHAR(20) NOT NULL DEFAULT 'INVITED',
    rating                   INTEGER,
    would_rehire             BOOLEAN,
    structured_feedback      JSONB,
    free_text_feedback       TEXT,
    requested_by_employee_id UUID
);
CREATE INDEX IF NOT EXISTS ix_refcheck_candidate ON reference_checks(tenant_id, candidate_id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_refcheck_token ON reference_checks(invite_token);

CREATE TABLE IF NOT EXISTS recruit_alumni (
    id                       UUID PRIMARY KEY,
    tenant_id                VARCHAR(100) NOT NULL,
    created_by               VARCHAR(100),
    updated_by               VARCHAR(100),
    created_at               TIMESTAMP WITH TIME ZONE,
    updated_at               TIMESTAMP WITH TIME ZONE,
    is_deleted               BOOLEAN NOT NULL DEFAULT FALSE,

    former_employee_id       UUID NOT NULL,
    full_name                VARCHAR(200) NOT NULL,
    personal_email           VARCHAR(500),
    phone                    VARCHAR(500),
    linkedin_url             VARCHAR(500),
    last_designation         VARCHAR(200),
    last_department          VARCHAR(200),
    hire_date                DATE,
    exit_date                DATE,
    tenure_years             INTEGER,
    exit_reason              VARCHAR(500),
    rehire_recommended       BOOLEAN,
    boomerang_eligible       BOOLEAN,
    do_not_rehire            BOOLEAN,
    do_not_rehire_reason     VARCHAR(1000),
    opt_in_alumni_comms      BOOLEAN,
    opt_in_job_alerts        BOOLEAN,
    rejoined_on              DATE,
    rejoined_employee_id     UUID
);
CREATE INDEX IF NOT EXISTS ix_alumni_email ON recruit_alumni(tenant_id, personal_email);

CREATE TABLE IF NOT EXISTS recruit_internal_applications (
    id                  UUID PRIMARY KEY,
    tenant_id           VARCHAR(100) NOT NULL,
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100),
    created_at          TIMESTAMP WITH TIME ZONE,
    updated_at          TIMESTAMP WITH TIME ZONE,
    is_deleted          BOOLEAN NOT NULL DEFAULT FALSE,

    employee_id         UUID NOT NULL,
    requisition_id      UUID NOT NULL,
    current_manager_id  UUID,
    manager_notified    BOOLEAN,
    confidential_mode   BOOLEAN,
    cover_note          TEXT,
    resume_uri          VARCHAR(1000),
    status              VARCHAR(30) NOT NULL DEFAULT 'SUBMITTED',
    submitted_at        TIMESTAMP WITH TIME ZONE,
    decision_at         TIMESTAMP WITH TIME ZONE,
    decision_reason     TEXT
);
CREATE INDEX IF NOT EXISTS ix_iapp_emp ON recruit_internal_applications(tenant_id, employee_id);
CREATE INDEX IF NOT EXISTS ix_iapp_req ON recruit_internal_applications(requisition_id);

CREATE TABLE IF NOT EXISTS recruit_scorecard_templates (
    id                       UUID PRIMARY KEY,
    tenant_id                VARCHAR(100) NOT NULL,
    created_by               VARCHAR(100),
    updated_by               VARCHAR(100),
    created_at               TIMESTAMP WITH TIME ZONE,
    updated_at               TIMESTAMP WITH TIME ZONE,
    is_deleted               BOOLEAN NOT NULL DEFAULT FALSE,

    code                     VARCHAR(100) NOT NULL,
    name                     VARCHAR(200) NOT NULL,
    description              TEXT,
    round_type               VARCHAR(50),
    job_family               VARCHAR(100),
    level                    VARCHAR(30),
    competencies             JSONB,
    min_overall_to_advance   INTEGER,
    duration_minutes         INTEGER,
    active                   BOOLEAN DEFAULT TRUE
);
CREATE INDEX IF NOT EXISTS ix_sct_tenant_active ON recruit_scorecard_templates(tenant_id, active);

CREATE TABLE IF NOT EXISTS recruit_interview_scorecards (
    id                  UUID PRIMARY KEY,
    tenant_id           VARCHAR(100) NOT NULL,
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100),
    created_at          TIMESTAMP WITH TIME ZONE,
    updated_at          TIMESTAMP WITH TIME ZONE,
    is_deleted          BOOLEAN NOT NULL DEFAULT FALSE,

    interview_id        UUID NOT NULL,
    application_id      UUID NOT NULL,
    candidate_id        UUID NOT NULL,
    panellist_id        UUID NOT NULL,
    template_id         UUID,
    competency_scores   JSONB,
    overall_score       INTEGER,
    recommendation      VARCHAR(20),
    strengths           TEXT,
    areas_of_concern    TEXT,
    would_work_with     BOOLEAN,
    private_notes       TEXT,
    submitted_at        TIMESTAMP WITH TIME ZONE,
    locked              BOOLEAN DEFAULT FALSE
);
CREATE INDEX IF NOT EXISTS ix_score_interview   ON recruit_interview_scorecards(interview_id);
CREATE INDEX IF NOT EXISTS ix_score_application ON recruit_interview_scorecards(tenant_id, application_id);

CREATE TABLE IF NOT EXISTS recruit_hiring_loops (
    id              UUID PRIMARY KEY,
    tenant_id       VARCHAR(100) NOT NULL,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    created_at      TIMESTAMP WITH TIME ZONE,
    updated_at      TIMESTAMP WITH TIME ZONE,
    is_deleted      BOOLEAN NOT NULL DEFAULT FALSE,

    application_id  UUID NOT NULL,
    candidate_id    UUID NOT NULL,
    requisition_id  UUID,
    loop_date       DATE,
    location        VARCHAR(200),
    format          VARCHAR(20),
    panel           JSONB,
    schedule        JSONB,
    debrief_notes   JSONB,
    status          VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    outcome         VARCHAR(20)
);
CREATE INDEX IF NOT EXISTS ix_loop_app ON recruit_hiring_loops(tenant_id, application_id);

CREATE TABLE IF NOT EXISTS recruit_psychometric_assessments (
    id                UUID PRIMARY KEY,
    tenant_id         VARCHAR(100) NOT NULL,
    created_by        VARCHAR(100),
    updated_by        VARCHAR(100),
    created_at        TIMESTAMP WITH TIME ZONE,
    updated_at        TIMESTAMP WITH TIME ZONE,
    is_deleted        BOOLEAN NOT NULL DEFAULT FALSE,

    candidate_id      UUID NOT NULL,
    application_id    UUID,
    vendor            VARCHAR(30) NOT NULL,
    test_code         VARCHAR(100) NOT NULL,
    test_name         VARCHAR(200),
    invite_token      VARCHAR(200),
    invite_url        VARCHAR(1000),
    invited_at        TIMESTAMP WITH TIME ZONE,
    started_at        TIMESTAMP WITH TIME ZONE,
    completed_at      TIMESTAMP WITH TIME ZONE,
    expires_at        TIMESTAMP WITH TIME ZONE,
    status            VARCHAR(20) NOT NULL DEFAULT 'INVITED',
    overall_score     INTEGER,
    percentile        INTEGER,
    recommendation    VARCHAR(30),
    section_scores    JSONB,
    raw_payload       JSONB,
    report_uri        VARCHAR(1000)
);
CREATE INDEX IF NOT EXISTS ix_psych_candidate ON recruit_psychometric_assessments(tenant_id, candidate_id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_psych_token ON recruit_psychometric_assessments(invite_token);

CREATE TABLE IF NOT EXISTS recruit_cost_entries (
    id                      UUID PRIMARY KEY,
    tenant_id               VARCHAR(100) NOT NULL,
    created_by              VARCHAR(100),
    updated_by              VARCHAR(100),
    created_at              TIMESTAMP WITH TIME ZONE,
    updated_at              TIMESTAMP WITH TIME ZONE,
    is_deleted              BOOLEAN NOT NULL DEFAULT FALSE,

    category                VARCHAR(30) NOT NULL,
    amount                  NUMERIC(14,2) NOT NULL,
    currency                VARCHAR(3) NOT NULL DEFAULT 'INR',
    incurred_on             DATE NOT NULL,
    requisition_id          UUID,
    candidate_id            UUID,
    agency_id               UUID,
    referrer_employee_id    UUID,
    description             VARCHAR(500),
    invoice_reference       VARCHAR(100)
);
CREATE INDEX IF NOT EXISTS ix_recruit_cost_date ON recruit_cost_entries(tenant_id, incurred_on);
