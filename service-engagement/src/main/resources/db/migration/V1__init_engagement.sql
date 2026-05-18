CREATE TABLE surveys (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       VARCHAR(100) NOT NULL,
    title           VARCHAR(200) NOT NULL,
    description     VARCHAR(1000),
    type            VARCHAR(30),
    status          VARCHAR(30),
    is_anonymous    BOOLEAN,
    starts_on       DATE,
    ends_on         DATE,
    questions       JSONB,
    audience_filter JSONB
);

CREATE TABLE survey_responses (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       VARCHAR(100) NOT NULL,
    survey_id       UUID NOT NULL REFERENCES surveys(id) ON DELETE CASCADE,
    respondent_id   UUID,
    answers         JSONB,
    submitted_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX ix_resp_survey ON survey_responses (survey_id);

CREATE TABLE polls (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       VARCHAR(100) NOT NULL,
    question        VARCHAR(500) NOT NULL,
    options         JSONB,
    allow_multi     BOOLEAN,
    is_anonymous    BOOLEAN,
    closes_at       TIMESTAMPTZ,
    created_by      UUID,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE poll_votes (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       VARCHAR(100) NOT NULL,
    poll_id         UUID NOT NULL REFERENCES polls(id) ON DELETE CASCADE,
    voter_id        UUID,
    selected_options JSONB,
    voted_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (poll_id, voter_id)
);

CREATE TABLE kudos (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       VARCHAR(100) NOT NULL,
    giver_id        UUID NOT NULL,
    recipient_id    UUID NOT NULL,
    value           VARCHAR(100),
    message         VARCHAR(1000) NOT NULL,
    is_public       BOOLEAN,
    points          INTEGER,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX ix_kudos_recipient ON kudos (tenant_id, recipient_id);
CREATE INDEX ix_kudos_giver     ON kudos (tenant_id, giver_id);
