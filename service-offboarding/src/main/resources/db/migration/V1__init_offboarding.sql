-- ============================================================
-- Offboarding Service — initial schema
-- ============================================================

-- ------------------------------------------------------------
-- separations
-- ------------------------------------------------------------
CREATE TABLE separations (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id             VARCHAR(50)  NOT NULL,
    created_by            VARCHAR(100),
    updated_by            VARCHAR(100),
    created_at            TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    is_deleted            BOOLEAN      NOT NULL DEFAULT FALSE,

    employee_id           UUID         NOT NULL,
    separation_type       VARCHAR(50)  NOT NULL DEFAULT 'RESIGNATION'
                              CHECK (separation_type IN ('RESIGNATION','TERMINATION','RETIREMENT','REDUNDANCY','CONTRACT_END')),
    last_working_date     DATE,
    notice_date           DATE,
    reason                TEXT,
    status                VARCHAR(30)  NOT NULL DEFAULT 'INITIATED'
                              CHECK (status IN ('INITIATED','IN_PROGRESS','COMPLETED','CANCELLED')),
    initiated_by          UUID,
    approved_by           UUID,
    final_settlement_date DATE,
    notes                 TEXT
);

CREATE INDEX idx_separations_tenant_id      ON separations (tenant_id);
CREATE INDEX idx_separations_tenant_employee ON separations (tenant_id, employee_id);

-- ------------------------------------------------------------
-- exit_checklists
-- ------------------------------------------------------------
CREATE TABLE exit_checklists (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id      VARCHAR(50)  NOT NULL,
    created_by     VARCHAR(100),
    updated_by     VARCHAR(100),
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    is_deleted     BOOLEAN      NOT NULL DEFAULT FALSE,

    separation_id  UUID         NOT NULL REFERENCES separations(id) ON DELETE CASCADE,
    task_title     VARCHAR(300) NOT NULL,
    task_category  VARCHAR(100),
    assigned_to    UUID,
    due_date       DATE,
    completed      BOOLEAN      NOT NULL DEFAULT FALSE,
    completed_at   TIMESTAMPTZ,
    notes          VARCHAR(500)
);

CREATE INDEX idx_exit_checklists_separation_id ON exit_checklists (separation_id);
CREATE INDEX idx_exit_checklists_tenant_id     ON exit_checklists (tenant_id);

-- ------------------------------------------------------------
-- exit_interviews
-- ------------------------------------------------------------
CREATE TABLE exit_interviews (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           VARCHAR(50) NOT NULL,
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_deleted          BOOLEAN     NOT NULL DEFAULT FALSE,

    separation_id       UUID        NOT NULL REFERENCES separations(id) ON DELETE CASCADE,
    interviewer_id      UUID,
    scheduled_at        TIMESTAMPTZ,
    completed_at        TIMESTAMPTZ,
    satisfaction_rating INT,
    reason_for_leaving  TEXT,
    would_rejoin        BOOLEAN,
    feedback            TEXT,
    status              VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED'
                            CHECK (status IN ('SCHEDULED','COMPLETED','SKIPPED'))
);

CREATE INDEX idx_exit_interviews_separation_id ON exit_interviews (separation_id);
CREATE INDEX idx_exit_interviews_tenant_id     ON exit_interviews (tenant_id);

-- ------------------------------------------------------------
-- knowledge_transfers
-- ------------------------------------------------------------
CREATE TABLE knowledge_transfers (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id        VARCHAR(50)  NOT NULL,
    created_by       VARCHAR(100),
    updated_by       VARCHAR(100),
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    is_deleted       BOOLEAN      NOT NULL DEFAULT FALSE,

    separation_id    UUID         NOT NULL REFERENCES separations(id) ON DELETE CASCADE,
    from_employee_id UUID         NOT NULL,
    to_employee_id   UUID         NOT NULL,
    topic            VARCHAR(300) NOT NULL,
    description      TEXT,
    document_url     VARCHAR(500),
    status           VARCHAR(20)  NOT NULL DEFAULT 'PENDING'
                         CHECK (status IN ('PENDING','IN_PROGRESS','COMPLETED')),
    due_date         DATE,
    completed_at     TIMESTAMPTZ
);

CREATE INDEX idx_knowledge_transfers_separation_id ON knowledge_transfers (separation_id);
CREATE INDEX idx_knowledge_transfers_tenant_id     ON knowledge_transfers (tenant_id);
