-- ============================================================================
-- V2__seed_default_roles.sql
-- Seeds system roles, permissions, and a demo super-admin user for the
-- 'demo' tenant.  Uses pgcrypto so the BCrypt hash is computed at
-- migration time — no hard-coded hash in source control.
-- Fully idempotent: safe to inspect; Flyway will only run it once.
-- ============================================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ── 1. Permissions ────────────────────────────────────────────────────────────
-- Permissions are global (no tenant_id).  Scope is NULL here to match the
-- format that RoleService.findOrCreatePermission() produces at runtime,
-- so initDefaultRoles() will reuse these rows instead of creating duplicates.
-- Fixed UUIDs make the seed idempotent on PK conflict.

INSERT INTO permissions (id, module, action, scope, description) VALUES
  -- Users
  ('b0100000-0000-4000-a000-000000000001', 'USERS',       'READ',    NULL, 'View user accounts'),
  ('b0100000-0000-4000-a000-000000000002', 'USERS',       'WRITE',   NULL, 'Create and update user accounts'),
  ('b0100000-0000-4000-a000-000000000003', 'USERS',       'DELETE',  NULL, 'Delete user accounts'),
  -- Employees
  ('b0200000-0000-4000-a000-000000000001', 'EMPLOYEES',   'READ',    NULL, 'View employee records'),
  ('b0200000-0000-4000-a000-000000000002', 'EMPLOYEES',   'WRITE',   NULL, 'Create and update employee records'),
  ('b0200000-0000-4000-a000-000000000003', 'EMPLOYEES',   'DELETE',  NULL, 'Delete employee records'),
  -- Leave
  ('b0300000-0000-4000-a000-000000000001', 'LEAVE',       'READ',    NULL, 'View leave requests'),
  ('b0300000-0000-4000-a000-000000000002', 'LEAVE',       'WRITE',   NULL, 'Submit leave requests'),
  ('b0300000-0000-4000-a000-000000000003', 'LEAVE',       'APPROVE', NULL, 'Approve or reject leave requests'),
  -- Attendance
  ('b0400000-0000-4000-a000-000000000001', 'ATTENDANCE',  'READ',    NULL, 'View attendance records'),
  ('b0400000-0000-4000-a000-000000000002', 'ATTENDANCE',  'WRITE',   NULL, 'Update attendance records'),
  -- Payroll
  ('b0500000-0000-4000-a000-000000000001', 'PAYROLL',     'READ',    NULL, 'View payroll data'),
  ('b0500000-0000-4000-a000-000000000002', 'PAYROLL',     'WRITE',   NULL, 'Process payroll'),
  ('b0500000-0000-4000-a000-000000000003', 'PAYROLL',     'DELETE',  NULL, 'Delete payroll records'),
  ('b0500000-0000-4000-a000-000000000004', 'PAYROLL',     'APPROVE', NULL, 'Approve payroll runs'),
  -- Recruitment
  ('b0600000-0000-4000-a000-000000000001', 'RECRUITMENT', 'READ',    NULL, 'View job postings and applications'),
  ('b0600000-0000-4000-a000-000000000002', 'RECRUITMENT', 'WRITE',   NULL, 'Manage job postings'),
  ('b0600000-0000-4000-a000-000000000003', 'RECRUITMENT', 'DELETE',  NULL, 'Delete recruitment records'),
  -- Performance
  ('b0700000-0000-4000-a000-000000000001', 'PERFORMANCE', 'READ',    NULL, 'View performance reviews'),
  ('b0700000-0000-4000-a000-000000000002', 'PERFORMANCE', 'WRITE',   NULL, 'Submit performance reviews'),
  -- Reports
  ('b0800000-0000-4000-a000-000000000001', 'REPORTS',     'READ',    NULL, 'Access reports'),
  -- Roles & permissions management
  ('b0900000-0000-4000-a000-000000000001', 'ROLES',       'READ',    NULL, 'View roles and permissions'),
  ('b0900000-0000-4000-a000-000000000002', 'ROLES',       'WRITE',   NULL, 'Manage roles and permissions'),
  -- Profile (self-service)
  ('b1000000-0000-4000-a000-000000000001', 'PROFILE',     'READ',    NULL, 'View own profile'),
  ('b1000000-0000-4000-a000-000000000002', 'PROFILE',     'WRITE',   NULL, 'Update own profile')
