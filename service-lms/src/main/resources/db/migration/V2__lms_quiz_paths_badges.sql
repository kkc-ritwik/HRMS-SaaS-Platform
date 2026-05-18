CREATE TABLE IF NOT EXISTS lms_questions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       VARCHAR(100) NOT NULL,
    assessment_id   UUID NOT NULL,
    text            VARCHAR(2000) NOT NULL,
    type            VARCHAR(30) NOT NULL,
    options         JSONB,
    correct_answers JSONB,
    points          INTEGER NOT NULL DEFAULT 1,
    explanation     VARCHAR(2000),
    display_order   INTEGER,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted         BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS lms_quiz_attempts (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           VARCHAR(100) NOT NULL,
    assessment_id       UUID NOT NULL,
    employee_id         UUID NOT NULL,
    attempt_number      INTEGER NOT NULL,
    started_at          TIMESTAMPTZ,
    submitted_at        TIMESTAMPTZ,
    time_spent_seconds  INTEGER,
    score               NUMERIC(6,2),
    max_score           NUMERIC(6,2),
    score_percent       NUMERIC(5,2),
    passed              BOOLEAN,
    answers             JSONB,
    grading             JSONB,
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted             BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX IF NOT EXISTS ix_attempt_emp_assess ON lms_quiz_attempts (tenant_id, employee_id, assessment_id);

CREATE TABLE IF NOT EXISTS lms_learning_paths (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id          VARCHAR(100) NOT NULL,
    name               VARCHAR(200) NOT NULL,
    description        VARCHAR(2000),
    category           VARCHAR(100),
    is_mandatory       BOOLEAN,
    audience_filter    JSONB,
    steps              JSONB,
    is_active          BOOLEAN NOT NULL DEFAULT TRUE,
    created_by         VARCHAR(100),
    updated_by         VARCHAR(100),
    created_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted            BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS lms_badges (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id      VARCHAR(100) NOT NULL,
    code           VARCHAR(50) NOT NULL,
    name           VARCHAR(200) NOT NULL,
    description    VARCHAR(1000),
    icon_url       VARCHAR(1000),
    color_hex      VARCHAR(9),
    points         INTEGER,
    trigger_type   VARCHAR(50),
    trigger_ref_id UUID,
    is_active      BOOLEAN,
    created_by     VARCHAR(100),
    updated_by     VARCHAR(100),
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted        BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE (tenant_id, code)
);

CREATE TABLE IF NOT EXISTS lms_badge_awards (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id            VARCHAR(100) NOT NULL,
    badge_id             UUID NOT NULL REFERENCES lms_badges(id) ON DELETE CASCADE,
    employee_id          UUID NOT NULL,
    awarded_at           TIMESTAMPTZ NOT NULL,
    trigger_event        VARCHAR(100),
    is_visible_publicly  BOOLEAN,
    created_by           VARCHAR(100),
    updated_by           VARCHAR(100),
    created_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted              BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE (tenant_id, badge_id, employee_id)
);
