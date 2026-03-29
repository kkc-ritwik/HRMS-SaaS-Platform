-- ─────────────────────────────────────────────────────────────────────────────
-- service-performance schema
-- ─────────────────────────────────────────────────────────────────────────────

-- ── Review cycles ─────────────────────────────────────────────────────────────
CREATE TABLE review_cycles (
    id                          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id                   VARCHAR(64) NOT NULL,
    name                        VARCHAR(200) NOT NULL,
    description                 TEXT,
    cycle_type                  VARCHAR(20) NOT NULL DEFAULT 'ANNUAL',
    fiscal_year                 VARCHAR(10),
    period_start                DATE        NOT NULL,
    period_end                  DATE        NOT NULL,
    status                      VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    self_review_deadline        DATE,
    manager_review_deadline     DATE,
    calibration_date            DATE,
    include_goal_rating         BOOLEAN     NOT NULL DEFAULT true,
    include_competency_rating   BOOLEAN     NOT NULL DEFAULT true,
    include_360_feedback        BOOLEAN     NOT NULL DEFAULT false,
    rating_scale                INT         NOT NULL DEFAULT 5,
    created_by                  VARCHAR(100),
    updated_by                  VARCHAR(100),
    created_at                  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_deleted                  BOOLEAN     NOT NULL DEFAULT false,
    CONSTRAINT chk_cycle_dates CHECK (period_end >= period_start)
);
CREATE INDEX idx_cycles_tenant ON review_cycles(tenant_id, status) WHERE is_deleted = false;

-- ── Goals (OKR: Objective → Key Results via parent_goal_id) ──────────────────
CREATE TABLE goals (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       VARCHAR(64) NOT NULL,
    employee_id     UUID        NOT NULL,
    manager_id      UUID,
    cycle_id        UUID        REFERENCES review_cycles(id),
    parent_goal_id  UUID        REFERENCES goals(id),
    goal_type       VARCHAR(20) NOT NULL DEFAULT 'INDIVIDUAL',
    title           VARCHAR(500) NOT NULL,
    description     TEXT,
    weightage       NUMERIC(5,2) NOT NULL DEFAULT 100,
    target_value    NUMERIC(12,2),
    current_value   NUMERIC(12,2) DEFAULT 0,
    unit            VARCHAR(50),
    progress        NUMERIC(5,2) NOT NULL DEFAULT 0,
    status          VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    priority        VARCHAR(10) NOT NULL DEFAULT 'MEDIUM',
    start_date      DATE,
    due_date        DATE,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_deleted      BOOLEAN     NOT NULL DEFAULT false,
    CONSTRAINT chk_goal_weightage CHECK (weightage BETWEEN 0 AND 100),
    CONSTRAINT chk_goal_progress  CHECK (progress BETWEEN 0 AND 100)
);
CREATE INDEX idx_goals_employee ON goals(tenant_id, employee_id, status) WHERE is_deleted = false;
CREATE INDEX idx_goals_cycle    ON goals(tenant_id, cycle_id)            WHERE is_deleted = false;
CREATE INDEX idx_goals_parent   ON goals(parent_goal_id)                 WHERE is_deleted = false;

-- ── Goal progress check-ins ────────────────────────────────────────────────────
CREATE TABLE goal_updates (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       VARCHAR(64) NOT NULL,
    goal_id         UUID        NOT NULL REFERENCES goals(id),
    progress_value  NUMERIC(5,2) NOT NULL,
    current_value   NUMERIC(12,2),
    comment         TEXT,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_deleted      BOOLEAN     NOT NULL DEFAULT false,
    CONSTRAINT chk_upd_progress CHECK (progress_value BETWEEN 0 AND 100)
);
CREATE INDEX idx_goal_updates_goal ON goal_updates(goal_id) WHERE is_deleted = false;

-- ── Competency library ────────────────────────────────────────────────────────
CREATE TABLE competencies (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id   VARCHAR(64) NOT NULL,
    name        VARCHAR(200) NOT NULL,
    description TEXT,
    category    VARCHAR(50) NOT NULL DEFAULT 'CORE',
    behaviors   JSONB       NOT NULL DEFAULT '[]',
    is_active   BOOLEAN     NOT NULL DEFAULT true,
    created_by  VARCHAR(100),
    updated_by  VARCHAR(100),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_deleted  BOOLEAN     NOT NULL DEFAULT false,
    CONSTRAINT uq_competency_name UNIQUE (tenant_id, name)
);
CREATE INDEX idx_competencies_tenant ON competencies(tenant_id, category) WHERE is_deleted = false;

-- ── Competency → role / department mapping ────────────────────────────────────
CREATE TABLE role_competencies (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       VARCHAR(64) NOT NULL,
    competency_id   UUID        NOT NULL REFERENCES competencies(id),
    role_id         UUID,
    department_id   UUID,
    expected_level  INT         NOT NULL DEFAULT 3,
    weightage       NUMERIC(5,2) NOT NULL DEFAULT 20,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_deleted      BOOLEAN     NOT NULL DEFAULT false,
    CONSTRAINT chk_exp_level CHECK (expected_level BETWEEN 1 AND 5)
);
CREATE INDEX idx_role_comp_competency ON role_competencies(competency_id) WHERE is_deleted = false;

