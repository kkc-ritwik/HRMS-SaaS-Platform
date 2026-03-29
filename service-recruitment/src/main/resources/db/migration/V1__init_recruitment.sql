-- ─────────────────────────────────────────────────────────────────────────────
-- service-recruitment schema
-- ─────────────────────────────────────────────────────────────────────────────

-- ── Recruitment agencies ──────────────────────────────────────────────────────
CREATE TABLE recruitment_agencies (
    id               UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id        VARCHAR(64) NOT NULL,
    name             VARCHAR(200) NOT NULL,
    contact_person   VARCHAR(100),
    contact_email    VARCHAR(200),
    contact_phone    VARCHAR(20),
    commission_percent NUMERIC(5,2) NOT NULL DEFAULT 0,
    is_active        BOOLEAN NOT NULL DEFAULT true,
    created_by       VARCHAR(100),
    updated_by       VARCHAR(100),
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_deleted       BOOLEAN NOT NULL DEFAULT false
);
CREATE INDEX idx_agencies_tenant ON recruitment_agencies(tenant_id) WHERE is_deleted = false;

-- ── Job requisitions ──────────────────────────────────────────────────────────
CREATE TABLE job_requisitions (
    id                   UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id            VARCHAR(64) NOT NULL,
    title                VARCHAR(200) NOT NULL,
    department_id        UUID,                           -- reference to core-hr
    location             VARCHAR(200),
    employment_type      VARCHAR(30)  NOT NULL DEFAULT 'FULL_TIME',
    positions_count      INT          NOT NULL DEFAULT 1,
    positions_filled     INT          NOT NULL DEFAULT 0,
    description          TEXT,
    requirements         TEXT,
    salary_min           NUMERIC(14,2),
    salary_max           NUMERIC(14,2),
    status               VARCHAR(30)  NOT NULL DEFAULT 'DRAFT',
    priority             VARCHAR(10)  NOT NULL DEFAULT 'MEDIUM',
    target_date          DATE,
    source               VARCHAR(50),                    -- INTERNAL, AGENCY, JOB_PORTAL, etc.
    agency_id            UUID REFERENCES recruitment_agencies(id),
    requested_by         VARCHAR(100),
    approved_by          VARCHAR(100),
    approved_at          TIMESTAMPTZ,
    created_by           VARCHAR(100),
    updated_by           VARCHAR(100),
    created_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_deleted           BOOLEAN NOT NULL DEFAULT false,
    CONSTRAINT chk_positions CHECK (positions_count >= 1),
    CONSTRAINT chk_salary    CHECK (salary_min IS NULL OR salary_max IS NULL OR salary_max >= salary_min)
);
CREATE INDEX idx_requisitions_tenant_status ON job_requisitions(tenant_id, status) WHERE is_deleted = false;
CREATE INDEX idx_requisitions_dept         ON job_requisitions(tenant_id, department_id) WHERE is_deleted = false;

-- ── Candidates ────────────────────────────────────────────────────────────────
CREATE TABLE candidates (
    id                      UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id               VARCHAR(64) NOT NULL,
    first_name              VARCHAR(100) NOT NULL,
    last_name               VARCHAR(100) NOT NULL,
    email                   VARCHAR(200) NOT NULL,
    phone                   VARCHAR(20),
    current_company         VARCHAR(200),
    current_title           VARCHAR(200),
    total_experience_years  NUMERIC(4,1),
    resume_url              VARCHAR(1000),              -- MinIO presigned URL / key
    linkedin_url            VARCHAR(500),
    source                  VARCHAR(50),                -- AGENCY, REFERRAL, JOB_PORTAL, etc.
    agency_id               UUID REFERENCES recruitment_agencies(id),
    referred_by             UUID,                       -- employee UUID
    tags                    JSONB DEFAULT '[]',
    created_by              VARCHAR(100),
    updated_by              VARCHAR(100),
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_deleted              BOOLEAN NOT NULL DEFAULT false,
    CONSTRAINT uq_candidate_email UNIQUE (tenant_id, email)
);
CREATE INDEX idx_candidates_tenant  ON candidates(tenant_id) WHERE is_deleted = false;
CREATE INDEX idx_candidates_email   ON candidates(tenant_id, email) WHERE is_deleted = false;
CREATE INDEX idx_candidates_source  ON candidates(tenant_id, source) WHERE is_deleted = false;

