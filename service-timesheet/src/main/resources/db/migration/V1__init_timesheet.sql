CREATE TABLE projects (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       VARCHAR(100) NOT NULL,
    name            VARCHAR(200) NOT NULL,
    code            VARCHAR(50) NOT NULL,
    client          VARCHAR(200),
    start_date      DATE,
    end_date        DATE,
    status          VARCHAR(30),
    billing_rate    NUMERIC(12,2),
    currency        VARCHAR(3),
    is_billable     BOOLEAN,
    manager_id      UUID,
    UNIQUE (tenant_id, code)
);

CREATE TABLE project_tasks (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       VARCHAR(100) NOT NULL,
    project_id      UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    name            VARCHAR(200) NOT NULL,
    description     VARCHAR(1000),
    is_billable     BOOLEAN,
    is_active       BOOLEAN DEFAULT TRUE
);

CREATE TABLE timesheet_entries (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       VARCHAR(100) NOT NULL,
    employee_id     UUID NOT NULL,
    project_id      UUID NOT NULL,
    task_id         UUID,
    work_date       DATE NOT NULL,
    hours           NUMERIC(5,2),
    is_billable     BOOLEAN,
    notes           VARCHAR(1000),
    status          VARCHAR(30)
);
CREATE INDEX ix_ts_emp_date ON timesheet_entries (tenant_id, employee_id, work_date);
CREATE INDEX ix_ts_project  ON timesheet_entries (project_id);

CREATE TABLE weekly_timesheets (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       VARCHAR(100) NOT NULL,
    employee_id     UUID NOT NULL,
    week_start      DATE NOT NULL,
    total_hours     NUMERIC(6,2),
    billable_hours  NUMERIC(6,2),
    status          VARCHAR(30),
    submitted_at    TIMESTAMPTZ,
    approver_id     UUID,
    approved_at     TIMESTAMPTZ,
    UNIQUE (tenant_id, employee_id, week_start)
);
