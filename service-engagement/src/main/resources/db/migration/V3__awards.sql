-- Recognition Awards (nominations + approvals). Complements peer kudos with
-- structured, manager/committee-approved awards (Spot Award, Employee of the Month, etc.).
CREATE TABLE IF NOT EXISTS awards (
    id              UUID PRIMARY KEY,
    tenant_id       VARCHAR(100) NOT NULL,
    title           VARCHAR(200) NOT NULL,
    award_type      VARCHAR(40)  NOT NULL,
    nominee_id      UUID         NOT NULL,
    nominated_by    UUID,
    reason          VARCHAR(2000),
    status          VARCHAR(20)  NOT NULL DEFAULT 'NOMINATED',
    period          VARCHAR(40),
    points          INTEGER,
    monetary_value  NUMERIC(12,2),
    currency        VARCHAR(3),
    decided_by      UUID,
    decided_at      TIMESTAMP WITH TIME ZONE,
    decision_notes  VARCHAR(1000),
    created_at      TIMESTAMP WITH TIME ZONE,
    updated_at      TIMESTAMP WITH TIME ZONE
);
CREATE INDEX IF NOT EXISTS ix_award_nominee ON awards(tenant_id, nominee_id);
CREATE INDEX IF NOT EXISTS ix_award_status  ON awards(tenant_id, status);
