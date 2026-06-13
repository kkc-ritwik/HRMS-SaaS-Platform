# HRMS Frontend ↔ Backend Parity Audit

Generated 2026-05-31, updated 2026-06-04. Re-runnable via `python .audit/extract_backend.py && python .audit/extract_frontend.py && python .audit/compute_gap.py`.

## Pass 6 (2026-06-05) — second backend wave + deep path-drift sweep

Continued building missing controllers and fixing FE path-drift. **Orphans 62 → 31.**
Backend now **164 controllers / 981 endpoints**; `mvn compile` across all 44 modules ✓,
FE `tsc -b` ✓, `vite build` ✓.

Backend built (each compiled):
- **service-recruitment** — `GET /recruitment/bgv` (list all BGV cases).
- **service-cases** — `GET /cases/{id}`.
- **service-notification** — `POST /notifications/read-all`.
- **service-helpdesk** — `GET /helpdesk/tickets/csat/summary`, `POST /tickets/kb-articles/{id}/feedback`.
- **service-leave-attendance** — biometric **device registry**: `GET/POST /attendance/biometric/devices`, `POST …/{id}/sync` (+ `V5__biometric_devices.sql`).
- **service-compensation** — `GET /compensation/total-rewards/{employeeId}` (benefits statement).
- **service-social** — `POST /groups/events/{id}/rsvp`.
- **service-auth** — `POST /users/{id}/deactivate`.

FE path-drift fixed (now hit real endpoints): workflow approvals → `/workflows/instances/{id}/*`,
OOO → `/workflow/ooo/*`, workflow templates → `/workflows/definitions`, interview panellists →
`/panelists`, reference-checks → `/recruitment/references`, onboarding task complete → `PUT /{id}`,
groups join → `/groups/members/join`, recruitment analytics → dashboard/pipeline, ticket rate →
`/helpdesk/tickets/{id}/csat`, ticket comments → `/tickets/comments/ticket/{id}`, kb → `/tickets/kb-articles`,
exit-interview submit → `/complete`, benefits → `/compensation/{benefits,employee-benefits}`,
events → `/groups/events`, letters/employment → `/letters/employment/{type}`, audit → `/audit/entity`.

**Remaining 31** need new entities/integrations (each a discrete build): POSH annual-return,
e-sign request flow (needs the `signature_requests` table in service-document + provider config),
expense OCR parse, integration-connector CRUD/test/secret-rotation, public careers job list,
notification dispatch-history, document-template generate, offboarding clearance sign-off,
RBAC role-permission/bulk-role editor, and a few employee-scoped `/me` list variants
(salary, advances, asset-requests, shifts). Regenerate live list: `python .audit/orphans.py`.

---

## Pass 5 (2026-06-05) — building the missing backend, module by module

Continued closing the orphan list by **building the genuinely-missing backend
controllers** (each compiled with `mvn -pl <module> -am compile -o`). Where a new
table was required, a matching Flyway migration was added (`ddl-auto: validate`).

| Module | Endpoints added | Migration? |
|---|---|---|
| **service-auth** — MFA + Sessions | `GET/POST /auth/mfa/{status,enroll,verify,disable}`, `GET /auth/sessions`, `DELETE /auth/sessions/{id}` | none (mfa cols + sessions table already existed) |
| **service-auth** — Tenant settings | `GET/POST /tenants/current/branding`, `POST /branding/logo`, `POST /flags/{key}` | `V3__tenant_settings.sql` |
| **service-reports** — Execution | `POST /reports/{id}/{run,export,schedule}`, `POST /reports/saved/{id}/share` | `V4__saved_report_sharing.sql` (reuses existing query engine + exporter + schedules) |
| **service-performance** — Analytics | `GET /performance/analytics/{distribution,rating-spread,calibration}` | none |
| **service-compliance** — Statutory returns | `GET/POST /compliance/statutory-returns`, `GET /{id}`, `POST /{id}/submit` | `V2__statutory_returns.sql` |
| **service-offboarding** — Extras | `POST /offboarding/notice-buyout/calc`, `POST /offboarding/separations/{id}/withdraw` | none |
| **service-leave-attendance** — Optional holidays | `GET /holidays/optional/quota`, `POST /holidays/optional/select` | `V4__optional_holiday_selections.sql` |

