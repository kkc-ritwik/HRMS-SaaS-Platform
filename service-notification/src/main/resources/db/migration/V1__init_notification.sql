-- ============================================================
-- Notification Service Schema
-- ============================================================

-- notifications table
CREATE TABLE notifications (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id        VARCHAR(50)  NOT NULL,
    created_by       VARCHAR(100),
    updated_by       VARCHAR(100),
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    is_deleted       BOOLEAN      NOT NULL DEFAULT FALSE,

    employee_id      UUID         NOT NULL,
    title            VARCHAR(300) NOT NULL,
    message          TEXT,
    notification_type VARCHAR(50) NOT NULL,
    reference_type   VARCHAR(100),
    reference_id     UUID,
    is_read          BOOLEAN      NOT NULL DEFAULT FALSE,
    read_at          TIMESTAMPTZ,
    sent_at          TIMESTAMPTZ
);

CREATE INDEX idx_notifications_tenant_id      ON notifications (tenant_id);
CREATE INDEX idx_notifications_tenant_employee ON notifications (tenant_id, employee_id);

-- notification_preferences table
CREATE TABLE notification_preferences (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id         VARCHAR(50)  NOT NULL,
    created_by        VARCHAR(100),
    updated_by        VARCHAR(100),
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    is_deleted        BOOLEAN      NOT NULL DEFAULT FALSE,

    employee_id       UUID         NOT NULL,
    notification_type VARCHAR(50)  NOT NULL,
    email_enabled     BOOLEAN      NOT NULL DEFAULT TRUE,
    push_enabled      BOOLEAN      NOT NULL DEFAULT TRUE,
    in_app_enabled    BOOLEAN      NOT NULL DEFAULT TRUE,
    sms_enabled       BOOLEAN      NOT NULL DEFAULT FALSE,

    CONSTRAINT uq_notification_preferences UNIQUE (tenant_id, employee_id, notification_type)
);

CREATE INDEX idx_notification_preferences_tenant_id ON notification_preferences (tenant_id);

-- announcements table
CREATE TABLE announcements (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id     VARCHAR(50)  NOT NULL,
    created_by    VARCHAR(100),
    updated_by    VARCHAR(100),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    is_deleted    BOOLEAN      NOT NULL DEFAULT FALSE,

    title         VARCHAR(300) NOT NULL,
    content       TEXT,
    audience_type VARCHAR(30)  NOT NULL DEFAULT 'ALL',
    target_ids    JSONB                 DEFAULT '[]',
    priority      VARCHAR(20)  NOT NULL DEFAULT 'MEDIUM',
    start_date    TIMESTAMPTZ,
    end_date      TIMESTAMPTZ,
    author_id     UUID,
    pinned        BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_announcements_tenant_id ON announcements (tenant_id);

-- email_templates table
CREATE TABLE email_templates (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id   VARCHAR(50)  NOT NULL,
    created_by  VARCHAR(100),
    updated_by  VARCHAR(100),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    is_deleted  BOOLEAN      NOT NULL DEFAULT FALSE,

    name        VARCHAR(200) NOT NULL,
    code        VARCHAR(50)  NOT NULL,
    subject     VARCHAR(500) NOT NULL,
    body_html   TEXT,
    body_text   TEXT,
    variables   JSONB                 DEFAULT '[]',
    category    VARCHAR(100),
    active      BOOLEAN      NOT NULL DEFAULT TRUE,

    CONSTRAINT uq_email_templates_code_tenant UNIQUE (code, tenant_id)
);

CREATE INDEX idx_email_templates_tenant_id ON email_templates (tenant_id);
