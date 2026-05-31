-- Out-of-office delegation: redirects pending approvals to the named delegate during
-- the user's absence window.

CREATE TABLE IF NOT EXISTS workflow_out_of_office (
    id              UUID PRIMARY KEY,
    tenant_id       VARCHAR(100) NOT NULL,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    created_at      TIMESTAMP WITH TIME ZONE,
    updated_at      TIMESTAMP WITH TIME ZONE,
    is_deleted      BOOLEAN NOT NULL DEFAULT FALSE,

    user_id         UUID NOT NULL,
    delegate_id     UUID NOT NULL,
    reason          VARCHAR(500),
    start_date      DATE NOT NULL,
    end_date        DATE NOT NULL,
    auto_reply_msg  VARCHAR(2000),
    active          BOOLEAN DEFAULT TRUE
);
CREATE INDEX IF NOT EXISTS ix_ooo_user   ON workflow_out_of_office(tenant_id, user_id);
CREATE INDEX IF NOT EXISTS ix_ooo_active ON workflow_out_of_office(tenant_id, active, start_date, end_date);
