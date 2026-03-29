-- ============================================================
-- Reports Service — Initial Schema
-- ============================================================

-- report_definitions
CREATE TABLE report_definitions (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id          VARCHAR(50)  NOT NULL,
    created_by         VARCHAR(100),
    updated_by         VARCHAR(100),
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    is_deleted         BOOLEAN      NOT NULL DEFAULT FALSE,
    name               VARCHAR(200) NOT NULL,
    code               VARCHAR(50)  NOT NULL,
    description        TEXT,
    category           VARCHAR(100),
    query_config       JSONB        NOT NULL DEFAULT '{}',
    parameters_config  JSONB        NOT NULL DEFAULT '[]',
    output_formats     JSONB        NOT NULL DEFAULT '["PDF","EXCEL","CSV"]',
    schedule_cron      VARCHAR(100),
    owner_id           UUID,
    active             BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_report_definition_code_tenant UNIQUE (code, tenant_id)
);

CREATE INDEX idx_report_definitions_tenant_id ON report_definitions (tenant_id);

-- saved_reports
CREATE TABLE saved_reports (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       VARCHAR(50)  NOT NULL,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    is_deleted      BOOLEAN      NOT NULL DEFAULT FALSE,
    definition_id   UUID         NOT NULL REFERENCES report_definitions (id) ON DELETE CASCADE,
    name            VARCHAR(300) NOT NULL,
    filters         JSONB        NOT NULL DEFAULT '{}',
    generated_by    UUID         NOT NULL,
    generated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    file_url        VARCHAR(500),
    file_format     VARCHAR(20)  NOT NULL DEFAULT 'PDF'
                        CHECK (file_format IN ('PDF', 'EXCEL', 'CSV')),
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING'
                        CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED')),
    error_message   TEXT
);

CREATE INDEX idx_saved_reports_tenant_id            ON saved_reports (tenant_id);
CREATE INDEX idx_saved_reports_tenant_definition    ON saved_reports (tenant_id, definition_id);
CREATE INDEX idx_saved_reports_tenant_generated_by  ON saved_reports (tenant_id, generated_by);

-- dashboards
CREATE TABLE dashboards (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id         VARCHAR(50)  NOT NULL,
    created_by        VARCHAR(100),
    updated_by        VARCHAR(100),
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    is_deleted        BOOLEAN      NOT NULL DEFAULT FALSE,
    name              VARCHAR(200) NOT NULL,
    description       TEXT,
    owner_id          UUID         NOT NULL,
    shared            BOOLEAN      NOT NULL DEFAULT FALSE,
    shared_with       JSONB        NOT NULL DEFAULT '[]',
    layout_config     JSONB        NOT NULL DEFAULT '{}',
    theme             VARCHAR(50)           DEFAULT 'DEFAULT',
    default_dashboard BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_dashboards_tenant_id       ON dashboards (tenant_id);
CREATE INDEX idx_dashboards_tenant_owner    ON dashboards (tenant_id, owner_id);

-- dashboard_widgets
CREATE TABLE dashboard_widgets (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id        VARCHAR(50)  NOT NULL,
    created_by       VARCHAR(100),
    updated_by       VARCHAR(100),
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    is_deleted       BOOLEAN      NOT NULL DEFAULT FALSE,
    dashboard_id     UUID         NOT NULL REFERENCES dashboards (id) ON DELETE CASCADE,
    widget_type      VARCHAR(30)  NOT NULL DEFAULT 'KPI'
                         CHECK (widget_type IN ('CHART', 'TABLE', 'KPI', 'CALENDAR', 'HEATMAP')),
    title            VARCHAR(200) NOT NULL,
    data_source      VARCHAR(200),
    query_config     JSONB        NOT NULL DEFAULT '{}',
    display_config   JSONB        NOT NULL DEFAULT '{}',
    position_x       INT          NOT NULL DEFAULT 0,
    position_y       INT          NOT NULL DEFAULT 0,
    width            INT          NOT NULL DEFAULT 4,
    height           INT          NOT NULL DEFAULT 3,
    refresh_seconds  INT                   DEFAULT 300
);

CREATE INDEX idx_dashboard_widgets_tenant_id   ON dashboard_widgets (tenant_id);
CREATE INDEX idx_dashboard_widgets_dashboard   ON dashboard_widgets (dashboard_id);
