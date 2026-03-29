-- ============================================================================
-- V2__attendance_tables.sql
-- Attendance Management: shifts, schedules, records, punches,
-- regularization and overtime.
-- ============================================================================

-- ── 1. Shifts ─────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS shifts (
    id                      UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id               VARCHAR(50) NOT NULL,
    name                    VARCHAR(150) NOT NULL,
    code                    VARCHAR(50)  NOT NULL,
    start_time              TIME        NOT NULL,
    end_time                TIME        NOT NULL,
    break_duration_mins     INTEGER     NOT NULL DEFAULT 0,
    min_hours_full_day      NUMERIC(5,2) NOT NULL DEFAULT 8.0,
    min_hours_half_day      NUMERIC(5,2) NOT NULL DEFAULT 4.0,
    grace_period_mins       INTEGER     NOT NULL DEFAULT 0,
    overtime_threshold_mins INTEGER     NOT NULL DEFAULT 0,
    is_flexible             BOOLEAN     NOT NULL DEFAULT FALSE,
    flex_start_time         TIME,
    flex_end_time           TIME,
    core_start_time         TIME,
    core_end_time           TIME,
    is_night_shift          BOOLEAN     NOT NULL DEFAULT FALSE,
    color                   VARCHAR(7),
    is_active               BOOLEAN     NOT NULL DEFAULT TRUE,
    created_by              VARCHAR(100),
    updated_by              VARCHAR(100),
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_deleted              BOOLEAN     NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_shift_code_tenant UNIQUE (code, tenant_id)
);
CREATE INDEX IF NOT EXISTS idx_shift_tenant ON shifts(tenant_id);


-- ── 2. Shift Schedules ────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS shift_schedules (
    id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id    VARCHAR(50) NOT NULL,
    employee_id  UUID        NOT NULL,
    shift_id     UUID        REFERENCES shifts(id) ON DELETE SET NULL,
    date         DATE        NOT NULL,
    is_week_off  BOOLEAN     NOT NULL DEFAULT FALSE,
    is_holiday   BOOLEAN     NOT NULL DEFAULT FALSE,
    assigned_by  VARCHAR(100),
    created_by   VARCHAR(100),
    updated_by   VARCHAR(100),
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_deleted   BOOLEAN     NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_schedule_emp_date UNIQUE (tenant_id, employee_id, date)
);
CREATE INDEX IF NOT EXISTS idx_ss_employee ON shift_schedules(employee_id);
CREATE INDEX IF NOT EXISTS idx_ss_date     ON shift_schedules(date);
CREATE INDEX IF NOT EXISTS idx_ss_shift    ON shift_schedules(shift_id);


-- ── 3. Attendance Records ─────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS attendance_records (
    id                  UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           VARCHAR(50) NOT NULL,
    employee_id         UUID        NOT NULL,
    date                DATE        NOT NULL,
    shift_id            UUID        REFERENCES shifts(id) ON DELETE SET NULL,
    first_check_in      TIMESTAMPTZ,
    last_check_out      TIMESTAMPTZ,
    total_hours         NUMERIC(5,2),
    effective_hours     NUMERIC(5,2),
    break_hours         NUMERIC(5,2),
    overtime_hours      NUMERIC(5,2),
    status              VARCHAR(20) NOT NULL DEFAULT 'ABSENT',
    late_by_mins        INTEGER     NOT NULL DEFAULT 0,
    early_leaving_mins  INTEGER     NOT NULL DEFAULT 0,
    source              VARCHAR(20),
    check_in_lat        NUMERIC(10,7),
    check_in_lng        NUMERIC(10,7),
    is_regularized      BOOLEAN     NOT NULL DEFAULT FALSE,
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_deleted          BOOLEAN     NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_attendance_emp_date UNIQUE (tenant_id, employee_id, date)
);
CREATE INDEX IF NOT EXISTS idx_ar_employee    ON attendance_records(employee_id);
CREATE INDEX IF NOT EXISTS idx_ar_tenant_date ON attendance_records(tenant_id, date);
CREATE INDEX IF NOT EXISTS idx_ar_status      ON attendance_records(status);


-- ── 4. Attendance Punches ─────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS attendance_punches (
    id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id    VARCHAR(50) NOT NULL,
    employee_id  UUID        NOT NULL,
    punch_time   TIMESTAMPTZ NOT NULL,
    type         VARCHAR(15) NOT NULL,   -- IN / OUT / BREAK_START / BREAK_END
    source       VARCHAR(20),
    ip_address   VARCHAR(45),
    latitude     NUMERIC(10,7),
    longitude    NUMERIC(10,7),
    is_valid     BOOLEAN     NOT NULL DEFAULT TRUE,
    created_by   VARCHAR(100),
    updated_by   VARCHAR(100),
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_deleted   BOOLEAN     NOT NULL DEFAULT FALSE
);
CREATE INDEX IF NOT EXISTS idx_ap_employee   ON attendance_punches(employee_id);
CREATE INDEX IF NOT EXISTS idx_ap_time       ON attendance_punches(punch_time);
CREATE INDEX IF NOT EXISTS idx_ap_tenant     ON attendance_punches(tenant_id);


-- ── 5. Regularization Requests ────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS regularization_requests (
    id            UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id     VARCHAR(50) NOT NULL,
    employee_id   UUID        NOT NULL,
    date          DATE        NOT NULL,
    original_in   TIMESTAMPTZ,
    original_out  TIMESTAMPTZ,
    corrected_in  TIMESTAMPTZ NOT NULL,
    corrected_out TIMESTAMPTZ NOT NULL,
    reason        TEXT,
    status        VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    approved_by   UUID,
    created_by    VARCHAR(100),
    updated_by    VARCHAR(100),
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_deleted    BOOLEAN     NOT NULL DEFAULT FALSE
);
CREATE INDEX IF NOT EXISTS idx_rr_employee ON regularization_requests(employee_id);
CREATE INDEX IF NOT EXISTS idx_rr_date     ON regularization_requests(date);
CREATE INDEX IF NOT EXISTS idx_rr_status   ON regularization_requests(status);


-- ── 6. Overtime Records ───────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS overtime_records (
    id                  UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           VARCHAR(50) NOT NULL,
    employee_id         UUID        NOT NULL,
    date                DATE        NOT NULL,
    ot_hours            NUMERIC(5,2) NOT NULL,
    ot_rate_multiplier  NUMERIC(4,2) NOT NULL DEFAULT 1.5,
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    approved_by         UUID,
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_deleted          BOOLEAN     NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_ot_emp_date UNIQUE (tenant_id, employee_id, date)
);
CREATE INDEX IF NOT EXISTS idx_ot_employee ON overtime_records(employee_id);
CREATE INDEX IF NOT EXISTS idx_ot_tenant   ON overtime_records(tenant_id);
