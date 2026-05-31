/**
 * Thin service stubs for remaining backend modules. All functions are async and
 * return unwrapped data so they plug cleanly into useQuery / useMutation.
 */
import { api, unwrap } from '@/lib/api'

async function GET<T = unknown>(url: string, params?: Record<string, unknown>): Promise<T> {
  return unwrap(await api.get(url, { params })) as T
}
async function POST<T = unknown>(url: string, body?: Record<string, unknown>): Promise<T> {
  return unwrap(await api.post(url, body)) as T
}
async function PUT<T = unknown>(url: string, body?: Record<string, unknown>): Promise<T> {
  return unwrap(await api.put(url, body)) as T
}
async function DEL(url: string): Promise<void> { await api.delete(url) }

// ── Admin / RBAC ─────────────────────────────────────────────────────────
export const userService = {
  list: () => GET('/api/v1/users'),
  create: (v: Record<string, unknown>) => POST('/api/v1/users', v),
  setRoles: (id: string, roles: string[]) => POST(`/api/v1/users/${id}/roles`, { roles }),
  deactivate: (id: string) => POST(`/api/v1/users/${id}/deactivate`),
}
export const roleService = {
  list: () => GET('/api/v1/roles'),
  create: (v: Record<string, unknown>) => POST('/api/v1/roles', v),
  setPermissions: (id: string, permissions: string[]) => POST(`/api/v1/roles/${id}/permissions`, { permissions }),
}
export const apiKeyService = {
  list: () => GET('/api/v1/api-keys'),
  create: (v: Record<string, unknown>) => POST('/api/v1/api-keys', v),
  revoke: (id: string) => DEL(`/api/v1/api-keys/${id}`),
}
export const legalEntityService = {
  list: () => GET('/api/v1/legal-entities'),
  create: (v: Record<string, unknown>) => POST('/api/v1/legal-entities', v),
}
export const customFieldService = {
  list: (entity: string) => () => GET('/api/v1/custom-fields', { entity }),
  create: (v: Record<string, unknown>) => POST('/api/v1/custom-fields', v),
}
export const biometricService = {
  devices: () => GET('/api/v1/attendance/biometric/devices'),
  registerDevice: (v: Record<string, unknown>) => POST('/api/v1/attendance/biometric/devices', v),
  syncDevice: (id: string) => POST(`/api/v1/attendance/biometric/devices/${id}/sync`),
}

// ── Lifecycle ────────────────────────────────────────────────────────────
export const probationReviewService = {
  list: () => GET('/api/v1/onboarding/probation-reviews'),
  decide: (id: string, decision: 'CONFIRM' | 'EXTEND' | 'TERMINATE', notes?: string) =>
    POST(`/api/v1/onboarding/probation-reviews/${id}/decide`, { decision, notes }),
}
export const buddyAssignmentService = {
  list: () => GET('/api/v1/onboarding/buddy-assignments'),
  assign: (v: Record<string, unknown>) => POST('/api/v1/onboarding/buddy-assignments', v),
}
export const preOnboardingService = {
  list: () => GET('/api/v1/onboarding/preboard'),
  invite: (v: Record<string, unknown>) => POST('/api/v1/onboarding/preboard', v),
}
export const onboardingTemplateService = {
  list: () => GET('/api/v1/onboarding/templates'),
  create: (v: Record<string, unknown>) => POST('/api/v1/onboarding/templates', v),
}
export const onboardingTaskAdminService = {
  list: () => GET('/api/v1/onboarding/tasks'),
}
export const exitInterviewService = {
  list: () => GET('/api/v1/offboarding/exit-interviews'),
  schedule: (v: Record<string, unknown>) => POST('/api/v1/offboarding/exit-interviews', v),
}
export const exitChecklistService = {
  list: () => GET('/api/v1/offboarding/checklists'),
  create: (v: Record<string, unknown>) => POST('/api/v1/offboarding/checklists', v),
}
export const knowledgeTransferService = {
  list: () => GET('/api/v1/offboarding/knowledge-transfers'),
  create: (v: Record<string, unknown>) => POST('/api/v1/offboarding/knowledge-transfers', v),
}

