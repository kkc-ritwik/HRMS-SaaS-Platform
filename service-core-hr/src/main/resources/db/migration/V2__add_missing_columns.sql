-- V2: Add missing columns to existing tables
-- Uses ADD COLUMN IF NOT EXISTS (PostgreSQL 9.6+) to be idempotent

-- ─────────────────────────────────────────────────────────────────────────────
-- employees: statutory, banking, HR, and supplemental fields
-- ─────────────────────────────────────────────────────────────────────────────
ALTER TABLE employees ADD COLUMN IF NOT EXISTS pan_number            VARCHAR(10);
ALTER TABLE employees ADD COLUMN IF NOT EXISTS aadhar_number         VARCHAR(12);
ALTER TABLE employees ADD COLUMN IF NOT EXISTS uan_number            VARCHAR(12);
ALTER TABLE employees ADD COLUMN IF NOT EXISTS esi_number            VARCHAR(17);

ALTER TABLE employees ADD COLUMN IF NOT EXISTS bank_name             VARCHAR(100);
ALTER TABLE employees ADD COLUMN IF NOT EXISTS bank_account_number   VARCHAR(30);
ALTER TABLE employees ADD COLUMN IF NOT EXISTS ifsc_code             VARCHAR(11);
ALTER TABLE employees ADD COLUMN IF NOT EXISTS bank_branch           VARCHAR(100);

ALTER TABLE employees ADD COLUMN IF NOT EXISTS blood_group           VARCHAR(5);
ALTER TABLE employees ADD COLUMN IF NOT EXISTS display_name          VARCHAR(255);
ALTER TABLE employees ADD COLUMN IF NOT EXISTS phone_secondary        VARCHAR(20);
ALTER TABLE employees ADD COLUMN IF NOT EXISTS about_me              VARCHAR(1000);

ALTER TABLE employees ADD COLUMN IF NOT EXISTS secondary_manager_id  UUID;
ALTER TABLE employees ADD COLUMN IF NOT EXISTS shift_id              UUID;
ALTER TABLE employees ADD COLUMN IF NOT EXISTS pay_grade_id          UUID;

ALTER TABLE employees ADD COLUMN IF NOT EXISTS probation_end_date    DATE;
ALTER TABLE employees ADD COLUMN IF NOT EXISTS resignation_date      DATE;
ALTER TABLE employees ADD COLUMN IF NOT EXISTS last_working_date     DATE;
ALTER TABLE employees ADD COLUMN IF NOT EXISTS exit_date             DATE;

ALTER TABLE employees ADD COLUMN IF NOT EXISTS cost_center_code      VARCHAR(50);
ALTER TABLE employees ADD COLUMN IF NOT EXISTS custom_fields         JSONB DEFAULT '{}';
ALTER TABLE employees ADD COLUMN IF NOT EXISTS tags                  TEXT;
ALTER TABLE employees ADD COLUMN IF NOT EXISTS notice_period_days    INT DEFAULT 30;

-- ─────────────────────────────────────────────────────────────────────────────
-- designations: band and grade
-- ─────────────────────────────────────────────────────────────────────────────
ALTER TABLE designations ADD COLUMN IF NOT EXISTS band   VARCHAR(20);
ALTER TABLE designations ADD COLUMN IF NOT EXISTS grade  VARCHAR(20);

-- ─────────────────────────────────────────────────────────────────────────────
-- family_members: gender and dependency flag
-- ─────────────────────────────────────────────────────────────────────────────
ALTER TABLE family_members ADD COLUMN IF NOT EXISTS gender       VARCHAR(20);
ALTER TABLE family_members ADD COLUMN IF NOT EXISTS is_dependent BOOLEAN DEFAULT FALSE;

-- ─────────────────────────────────────────────────────────────────────────────
-- education: passing year, percentage, document proof
-- ─────────────────────────────────────────────────────────────────────────────
ALTER TABLE education ADD COLUMN IF NOT EXISTS year_of_passing INT;
ALTER TABLE education ADD COLUMN IF NOT EXISTS percentage      DECIMAL(5,2);
ALTER TABLE education ADD COLUMN IF NOT EXISTS document_url    TEXT;

-- ─────────────────────────────────────────────────────────────────────────────
-- employment_history: CTC
-- ─────────────────────────────────────────────────────────────────────────────
ALTER TABLE employment_history ADD COLUMN IF NOT EXISTS ctc DECIMAL(12,2);

-- ─────────────────────────────────────────────────────────────────────────────
-- employee_lifecycle_events: effective date, reason, comments, approver
-- ─────────────────────────────────────────────────────────────────────────────
ALTER TABLE employee_lifecycle_events ADD COLUMN IF NOT EXISTS effective_date DATE;
ALTER TABLE employee_lifecycle_events ADD COLUMN IF NOT EXISTS reason        TEXT;
ALTER TABLE employee_lifecycle_events ADD COLUMN IF NOT EXISTS comments      TEXT;
ALTER TABLE employee_lifecycle_events ADD COLUMN IF NOT EXISTS approved_by   UUID;