-- ── Applications ──────────────────────────────────────────────────────────────
CREATE TABLE applications (
    id                  UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           VARCHAR(64) NOT NULL,
    requisition_id      UUID        NOT NULL REFERENCES job_requisitions(id),
    candidate_id        UUID        NOT NULL REFERENCES candidates(id),
    stage               VARCHAR(20) NOT NULL DEFAULT 'APPLIED',
    rejection_reason    VARCHAR(500),
    applied_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    stage_changed_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    current_ctc         NUMERIC(14,2),
    expected_ctc        NUMERIC(14,2),
    notice_period_days  INT,
    notes               TEXT,
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_deleted          BOOLEAN NOT NULL DEFAULT false,
    CONSTRAINT uq_application UNIQUE (tenant_id, requisition_id, candidate_id)
);
CREATE INDEX idx_applications_tenant_req   ON applications(tenant_id, requisition_id) WHERE is_deleted = false;
CREATE INDEX idx_applications_tenant_cand  ON applications(tenant_id, candidate_id)   WHERE is_deleted = false;
CREATE INDEX idx_applications_stage        ON applications(tenant_id, stage)           WHERE is_deleted = false;

-- ── Interviews ────────────────────────────────────────────────────────────────
CREATE TABLE interviews (
    id               UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id        VARCHAR(64) NOT NULL,
    application_id   UUID        NOT NULL REFERENCES applications(id),
    interview_type   VARCHAR(30) NOT NULL,   -- PHONE_SCREEN, TECHNICAL, HR, PANEL, FINAL
    round_number     INT         NOT NULL DEFAULT 1,
    scheduled_at     TIMESTAMPTZ NOT NULL,
    duration_minutes INT         NOT NULL DEFAULT 60,
    mode             VARCHAR(20) NOT NULL DEFAULT 'VIDEO',  -- IN_PERSON, VIDEO, PHONE
    meeting_link     VARCHAR(1000),
    venue            VARCHAR(500),
    status           VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED', -- SCHEDULED, COMPLETED, CANCELLED, NO_SHOW
    overall_rating   INT         CHECK (overall_rating BETWEEN 1 AND 5),
    recommendation   VARCHAR(20),    -- STRONGLY_HIRE, HIRE, NEUTRAL, NO_HIRE, STRONG_NO_HIRE
    feedback         TEXT,
    created_by       VARCHAR(100),
    updated_by       VARCHAR(100),
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_deleted       BOOLEAN NOT NULL DEFAULT false
);
CREATE INDEX idx_interviews_application ON interviews(application_id) WHERE is_deleted = false;
CREATE INDEX idx_interviews_tenant      ON interviews(tenant_id, status) WHERE is_deleted = false;

-- ── Interview panelists ───────────────────────────────────────────────────────
CREATE TABLE interview_panelists (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       VARCHAR(64) NOT NULL,
    interview_id    UUID        NOT NULL REFERENCES interviews(id),
    interviewer_id  UUID        NOT NULL,
    role            VARCHAR(20) NOT NULL DEFAULT 'PANELIST',  -- LEAD, PANELIST, SHADOW
    rating          INT         CHECK (rating BETWEEN 1 AND 5),
    feedback        TEXT,
    submitted_at    TIMESTAMPTZ,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_deleted      BOOLEAN NOT NULL DEFAULT false,
    CONSTRAINT uq_panelist UNIQUE (interview_id, interviewer_id)
);
CREATE INDEX idx_panelists_interview ON interview_panelists(interview_id) WHERE is_deleted = false;

-- ── Offer letters ─────────────────────────────────────────────────────────────
CREATE TABLE offer_letters (
    id                UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id         VARCHAR(64) NOT NULL,
    application_id    UUID        NOT NULL REFERENCES applications(id),
    offered_ctc       NUMERIC(14,2) NOT NULL,
    offered_title     VARCHAR(200) NOT NULL,
    joining_date      DATE,
    offer_expiry_date DATE,
    template_used     VARCHAR(100),
    content           TEXT,
    status            VARCHAR(20) NOT NULL DEFAULT 'DRAFT',  -- DRAFT, SENT, ACCEPTED, DECLINED, EXPIRED, REVOKED
    sent_at           TIMESTAMPTZ,
    responded_at      TIMESTAMPTZ,
    response_notes    VARCHAR(500),
    created_by        VARCHAR(100),
    updated_by        VARCHAR(100),
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_deleted        BOOLEAN NOT NULL DEFAULT false
);
CREATE INDEX idx_offers_application ON offer_letters(application_id) WHERE is_deleted = false;
CREATE INDEX idx_offers_tenant      ON offer_letters(tenant_id, status) WHERE is_deleted = false;