// ── Compliance ──────────────────────────────────────────────────────────
export const statutoryReturnService = {
  list: () => GET('/api/v1/compliance/statutory-returns'),
  generate: (v: Record<string, unknown>) => POST('/api/v1/compliance/statutory-returns', v),
  submit: (id: string) => POST(`/api/v1/compliance/statutory-returns/${id}/submit`),
}
export const complianceTaskService = {
  list: () => GET('/api/v1/compliance/tasks'),
  complete: (id: string) => POST(`/api/v1/compliance/tasks/${id}/complete`),
}

// ── LMS depth ───────────────────────────────────────────────────────────
export const courseModuleService = {
  list: (courseId?: string) => () => GET('/api/v1/courses/modules', courseId ? { courseId } : undefined),
  create: (v: Record<string, unknown>) => POST('/api/v1/courses/modules', v),
}
export const assessmentAdminService = {
  list: (courseId?: string) => () => GET('/api/v1/courses/assessments', courseId ? { courseId } : undefined),
  create: (v: Record<string, unknown>) => POST('/api/v1/courses/assessments', v),
}

// ── Expense depth ───────────────────────────────────────────────────────
export const receiptOcrService = {
  parse: async (file: File): Promise<Record<string, unknown>> => {
    const fd = new FormData(); fd.append('file', file)
    return unwrap(await api.post('/api/v1/expenses/ocr/parse', fd, { headers: { 'Content-Type': 'multipart/form-data' } })) as Record<string, unknown>
  },
}
export const expensePolicyService = {
  list: () => GET('/api/v1/expenses/policies'),
  create: (v: Record<string, unknown>) => POST('/api/v1/expenses/policies', v),
}

// ── Performance depth ───────────────────────────────────────────────────
export const performanceAnalyticsService = {
  distribution: () => GET('/api/v1/performance/analytics/distribution'),
  ratingSpread: (cycleId: string) => () => GET('/api/v1/performance/analytics/rating-spread', { cycleId }),
  calibrationMatrix: (cycleId: string) => () => GET('/api/v1/performance/analytics/calibration', { cycleId }),
}
export const continuousFeedbackService = {
  feed: () => GET('/api/v1/feedback'),
  give: (v: Record<string, unknown>) => POST('/api/v1/feedback', v),
}

// ── Recruitment depth ──────────────────────────────────────────────────
export const recruitmentAgencyService = {
  list: () => GET('/api/v1/recruitment/agencies'),
  create: (v: Record<string, unknown>) => POST('/api/v1/recruitment/agencies', v),
}
export const recruitmentAnalyticsService = {
  funnel: () => GET('/api/v1/recruitment/analytics/funnel'),
  sourceEffectiveness: () => GET('/api/v1/recruitment/analytics/source-effectiveness'),
  timeToHire: () => GET('/api/v1/recruitment/analytics/time-to-hire'),
}
export const bgvService = {
  list: () => GET('/api/v1/recruitment/bgv'),
  initiate: (v: Record<string, unknown>) => POST('/api/v1/recruitment/bgv', v),
  status: (id: string) => GET(`/api/v1/recruitment/bgv/${id}`),
}

// ── Social depth ───────────────────────────────────────────────────────
export const groupService = {
  list: () => GET('/api/v1/groups'),
  create: (v: Record<string, unknown>) => POST('/api/v1/groups', v),
  join: (id: string) => POST(`/api/v1/groups/${id}/join`),
  members: (id: string) => GET('/api/v1/groups/members', { groupId: id }),
}
export const eventService = {
  list: () => GET('/api/v1/groups/events'),
  create: (v: Record<string, unknown>) => POST('/api/v1/groups/events', v),
  rsvp: (id: string, attending: boolean) => POST(`/api/v1/groups/events/${id}/rsvp`, { attending }),
}