ON CONFLICT (id) DO NOTHING;


-- ── 2. System roles (demo tenant) ─────────────────────────────────────────────

INSERT INTO roles (id, tenant_id, name, code, description, system_role, active, created_at) VALUES
  ('a0100000-0000-4000-a000-000000000001', 'demo', 'Super Administrator', 'SUPER_ADMIN',
   'Unrestricted access to all modules (enforced at security layer)',   true, true, NOW()),
  ('a0100000-0000-4000-a000-000000000002', 'demo', 'HR Administrator',    'HR_ADMIN',
   'Full HR operations: employees, leave, attendance, payroll, recruitment, reports',
   true, true, NOW()),
  ('a0100000-0000-4000-a000-000000000003', 'demo', 'Manager',             'MANAGER',
   'Team-level visibility for employees, leave, attendance, performance and reports',
   true, true, NOW()),
  ('a0100000-0000-4000-a000-000000000004', 'demo', 'Employee',            'EMPLOYEE',
   'Self-service: own leave, profile',                                   true, true, NOW()),
  ('a0100000-0000-4000-a000-000000000005', 'demo', 'Recruiter',           'RECRUITER',
   'Full recruitment and candidate pipeline management',                 true, true, NOW()),
  ('a0100000-0000-4000-a000-000000000006', 'demo', 'Payroll Administrator','PAYROLL_ADMIN',
   'Full payroll processing, approval and reporting',                    true, true, NOW())
ON CONFLICT (code, tenant_id) DO NOTHING;


-- ── 3. Role → Permission assignments ─────────────────────────────────────────
-- SUPER_ADMIN: no DB-level grants — all access is checked at the security layer.

-- HR_ADMIN
INSERT INTO role_permissions (id, role_id, permission_id)
SELECT gen_random_uuid(), r.id, p.id
FROM   roles r
CROSS JOIN UNNEST(ARRAY[
    'b0100000-0000-4000-a000-000000000001'::uuid,  -- USERS:READ
    'b0100000-0000-4000-a000-000000000002'::uuid,  -- USERS:WRITE
    'b0200000-0000-4000-a000-000000000001'::uuid,  -- EMPLOYEES:READ
    'b0200000-0000-4000-a000-000000000002'::uuid,  -- EMPLOYEES:WRITE
    'b0200000-0000-4000-a000-000000000003'::uuid,  -- EMPLOYEES:DELETE
    'b0300000-0000-4000-a000-000000000001'::uuid,  -- LEAVE:READ
    'b0300000-0000-4000-a000-000000000002'::uuid,  -- LEAVE:WRITE
    'b0300000-0000-4000-a000-000000000003'::uuid,  -- LEAVE:APPROVE
    'b0400000-0000-4000-a000-000000000001'::uuid,  -- ATTENDANCE:READ
    'b0400000-0000-4000-a000-000000000002'::uuid,  -- ATTENDANCE:WRITE
    'b0500000-0000-4000-a000-000000000001'::uuid,  -- PAYROLL:READ
    'b0600000-0000-4000-a000-000000000001'::uuid,  -- RECRUITMENT:READ
    'b0700000-0000-4000-a000-000000000001'::uuid,  -- PERFORMANCE:READ
    'b0800000-0000-4000-a000-000000000001'::uuid,  -- REPORTS:READ
    'b0900000-0000-4000-a000-000000000001'::uuid   -- ROLES:READ
]) AS perm_id
JOIN permissions p ON p.id = perm_id
WHERE  r.id = 'a0100000-0000-4000-a000-000000000002'
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- MANAGER
INSERT INTO role_permissions (id, role_id, permission_id)
SELECT gen_random_uuid(), r.id, p.id
FROM   roles r
CROSS JOIN UNNEST(ARRAY[
    'b0200000-0000-4000-a000-000000000001'::uuid,  -- EMPLOYEES:READ
    'b0300000-0000-4000-a000-000000000001'::uuid,  -- LEAVE:READ
    'b0300000-0000-4000-a000-000000000003'::uuid,  -- LEAVE:APPROVE
    'b0400000-0000-4000-a000-000000000001'::uuid,  -- ATTENDANCE:READ
    'b0700000-0000-4000-a000-000000000001'::uuid,  -- PERFORMANCE:READ
    'b0800000-0000-4000-a000-000000000001'::uuid   -- REPORTS:READ
]) AS perm_id
JOIN permissions p ON p.id = perm_id
WHERE  r.id = 'a0100000-0000-4000-a000-000000000003'
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- EMPLOYEE (self-service only)
INSERT INTO role_permissions (id, role_id, permission_id)
SELECT gen_random_uuid(), r.id, p.id
FROM   roles r
CROSS JOIN UNNEST(ARRAY[
    'b0300000-0000-4000-a000-000000000001'::uuid,  -- LEAVE:READ
    'b0300000-0000-4000-a000-000000000002'::uuid,  -- LEAVE:WRITE
    'b1000000-0000-4000-a000-000000000001'::uuid,  -- PROFILE:READ
    'b1000000-0000-4000-a000-000000000002'::uuid   -- PROFILE:WRITE
]) AS perm_id
JOIN permissions p ON p.id = perm_id
WHERE  r.id = 'a0100000-0000-4000-a000-000000000004'
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- RECRUITER
INSERT INTO role_permissions (id, role_id, permission_id)
SELECT gen_random_uuid(), r.id, p.id
FROM   roles r
CROSS JOIN UNNEST(ARRAY[
    'b0600000-0000-4000-a000-000000000001'::uuid,  -- RECRUITMENT:READ
    'b0600000-0000-4000-a000-000000000002'::uuid,  -- RECRUITMENT:WRITE
    'b0600000-0000-4000-a000-000000000003'::uuid   -- RECRUITMENT:DELETE
]) AS perm_id
JOIN permissions p ON p.id = perm_id
WHERE  r.id = 'a0100000-0000-4000-a000-000000000005'
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- PAYROLL_ADMIN
INSERT INTO role_permissions (id, role_id, permission_id)
SELECT gen_random_uuid(), r.id, p.id
FROM   roles r
CROSS JOIN UNNEST(ARRAY[
    'b0500000-0000-4000-a000-000000000001'::uuid,  -- PAYROLL:READ
    'b0500000-0000-4000-a000-000000000002'::uuid,  -- PAYROLL:WRITE
    'b0500000-0000-4000-a000-000000000003'::uuid,  -- PAYROLL:DELETE
    'b0500000-0000-4000-a000-000000000004'::uuid,  -- PAYROLL:APPROVE
    'b0200000-0000-4000-a000-000000000001'::uuid   -- EMPLOYEES:READ
]) AS perm_id
JOIN permissions p ON p.id = perm_id
WHERE  r.id = 'a0100000-0000-4000-a000-000000000006'
ON CONFLICT (role_id, permission_id) DO NOTHING;


