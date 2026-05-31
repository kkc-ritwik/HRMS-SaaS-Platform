-- Mood / eNPS pulse, anonymous suggestion box, stay interviews, awards, rewards budget
-- and catalog, redemptions, wellness programs and activity logs.

CREATE TABLE IF NOT EXISTS engagement_mood_checkins (
    id              UUID PRIMARY KEY,
    tenant_id       VARCHAR(100) NOT NULL,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    created_at      TIMESTAMP WITH TIME ZONE,
    updated_at      TIMESTAMP WITH TIME ZONE,
    is_deleted      BOOLEAN NOT NULL DEFAULT FALSE,

    employee_id     UUID NOT NULL,
    check_in_date   DATE NOT NULL,
    score           INTEGER NOT NULL,
    comment         VARCHAR(2000),
    category        VARCHAR(30),
    is_anonymous    BOOLEAN,
    theme           VARCHAR(30)
);
CREATE INDEX IF NOT EXISTS ix_mood_emp_date    ON engagement_mood_checkins(tenant_id, employee_id, check_in_date);
CREATE INDEX IF NOT EXISTS ix_mood_tenant_date ON engagement_mood_checkins(tenant_id, check_in_date);

CREATE TABLE IF NOT EXISTS engagement_suggestions (
    id                             UUID PRIMARY KEY,
    tenant_id                      VARCHAR(100) NOT NULL,
    created_by                     VARCHAR(100),
    updated_by                     VARCHAR(100),
    created_at                     TIMESTAMP WITH TIME ZONE,
    updated_at                     TIMESTAMP WITH TIME ZONE,
    is_deleted                     BOOLEAN NOT NULL DEFAULT FALSE,

    title                          VARCHAR(200) NOT NULL,
    description                    VARCHAR(5000) NOT NULL,
    submitted_by_encrypted         VARCHAR(500),
    category                       VARCHAR(30) NOT NULL,
    status                         VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    votes_up                       INTEGER DEFAULT 0,
    votes_down                     INTEGER DEFAULT 0,
    comments_count                 INTEGER DEFAULT 0,
    assigned_reviewer_id           UUID,
    resolution                     VARCHAR(2000),
    resolved_at                    TIMESTAMP WITH TIME ZONE
);
CREATE INDEX IF NOT EXISTS ix_sugg_tenant_status ON engagement_suggestions(tenant_id, status);
CREATE INDEX IF NOT EXISTS ix_sugg_category     ON engagement_suggestions(tenant_id, category);

CREATE TABLE IF NOT EXISTS engagement_suggestion_votes (
    id                  UUID PRIMARY KEY,
    tenant_id           VARCHAR(100) NOT NULL,
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100),
    created_at          TIMESTAMP WITH TIME ZONE,
    updated_at          TIMESTAMP WITH TIME ZONE,
    is_deleted          BOOLEAN NOT NULL DEFAULT FALSE,

    suggestion_id       UUID NOT NULL,
    voter_employee_id   UUID NOT NULL,
    direction           VARCHAR(4) NOT NULL,
    CONSTRAINT uq_sugg_vote UNIQUE (suggestion_id, voter_employee_id)
);
CREATE INDEX IF NOT EXISTS ix_vote_sugg ON engagement_suggestion_votes(suggestion_id);

CREATE TABLE IF NOT EXISTS engagement_stay_interviews (
    id                   UUID PRIMARY KEY,
    tenant_id            VARCHAR(100) NOT NULL,
    created_by           VARCHAR(100),
    updated_by           VARCHAR(100),
    created_at           TIMESTAMP WITH TIME ZONE,
    updated_at           TIMESTAMP WITH TIME ZONE,
    is_deleted           BOOLEAN NOT NULL DEFAULT FALSE,

    employee_id          UUID NOT NULL,
    interviewer_id       UUID NOT NULL,
    trigger              VARCHAR(30) NOT NULL,
    scheduled_date       DATE,
    conducted_at         TIMESTAMP WITH TIME ZONE,
    status               VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    engagement_score     INTEGER,
    flight_risk          VARCHAR(10),
    responses            JSONB,
    notes                VARCHAR(5000),
    action_items         JSONB,
    next_review_date     DATE
);
CREATE INDEX IF NOT EXISTS ix_stay_employee ON engagement_stay_interviews(tenant_id, employee_id);
CREATE INDEX IF NOT EXISTS ix_stay_status   ON engagement_stay_interviews(tenant_id, status, scheduled_date);

