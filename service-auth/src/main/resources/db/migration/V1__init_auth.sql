-- Auth Service schema

CREATE TABLE IF NOT EXISTS users (
    id                   UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id            VARCHAR(100) NOT NULL,
    employee_id          VARCHAR(100),
    email                VARCHAR(255) NOT NULL,
    password_hash        VARCHAR(255) NOT NULL,
    full_name            VARCHAR(255) NOT NULL,
    phone                VARCHAR(50),
    avatar_url           TEXT,
    status               VARCHAR(30)  NOT NULL DEFAULT 'PENDING_VERIFICATION',
    mfa_enabled          BOOLEAN      NOT NULL DEFAULT FALSE,
    mfa_secret           VARCHAR(255),
    email_verified       BOOLEAN      NOT NULL DEFAULT FALSE,
    last_login_at        TIMESTAMPTZ,
    last_login_ip        VARCHAR(100),
    failed_login_count   INT          NOT NULL DEFAULT 0,
    locked_until         TIMESTAMPTZ,
    password_changed_at  TIMESTAMPTZ,
    created_by           VARCHAR(100),
    updated_by           VARCHAR(100),
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    is_deleted           BOOLEAN      NOT NULL DEFAULT FALSE,
    UNIQUE (email, tenant_id)
);

CREATE TABLE IF NOT EXISTS roles (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id   VARCHAR(100) NOT NULL,
    name        VARCHAR(100) NOT NULL,
    code        VARCHAR(100) NOT NULL,
    description TEXT,
    system_role BOOLEAN      NOT NULL DEFAULT FALSE,
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE (code, tenant_id)
);

CREATE TABLE IF NOT EXISTS permissions (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    module      VARCHAR(100) NOT NULL,
    action      VARCHAR(100) NOT NULL,
    scope       VARCHAR(100),
    description TEXT,
    UNIQUE (module, action, scope)
);

CREATE TABLE IF NOT EXISTS role_permissions (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    role_id       UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    UNIQUE (role_id, permission_id)
);

CREATE TABLE IF NOT EXISTS user_roles (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id     UUID         NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    assigned_by VARCHAR(100),
    assigned_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, role_id)
);

CREATE TABLE IF NOT EXISTS sessions (
    id                 UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id            UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    refresh_token_hash VARCHAR(255) NOT NULL,
    ip_address         VARCHAR(100),
    user_agent         TEXT,
    device_info        TEXT,
    expires_at         TIMESTAMPTZ  NOT NULL,
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    active             BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS login_history (
    id             BIGSERIAL    PRIMARY KEY,
    tenant_id      VARCHAR(100) NOT NULL,
    user_id        UUID         REFERENCES users(id) ON DELETE SET NULL,
    ip_address     VARCHAR(100),
    user_agent     TEXT,
    status         VARCHAR(50)  NOT NULL,
    failure_reason TEXT,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS password_history (
    id            BIGSERIAL    PRIMARY KEY,
    user_id       UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    password_hash VARCHAR(255) NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_users_tenant_email   ON users (tenant_id, email);
CREATE INDEX IF NOT EXISTS idx_users_tenant_deleted ON users (tenant_id, is_deleted);
CREATE INDEX IF NOT EXISTS idx_sessions_user_active ON sessions (user_id, active);
CREATE INDEX IF NOT EXISTS idx_login_history_user   ON login_history (user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_role_permissions_role ON role_permissions (role_id);
CREATE INDEX IF NOT EXISTS idx_user_roles_user       ON user_roles (user_id);
