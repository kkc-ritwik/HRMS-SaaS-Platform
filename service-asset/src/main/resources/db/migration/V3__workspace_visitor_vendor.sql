-- Workspace (floors / desks / bookings), Visitors, Vendors, AMC contracts.

CREATE TABLE IF NOT EXISTS amc_contracts (
    id                   UUID PRIMARY KEY,
    tenant_id            VARCHAR(100) NOT NULL,
    created_by           VARCHAR(100),
    updated_by           VARCHAR(100),
    created_at           TIMESTAMP WITH TIME ZONE,
    updated_at           TIMESTAMP WITH TIME ZONE,
    is_deleted           BOOLEAN NOT NULL DEFAULT FALSE,

    contract_number      VARCHAR(100) NOT NULL,
    asset_id             UUID,
    asset_category_id    UUID,
    contract_type        VARCHAR(20) NOT NULL,
    vendor_name          VARCHAR(200),
    vendor_contact       VARCHAR(200),
    vendor_email         VARCHAR(200),
    vendor_phone         VARCHAR(30),
    start_date           DATE NOT NULL,
    end_date             DATE NOT NULL,
    notice_period_days   INTEGER,
    contract_value       NUMERIC(14,2),
    currency             VARCHAR(3),
    payment_terms        VARCHAR(500),
    scope_of_coverage    TEXT,
    sla_response_hours   INTEGER,
    sla_resolution_hours INTEGER,
    document_uri         VARCHAR(1000),
    auto_renew           BOOLEAN,
    status               VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
);
CREATE INDEX IF NOT EXISTS ix_amc_asset  ON amc_contracts(tenant_id, asset_id);
CREATE INDEX IF NOT EXISTS ix_amc_expiry ON amc_contracts(tenant_id, end_date);

CREATE TABLE IF NOT EXISTS workspace_floors (
    id                  UUID PRIMARY KEY,
    tenant_id           VARCHAR(100) NOT NULL,
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100),
    created_at          TIMESTAMP WITH TIME ZONE,
    updated_at          TIMESTAMP WITH TIME ZONE,
    is_deleted          BOOLEAN NOT NULL DEFAULT FALSE,

    office_id           UUID NOT NULL,
    name                VARCHAR(100) NOT NULL,
    level_number        INTEGER,
    capacity            INTEGER,
    width_px            INTEGER,
    height_px           INTEGER,
    floorplan_svg       TEXT,
    floorplan_image_uri VARCHAR(500),
    zones               JSONB,
    active              BOOLEAN DEFAULT TRUE
);
CREATE INDEX IF NOT EXISTS ix_floor_office ON workspace_floors(tenant_id, office_id);

CREATE TABLE IF NOT EXISTS workspace_desks (
    id                       UUID PRIMARY KEY,
    tenant_id                VARCHAR(100) NOT NULL,
    created_by               VARCHAR(100),
    updated_by               VARCHAR(100),
    created_at               TIMESTAMP WITH TIME ZONE,
    updated_at               TIMESTAMP WITH TIME ZONE,
    is_deleted               BOOLEAN NOT NULL DEFAULT FALSE,

    code                     VARCHAR(30) NOT NULL,
    name                     VARCHAR(100),
    office_id                UUID,
    floor_id                 UUID,
    zone                     VARCHAR(100),
    x_coord                  INTEGER,
    y_coord                  INTEGER,
    type                     VARCHAR(20) NOT NULL DEFAULT 'HOT_DESK',
    has_monitor              BOOLEAN,
    has_phone                BOOLEAN,
    has_dock                 BOOLEAN,
    accessibility            BOOLEAN,
    dedicated                BOOLEAN,
    permanent_employee_id    UUID,
    active                   BOOLEAN DEFAULT TRUE,
    CONSTRAINT uq_desk_code UNIQUE (tenant_id, code)
);
CREATE INDEX IF NOT EXISTS ix_desk_floor ON workspace_desks(tenant_id, floor_id);

