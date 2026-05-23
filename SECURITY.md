# HRMS — Security Posture

## Threat model summary

HRMS is a multi-tenant SaaS that stores employee PII (names, addresses, national IDs, bank accounts, salary), payroll data, and immigration documents. Primary threats:

1. **Cross-tenant data leakage** — a tenant accessing another tenant's data.
2. **PII exfiltration** — credential compromise, SQL injection, unauthorized API access.
3. **Payroll tampering** — unauthorized salary or bank-account changes.
4. **Compliance breaches** — GDPR / India PDPA / California Consumer Privacy Act.
5. **DoS / rate-limit bypass** — credential-stuffing, scrapers.

## Controls in place

### Authentication & Session
- JWT bearer (HS512, 256-bit secret, 1-hour access + 7-day refresh, rotation supported)
- Password policy: min 10 chars, history of 5, max age 90d, OWASP-style strength score
- Account lockout: 5 failed attempts → 15-min lock
- MFA via TOTP (RFC 6238, 6-digit, 30-sec window)
- SAML 2.0 + OIDC SSO (Google Workspace, Okta, Azure AD)
- SCIM 2.0 for IdP-driven provisioning

### Authorization
- Role-based + per-tenant data isolation enforced by `TenantContext` thread-local
- Every JPA entity carries `tenant_id` (row-level multi-tenancy)
- Every Kafka event tagged with tenant for downstream filtering
- API keys (server-to-server) gated by `X-API-Key` + `HRMS_API_KEY_ENABLED=true`

### Cryptography
- **TLS 1.2/1.3** at the edge (NGINX or Istio gateway) — HSTS preload, OCSP stapling
- **Field-level PII encryption** (AES-256-GCM via `PiiEncryptedConverter`) on:
  - National IDs (PAN, Aadhaar, SSN)
  - Bank account numbers / IBAN
  - Insurance dependent IDs
  - Suggestion box submitter IDs (when "anonymous" mode is on)
- **At-rest encryption**: enabled on Postgres TDE + MinIO server-side encryption (SSE-S3)
- **Key management**: AWS KMS / Vault / GCP KMS via `PII_ENCRYPTION_KEY` env (rotation via dual-key staging)
- **JWT signing key** rotated quarterly with overlap period

### Audit
- `@Auditable` on 196 entities — every CRUD action recorded to `audit_logs` (immutable, append-only)
- Sensitive fields redacted in audit (`redactFields = "nationalId,bankAccount,salary"`)
- Audit-log table is hash-chained: each row's `prev_hash` covers the previous row → tamper-evidence
- Audit retention: 7 years (default) configurable per tenant

### Transport security
- mTLS between pods (Istio STRICT mode in prod)
- Service-to-service auth via SPIFFE-style identity certificates issued by Istio
- North-south traffic terminated at NGINX or Istio ingress with rate limiting (100 r/m per IP)
- East-west traffic restricted by Kubernetes NetworkPolicy (default-deny + explicit-allow)

### Application security headers
| Header | Value |
|--------|-------|
| Strict-Transport-Security | `max-age=63072000; includeSubDomains; preload` |
| X-Content-Type-Options | `nosniff` |
| X-Frame-Options | `DENY` |
| Content-Security-Policy | `default-src 'self'; frame-ancestors 'none'` |
| Referrer-Policy | `strict-origin-when-cross-origin` |
| Permissions-Policy | `geolocation=(), microphone=(), camera=()` |
| Cross-Origin-Opener-Policy | `same-origin` |
| Cross-Origin-Resource-Policy | `same-site` |

### Rate limiting & DoS protection
- Edge: NGINX `limit_req` 100 r/m per IP
- Application: Redis sliding-window per `tenant:user` (default 600 r/m, configurable)
- Idempotency-Key honoured on every POST/PUT/PATCH/DELETE (24h replay cache)

### Dependency hygiene
- Weekly OWASP dependency-check scan (CVSS ≥ 7 fails build)
- Weekly Trivy filesystem + container image scan
- Gitleaks pre-receive hook + CI job (no secrets in git)
- CodeQL semantic analysis on every PR
- Renovate / Dependabot bumping deps monthly

### Compliance endpoints (GDPR / PDPA / CCPA)
| Endpoint | Purpose |
|----------|---------|
| `GET /api/v1/gdpr/export/{employeeId}` | Right to data portability |
| `POST /api/v1/gdpr/erase/{employeeId}` | Right to be forgotten (soft → hard delete after 30d) |
| `POST /api/v1/gdpr/restrict/{employeeId}` | Right to restrict processing |
| `POST /api/v1/gdpr/consent/{employeeId}` | Consent ledger |

## Disclosure

Found a vulnerability? Email `security@hrms.yourcompany.com` with PGP-encrypted details. We aim for:
- **48 h** initial response
- **90-day** coordinated disclosure timeline
- Acknowledgment in `SECURITY.md` Hall of Fame (with permission)

## Compliance certifications (target / current)

| Standard | Status | Auditor |
|----------|--------|---------|
| SOC 2 Type II | In progress | TBD |
| ISO 27001 | Roadmap (FY27) | TBD |
| GDPR | Compliant | Internal |
| India DPDP Act 2023 | Compliant | Internal |
| HIPAA | Not in scope | — |