CREATE TABLE IF NOT EXISTS engagement_stay_questions (
    id                    UUID PRIMARY KEY,
    tenant_id             VARCHAR(100) NOT NULL,
    created_by            VARCHAR(100),
    updated_by            VARCHAR(100),
    created_at            TIMESTAMP WITH TIME ZONE,
    updated_at            TIMESTAMP WITH TIME ZONE,
    is_deleted            BOOLEAN NOT NULL DEFAULT FALSE,

    code                  VARCHAR(60) NOT NULL,
    question_text         VARCHAR(1000) NOT NULL,
    response_type         VARCHAR(20) NOT NULL,
    is_required           BOOLEAN,
    is_active             BOOLEAN DEFAULT TRUE,
    display_order         INTEGER,
    applies_to_triggers   VARCHAR(200)
);
CREATE INDEX IF NOT EXISTS ix_stayq_tenant_active ON engagement_stay_questions(tenant_id, is_active);

CREATE TABLE IF NOT EXISTS engagement_awards (
    id                          UUID PRIMARY KEY,
    tenant_id                   VARCHAR(100) NOT NULL,
    created_by                  VARCHAR(100),
    updated_by                  VARCHAR(100),
    created_at                  TIMESTAMP WITH TIME ZONE,
    updated_at                  TIMESTAMP WITH TIME ZONE,
    is_deleted                  BOOLEAN NOT NULL DEFAULT FALSE,

    award_type                  VARCHAR(30) NOT NULL,
    program_id                  UUID,
    recipient_employee_id       UUID NOT NULL,
    nominated_by_employee_id    UUID,
    approved_by_employee_id     UUID,
    title                       VARCHAR(200) NOT NULL,
    citation                    VARCHAR(2000),
    monetary_value              NUMERIC(12,2),
    currency                    VARCHAR(3),
    points                      INTEGER,
    awarded_on                  DATE,
    certificate_uri             VARCHAR(1000),
    status                      VARCHAR(20) NOT NULL DEFAULT 'NOMINATED'
);
CREATE INDEX IF NOT EXISTS ix_award_recipient ON engagement_awards(tenant_id, recipient_employee_id);

CREATE TABLE IF NOT EXISTS engagement_rewards_budgets (
    id                  UUID PRIMARY KEY,
    tenant_id           VARCHAR(100) NOT NULL,
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100),
    created_at          TIMESTAMP WITH TIME ZONE,
    updated_at          TIMESTAMP WITH TIME ZONE,
    is_deleted          BOOLEAN NOT NULL DEFAULT FALSE,

    name                VARCHAR(200) NOT NULL,
    owner_manager_id    UUID,
    department_id       UUID,
    fiscal_year         VARCHAR(10),
    period_start        DATE NOT NULL,
    period_end          DATE NOT NULL,
    allocated_amount    NUMERIC(14,2) NOT NULL,
    consumed_amount     NUMERIC(14,2) NOT NULL DEFAULT 0,
    currency            VARCHAR(3) NOT NULL DEFAULT 'INR',
    allow_overdraft     BOOLEAN DEFAULT FALSE,
    active              BOOLEAN DEFAULT TRUE
);
CREATE INDEX IF NOT EXISTS ix_rbudget_owner ON engagement_rewards_budgets(tenant_id, owner_manager_id);
CREATE INDEX IF NOT EXISTS ix_rbudget_dept  ON engagement_rewards_budgets(tenant_id, department_id);

