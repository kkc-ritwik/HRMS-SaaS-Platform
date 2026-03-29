# HRMS Platform — Complete Setup Guide

---

## TASK 1: DATABASE SETUP

### 1.1 Docker Compose — PostgreSQL Configuration

| Setting | Value |
|---------|-------|
| Container name | `hrms-postgres` |
| Database name | `hrms_db` |
| Username | `hrms_user` |
| Password | `hrms_pass` |
| Port | `5432` (host) → `5432` (container) |
| Image | `postgres:16-alpine` |

Other containers:

| Service | Container | Port(s) |
|---------|-----------|---------|
| Redis | hrms-redis | 6379 |
| Kafka | hrms-kafka | 9092 |
| Zookeeper | hrms-zookeeper | 2181 |
| Elasticsearch | hrms-elasticsearch | 9200 |
| MinIO | hrms-minio | 9000 (API), 9001 (Console) |

---

### 1.2 init-db.sql — What It Creates

The file `scripts/init-db.sql` runs automatically the **first time** the PostgreSQL container starts (via `docker-entrypoint-initdb.d`).

**Schemas created:**
- `shared` — platform-level tables (multi-tenant metadata)
- `tenant_demo` — default demo tenant schema (empty, ready for Flyway migrations)

**Tables created in `shared`:**

| Table | Purpose |
|-------|---------|
| `shared.tenants` | Registered tenants (one row: Demo Company / slug=demo) |
| `shared.plans` | Subscription plans (FREE, STARTER, PRO, ENTERPRISE) |
| `shared.audit_logs` | Platform-wide audit trail |

**Seed data inserted:**
- 4 plans: FREE (5 users, ₹0), STARTER (50 users, ₹2999), PRO (200 users, ₹5999), ENTERPRISE (10000 users, ₹9999)
- 1 tenant: Demo Company (slug=`demo`, schema=`tenant_demo`)

---

### 1.3 Flyway Migrations — Execution Order Per Service

All services use `spring.jpa.hibernate.default_schema: public`, so Flyway creates tables in the `public` schema of `hrms_db`.

Each service runs its own migrations **automatically on startup** from `src/main/resources/db/migration/`.

| Service | Port | Migrations (in order) |
|---------|------|-----------------------|
| service-auth | 8081 | V1__init_auth.sql → V2__seed_default_roles.sql |
| service-core-hr | 8082 | V1__init_corehr.sql → V2__add_missing_columns.sql |
| service-leave-attendance | 8083 | V1__init_leave.sql → V2__attendance_tables.sql |
| service-payroll | 8084 | V1__init_payroll.sql → V2__seed_default_components.sql → V3__payroll_processing_tables.sql |
| service-recruitment | 8085 | V1__init_recruitment.sql |
| service-performance | 8086 | V1__init_performance.sql |
| service-onboarding | 8090 | V1__init_onboarding.sql |
| service-document | 8091 | V1__init_document.sql |
| service-notification | 8092 | V1__init_notification.sql |
| service-expense | 8093 | V1__init_expense.sql |
| service-asset | 8094 | V1__init_asset.sql |
| service-helpdesk | 8095 | V1__init_helpdesk.sql |
| service-social | 8096 | V1__init_social.sql |
| service-compensation | 8097 | V1__init_compensation.sql |
| service-compliance | 8098 | V1__init_compliance.sql |
| service-offboarding | 8099 | V1__init_offboarding.sql |
| service-lms | 8100 | V1__init_lms.sql |
| service-workflow | 8101 | V1__init_workflow.sql |
| service-reports | 8102 | V1__init_reports.sql |

---

### 1.4 Step-by-Step: Start Docker & Verify

**Step 1 — Start Docker Desktop**
Make sure Docker Desktop is running on Windows.

**Step 2 — Start all containers**
```bat
cd C:\Users\ritwi\Desktop\HRMS\hrms
docker-compose up -d
```

**Step 3 — Wait ~15 seconds, then verify PostgreSQL**
```bat
docker exec hrms-postgres pg_isready -U hrms_user -d hrms_db
```
Expected: `localhost:5432 - accepting connections`

**Step 4 — Check schemas were created**
```bat
docker exec hrms-postgres psql -U hrms_user -d hrms_db -c "\dn"
```
Expected output includes: `shared` and `tenant_demo`

**Step 5 — Check seed data**
```bat
docker exec hrms-postgres psql -U hrms_user -d hrms_db -c "SELECT slug, name FROM shared.tenants;"
docker exec hrms-postgres psql -U hrms_user -d hrms_db -c "SELECT code, name FROM shared.plans;"
```

**Step 6 — Verify Redis**
```bat
docker exec hrms-redis redis-cli ping
```
Expected: `PONG`

**Step 7 — Run the automated verification script**
```bat
scripts\verify-infra.bat
```

---