Plus more FE path-drift fixes (`pollService` → `/engagement/polls`, social likes/comments/group-join,
letters/employment, maintenance/asset `complete`/`retire`, performance-analytics curried-fn bug,
audit `auditByEntity` → `/entity`).

**Verified:** `mvn compile` across **all 44 modules ✓**, FE `tsc -b` ✓, `vite build` ✓.
Backend grew to **162 controllers / 970 endpoints**. Broken frontend calls: **128 → 62**
(remaining are lower-value admin stubs: e-sign, integration connectors, biometric device
registry, POSH return, CSAT summary, total-rewards, expense OCR, events RSVP, reference
checks — each needs its own entity/migration).

---

## Pass 4 (2026-06-04) — broken-call audit + missing backend built

Earlier passes measured *catalog coverage* (every backend endpoint had a wrapper).
This pass measured the inverse — **frontend calls that hit a path the backend does
not expose** (real runtime 404s the coverage metric hid). New tool:
`.audit/orphans.py`.

**Found 128 frontend-orphan paths.** Two classes:

**(A) Path-drift bugs — backend exists, frontend called the wrong path. Fixed ~20:**

| Service | Was | Now |
|---|---|---|
| loanService | `/api/v1/loans/*` | `/api/v1/payroll/loans/*` |
| gdprService (×2 files) | `/api/v1/gdpr/{export,erase,…}/{id}` | `/api/v1/me/{data,erase,restrict,consent}` (self-service) |
| kbService | `/api/v1/kb` | `/api/v1/tickets/kb-articles` |
| certificationService | `/api/v1/certifications` | `/api/v1/courses/certifications` |
| assessmentService | `/api/v1/courses/{id}/assessments` | `/api/v1/courses/assessments/course/{id}` |
| orgChartService | `/api/v1/org-chart/tree` | `/api/v1/org-chart` |
| skillsService | `/api/v1/skills`, `/skills/employee/{id}` | `/skills/gap`, `/skills/employees/{id}` |
| selfServiceService | `/api/v1/self-service/*` | `/api/v1/me/{dashboard,team,approvals}` |
| documentTemplateService | `/api/v1/document-templates` | `/api/v1/documents/templates` |
| companyPolicyService | `/api/v1/policies` | `/api/v1/documents/policies` |
| delegationRuleService | `/api/v1/workflow/delegation-rules` | `/api/v1/workflows/delegations` |
| workflowDefinitionService | `…/{id}/publish` | `…/{id}/activate` |
| buddyService | `/api/v1/onboarding/buddies` | `/api/v1/onboarding/buddy-assignments` |
| bgvService | `/recruitment/bgv/{id}` | `/recruitment/bgv/candidate/{id}` |
| complianceItemService | `…/{id}/done` | `…/{id}/mark-compliant` |
| salaryStructureService | `/api/v1/salary-structures` | `/api/v1/salary/structures` |
| offerService | `…/{id}/issue`, `/withdraw` | `…/{id}/send`, `/revoke` |
| reviewCycleService | `…/{id}/launch` | `…/{id}/activate` + real stage endpoints |
| probationReviewService (×2) | `…/{id}/decide` | `PUT …/{id}` |

GdprPage was also rebuilt as a self-service DSAR page (the backend is per-current-user).

**(B) Genuinely-missing backend — built end-to-end:**

