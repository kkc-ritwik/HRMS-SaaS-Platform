-- Registry of biometric / face / RFID attendance devices that push punches.
CREATE TABLE IF NOT EXISTS biometric_devices (
    id            UUID PRIMARY KEY,
    tenant_id     VARCHAR(100) NOT NULL,
    created_by    VARCHAR(100),
    updated_by    VARCHAR(100),
    created_at    TIMESTAMPTZ,
    updated_at    TIMESTAMPTZ,
    is_deleted    BOOLEAN NOT NULL DEFAULT FALSE,
    device_code   VARCHAR(100) NOT NULL,
    name          VARCHAR(200) NOT NULL,
    make          VARCHAR(100),
    model         VARCHAR(100),
    location_id   UUID,
    ip_address    VARCHAR(64),
    status        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    last_sync_at  TIMESTAMPTZ,
    last_seen_at  TIMESTAMPTZ,
    firmware      VARCHAR(50),
    notes         VARCHAR(1000)
);
CREATE INDEX IF NOT EXISTS ix_biometric_device_tenant ON biometric_devices (tenant_id, status);
CREATE UNIQUE INDEX IF NOT EXISTS uq_biometric_device_code ON biometric_devices (tenant_id, device_code);