### 1.5 How Flyway Migrations Run Automatically

When each Spring Boot service starts:
1. Spring Boot detects `spring.flyway.enabled: true`
2. Flyway connects to the same PostgreSQL database (`hrms_db`)
3. Flyway checks the `flyway_schema_history` table in the `public` schema
4. Any migrations not yet applied are executed in version order (V1 → V2 → V3...)
5. If `ddl-auto: validate` is set, Hibernate then validates entity fields match database columns

**Key:** All 19 business services share the same single database `hrms_db` but have separate table namespaces by convention (e.g., auth service owns `users`, `roles`, `sessions`; payroll owns `salary_structures`, `payslips`, etc.).

---

### 1.6 Potential SQL Issues & Flags

**Issue 1: `tenant_demo` schema created but not used by migrations**
- `init-db.sql` creates the `tenant_demo` schema, but all Flyway migrations write to the `public` schema (via `default_schema: public`).
- **Status:** Not a conflict — `tenant_demo` is a placeholder for future schema-per-tenant isolation.
- **No fix needed** for current single-schema setup.

**Issue 2: Flyway `flyway_schema_history` table conflict**
- All 19 services share one database. Each service has its own Flyway migration scripts. They all write to the same `flyway_schema_history` table in `public`.
- Flyway tracks migrations by `(version, description)` — **this is safe** as long as no two services use the same version number with the same name, which they don't (each has its own description prefix).
- **Status:** OK, no conflict.

**Issue 3: `shared.audit_logs` tenant_id column type**
- `tenant_id` in `shared.audit_logs` is `VARCHAR(100)` but entity tenant IDs are UUID strings. Compatible, no fix needed.

---

## TASK 2: CONFIGURATION AUDIT

### 2.1 Port Assignments (No Conflicts Found)

| Service | Port |
|---------|------|
| API Gateway | **8080** |
| service-auth | **8081** |
| service-core-hr | **8082** |
| service-leave-attendance | **8083** |
| service-payroll | **8084** |
| service-recruitment | **8085** |
| service-performance | **8086** |
| service-onboarding | **8090** |
| service-document | **8091** |
| service-notification | **8092** |
| service-expense | **8093** |
| service-asset | **8094** |
| service-helpdesk | **8095** |
| service-social | **8096** |
| service-compensation | **8097** |
| service-compliance | **8098** |
| service-offboarding | **8099** |
| service-lms | **8100** |
| service-workflow | **8101** |
| service-reports | **8102** |
| service-discovery (Eureka) | **8761** |
| config-server | **8888** |

**No port conflicts detected.**

---

### 2.2 Files You DON'T Need to Change (defaults work with Docker)

All services already have:
- `spring.datasource.url: jdbc:postgresql://localhost:5432/hrms_db` ✅
- `spring.datasource.username: hrms_user` ✅ (matches Docker)
- `spring.datasource.password: hrms_pass` ✅ (matches Docker)
- `spring.data.redis.host: localhost` / port `6379` ✅
- `spring.kafka.bootstrap-servers: localhost:9092` ✅
- `eureka.client.service-url.defaultZone: http://localhost:8761/eureka/` ✅

**These services need NO changes for local dev:**
- service-core-hr, service-leave-attendance, service-payroll, service-recruitment,
  service-performance, service-onboarding, service-document, service-notification,
  service-expense, service-asset, service-helpdesk, service-social, service-compensation,
  service-compliance, service-offboarding, service-lms, service-workflow, service-reports,
  service-discovery, api-gateway

---

### 2.3 Files You MUST Change Before Testing

#### service-auth: `src/main/resources/application.yml`

**Email/SMTP — Lines ~40-53:**
```yaml
spring:
  mail:
    username: ${MAIL_USERNAME:hrms.noreply@gmail.com}  # ← Change this
    password: ${MAIL_PASSWORD:change-me}                # ← MUST CHANGE
```

**For local dev (OTP just logs — no real email needed):**
> The `OtpService.java` does **NOT** actually send emails — it logs the OTP to the console with a TODO comment. See `service-auth/src/main/java/com/hrms/auth/service/OtpService.java`, method `sendOtpEmail()`.
>
> **You DO NOT need SMTP configured to test OTP locally.** The OTP will appear in the auth service console output (`service-auth.log`).

**For production (when you want real emails):**
Option A — Gmail App Password (recommended for testing):
1. Go to myaccount.google.com → Security → 2-Step Verification → App passwords
2. Create app password for "Mail"
3. Set environment variables:
   ```bat
   set MAIL_USERNAME=your.email@gmail.com
   set MAIL_PASSWORD=xxxx-xxxx-xxxx-xxxx
   ```

Option B — Mailtrap (free email sandbox, zero config):
1. Sign up at mailtrap.io → Inbox → SMTP Settings
2. Update application.yml:
   ```yaml
   spring:
     mail:
       host: smtp.mailtrap.io
       port: 587
       username: your-mailtrap-username
       password: your-mailtrap-password
   ```