CREATE TABLE IF NOT EXISTS workspace_desk_bookings (
    id                       UUID PRIMARY KEY,
    tenant_id                VARCHAR(100) NOT NULL,
    created_by               VARCHAR(100),
    updated_by               VARCHAR(100),
    created_at               TIMESTAMP WITH TIME ZONE,
    updated_at               TIMESTAMP WITH TIME ZONE,
    is_deleted               BOOLEAN NOT NULL DEFAULT FALSE,

    desk_id                  UUID NOT NULL,
    employee_id              UUID NOT NULL,
    booking_date             DATE NOT NULL,
    start_time_offset        INTEGER,
    end_time_offset          INTEGER,
    status                   VARCHAR(20) NOT NULL DEFAULT 'BOOKED',
    checked_in_at            TIMESTAMP WITH TIME ZONE,
    checked_out_at           TIMESTAMP WITH TIME ZONE,
    qr_token                 VARCHAR(100),
    purpose                  VARCHAR(200),
    CONSTRAINT uq_desk_booking_day UNIQUE (desk_id, booking_date)
);
CREATE INDEX IF NOT EXISTS ix_db_emp_date ON workspace_desk_bookings(tenant_id, employee_id, booking_date);
CREATE INDEX IF NOT EXISTS ix_db_date     ON workspace_desk_bookings(tenant_id, booking_date);

CREATE TABLE IF NOT EXISTS workspace_visitors (
    id                       UUID PRIMARY KEY,
    tenant_id                VARCHAR(100) NOT NULL,
    created_by               VARCHAR(100),
    updated_by               VARCHAR(100),
    created_at               TIMESTAMP WITH TIME ZONE,
    updated_at               TIMESTAMP WITH TIME ZONE,
    is_deleted               BOOLEAN NOT NULL DEFAULT FALSE,

    full_name                VARCHAR(200) NOT NULL,
    company                  VARCHAR(200),
    purpose                  VARCHAR(500),
    phone                    VARCHAR(500),
    email                    VARCHAR(500),
    id_type                  VARCHAR(30),
    id_number                VARCHAR(500),
    vehicle_number           VARCHAR(500),
    host_employee_id         UUID,
    location_id              UUID,
    floor_id                 UUID,
    badge_number             VARCHAR(50),
    photo_uri                VARCHAR(500),
    qr_token                 VARCHAR(100),
    visit_date               DATE,
    expected_arrival         TIMESTAMP WITH TIME ZONE,
    checked_in_at            TIMESTAMP WITH TIME ZONE,
    checked_out_at           TIMESTAMP WITH TIME ZONE,
    status                   VARCHAR(20) NOT NULL DEFAULT 'PRE_REGISTERED',
    nda_signed               BOOLEAN,
    nda_document_uri         VARCHAR(500),
    temperature_check        VARCHAR(50),
    health_declaration       BOOLEAN
);
CREATE INDEX IF NOT EXISTS ix_visitor_date ON workspace_visitors(tenant_id, visit_date);
CREATE INDEX IF NOT EXISTS ix_visitor_host ON workspace_visitors(tenant_id, host_employee_id);

CREATE TABLE IF NOT EXISTS vendors (
    id                       UUID PRIMARY KEY,
    tenant_id                VARCHAR(100) NOT NULL,
    created_by               VARCHAR(100),
    updated_by               VARCHAR(100),
    created_at               TIMESTAMP WITH TIME ZONE,
    updated_at               TIMESTAMP WITH TIME ZONE,
    is_deleted               BOOLEAN NOT NULL DEFAULT FALSE,

    vendor_code              VARCHAR(50) NOT NULL,
    legal_name               VARCHAR(300) NOT NULL,
    display_name             VARCHAR(200),
    category                 VARCHAR(30) NOT NULL,
    primary_contact_name     VARCHAR(200),
    primary_contact_email    VARCHAR(200),
    primary_contact_phone    VARCHAR(30),
    address_line1            VARCHAR(300),
    address_line2            VARCHAR(300),
    city                     VARCHAR(100),
    state                    VARCHAR(100),
    postal_code              VARCHAR(20),
    country                  VARCHAR(2),
    pan_number               VARCHAR(500),
    gstin                    VARCHAR(500),
    tan_number               VARCHAR(30),
    msme_registered          BOOLEAN,
    msme_classification      VARCHAR(20),
    udyam_number             VARCHAR(30),
    bank_name                VARCHAR(200),
    bank_account             VARCHAR(500),
    ifsc                     VARCHAR(500),
    swift_code               VARCHAR(30),
    payment_terms_days       INTEGER,
    credit_limit             NUMERIC(14,2),
    currency                 VARCHAR(3),
    tds_section              VARCHAR(20),
    tds_rate_percent         NUMERIC(5,2),
    onboarded_on             DATE,
    contract_start           DATE,
    contract_end             DATE,
    rating                   INTEGER,
    blacklisted              BOOLEAN,
    blacklist_reason         VARCHAR(1000),
    active                   BOOLEAN DEFAULT TRUE
);
CREATE INDEX IF NOT EXISTS ix_vendor_active   ON vendors(tenant_id, active);
CREATE INDEX IF NOT EXISTS ix_vendor_category ON vendors(tenant_id, category);
