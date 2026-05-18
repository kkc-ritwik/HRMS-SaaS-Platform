# HRMS Platform — Configuration Reference

Everything you need to set before going live. Copy `.env.example` to `.env`,
fill in the placeholders marked `REPLACE_ME`, then bring up infra:

```bash
docker compose up -d
```

For local dev, the defaults already work (MailHog at http://localhost:8025
captures outbound mail; MinIO console at http://localhost:9001; Kibana at
http://localhost:5601; Grafana at http://localhost:3001 admin/admin).

---

## What you must provide before production

### 1. SMTP (transactional email)
Every notification, OTP, password reset, payslip distribution, leave approval
notice goes through `common-mail`. Configure via:

```
SMTP_HOST=email-smtp.us-east-1.amazonaws.com   # or smtp.sendgrid.net / smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=<from your provider>
SMTP_PASSWORD=<from your provider>
SMTP_STARTTLS=true
MAIL_FROM=noreply@yourcompany.com              # must be a verified sender
MAIL_FROM_NAME=Your Company HR
```

Verified provider domains: SES, SendGrid, Mailgun, Postmark, Gmail
(app password), Google Workspace, MS 365 SMTP relay — any will work.

### 2. File storage (MinIO local / S3 prod)
All resumes, payslips, letters, documents, evidence files go through
`common-storage`. For AWS S3 in production:

```
STORAGE_ENDPOINT=https://s3.<region>.amazonaws.com
STORAGE_ACCESS_KEY=AKIA…
STORAGE_SECRET_KEY=…
STORAGE_REGION=us-east-1
STORAGE_PATH_STYLE=false           # MUST be false for AWS
STORAGE_DEFAULT_BUCKET=hrms-prod
```

Buckets are auto-created per tenant on first write (`hrms-prod-<tenantId>`).

### 3. SMS gateway (Twilio recommended)
For OTP fallback and high-priority alerts. Get credentials at
https://console.twilio.com:

```
TWILIO_ACCOUNT_SID=AC…
TWILIO_AUTH_TOKEN=…
TWILIO_FROM_NUMBER=+1…
```

### 4. Push notifications (Firebase Cloud Messaging)
Download your service-account JSON from Firebase Console → Project Settings →
Service Accounts → Generate New Private Key. Mount at:

```
FCM_SERVICE_ACCOUNT_PATH=/etc/hrms/firebase-service-account.json
FCM_PROJECT_ID=your-firebase-project-id
```

### 5. JWT signing key
Generate once, never rotate without a rolling-restart plan:

```bash
openssl rand -base64 64
```

Put the result into `JWT_SECRET`. Anything shorter than 64 bytes will be
rejected by `HS512`.

### 6. SSO providers (optional but expected by Zoho parity)
Pick the ones you need. Each provider goes into the `OAUTH_*` block in `.env`:

- **Google Workspace** — at https://console.cloud.google.com → APIs → Credentials → OAuth client ID (Web)
- **Microsoft 365 / Azure AD** — at https://portal.azure.com → App registrations
- **Okta** — admin console → Applications → Add → Web

### 7. E-signature provider
For offer letters, policy acknowledgment, separation letters:

- **DocuSign** (most enterprise): Integration Key + RSA private key (JWT grant)
- **Aadhaar eSign** (India-only): API key from your ASP (eMudhra, NSDL eSign, etc.)

### 8. Background verification
Most Indian deployments use AuthBridge or OnGrid. Both expose REST APIs:

```
BGV_PROVIDER=authbridge
BGV_API_KEY=…
BGV_BASE_URL=https://api.authbridge.com
```

### 9. Job board integrations (recruitment)
- LinkedIn Talent Solutions API — partner approval required
- Indeed Publisher API — sign up at https://opensource.indeedeng.io
- Naukri.com API — contact sales for credentials

### 10. Bank payroll disbursement
For NEFT/RTGS bulk payroll. Sample providers: ICICI Corporate API, HDFC
NetBanking corporate, Axis Sastra. Each has its own credential ceremony —
fill the `BANK_API_*` block.

### 11. Slack / Teams workspace integration
- **Slack**: Create app at https://api.slack.com/apps → Bot Token
- **Teams**: Incoming webhook URL from a channel

### 12. Statutory codes (India payroll)
Provide for accurate ECR / Form 24Q generation:

```
EPFO_ESTABLISHMENT_ID=…
ESIC_EMPLOYER_CODE=…
NSDL_TAN_NUMBER=…
```

---

## Per-environment overrides

| Env | Strategy |
|-----|----------|
| dev | Use `.env` at repo root + docker-compose defaults |
| staging | Secrets in HashiCorp Vault / AWS SSM Parameter Store |
| prod | Secrets in Vault / SSM / Kubernetes Secrets; never committed |

For Kubernetes, mount `.env` as a `Secret` and reference via `envFrom`.

---

## Profile-driven config

Each service supports Spring profiles: `dev`, `staging`, `prod`. Set
`SPRING_PROFILES_ACTIVE` per environment. Profile-specific `application-prod.yml`
files override defaults from `application.yml`.

---

## Health & observability endpoints

Every service exposes the same actuator surface (port shown is service-auth example):

| Endpoint | Purpose |
|---|---|
| `http://localhost:8081/actuator/health` | Liveness/readiness |
| `http://localhost:8081/actuator/prometheus` | Metrics scrape target |
| `http://localhost:8081/swagger-ui.html` | API docs |
| `http://localhost:8081/v3/api-docs` | OpenAPI JSON |

Visit Grafana (http://localhost:3001), import the included HRMS dashboards
from `scripts/grafana-dashboards/`.

---

## What gets validated at startup

When a service starts, it checks:
1. Database is reachable + Flyway migrations applied
2. Kafka broker accessible (if `common-events` on classpath)
3. Elasticsearch reachable (if `common-search`, soft-fail if disabled)
4. MinIO reachable (if `common-storage`, soft-fail if disabled)
5. SMTP reachable (if `MAIL_ENABLED=true`, soft-fail with WARN log)

A red service in Eureka means at least one hard dependency is down — check the
service's startup log. Soft failures print a single WARN and the service
keeps running with that feature disabled.