// ── Reports depth ──────────────────────────────────────────────────────
export const dashboardAdminService = {
  list: () => GET('/api/v1/reports/dashboards'),
  create: (v: Record<string, unknown>) => POST('/api/v1/reports/dashboards', v),
  get: (id: string) => GET(`/api/v1/reports/dashboards/${id}`),
}
export const savedReportService = {
  list: () => GET('/api/v1/reports/saved'),
  save: (v: Record<string, unknown>) => POST('/api/v1/reports/saved', v),
}
export const widgetService = {
  list: (dashboardId: string) => () => GET('/api/v1/reports/widgets', { dashboardId }),
  create: (v: Record<string, unknown>) => POST('/api/v1/reports/widgets', v),
}

// ── Notifications depth ────────────────────────────────────────────────
export const announcementAdminService = {
  list: () => GET('/api/v1/notifications/announcements'),
  publish: (v: Record<string, unknown>) => POST('/api/v1/notifications/announcements', v),
}
export const emailTemplateService = {
  list: () => GET('/api/v1/notifications/email-templates'),
  save: (v: Record<string, unknown>) => POST('/api/v1/notifications/email-templates', v),
}
export const notificationPreferenceService = {
  mine: () => GET('/api/v1/notifications/preferences'),
  update: (v: Record<string, boolean>) => PUT('/api/v1/notifications/preferences', v),
}

// ── Payroll depth ──────────────────────────────────────────────────────
export const salaryComponentService = {
  list: () => GET('/api/v1/salary/components'),
  create: (v: Record<string, unknown>) => POST('/api/v1/salary/components', v),
}
export const employeeSalaryService = {
  list: () => GET('/api/v1/salary/employee'),
  assign: (v: Record<string, unknown>) => POST('/api/v1/salary/employee', v),
}
export const taxConfigService = {
  list: () => GET('/api/v1/salary/tax-config'),
  upsert: (v: Record<string, unknown>) => POST('/api/v1/salary/tax-config', v),
}

// ── Helpdesk depth ─────────────────────────────────────────────────────
export const ticketCategoryService = {
  list: () => GET('/api/v1/tickets/categories'),
  create: (v: Record<string, unknown>) => POST('/api/v1/tickets/categories', v),
}
export const csatService = {
  submit: (ticketId: string, rating: number, feedback?: string) =>
    POST(`/api/v1/helpdesk/tickets/${ticketId}/csat`, { rating, feedback }),
  summary: () => GET('/api/v1/helpdesk/tickets/csat/summary'),
}

// ── Documents depth ────────────────────────────────────────────────────
export const documentTemplateAdminService = {
  list: () => GET('/api/v1/documents/templates'),
  create: (v: Record<string, unknown>) => POST('/api/v1/documents/templates', v),
}
export const documentTypeService = {
  list: () => GET('/api/v1/documents/types'),
  create: (v: Record<string, unknown>) => POST('/api/v1/documents/types', v),
}
export const fileVaultService = {
  list: () => GET('/api/v1/file-vault'),
}
export const documentVersionService = {
  list: (documentId: string) => () => GET(`/api/v1/documents/${documentId}/versions`),
}

// ── Workflow depth ────────────────────────────────────────────────────
export const workflowInstanceAdminService = {
  list: () => GET('/api/v1/workflows/instances'),
  get: (id: string) => GET(`/api/v1/workflows/instances/${id}`),
}
export const delegationRuleService = {
  list: () => GET('/api/v1/workflows/delegations'),
  create: (v: Record<string, unknown>) => POST('/api/v1/workflows/delegations', v),
}

// ── Integrations depth ────────────────────────────────────────────────
export const integrationsService = {
  webhooks: () => GET('/api/v1/integrations/webhooks'),
  registerWebhook: (v: Record<string, unknown>) => POST('/api/v1/integrations/webhooks', v),
  rotateSecret: (id: string) => POST(`/api/v1/integrations/webhooks/${id}/rotate-secret`),
  slackInboundEvents: () => GET('/api/v1/integrations/slack/inbound/events'),
  teamsInboundEvents: () => GET('/api/v1/integrations/teams/inbound/events'),
}

// ── Job-cost reports ──────────────────────────────────────────────────
export const jobCostService = {
  list: () => GET('/api/v1/timesheet/reports/job-cost'),
}