-- ── 4. Default super-admin user ───────────────────────────────────────────────
-- Password is hashed at migration time via pgcrypto (BCrypt, cost 12).
-- Plain-text credential (for first login only): Admin@123
-- Change immediately after first login via POST /api/v1/auth/change-password

INSERT INTO users (
    id, tenant_id, email, password_hash, full_name,
    status, email_verified,
    created_by, updated_by, created_at, updated_at, is_deleted
)
VALUES (
    'c0000000-0000-4000-a000-000000000001',
    'demo',
    'admin@demo.com',
    crypt('Admin@123', gen_salt('bf', 12)),   -- BCrypt cost 12, random salt
    'System Administrator',
    'ACTIVE',
    true,
    'SYSTEM', 'SYSTEM',
    NOW(), NOW(),
    false
)
ON CONFLICT (id) DO NOTHING;


-- ── 5. Assign SUPER_ADMIN role to the admin user ──────────────────────────────

INSERT INTO user_roles (id, user_id, role_id, assigned_by, assigned_at)
VALUES (
    'e0000000-0000-4000-a000-000000000001',
    'c0000000-0000-4000-a000-000000000001',   -- admin@demo.com
    'a0100000-0000-4000-a000-000000000001',   -- SUPER_ADMIN
    'SYSTEM',
    NOW()
)
ON CONFLICT (id) DO NOTHING;


-- ── 6. Seed initial password-history entry ────────────────────────────────────
-- Ensures the changePassword check against the last 5 passwords works correctly
-- from the very first login.

INSERT INTO password_history (user_id, password_hash, created_at)
SELECT 'c0000000-0000-4000-a000-000000000001',
       password_hash,
       created_at
FROM   users
WHERE  id = 'c0000000-0000-4000-a000-000000000001'
ON CONFLICT DO NOTHING;