**JWT Secret (Lines ~62-65):**
```yaml
jwt:
  secret: ${JWT_SECRET:hrms-platform-jwt-secret-key-change-this-in-production-256bit!!}
```
> For local dev: the default value works. For production, set `JWT_SECRET` environment variable to a random 256-bit string.

---

### 2.4 Files to Change for PRODUCTION (Skip for Local Dev)

All service `application.yml` files that have hardcoded credentials (service-recruitment, service-performance, service-onboarding, and 13 scaffold services) should use environment variable substitution for production:
```yaml
# Change this (hardcoded):
username: hrms_user
password: hrms_pass

# To this (env-var based):
username: ${DB_USERNAME:hrms_user}
password: ${DB_PASSWORD:hrms_pass}
```
**Files affected:** service-recruitment, service-performance, service-onboarding, service-document, service-notification, service-expense, service-asset, service-helpdesk, service-social, service-compensation, service-compliance, service-offboarding, service-lms, service-workflow, service-reports.

---

### 2.5 Eureka Config — All Services Point to Same URL ✅

All services: `http://localhost:8761/eureka/` — consistent, no issues.

---

### 2.6 OTP Behavior Summary

| Scenario | What Happens |
|----------|--------------|
| `POST /api/v1/auth/otp/send` | OTP generated, stored in Redis for 5 min, **logged to console only** |
| Where to find OTP | Search `service-auth.log` for: `OTP for [email]` |
| Email sent? | **NO** — `sendOtpEmail()` is a TODO stub |
| To enable email | Wire up `JavaMailSender` in `OtpService.sendOtpEmail()` and set `MAIL_USERNAME` + `MAIL_PASSWORD` |

---

## TASK 3: SERVICE STARTUP ORDER

### 3.1 Mandatory Startup Order

```
Layer 1 (Infrastructure — must be running first):
  ├── Docker: PostgreSQL, Redis, Kafka, Zookeeper

Layer 2 (Service Registry — must start before all microservices):
  └── service-discovery (Eureka) :8761
      Wait: 20-25 seconds

Layer 3 (Core Services — parallel startup OK within layer):
  ├── service-auth :8081          (handles all authentication)
  └── service-core-hr :8082       (master employee data)
      Wait: 15-20 seconds each

Layer 4 (Business Services — depend on Layer 3 being in Eureka):
  ├── service-leave-attendance :8083
  ├── service-payroll :8084
  ├── service-recruitment :8085
  └── service-performance :8086
      Wait: 15 seconds

Layer 5 (Supporting Services — can start in parallel):
  ├── service-onboarding :8090
  ├── service-document :8091
  ├── service-notification :8092
  ├── service-expense :8093
  ├── service-asset :8094
  ├── service-helpdesk :8095
  ├── service-social :8096
  ├── service-compensation :8097
  ├── service-compliance :8098
  ├── service-offboarding :8099
  ├── service-lms :8100
  ├── service-workflow :8101
  └── service-reports :8102

Layer 6 (Gateway — must start last, after services are in Eureka):
  └── api-gateway :8080
```

### 3.2 Services That Can Start Independently

All services are loosely coupled via Eureka discovery. No service makes direct synchronous calls to another service at startup. Dependencies are:
- All services → PostgreSQL (via JDBC at startup, fails if DB is down)
- All services → Redis (used for caching/OTP, fails if Redis is down)
- All services → Eureka (for registration, will retry if Eureka is slow)
- api-gateway → Eureka (needs service registry to route requests)

### 3.3 Minimum Set to Test Auth + Core HR + Leave + Payroll

```
1. Docker (postgres + redis + kafka)
2. service-discovery :8761
3. service-auth :8081
4. service-core-hr :8082
5. service-leave-attendance :8083
6. service-payroll :8084
7. api-gateway :8080  (optional — can call services directly by port)
```

Run: `scripts\start.bat` (default mode = minimum set above)

### 3.4 Scripts

| Script | Purpose |
|--------|---------|
| `scripts\verify-infra.bat` | Start Docker, verify all containers |
| `scripts\start.bat` | Start minimum services (or `start.bat full` for all) |
| `scripts\stop.bat` | Stop all running HRMS services |
| `scripts\test-backend.bat` | Integration smoke tests |

### 3.5 Recommended Complete Startup Sequence

```bat
:: Step 1 - Start Docker infrastructure
scripts\verify-infra.bat

:: Step 2 - Start services (minimum for testing)
scripts\start.bat

:: Step 3 - Verify Eureka has services registered
start http://localhost:8761

:: Step 4 - Run smoke tests
scripts\test-backend.bat

:: Step 5 - Import Postman collection
:: Import: postman\HRMS_Complete_Collection.json into Postman
```
