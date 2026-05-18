# HRMS Platform — Engineer Onboarding

Welcome. This guide gets you from a fresh clone to running every service locally in about 15 minutes.

## TL;DR

```bash
# 1. Clone + copy env defaults
git clone <repo>
cd hrms
cp .env.example .env

# 2. Bring up infrastructure (Postgres, Redis, Kafka, ES, MinIO, MailHog, Jaeger, Prometheus, Grafana)
docker compose up -d

# 3. Build everything
mvn -B -DskipTests=true install

# 4. Run service-discovery first, then anything else
( cd service-discovery && mvn spring-boot:run )

# 5. In another shell, run a business service
( cd service-auth && mvn spring-boot:run )
```

Visit:
- **Eureka:** http://localhost:8761
- **API Gateway:** http://localhost:8080
- **Swagger (each service):** http://localhost:&lt;port&gt;/swagger-ui.html
- **MailHog (outgoing mail):** http://localhost:8025
- **MinIO console:** http://localhost:9001 (user `hrms_minio` / pass `hrms_minio_secret`)
- **Kibana:** http://localhost:5601
- **Jaeger:** http://localhost:16686
- **Prometheus:** http://localhost:9090
- **Grafana:** http://localhost:3001 (admin / admin) — import `scripts/grafana-dashboards/hrms-overview.json`
- **Kafka UI:** http://localhost:8090

## Repository layout

```
hrms/
├─ pom.xml                 # parent multi-module reactor
├─ common-*/                # 10 shared libraries (storage, mail, pdf, audit, events, …)
├─ service-*/               # 28 microservices
├─ deploy/helm/             # Helm chart for k8s deploys
├─ docker-compose.yml       # local infra
├─ scripts/                 # prometheus.yml, dashboards, bootstrap scripts
├─ .env.example             # placeholder env file (copy → .env)
├─ CONFIGURATION.md         # what each env var does
├─ RUNBOOK.md               # on-call / incident response
└─ README.md
```

## How shared libraries work

Every service pulls in `common-lib`, `common-security`, `common-tenant` at minimum. Then:

| Need... | Add dep |
|---|---|
| Upload/download files | `common-storage` |
| Send email | `common-mail` |
| Generate PDFs | `common-pdf` |
| Audit entity changes | `common-audit` |
| Publish/consume Kafka events | `common-events` |
| Call another service | `common-clients` |
| Full-text search | `common-search` |
| Localised strings + currency | `common-i18n` |
| Rate-limit endpoints | `common-resilience` |
| Tracing + metrics + request-id MDC | `common-observability` |

Each auto-configures via `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` — just add the dep, no `@Import` needed.

## Adding a new service

1. Copy `service-skills` as a template (small, modern).
2. Update `artifactId`, package, port in `application.yml`.
3. Register the module in the root `pom.xml` modules list.
4. Add it to `deploy/helm/hrms/values.yaml` services map.
5. Add it to `.github/workflows/release.yml` matrix.
6. Add it to `scripts/prometheus.yml` scrape targets.

## Conventions

- **Multi-tenancy**: every entity extends `BaseEntity`, has `tenantId`. All queries scope by `tenantId` from `TenantContext.get()`.
- **Auditing**: add `@Auditable("EntityName")` + `@EntityListeners(AuditEntityListener.class)` to any entity you want change-tracked.
- **Events**: state changes that other services care about → publish `DomainEvent` via `EventPublisher` (uses outbox automatically).
- **No raw SQL in controllers**: services own the data, controllers translate DTOs.
- **No business logic in repositories**: keep them to data fetching only.
- **Tests**: integration tests against real Postgres (TestContainers preferred) — no mocked DB.

## What's next

- Read `CONFIGURATION.md` for the full env-var reference.
- Read `RUNBOOK.md` if you're on-call.
- Read the comprehensive backend audit at `.claude/audit_report.md`.
