# HRMS Enterprise Platform

Production-ready, multi-tenant HRMS SaaS platform built for end-to-end people operations at enterprise scale.

Stack: Java 17, Spring Boot 3.2.x, Spring Cloud, Kafka, PostgreSQL 16, Flyway, React + TypeScript + Vite, Docker, Helm.

## Why this platform stands out

- Full employee lifecycle coverage from hiring to offboarding in a modular microservices architecture.
- Multi-tenant foundation with tenant context, feature flags, quotas, and async operations.
- Event-driven backbone with outbox pattern, Kafka publishers/listeners, and saga orchestration.
- Deep frontend-backend parity with typed endpoint catalog and broad route coverage.
- Platform capabilities beyond basic HRMS: auditability, compliance workflows, extensible integrations, and observability.

## Platform snapshot

- 28 backend microservices.
- 14 shared platform/common libraries.
- 900+ backend REST endpoints (frontend audit indicates 926 endpoints).
- 99.8 percent frontend service-layer coverage (remaining SSE streams are EventSource based).

Reference: FRONTEND-AUDIT.md, gap_summary.txt, gap_analysis.txt.

## Architecture

- Edge and infra:
	- api-gateway
	- service-discovery (Eureka)
	- config-server
- Shared platform modules:
	- common-lib
	- common-security
	- common-tenant
	- common-storage
	- common-mail
	- common-pdf
	- common-audit
	- common-events
	- common-clients
	- common-search
	- common-i18n
	- common-resilience
	- common-observability
	- common-esign
- Business domain services:
	- service-auth
	- service-core-hr
	- service-leave-attendance
	- service-payroll
	- service-recruitment
	- service-onboarding
	- service-performance
	- service-lms
	- service-expense
	- service-document
	- service-notification
	- service-workflow
	- service-reports
	- service-social
	- service-helpdesk
	- service-asset
	- service-compensation
	- service-compliance
	- service-offboarding
	- service-travel
	- service-timesheet
	- service-cases
	- service-files
	- service-forms
	- service-engagement
	- service-skills
	- service-integrations
	- service-workplace

## Feature map

### Core HR and organization

- Employee master data and lifecycle events.
- Department, designation, location, org chart, and cost center management.
- Employee self-service surfaces for profile, documents, leave, timesheets, and payslips.

### Time, attendance, leave, and workforce scheduling

- Attendance capture and approval workflows.
- Leave policies, applications, balances, and leave calendar.
- Shift management and holiday calendars.
- Timesheet and travel request modules.

### Payroll, compensation, and tax

- Payroll runs and salary structures.
- Pay grades, salary components, and employee salary administration.
- Benefits, loans, tax configuration, declarations, and investment proofs.

### Talent acquisition and lifecycle automation

- Requisitions, jobs, candidates, applications, interviews, offers, and pipelines.
- Recruitment analytics (funnel, time-to-hire, cost-per-hire style advanced metrics).
- Onboarding templates, tasks, buddy assignment, probation, preboarding portal.
- Offboarding separations, checklists, exit interviews, and knowledge transfer.

### Performance, learning, and engagement

- Goals, review cycles, one-on-ones, competencies, PIP plans, and nine-box talent views.
- LMS courses, modules, enrollments, assessments, and certifications.
- Social feed, kudos, polls, surveys, rewards, pulse, and wellness-oriented engagement flows.

### Operations, compliance, and service delivery

- Assets, assignments, maintenance, requests, vendors, and workplace desk/visitor management.
- Helpdesk tickets, categories, comments, and knowledge-base management.
- Compliance items, audit logs, GDPR workflows, statutory/statements support modules.
- Reports, dashboards, saved reports, widgets, and DEI analytics surfaces.

## Shared platform capabilities

- Security and identity:
	- JWT-based auth patterns.
	- MFA support building blocks (TOTP dependency included).
	- SCIM endpoints represented in frontend catalog.
- Multi-tenancy:
	- Tenant context and tenant schema resolution.
	- Feature flags and tenant quota services in common-tenant.
	- Async operation support for long-running tasks.
- Eventing and reliability:
	- Domain events and topic model.
	- Outbox persistence and polling.
	- Saga orchestration support.
	- Resilience4j integration and trace propagation.
- Storage and documents:
	- MinIO/S3-style object handling.
	- PDF and document template tooling.
	- File vault/document APIs and pre-signed download patterns.
- Observability and operations:
	- Micrometer + OpenTelemetry dependencies.
	- Actuator integration across services.
	- Grafana/Prometheus assets in scripts and deploy folders.

## Frontend highlights

- React + TypeScript + Vite single-page app in hrms-frontend.
- Extensive route map spanning HR, payroll, talent, engagement, compliance, reports, and admin surfaces.
- Unified typed endpoint catalog in hrms-frontend/src/services/catalog.ts with broad backend coverage.
- Realtime support via:
	- Server-Sent Events for dashboard/notification streams.
	- WebSocket stream hooks for notification/event updates.
- PWA-ready assets are present (manifest and service worker).

## Integrations currently represented

- Slack:
	- SDK dependency present.
	- Inbound slash command/events webhook controller.
	- HMAC signing verification path.
- Microsoft Teams:
	- Inbound bot activity endpoint.
- Generic enterprise webhooks:
	- Subscription API.
	- Signed outbound delivery with exponential retry/backoff.
- Messaging and notification ecosystem dependencies include Kafka, Firebase Admin, Twilio, and Slack client.

## Getting started (local)

### Prerequisites

- JDK 17
- Maven 3.9+
- Docker + Docker Compose
- Node.js 20+ (for hrms-frontend)

### Boot infrastructure

```bash
docker-compose up -d
```

### Build backend

```bash
mvn clean install -DskipTests
```

### Run core backend services

```bash
mvn -pl service-discovery spring-boot:run
mvn -pl config-server spring-boot:run
mvn -pl api-gateway spring-boot:run
mvn -pl service-auth spring-boot:run
mvn -pl service-core-hr spring-boot:run
```

### Run frontend

```bash
cd hrms-frontend
npm install
npm run dev
```

### Common local endpoints

- Eureka: http://localhost:8761
- API Gateway: http://localhost:8080
- MinIO Console: http://localhost:9001

## Deployments and runbooks

- Docker: docker-compose.yml, docker-compose.prod.yml, Dockerfile
- Kubernetes/Helm assets: deploy/helm
- Operational docs:
	- ONBOARDING.md
	- RUNBOOK.md
	- DEPLOY.md
	- PRODUCTION-CHECKLIST.md
	- SECURITY.md
	- DR.md

## What to build next to become best-in-class

- Deep product analytics:
	- Cohort retention, manager effectiveness, workforce planning forecasts.
- AI-assisted experiences:
	- Resume parsing plus candidate-job fit scoring.
	- Employee policy Q and A assistant grounded in document vault.
	- Review writing co-pilot and performance calibration recommendations.
- Enterprise integration accelerators:
	- Native connectors for Google Workspace, Microsoft 365, SAP/Oracle payroll, Okta/Azure AD, ATS ecosystems.
	- Marketplace-style integration templates for low-code setup.
- Governance and trust:
	- Fine-grained data residency controls.
	- Immutable audit timelines and retention/legal hold automation.
	- More comprehensive automated compliance evidence exports.
- Reliability and scale:
	- Chaos testing and SLO error budget workflows.
	- Progressive delivery with canary and automated rollback pipelines.

## License and ownership

Internal project. Add your preferred license file if this is intended to be open-sourced.