CREATE TABLE IF NOT EXISTS engagement_catalog_items (
    id                  UUID PRIMARY KEY,
    tenant_id           VARCHAR(100) NOT NULL,
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100),
    created_at          TIMESTAMP WITH TIME ZONE,
    updated_at          TIMESTAMP WITH TIME ZONE,
    is_deleted          BOOLEAN NOT NULL DEFAULT FALSE,

    sku                 VARCHAR(50) NOT NULL,
    title               VARCHAR(200) NOT NULL,
    description         VARCHAR(2000),
    image_url           VARCHAR(500),
    category            VARCHAR(30) NOT NULL,
    points_cost         INTEGER NOT NULL,
    monetary_value      NUMERIC(12,2),
    currency            VARCHAR(3),
    stock_qty           INTEGER,
    vendor_name         VARCHAR(200),
    vendor_sku          VARCHAR(100),
    is_active           BOOLEAN DEFAULT TRUE,
    requires_shipping   BOOLEAN
);
CREATE INDEX IF NOT EXISTS ix_cat_item_active ON engagement_catalog_items(tenant_id, is_active);

CREATE TABLE IF NOT EXISTS engagement_redemptions (
    id                       UUID PRIMARY KEY,
    tenant_id                VARCHAR(100) NOT NULL,
    created_by               VARCHAR(100),
    updated_by               VARCHAR(100),
    created_at               TIMESTAMP WITH TIME ZONE,
    updated_at               TIMESTAMP WITH TIME ZONE,
    is_deleted               BOOLEAN NOT NULL DEFAULT FALSE,

    employee_id              UUID NOT NULL,
    catalog_item_id          UUID NOT NULL,
    quantity                 INTEGER NOT NULL DEFAULT 1,
    points_spent             INTEGER NOT NULL,
    shipping_address         VARCHAR(1000),
    status                   VARCHAR(20) NOT NULL DEFAULT 'REQUESTED',
    voucher_code_encrypted   VARCHAR(500),
    fulfilled_at             TIMESTAMP WITH TIME ZONE,
    cancelled_at             TIMESTAMP WITH TIME ZONE,
    cancellation_reason      VARCHAR(500)
);
CREATE INDEX IF NOT EXISTS ix_redeem_emp ON engagement_redemptions(tenant_id, employee_id);

CREATE TABLE IF NOT EXISTS engagement_wellness_programs (
    id                   UUID PRIMARY KEY,
    tenant_id            VARCHAR(100) NOT NULL,
    created_by           VARCHAR(100),
    updated_by           VARCHAR(100),
    created_at           TIMESTAMP WITH TIME ZONE,
    updated_at           TIMESTAMP WITH TIME ZONE,
    is_deleted           BOOLEAN NOT NULL DEFAULT FALSE,

    code                 VARCHAR(50) NOT NULL,
    name                 VARCHAR(200) NOT NULL,
    description          VARCHAR(2000),
    category             VARCHAR(30) NOT NULL,
    start_date           DATE,
    end_date             DATE,
    goal_metric          VARCHAR(50),
    goal_target          BIGINT,
    points_per_unit      INTEGER,
    max_points_per_day   INTEGER,
    image_url            VARCHAR(500),
    active               BOOLEAN DEFAULT TRUE
);
CREATE INDEX IF NOT EXISTS ix_well_active ON engagement_wellness_programs(tenant_id, active);

CREATE TABLE IF NOT EXISTS engagement_wellness_logs (
    id             UUID PRIMARY KEY,
    tenant_id      VARCHAR(100) NOT NULL,
    created_by     VARCHAR(100),
    updated_by     VARCHAR(100),
    created_at     TIMESTAMP WITH TIME ZONE,
    updated_at     TIMESTAMP WITH TIME ZONE,
    is_deleted     BOOLEAN NOT NULL DEFAULT FALSE,

    employee_id    UUID NOT NULL,
    program_id     UUID NOT NULL,
    log_date       DATE NOT NULL,
    metric_value   BIGINT NOT NULL,
    points_earned  INTEGER,
    source         VARCHAR(20) NOT NULL DEFAULT 'MANUAL',
    device_id      VARCHAR(100),
    notes          VARCHAR(500),
    CONSTRAINT uq_wellness_log_day UNIQUE (tenant_id, employee_id, program_id, log_date)
);
CREATE INDEX IF NOT EXISTS ix_wlog_emp_date ON engagement_wellness_logs(tenant_id, employee_id, log_date);
CREATE INDEX IF NOT EXISTS ix_wlog_program  ON engagement_wellness_logs(program_id, log_date);