-- ── Performance reviews ───────────────────────────────────────────────────────
CREATE TABLE reviews (
    id                  UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           VARCHAR(64) NOT NULL,
    cycle_id            UUID        NOT NULL REFERENCES review_cycles(id),
    employee_id         UUID        NOT NULL,
    manager_id          UUID,
    reviewer_id         UUID        NOT NULL,
    review_type         VARCHAR(20) NOT NULL DEFAULT 'SELF',
    status              VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    overall_rating      NUMERIC(4,2),
    potential_rating    INT         CHECK (potential_rating BETWEEN 1 AND 5),
    performance_rating  NUMERIC(4,2),
    goal_score          NUMERIC(5,2),
    competency_score    NUMERIC(5,2),
    strengths           TEXT,
    development_areas   TEXT,
    manager_comments    TEXT,
    final_comments      TEXT,
    submitted_at        TIMESTAMPTZ,
    acknowledged_at     TIMESTAMPTZ,
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_deleted          BOOLEAN     NOT NULL DEFAULT false,
    CONSTRAINT uq_review UNIQUE (tenant_id, cycle_id, employee_id, reviewer_id, review_type)
);
CREATE INDEX idx_reviews_cycle    ON reviews(tenant_id, cycle_id)    WHERE is_deleted = false;
CREATE INDEX idx_reviews_employee ON reviews(tenant_id, employee_id) WHERE is_deleted = false;

-- ── Per-competency / per-goal ratings within a review ─────────────────────────
CREATE TABLE review_ratings (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       VARCHAR(64) NOT NULL,
    review_id       UUID        NOT NULL REFERENCES reviews(id),
    competency_id   UUID        REFERENCES competencies(id),
    goal_id         UUID        REFERENCES goals(id),
    rating_type     VARCHAR(20) NOT NULL,  -- COMPETENCY, GOAL, OVERALL
    rating          NUMERIC(4,2) NOT NULL,
    comments        TEXT,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_deleted      BOOLEAN     NOT NULL DEFAULT false
);
CREATE INDEX idx_ratings_review ON review_ratings(review_id) WHERE is_deleted = false;

-- ── Continuous / 360-degree feedback ──────────────────────────────────────────
CREATE TABLE feedback (
    id                  UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           VARCHAR(64) NOT NULL,
    from_employee_id    UUID        NOT NULL,
    to_employee_id      UUID        NOT NULL,
    feedback_type       VARCHAR(20) NOT NULL DEFAULT 'APPRECIATION',
    visibility          VARCHAR(20) NOT NULL DEFAULT 'PUBLIC',
    cycle_id            UUID        REFERENCES review_cycles(id),
    context             VARCHAR(200),
    message             TEXT        NOT NULL,
    tags                JSONB       NOT NULL DEFAULT '[]',
    is_anonymous        BOOLEAN     NOT NULL DEFAULT false,
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_deleted          BOOLEAN     NOT NULL DEFAULT false
);
CREATE INDEX idx_feedback_to   ON feedback(tenant_id, to_employee_id)   WHERE is_deleted = false;
CREATE INDEX idx_feedback_from ON feedback(tenant_id, from_employee_id) WHERE is_deleted = false;

-- ── 1-on-1 meetings ───────────────────────────────────────────────────────────
CREATE TABLE one_on_ones (
    id                  UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           VARCHAR(64) NOT NULL,
    manager_id          UUID        NOT NULL,
    employee_id         UUID        NOT NULL,
    scheduled_at        TIMESTAMPTZ NOT NULL,
    duration_minutes    INT         NOT NULL DEFAULT 30,
    status              VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    agenda              TEXT,
    manager_notes       TEXT,
    employee_notes      TEXT,
    action_items        JSONB       NOT NULL DEFAULT '[]',
    next_meeting_date   DATE,
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_deleted          BOOLEAN     NOT NULL DEFAULT false
);
CREATE INDEX idx_one_on_ones_mgr ON one_on_ones(tenant_id, manager_id)  WHERE is_deleted = false;
CREATE INDEX idx_one_on_ones_emp ON one_on_ones(tenant_id, employee_id) WHERE is_deleted = false;

-- ── Performance Improvement Plans ─────────────────────────────────────────────
CREATE TABLE pip_plans (
    id                  UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           VARCHAR(64) NOT NULL,
    employee_id         UUID        NOT NULL,
    manager_id          UUID        NOT NULL,
    hr_manager_id       UUID,
    review_cycle_id     UUID        REFERENCES review_cycles(id),
    title               VARCHAR(200) NOT NULL,
    reason              TEXT,
    start_date          DATE        NOT NULL,
    end_date            DATE        NOT NULL,
    status              VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    improvement_areas   JSONB       NOT NULL DEFAULT '[]',
    support_provided    TEXT,
    check_in_frequency  VARCHAR(20) NOT NULL DEFAULT 'WEEKLY',
    outcome_notes       TEXT,
    closed_at           TIMESTAMPTZ,
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_deleted          BOOLEAN     NOT NULL DEFAULT false,
    CONSTRAINT chk_pip_dates CHECK (end_date > start_date)
);
CREATE INDEX idx_pip_employee ON pip_plans(tenant_id, employee_id, status) WHERE is_deleted = false;
