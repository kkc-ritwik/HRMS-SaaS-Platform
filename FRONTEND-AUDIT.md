# HRMS Frontend ↔ Backend Parity Audit

Generated 2026-05-31. Re-runnable via `python .audit/extract_backend.py && python .audit/extract_frontend.py && python .audit/compute_gap.py`.

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
