CREATE TABLE trip_requests (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       VARCHAR(100) NOT NULL,
    employee_id     UUID NOT NULL,
    purpose         VARCHAR(200) NOT NULL,
    trip_type       VARCHAR(30),
    from_location   VARCHAR(200),
    to_location     VARCHAR(200),
    departure_date  DATE,
    return_date     DATE,
    estimated_cost  NUMERIC(14,2),
    currency        VARCHAR(3),
    status          VARCHAR(30) NOT NULL,
    approver_id     UUID,
    approved_at     TIMESTAMPTZ,
    rejection_reason VARCHAR(1000),
    client_billable BOOLEAN,
    project_code    VARCHAR(50),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX ix_trip_emp ON trip_requests (tenant_id, employee_id);
CREATE INDEX ix_trip_status ON trip_requests (tenant_id, status);

CREATE TABLE trip_itineraries (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       VARCHAR(100) NOT NULL,
    trip_id         UUID NOT NULL REFERENCES trip_requests(id) ON DELETE CASCADE,
    type            VARCHAR(30),
    from_place      VARCHAR(200),
    to_place        VARCHAR(200),
    depart_at       DATE,
    arrive_at       DATE,
    vendor          VARCHAR(200),
    booking_reference VARCHAR(100),
    cost            NUMERIC(14,2),
    currency        VARCHAR(3),
    booked_by_company BOOLEAN
);
CREATE INDEX ix_itin_trip ON trip_itineraries (trip_id);

CREATE TABLE travel_advances (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       VARCHAR(100) NOT NULL,
    trip_id         UUID NOT NULL,
    employee_id     UUID NOT NULL,
    amount          NUMERIC(14,2),
    currency        VARCHAR(3),
    status          VARCHAR(30),
    requested_at    DATE,
    disbursed_at    DATE,
    settled_at      DATE,
    settled_amount  NUMERIC(14,2)
);

CREATE TABLE mileage_claims (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       VARCHAR(100) NOT NULL,
    employee_id     UUID NOT NULL,
    trip_id         UUID,
    travel_date     DATE NOT NULL,
    from_location   VARCHAR(200),
    to_location     VARCHAR(200),
    kilometers      NUMERIC(10,2),
    rate_per_km     NUMERIC(8,2),
    amount          NUMERIC(14,2),
    currency        VARCHAR(3),
    status          VARCHAR(30),
    remarks         VARCHAR(500)
);
CREATE INDEX ix_mileage_emp_date ON mileage_claims (tenant_id, employee_id, travel_date);

CREATE TABLE per_diem_rates (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       VARCHAR(100) NOT NULL,
    country         VARCHAR(100),
    city            VARCHAR(100),
    grade           VARCHAR(50),
    daily_amount    NUMERIC(14,2) NOT NULL,
    lodging_cap     NUMERIC(14,2),
    meals_cap       NUMERIC(14,2),
    currency        VARCHAR(3),
    is_active       BOOLEAN DEFAULT TRUE,
    UNIQUE (tenant_id, country, city, grade)
);
