ALTER TABLE workflow_instances
    ADD COLUMN IF NOT EXISTS sla_due_at   TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS escalated_at TIMESTAMPTZ;

CREATE INDEX IF NOT EXISTS ix_wi_sla_due
    ON workflow_instances (status, sla_due_at)
    WHERE sla_due_at IS NOT NULL;