| Feature | What existed | What was added |
|---|---|---|
| **Vendors** (`/api/v1/vendors`) | `Vendor` entity + `vendors` table | `VendorRepository` + `VendorController` (full CRUD) |
| **AMC Contracts** (`/api/v1/assets/amc-contracts`) | `AmcContract` entity + table | `AmcContractRepository` + `AmcContractController` (CRUD + `/expiring`) |
| **Awards** (`/api/v1/awards`) | nothing | `V3__awards.sql` migration + `Award` entity + repo + `AwardController` (nominate/approve/reject/CRUD) |

`mvn -pl service-asset -am compile` ✓ and `mvn -pl service-engagement -am compile` ✓
(both offline, exit 0). Backend is now **157 controllers / 944 endpoints** (was 154/926).
Vendors, AMC, and Awards pages wired to full CRUD against the new endpoints.

**Result: 128 → 84 orphan paths.** All core day-to-day pages now hit real endpoints.

### Pass 4b — more path-drift fixes + audit search

Further FE path-drift fixes: `pollService` → `/api/v1/engagement/polls`; `socialService`
post-likes → `/api/v1/posts/likes`, comments → `/api/v1/posts/comments/post/{id}`,
group join → `/api/v1/groups/members/join`; `lettersService.employment` →
`/api/v1/letters/employment/{type}`; `assetMaintenanceService.complete` → `PUT …/{id}`;
`assetService.retire` → `PUT …/{id}` status; `complianceService.auditByEntity` →
`/api/v1/audit/entity`.

Backend: added **`GET /api/v1/audit/search`** (free-text audit search) to
`AuditController` + repo finders. `mvn compile` across **all 44 modules ✓** (offline,
exit 0). Backend now **157 controllers / 945 endpoints**.

### Remaining 84 — genuinely-missing backend controllers (not yet built)

These are admin/settings features with a frontend stub but no backend controller.
Each needs a new controller (and some a new entity/migration). None block the core
HR workflows. Grouped:

- **Security/admin:** MFA enroll/verify/disable + active sessions (only OTP exists), e-sign requests, audit full-text search, tenant branding/logo + feature-flag toggles, role-permission editor, user deactivate/role-assign.
- **Integrations:** connector CRUD + test + webhook secret rotation (only Slack/Teams inbound + webhook receivers exist).
- **Reports engine actions:** run / export / schedule / share a saved report.
- **Attendance:** biometric **device registry** + sync (punch ingestion exists).
- **Offboarding extras:** notice-period buy-out calc, clearance sign-off, separation withdraw.
- **Performance analytics:** rating distribution / spread / calibration board.
- **Misc:** optional-holiday quota/selection, total-rewards statement, POSH annual return, helpdesk CSAT summary, expense OCR parse, notifications read-all/dispatch-history.

Full live list regenerates via `python .audit/orphans.py`.

---

## Pass 3 (2026-06-04) — full CRUD on every list page

Pass 2 made detail pages deep. Pass 3 closed the **write gap**: ~30 list pages
were read-only (GET a table, no way to create / edit / delete / approve / reject /
activate), even though the backend exposed those verbs.

Introduced **`ResourcePage<T>`** (`src/components/ui/resource-page.tsx`) — a
declarative wrapper over DataList + FormDialog + ConfirmDialog that adds
create / edit / delete + custom state-transition row actions (with optional
confirm prompts) from a few lines of config. **32 pages** now use it.

Pages upgraded to full CRUD + actions:

| Domain | Pages | New actions wired |
|--------|-------|-------------------|
| Payroll/Comp | Benefits, PayGrades, SalaryStructures | create/edit/delete |
| Performance | Reviews, ReviewCycles, PIP, Competencies, 1-on-1s | submit/acknowledge, launch/calibrate/finalise, activate, complete |
| Assets | Assets, AssetCategories, AssetRequests, AssetMaintenance, AMC | create/edit/delete, approve/reject, complete, retire |
| Engagement | Polls, Wellness, StayInterviews, Suggestions, Kudos | create, complete, vote/de-anonymise, give-kudos |
| Compliance | ComplianceItems, Licenses | create/edit/delete, mark-compliant |
| Ops | Shifts, Workflows, Helpdesk Tickets, Vendors, Holidays | create/edit/delete, activate/deactivate, resolve/close |
| Recruitment | Interviews, Offers, HiringLoops | schedule, issue/withdraw, complete |
| Lifecycle | Onboarding (row→detail + start) | start onboarding |
| Workplace | Visitors | register, check-in, check-out |
| Travel/Expense | Travel, Advances | request/submit/approve/reject, settle |
| Documents | Policies, Letters | acknowledge, generate |

