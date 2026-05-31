-- Tamper-evident hash chain over audit_logs.
-- Each row's current_hash = sha256(prev_hash || canonical(row)). Verified periodically.

ALTER TABLE audit_logs
    ADD COLUMN IF NOT EXISTS prev_hash    VARCHAR(64),
    ADD COLUMN IF NOT EXISTS current_hash VARCHAR(64);

CREATE INDEX IF NOT EXISTS ix_audit_current_hash ON audit_logs(current_hash);
