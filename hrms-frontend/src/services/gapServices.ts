/** Services for the final batch of backend controllers without a dedicated page. */
import { api, unwrap } from '@/lib/api'

async function GET<T = unknown>(url: string, params?: Record<string, unknown>): Promise<T> {
  return unwrap(await api.get(url, { params })) as T
}
async function POST<T = unknown>(url: string, body?: Record<string, unknown>): Promise<T> {
  return unwrap(await api.post(url, body)) as T
}

// Compensation plans  → /api/v1/compensation/plans
export const compensationPlanService = {
  list: () => GET('/api/v1/compensation/plans'),
  forEmployee: (employeeId: string) => GET(`/api/v1/compensation/plans/employee/${employeeId}`),
  create: (v: Record<string, unknown>) => POST('/api/v1/compensation/plans', v),
  activate: (id: string) => POST(`/api/v1/compensation/plans/${id}/activate`),
}

// Employee benefits enrolment → /api/v1/compensation/employee-benefits
export const employeeBenefitService = {
  list: () => GET('/api/v1/compensation/employee-benefits'),
  forEmployee: (employeeId: string) => GET(`/api/v1/compensation/employee-benefits/employee/${employeeId}`),
  enroll: (v: Record<string, unknown>) => POST('/api/v1/compensation/employee-benefits', v),
  terminate: (id: string) => POST(`/api/v1/compensation/employee-benefits/${id}/terminate`),
}

// Market benchmark → /api/compensation/benchmark
export const marketBenchmarkService = {
  lookup: (roleCode: string, country = 'IN', level?: string) =>
    GET('/api/compensation/benchmark', { roleCode, country, level }),
  compaRatio: (currentCtc: number, roleCode: string, country = 'IN') =>
    GET('/api/compensation/benchmark/compa-ratio', { currentCtc, roleCode, country }),
  refresh: (v: Record<string, unknown>) => POST('/api/compensation/benchmark/refresh', v),
}

// Expense categories → /api/v1/expenses/categories
export const expenseCategoryService = {
  list: () => GET('/api/v1/expenses/categories'),
  create: (v: Record<string, unknown>) => POST('/api/v1/expenses/categories', v),
}

// Leave policies → /api/v1/leaves/policies
export const leavePolicyService = {
  list: () => GET('/api/v1/leaves/policies'),
  create: (v: Record<string, unknown>) => POST('/api/v1/leaves/policies', v),
}

// GST returns → /api/v1/compliance/gst/*
export const gstService = {
  gstr1: (v: Record<string, unknown>) => POST('/api/v1/compliance/gst/gstr1', v),
  gstr3b: (v: Record<string, unknown>) => POST('/api/v1/compliance/gst/gstr3b', v),
}

// Dashboard widgets → /api/v1/reports/widgets
export const dashboardWidgetService = {
  list: (dashboardId?: string) => GET('/api/v1/reports/widgets', dashboardId ? { dashboardId } : undefined),
  create: (v: Record<string, unknown>) => POST('/api/v1/reports/widgets', v),
}

// Workflow steps → /api/v1/workflows/steps
export const workflowStepService = {
  list: (definitionId?: string) => GET('/api/v1/workflows/steps', definitionId ? { definitionId } : undefined),
  create: (v: Record<string, unknown>) => POST('/api/v1/workflows/steps', v),
}

// Reference checks → /api/v1/recruitment/references
export const referenceCheckService = {
  list: () => GET('/api/v1/recruitment/references'),
  invite: (v: Record<string, unknown>) => POST('/api/v1/recruitment/references', v),
}

// Psychometric → /api/recruitment/psychometric
export const psychometricService = {
  forCandidate: (candidateId: string) => GET(`/api/recruitment/psychometric/candidate/${candidateId}`),
  invite: (v: Record<string, unknown>) => POST('/api/recruitment/psychometric/invite', v),
}

// Internal mobility → /api/recruitment/internal + alumni
export const internalMobilityService = {
  openings: () => GET('/api/recruitment/internal/openings'),
  myApplications: (employeeId: string) => GET('/api/recruitment/internal/my-applications', { employeeId }),
  apply: (v: Record<string, unknown>) => POST('/api/recruitment/internal/apply', v),
  alumni: () => GET('/api/recruitment/alumni'),
  boomerang: () => GET('/api/recruitment/alumni/boomerang'),
}

// Onboarding documents → /api/v1/onboarding/documents
export const onboardingDocumentService = {
  list: () => GET('/api/v1/onboarding/documents'),
  forEmployee: (employeeId: string) => GET(`/api/v1/onboarding/documents/employee/${employeeId}`),
}

// Document versions → /api/v1/documents/{id}/versions
export const documentVersionService = {
  list: (documentId: string) => GET(`/api/v1/documents/${documentId}/versions`),
}

// CSAT → /api/v1/helpdesk/tickets/{id}/csat
export const csatService = {
  summary: () => GET('/api/v1/helpdesk/tickets/csat/summary'),
}

// Employee bulk import → /api/v1/employees/bulk/import-csv
export const employeeBulkImportService = {
  importCsv: async (file: File): Promise<Record<string, unknown>> => {
    const fd = new FormData(); fd.append('file', file)
    return unwrap(await api.post('/api/v1/employees/bulk/import-csv', fd, { headers: { 'Content-Type': 'multipart/form-data' } })) as Record<string, unknown>
  },
}

// SCORM runtime → /api/v1/lms/scorm/runtime
export const scormService = {
  initialize: (enrollmentId: string) => POST('/api/v1/lms/scorm/runtime/initialize', { enrollmentId }),
  commit: (sessionId: string, cmi: Record<string, unknown>) => POST('/api/v1/lms/scorm/runtime/commit', { sessionId, cmi }),
  finish: (sessionId: string) => POST('/api/v1/lms/scorm/runtime/finish', { sessionId }),
}

// Files → /api/v1/files
export const filesService = {
  list: (folder?: string) => GET('/api/v1/files', folder ? { folder } : undefined),
}

// Notification dispatch (admin manual send) → covered by NotificationDispatchController
export const notificationDispatchService = {
  send: (v: Record<string, unknown>) => POST('/api/v1/notifications/dispatch', v),
  history: () => GET('/api/v1/notifications/dispatch/history'),
}