The unified `ApprovalsPage` (pass 2) already covers approve/reject across modules.
Only `CertificationsPage` stays read-only by design (personal "my certs" view;
issuance/revocation lives in CourseDetail + admin).

`tsc -b` exit 0 · `vite build` ✓ · endpoint coverage 924/926.

---

## Pass 2 (2026-06-04) — deep page integration

The first pass made all 926 backend endpoints *callable* (service layer). The
gap that remained was **depth**: many detail pages had empty/hardcoded tabs that
never queried the backend. Pass 2 rebuilt the core detail pages so each tab pulls
live data:

| Page | Tabs now wired to backend |
|------|---------------------------|
| `EmployeeDetailPage` | Overview · Personal (addresses/family/emergency) · Employment (education/work-history/team/lifecycle) · Documents · Payroll (payslips) · Leave (balances/applications) · Performance (PIPs/feedback/1-on-1s) · Learning (enrollments/certs/skills) · Assets (assignments/requests) · Workflow (OOO/instances/separation) · Audit (timeline/compliance/licenses) |
| `CandidateDetailPage` | Profile · Applications · Interviews · Offers · BGV · References · Psychometric · Resume |
| `JobDetailPage` | Overview · Pipeline · Funnel · Analytics (time-to-hire) · Interviews · Hiring Loops |
| `OnboardingDetailPage` | Overview · Tasks · Buddy · Documents · Probation · Pre-Onboarding portal invite |
| `AssetDetailPage` | Overview · Assignments · Maintenance · AMC · Audit |
| `TicketDetailPage` | Conversation · SLA tracker · KB Suggestions · Audit |

New pages added in pass 2:
- `SeparationDetailPage` (`/separations/:id`) — checklist, exit interview, knowledge transfer, FnF, audit
- `CourseDetailPage` (`/courses/:id`) — modules, assessments, enrollments, analytics
- `CaseDetailPage` (`/cases/:id`) — investigator notes, status workflow, parties, audit
- `MyTeamPage` (`/my-team`) — manager workspace: roster, who's-out-today, approvals, team metrics
- `ApprovalsPage` rebuilt as a unified cross-module approval inbox (`/api/v1/me/approvals` + workflow fallback, category filters, deep-links to source records)

List pages (`CoursesPage`, `OffboardingPage`, `CasesPage`) now row-link to their
new detail pages. Sidebar + command palette updated with **My Team**.

`tsc -b` exit 0 · `vite build` ✓ · endpoint coverage unchanged at 924/926.

---


## Headline numbers

|                                                | Before this pass | After this pass |
|------------------------------------------------|------------------|-----------------|
| Backend controllers (across 28 microservices)  | 154              | 154             |
| Backend REST endpoints (verb+path, normalised) | 926              | 926             |
| Frontend axios call sites                      | 576              | **1 084**       |
| Endpoints with **no** frontend caller          | **510**          | **2** *(SSE)*   |
| Frontend coverage                              | 62 %             | **99.8 %**      |

The remaining 2 endpoints (`GET /api/notifications/sse` and `…/sse/dashboard`) are
Server-Sent Event streams consumed via the browser `EventSource` API, not axios —
the URLs are exposed by `notificationsCatalog.sseUrl()` / `sseDashboardUrl()` so
they are wired, just not visible to the static axios-call extractor.

## What was added

### 1. `hrms-frontend/src/services/catalog.ts` *(new)*

One ~700-line, fully-typed file with **every** backend REST endpoint exposed as a
JS callable. Organised hierarchically per microservice:

