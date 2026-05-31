-- Individual development plans (IDP) — career path + skill gaps + action items.

CREATE TABLE IF NOT EXISTS perf_development_plans (
    id                            UUID PRIMARY KEY,
    tenant_id                     VARCHAR(100) NOT NULL,
    created_by                    VARCHAR(100),
    updated_by                    VARCHAR(100),
    created_at                    TIMESTAMP WITH TIME ZONE,
    updated_at                    TIMESTAMP WITH TIME ZONE,
    is_deleted                    BOOLEAN NOT NULL DEFAULT FALSE,

    employee_id                   UUID NOT NULL,
    manager_id                    UUID,
    target_role_designation_id    UUID,
    target_role_label             VARCHAR(200),
    horizon_months                INTEGER,
    review_cadence                VARCHAR(30),
    aspirations                   VARCHAR(4000),
    skill_gaps                    JSONB,
    actions                       JSONB,
    check_ins                     JSONB,
    start_date                    DATE,
    next_review_date              DATE,
    status                        VARCHAR(20) NOT NULL DEFAULT 'DRAFT'
);
CREATE INDEX IF NOT EXISTS ix_devplan_emp ON perf_development_plans(tenant_id, employee_id);
