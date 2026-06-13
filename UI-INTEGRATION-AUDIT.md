# UI Integration Audit — what the backend exposes vs. what the UI actually uses

Generated 2026-06-05. Regenerate: `python .audit/extract_backend.py && python .audit/reachability.py`

## Progress log (integration %)

Tracked by `python .audit/reachability.py`.

| Date | Reached / 981 | % | Module(s) wired |
|------|---------------|---|-----------------|
| 2026-06-05 (baseline) | 250 | 25% | — (honest baseline; "99%" was the wrong metric) |
| 2026-06-05 | 342 | 34% | analyzer fixes (comments + bare refs) |
| 2026-06-05 | 398 | 40% | Employees sub-resource CRUD; Expenses lifecycle + detail + categories/policies + OCR; Reports definitions CRUD + run/export/schedule + dashboards + saved; Onboarding admin (templates/tasks/buddy/probation CRUD); Courses (modules/assessments CRUD); Groups (CRUD + members/events detail) |
| 2026-06-05 | 426 | 43% | Ticket categories CRUD; Leave policies CRUD; Workflow steps + instances (approve/reject/cancel) + delegation rules CRUD; Dashboard widgets CRUD; Notification preferences CRUD; Salary components CRUD; Compensation plans CRUD; Social feed (comments + post delete); Attendance regularizations (request/approve/reject) |
| 2026-06-05 | 443 | 45% | Asset detail (assignment + maintenance CRUD/return/complete); Documents (delete + types + templates CRUD); Goals (CRUD + progress); Continuous Feedback (wall/received/given + give); Compliance Tasks CRUD |
| 2026-06-05 | 453 | 46% | Payroll runs (lock + mark-paid); Travel detail page (itinerary legs + advance + mileage + submit/approve/reject); Skills (gap analysis + rate employee + role requirements) |
| 2026-06-05 | 462 | 47% | Leave balances (adjust + initialize); Investment-proof verification queue (verify/reject); Payslip PDF download; Recruitment Analytics (cost-per-hire, time-to-fill, offer-acceptance, quality-of-hire, req-aging, pipeline) |
| 2026-06-05 | 472 | **48%** | Statutory Tax Config (PF/ESI/PT read+save); Rewards (catalogue + budgets management, add reward/budget, spend) |

Reusable infra built this drive: **`CrudSection`** (embedded sub-resource CRUD inside
detail pages) and the existing **`ResourcePage`** (full-CRUD list pages). New detail
pages: `ExpenseReportDetailPage`, `ReportDetailPage`, `GroupDetailPage`.

Remaining (~583) continues module-by-module: Documents, Notifications, Tickets/Helpdesk,
Recruitment, Compensation, Workflows, Attendance, Payroll, Performance, Social posts,
Leaves, Travel, Skills, Compliance — same pattern (list→ResourcePage CRUD, read-only
detail tabs→CrudSection, add state-transition actions).

## The headline truth

The earlier "924/926 endpoints covered" figure was **measuring the wrong thing** — it
counted whether a service *wrapper* existed (and `catalog.ts` is a 1:1 mirror of the
backend, so it always scored ~100%). It did **not** measure whether a real UI page
reaches the endpoint.

This audit traces the real call graph: **page (`.tsx`) → service object.method → endpoint**.
An endpoint counts as integrated only if some page/component actually references the
method that calls it.

| Metric | Value |
|---|---|
| Backend endpoints (164 controllers) | **981** |
| **Reachable from a UI page** | **342 (34%)** |
| **NOT reachable from any UI page** | **639 (66%)** |
| — of which inherently backend-only (webhooks, SCIM, SSE, inbound, public careers) | 21 |
| — **genuine UI-relevant gap** | **~618** |

Breakdown of the 639 unreached:
- **290 reads (GET)** — data the backend can return but no screen displays.
- **347 writes (POST/PUT/DELETE)** — operations with no button/form in the UI.
- 2 PATCH.

So the product is roughly **one-third wired**. The remaining two-thirds is real backend
capability with no UI surface — matching the "it's only 20-25% done" intuition.

## What "not integrated" looks like concretely

- **Employee profile**: you can *view* addresses, education, family, work-history,
  emergency contacts — but there are **no add/edit/delete buttons**, so all those
  `POST/PUT/DELETE /api/v1/employees/{id}/...` endpoints are dead to the user. Bulk
  import (`/employees/bulk`, `/bulk/import-csv`) and `/directory` also unsurfaced.
- **Expenses (33)**: report submit/approve/reject, expense items, OCR, policies,
  categories — most of the expense lifecycle has no screen.
- **Reports (38)**: definitions CRUD, saved reports, widgets, dashboards, run/export/
  schedule/share — the reporting engine is almost entirely unsurfaced.
- **Onboarding/Offboarding (31/26)**: task templates, buddy assignments, probation
  reviews, pre-board portal, separations checklist/clearance/knowledge-transfer.
- Similar story across **documents (30), recruitment (30), engagement (28),
  tickets (27), notifications (26), courses (24), compensation/salary (23/23),
  workflows (23), groups (22)** …

## Per-module gap (endpoints not reached from UI)

```
 38 reports        33 expenses       31 onboarding     30 documents
 30 recruitment    28 engagement     27 tickets        26 employees
 26 notifications  26 offboarding    24 courses        23 compensation
 23 salary         23 workflows      22 groups         18 compliance
 16 assets         15 leaves         15 posts          15 payroll
 14 attendance     13 auth           11 asset          10 performance
  8 goals  8 roles  8 reviews  8 travel   7 scim  7 file-vault  7 holidays
  7 tax  7 timesheet  6 feedback  6 files  6 interviews  6 integrations
  6 one-on-ones  6 workplace  5 competencies  5 shifts  5 jobs
  5 legal-entities  5 users  4 custom-fields  4 departments  4 designations
  4 locations  4 api-keys  4 applications  4 offers  4 payslips  4 skills
  4 tenants  3 candidates  3 lms  2 corehr  2 audit  2 awards  2 forms
  2 helpdesk  2 org-chart  2 pip-plans  2 workflow  1 vendors  1 cases
```

Full endpoint-level list: **`.audit/not_integrated.txt`**.

## Why the gap is so large

Most existing pages are **list + create** (via `DataList`/`ResourcePage`) or **read-only
detail tabs**. They surface the "happy path" GET + one POST, but skip:
- per-row **edit / delete**,
- **state transitions** (approve / reject / submit / activate / close) on many modules,
- **nested sub-resources** (an entity's addresses, comments, attachments, history),
- entire **admin/config** areas (report definitions, dashboards, widgets, templates,
  policies, categories, custom fields).

## Methodology / caveats

- `.audit/reachability.py` parses every `export const X = {...}` service object (incl.
  the nested `catalog.ts` tree + alias resolution), maps each leaf method to its
  endpoint, then scans `src/pages`, `src/components`, `src/hooks` for any reference to
  those chains (called **or** passed as a bare `queryFn`/`mutationFn` reference).
- Validated against known-wired pages (e.g. `BenefitsPage`'s `Catalog.compensation.
  benefits.{list,create,update,delete}` resolve as reached; `EmployeeDetailPage`'s
  read-only sub-tabs resolve, its absent mutations do not).
- It can still **under-count** slightly (e.g. a method stored in a local variable first),
  so the true integration figure may be a few points above 34% — but the order of
  magnitude (≈⅓ wired) is solid.