```ts
import { Catalog } from '@/services'

await Catalog.assets.assignments.return(id)
await Catalog.payrollRuns.process(runId)
await Catalog.recruitment.analytics.costPerHire()
await Catalog.offboarding.separations.approve(sepId, { effectiveDate })
await Catalog.workflows.delegations.deactivate(delegationId)
await Catalog.compliance.items.markCompliant(itemId, { evidenceUri })
await Catalog.scim.patchUser(userId, patchOps)
```

Covers the 510 endpoints that previously had no frontend service wrapper, plus
re-exposes the existing ones in a uniform shape. All 45 backend module groupings
are represented:

```
courses · documents · assets · onboarding · tickets · offboarding · recruitment
reports · workflows · compensation · leaves · notifications · compliance · groups
posts · engagement · expenses · workplace · oneOnOnes · scim · fileVault · me
auth · competencies · files · roles · payrollRuns · holidays · payslips
pipPlans · skills · workspace · performance · ooo · integrations · feedback
interviews · audit · cases · forms · users · careersPublic · apiKeys
employeesBulk · letters · tenants
```

### 2. `hrms-frontend/src/services/index.ts` *(new barrel)*

Single import surface — `import { Catalog } from '@/services'` works app-wide.

### 3. Bug fixes in existing pages

The full type-check (`tsc -b`) surfaced 8 pre-existing breakages once the new
catalog was added:

| File                                | Fix                                                    |
|-------------------------------------|--------------------------------------------------------|
| `pages/employees/EmployeeDetailPage.tsx` | Handle either unwrapped Employee or `{data: …}` shape |
| `pages/employees/EmployeeListPage.tsx`   | Accept `{employees,total}`, `{content,…}`, or raw array; add `pageSize` to params |
| `services/employeeService.ts`            | Add `pageSize` to `EmployeeListParams`                |
| `services/leaveService.ts`               | `listBalances` / `listApplications` accept optional pagination params |

The TypeScript build now passes cleanly (`tsc -b --force` exit 0) and the Vite
production build completes (`✓ built in 1.89s, 3068 modules`).

## How the audit works

```
.audit/
├── extract_backend.py     # walks /service-*/src/main/java/**/*Controller.java,
│                          # parses @RequestMapping + @{Get,Post,Put,Delete,Patch}Mapping
│                          # → backend_endpoints.txt (verb + path)
├── extract_frontend.py    # walks hrms-frontend/src/**/*.{ts,tsx}, finds
│                          # api.<verb>(…), G/P/PU/PA/D(…) helpers
│                          # → frontend_calls.txt
├── compute_gap.py         # set-diff after normalising `{anything}` → `{x}`
│                          # → gap.txt (currently 2 SSE entries)
├── backend_endpoints.txt
├── backend_by_controller.txt
├── frontend_calls.txt
└── gap.txt
```

Re-run whenever the backend grows new controllers — the catalog is intended to
stay 1:1 with the backend surface.

## Pages

The frontend already had **105 page components** wired across **120 routes** in
`App.tsx` before this pass — every Zoho People surface (recruitment, lifecycle,
payroll, performance, LMS, engagement, workplace, compliance, helpdesk, social,
admin) had at least one dedicated page. No new pages were created in this pass
because the gap was on the *service layer*, not the UI shell. New endpoints
added by the catalog can be wired into the existing pages by importing
`Catalog.<module>.<method>` — no routing changes required.

## What this does **not** cover

- **Backend infra services with no HTTP surface**: `FeatureFlagService`,
  `TenantQuotaService`, `AsyncOperationService`, `SagaOrchestrator` are
  internal-only. If you later expose admin endpoints for them, re-run the audit
  and they'll be flagged as new gaps.
- **End-to-end behavioural correctness**: this audit confirms each backend
  endpoint *can be called* from the frontend with the right shape. It does not
  exercise the calls at runtime. For that, use `/code-review ultra` or manual QA.
