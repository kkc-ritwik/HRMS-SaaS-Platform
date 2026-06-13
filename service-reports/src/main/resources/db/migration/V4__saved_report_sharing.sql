-- Allow a saved report to be shared with specific users.
ALTER TABLE saved_reports ADD COLUMN IF NOT EXISTS shared_with JSONB NOT NULL DEFAULT '[]';
