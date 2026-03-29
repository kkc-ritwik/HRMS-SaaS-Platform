-- ============================================================
-- Helpdesk Service — V1 Initial Schema
-- ============================================================

-- ── ticket_categories ───────────────────────────────────────
CREATE TABLE ticket_categories (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       VARCHAR(50)  NOT NULL,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    is_deleted      BOOLEAN      NOT NULL DEFAULT FALSE,

    name            VARCHAR(150) NOT NULL,
    code            VARCHAR(50)  NOT NULL,
    description     TEXT,
    sla_hours       INT          NOT NULL DEFAULT 24,
    auto_assign_to  UUID,
    active          BOOLEAN      NOT NULL DEFAULT TRUE,

    CONSTRAINT uq_ticket_category_code_tenant UNIQUE (code, tenant_id)
);

CREATE INDEX idx_ticket_categories_tenant_id ON ticket_categories (tenant_id);

-- ── tickets ─────────────────────────────────────────────────
CREATE TABLE tickets (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           VARCHAR(50)  NOT NULL,
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100),
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    is_deleted          BOOLEAN      NOT NULL DEFAULT FALSE,

    title               VARCHAR(300) NOT NULL,
    description         TEXT,
    category_id         UUID REFERENCES ticket_categories(id) ON DELETE SET NULL,
    requester_id        UUID         NOT NULL,
    assignee_id         UUID,
    priority            VARCHAR(20)  NOT NULL DEFAULT 'MEDIUM'
                            CHECK (priority IN ('LOW','MEDIUM','HIGH','CRITICAL')),
    status              VARCHAR(30)  NOT NULL DEFAULT 'OPEN'
                            CHECK (status IN ('OPEN','IN_PROGRESS','PENDING_REQUESTER','RESOLVED','CLOSED')),
    resolved_at         TIMESTAMPTZ,
    closed_at           TIMESTAMPTZ,
    due_by              TIMESTAMPTZ,
    satisfaction_rating INT
);

CREATE INDEX idx_tickets_tenant_id          ON tickets (tenant_id);
CREATE INDEX idx_tickets_tenant_requester   ON tickets (tenant_id, requester_id);
CREATE INDEX idx_tickets_tenant_assignee    ON tickets (tenant_id, assignee_id);

-- ── ticket_comments ─────────────────────────────────────────
CREATE TABLE ticket_comments (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id      VARCHAR(50)  NOT NULL,
    created_by     VARCHAR(100),
    updated_by     VARCHAR(100),
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    is_deleted     BOOLEAN      NOT NULL DEFAULT FALSE,

    ticket_id      UUID         NOT NULL REFERENCES tickets(id) ON DELETE CASCADE,
    author_id      UUID         NOT NULL,
    comment        TEXT         NOT NULL,
    internal       BOOLEAN      NOT NULL DEFAULT FALSE,
    attachment_url VARCHAR(500)
);

CREATE INDEX idx_ticket_comments_tenant_id ON ticket_comments (tenant_id);
CREATE INDEX idx_ticket_comments_ticket_id ON ticket_comments (ticket_id);

-- ── kb_articles ─────────────────────────────────────────────
CREATE TABLE kb_articles (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id      VARCHAR(50)  NOT NULL,
    created_by     VARCHAR(100),
    updated_by     VARCHAR(100),
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    is_deleted     BOOLEAN      NOT NULL DEFAULT FALSE,

    title          VARCHAR(300) NOT NULL,
    content        TEXT,
    category_id    UUID REFERENCES ticket_categories(id) ON DELETE SET NULL,
    tags           JSONB        NOT NULL DEFAULT '[]',
    status         VARCHAR(20)  NOT NULL DEFAULT 'DRAFT'
                       CHECK (status IN ('DRAFT','PUBLISHED','ARCHIVED')),
    views          INT          NOT NULL DEFAULT 0,
    helpful_votes  INT          NOT NULL DEFAULT 0,
    author_id      UUID,
    published_at   TIMESTAMPTZ
);

CREATE INDEX idx_kb_articles_tenant_id ON kb_articles (tenant_id);
