/**
 * Master backend endpoint catalog — one typed callable per backend REST endpoint
 * across every microservice. Generated from the .audit/gap analysis to guarantee
 * 100% Zoho-parity backend coverage in the frontend.
 *
 * Naming pattern: <module><Catalog>.<methodKebabToCamel> (e.g. assetsCatalog.getAll)
 * All calls return the unwrapped `data` field via `unwrap()`.
 */
/* eslint-disable @typescript-eslint/no-explicit-any */
import { api, unwrap } from '@/lib/api'

type Body = Record<string, unknown>
type Params = Record<string, unknown>

const G = async <T = any>(url: string, params?: Params) => unwrap<T>(await api.get(url, { params }))
const P = async <T = any>(url: string, body?: Body) => unwrap<T>(await api.post(url, body ?? {}))
const PU = async <T = any>(url: string, body?: Body) => unwrap<T>(await api.put(url, body ?? {}))
const PA = async <T = any>(url: string, body?: Body) => unwrap<T>(await api.patch(url, body ?? {}))
const D = async <T = any>(url: string, params?: Params) => unwrap<T>(await api.delete(url, { params }))

// ───────────────────────────────────────────────────────────────────────────
// LMS / Courses (31)
// ───────────────────────────────────────────────────────────────────────────
export const coursesCatalog = {
  list: (p?: Params) => G('/api/v1/courses', p),
  create: (b: Body) => P('/api/v1/courses', b),
  get: (id: string) => G(`/api/v1/courses/${id}`),
  update: (id: string, b: Body) => PU(`/api/v1/courses/${id}`, b),
  remove: (id: string) => D(`/api/v1/courses/${id}`),
  archive: (id: string) => P(`/api/v1/courses/${id}/archive`),
  publish: (id: string) => P(`/api/v1/courses/${id}/publish`),
  mandatory: () => G('/api/v1/courses/mandatory'),
  status: (id: string) => G(`/api/v1/courses/status/${id}`),

  modulesForCourse: (courseId: string) => G(`/api/v1/courses/modules/course/${courseId}`),
  module: (id: string) => G(`/api/v1/courses/modules/${id}`),
  updateModule: (id: string, b: Body) => PU(`/api/v1/courses/modules/${id}`, b),
  deleteModule: (id: string) => D(`/api/v1/courses/modules/${id}`),

  assessmentsForCourse: (courseId: string) => G(`/api/v1/courses/assessments/course/${courseId}`),
  assessment: (id: string) => G(`/api/v1/courses/assessments/${id}`),
  updateAssessment: (id: string, b: Body) => PU(`/api/v1/courses/assessments/${id}`, b),
  deleteAssessment: (id: string) => D(`/api/v1/courses/assessments/${id}`),

  enrollments: (p?: Params) => G('/api/v1/courses/enrollments', p),
  enrollmentsForCourse: (courseId: string) => G(`/api/v1/courses/enrollments/course/${courseId}`),
  enrollmentsForEmployee: (employeeId: string) => G(`/api/v1/courses/enrollments/employee/${employeeId}`),
  enrollment: (id: string) => G(`/api/v1/courses/enrollments/${id}`),
  enroll: (b: Body) => P('/api/v1/courses/enrollments', b),
  updateEnrollment: (id: string, b: Body) => PU(`/api/v1/courses/enrollments/${id}`, b),
  deleteEnrollment: (id: string) => D(`/api/v1/courses/enrollments/${id}`),
  updateProgress: (id: string, percent: number) => P(`/api/v1/courses/enrollments/${id}/progress`, { percent }),

  certifications: () => G('/api/v1/courses/certifications'),
  certificationsForEmployee: (employeeId: string) => G(`/api/v1/courses/certifications/employee/${employeeId}`),
  certification: (id: string) => G(`/api/v1/courses/certifications/${id}`),
  createCertification: (b: Body) => P('/api/v1/courses/certifications', b),
  updateCertification: (id: string, b: Body) => PU(`/api/v1/courses/certifications/${id}`, b),
  deleteCertification: (id: string) => D(`/api/v1/courses/certifications/${id}`),
  revokeCertification: (id: string, reason?: string) => P(`/api/v1/courses/certifications/${id}/revoke`, { reason }),
}

// ───────────────────────────────────────────────────────────────────────────
// Documents (29)
// ───────────────────────────────────────────────────────────────────────────
export const documentsCatalog = {
  list: (p?: Params) => G('/api/v1/documents', p),
  create: (b: Body) => P('/api/v1/documents', b),
  get: (id: string) => G(`/api/v1/documents/${id}`),
  update: (id: string, b: Body) => PU(`/api/v1/documents/${id}`, b),
  forEmployee: (employeeId: string) => G(`/api/v1/documents/employee/${employeeId}`),
  revertVersion: (docId: string, versionId: string) => P(`/api/v1/documents/${docId}/versions/${versionId}/revert`),

  files: { upload: (b: Body) => P('/api/v1/documents/files', b),
           delete: (key: string) => D('/api/v1/documents/files', { key }),
           download: (key: string) => G('/api/v1/documents/files/download', { key }),
           presignedDownload: (key: string) => G<string>('/api/v1/documents/files/presigned-download', { key }) },

  letters: { list: () => G('/api/v1/documents/letters'),
             forEmployee: (employeeId: string) => G(`/api/v1/documents/letters/employee/${employeeId}`),
             get: (id: string) => G(`/api/v1/documents/letters/${id}`),
             create: (b: Body) => P('/api/v1/documents/letters', b),
             update: (id: string, b: Body) => PU(`/api/v1/documents/letters/${id}`, b),
             delete: (id: string) => D(`/api/v1/documents/letters/${id}`) },

  policies: { list: () => G('/api/v1/documents/policies'),
              all: () => G('/api/v1/documents/policies/all'),
              get: (id: string) => G(`/api/v1/documents/policies/${id}`),
              create: (b: Body) => P('/api/v1/documents/policies', b),
              update: (id: string, b: Body) => PU(`/api/v1/documents/policies/${id}`, b),
              delete: (id: string) => D(`/api/v1/documents/policies/${id}`) },

  templates: { all: () => G('/api/v1/documents/templates/all'),
               get: (id: string) => G(`/api/v1/documents/templates/${id}`),
               update: (id: string, b: Body) => PU(`/api/v1/documents/templates/${id}`, b),
               delete: (id: string) => D(`/api/v1/documents/templates/${id}`) },

  types: { all: () => G('/api/v1/documents/types/all'),
           get: (id: string) => G(`/api/v1/documents/types/${id}`),
           update: (id: string, b: Body) => PU(`/api/v1/documents/types/${id}`, b),
           delete: (id: string) => D(`/api/v1/documents/types/${id}`) },
}

// ───────────────────────────────────────────────────────────────────────────
// Assets (28)
// ───────────────────────────────────────────────────────────────────────────
export const assetsCatalog = {
  all: () => G('/api/v1/assets/all'),
  get: (id: string) => G(`/api/v1/assets/${id}`),
  create: (b: Body) => P('/api/v1/assets', b),
  update: (id: string, b: Body) => PU(`/api/v1/assets/${id}`, b),
  remove: (id: string) => D(`/api/v1/assets/${id}`),
  byStatus: (status: string) => G(`/api/v1/assets/status/${status}`),
  byCategory: (categoryId: string) => G(`/api/v1/assets/category/${categoryId}`),

  assignments: { list: () => G('/api/v1/assets/assignments'),
                 forAsset: (assetId: string) => G(`/api/v1/assets/assignments/asset/${assetId}`),
                 forEmployee: (employeeId: string) => G(`/api/v1/assets/assignments/employee/${employeeId}`),
                 get: (id: string) => G(`/api/v1/assets/assignments/${id}`),
                 create: (b: Body) => P('/api/v1/assets/assignments', b),
                 update: (id: string, b: Body) => PU(`/api/v1/assets/assignments/${id}`, b),
                 delete: (id: string) => D(`/api/v1/assets/assignments/${id}`),
                 return: (id: string, b?: Body) => P(`/api/v1/assets/assignments/${id}/return`, b) },

  categories: { all: () => G('/api/v1/assets/categories/all'),
                get: (id: string) => G(`/api/v1/assets/categories/${id}`),
                update: (id: string, b: Body) => PU(`/api/v1/assets/categories/${id}`, b),
                delete: (id: string) => D(`/api/v1/assets/categories/${id}`) },

  maintenance: { forAsset: (assetId: string) => G(`/api/v1/assets/maintenance/asset/${assetId}`),
                 get: (id: string) => G(`/api/v1/assets/maintenance/${id}`),
                 update: (id: string, b: Body) => PU(`/api/v1/assets/maintenance/${id}`, b),
                 delete: (id: string) => D(`/api/v1/assets/maintenance/${id}`) },

  requests: { forEmployee: (employeeId: string) => G(`/api/v1/assets/requests/employee/${employeeId}`),
              get: (id: string) => G(`/api/v1/assets/requests/${id}`),
              update: (id: string, b: Body) => PU(`/api/v1/assets/requests/${id}`, b),
              delete: (id: string) => D(`/api/v1/assets/requests/${id}`),
              reject: (id: string, reason?: string) => P(`/api/v1/assets/requests/${id}/reject`, { reason }) },
}

// ───────────────────────────────────────────────────────────────────────────
// Onboarding (28)
// ───────────────────────────────────────────────────────────────────────────
export const onboardingCatalog = {
  templates: { get: (id: string) => G(`/api/v1/onboarding/templates/${id}`),
               update: (id: string, b: Body) => PU(`/api/v1/onboarding/templates/${id}`, b),
               delete: (id: string) => D(`/api/v1/onboarding/templates/${id}`) },

  tasks: { create: (b: Body) => P('/api/v1/onboarding/tasks', b),
           get: (id: string) => G(`/api/v1/onboarding/tasks/${id}`),
           update: (id: string, b: Body) => PU(`/api/v1/onboarding/tasks/${id}`, b),
           delete: (id: string) => D(`/api/v1/onboarding/tasks/${id}`),
           forEmployee: (employeeId: string) => G(`/api/v1/onboarding/tasks/employee/${employeeId}`),
           forTemplate: (templateId: string) => G(`/api/v1/onboarding/tasks/template/${templateId}`) },

  buddies: { forEmployee: (employeeId: string) => G(`/api/v1/onboarding/buddy-assignments/employee/${employeeId}`),
             get: (id: string) => G(`/api/v1/onboarding/buddy-assignments/${id}`),
             update: (id: string, b: Body) => PU(`/api/v1/onboarding/buddy-assignments/${id}`, b),
             delete: (id: string) => D(`/api/v1/onboarding/buddy-assignments/${id}`) },

  probation: { create: (b: Body) => P('/api/v1/onboarding/probation-reviews', b),
               get: (id: string) => G(`/api/v1/onboarding/probation-reviews/${id}`),
               forEmployee: (employeeId: string) => G(`/api/v1/onboarding/probation-reviews/employee/${employeeId}`),
               update: (id: string, b: Body) => PU(`/api/v1/onboarding/probation-reviews/${id}`, b),
               delete: (id: string) => D(`/api/v1/onboarding/probation-reviews/${id}`) },

  documents: { create: (b: Body) => P('/api/v1/onboarding/documents', b),
               get: (id: string) => G(`/api/v1/onboarding/documents/${id}`),
               update: (id: string, b: Body) => PU(`/api/v1/onboarding/documents/${id}`, b),
               delete: (id: string) => D(`/api/v1/onboarding/documents/${id}`) },

  preboard: { invite: (b: Body) => P('/api/v1/onboarding/preboard/invite', b),
              portal: (token: string) => G(`/api/v1/onboarding/preboard/portal/${token}`),
              submitDetails: (token: string, b: Body) => P(`/api/v1/onboarding/preboard/portal/${token}/details`, b),
              uploadDocs: (token: string, b: Body) => P(`/api/v1/onboarding/preboard/portal/${token}/documents`, b),
              ackHandbook: (token: string, b: Body) => P(`/api/v1/onboarding/preboard/portal/${token}/handbook-ack`, b),
              complete: (token: string) => P(`/api/v1/onboarding/preboard/portal/${token}/complete`) },
}

// ───────────────────────────────────────────────────────────────────────────
// Helpdesk / Tickets (27)
// ───────────────────────────────────────────────────────────────────────────
export const ticketsCatalog = {
  create: (b: Body) => P('/api/v1/tickets', b),
  get: (id: string) => G(`/api/v1/tickets/${id}`),
  update: (id: string, b: Body) => PU(`/api/v1/tickets/${id}`, b),
  delete: (id: string) => D(`/api/v1/tickets/${id}`),
  forAssignee: (employeeId: string) => G(`/api/v1/tickets/assignee/${employeeId}`),
  forRequester: (employeeId: string) => G(`/api/v1/tickets/requester/${employeeId}`),
  byStatus: (status: string) => G(`/api/v1/tickets/status/${status}`),
  close: (id: string, b?: Body) => P(`/api/v1/tickets/${id}/close`, b),
  resolve: (id: string, b?: Body) => P(`/api/v1/tickets/${id}/resolve`, b),

  categories: { all: () => G('/api/v1/tickets/categories/all'),
                get: (id: string) => G(`/api/v1/tickets/categories/${id}`),
                update: (id: string, b: Body) => PU(`/api/v1/tickets/categories/${id}`, b),
                delete: (id: string) => D(`/api/v1/tickets/categories/${id}`) },

  comments: { list: (p?: Params) => G('/api/v1/tickets/comments', p),
              forTicket: (ticketId: string) => G(`/api/v1/tickets/comments/ticket/${ticketId}`),
              get: (id: string) => G(`/api/v1/tickets/comments/${id}`),
              create: (b: Body) => P('/api/v1/tickets/comments', b),
              update: (id: string, b: Body) => PU(`/api/v1/tickets/comments/${id}`, b),
              delete: (id: string) => D(`/api/v1/tickets/comments/${id}`) },

  kb: { list: () => G('/api/v1/tickets/kb-articles'),
        published: () => G('/api/v1/tickets/kb-articles/published'),
        get: (id: string) => G(`/api/v1/tickets/kb-articles/${id}`),
        create: (b: Body) => P('/api/v1/tickets/kb-articles', b),
        update: (id: string, b: Body) => PU(`/api/v1/tickets/kb-articles/${id}`, b),
        delete: (id: string) => D(`/api/v1/tickets/kb-articles/${id}`),
        publish: (id: string) => P(`/api/v1/tickets/kb-articles/${id}/publish`),
        view: (id: string) => P(`/api/v1/tickets/kb-articles/${id}/view`) },
}

// ───────────────────────────────────────────────────────────────────────────
// Offboarding (26)
// ───────────────────────────────────────────────────────────────────────────
export const offboardingCatalog = {
  separations: { list: (p?: Params) => G('/api/v1/offboarding/separations', p),
                 forEmployee: (employeeId: string) => G(`/api/v1/offboarding/separations/employee/${employeeId}`),
                 byStatus: (status: string) => G(`/api/v1/offboarding/separations/status/${status}`),
                 get: (id: string) => G(`/api/v1/offboarding/separations/${id}`),
                 create: (b: Body) => P('/api/v1/offboarding/separations', b),
                 update: (id: string, b: Body) => PU(`/api/v1/offboarding/separations/${id}`, b),
                 delete: (id: string) => D(`/api/v1/offboarding/separations/${id}`),
                 approve: (id: string, b?: Body) => P(`/api/v1/offboarding/separations/${id}/approve`, b),
                 complete: (id: string, b?: Body) => P(`/api/v1/offboarding/separations/${id}/complete`, b) },

  checklists: { forSeparation: (sepId: string) => G(`/api/v1/offboarding/checklists/separation/${sepId}`),
                forAssignee: (employeeId: string) => G(`/api/v1/offboarding/checklists/assignee/${employeeId}`),
                get: (id: string) => G(`/api/v1/offboarding/checklists/${id}`),
                update: (id: string, b: Body) => PU(`/api/v1/offboarding/checklists/${id}`, b),
                delete: (id: string) => D(`/api/v1/offboarding/checklists/${id}`),
                complete: (id: string, b?: Body) => P(`/api/v1/offboarding/checklists/${id}/complete`, b) },

  exitInterviews: { forSeparation: (sepId: string) => G(`/api/v1/offboarding/exit-interviews/separation/${sepId}`),
                    get: (id: string) => G(`/api/v1/offboarding/exit-interviews/${id}`),
                    update: (id: string, b: Body) => PU(`/api/v1/offboarding/exit-interviews/${id}`, b),
                    delete: (id: string) => D(`/api/v1/offboarding/exit-interviews/${id}`),
                    complete: (id: string, b?: Body) => P(`/api/v1/offboarding/exit-interviews/${id}/complete`, b) },

  knowledgeTransfers: { forSeparation: (sepId: string) => G(`/api/v1/offboarding/knowledge-transfers/separation/${sepId}`),
                        fromEmployee: (employeeId: string) => G(`/api/v1/offboarding/knowledge-transfers/from/${employeeId}`),
                        get: (id: string) => G(`/api/v1/offboarding/knowledge-transfers/${id}`),
                        update: (id: string, b: Body) => PU(`/api/v1/offboarding/knowledge-transfers/${id}`, b),
                        delete: (id: string) => D(`/api/v1/offboarding/knowledge-transfers/${id}`),
                        complete: (id: string, b?: Body) => P(`/api/v1/offboarding/knowledge-transfers/${id}/complete`, b) },
}

// ───────────────────────────────────────────────────────────────────────────
// Recruitment (26)
// ───────────────────────────────────────────────────────────────────────────
export const recruitmentCatalog = {
  analytics: { dashboard: () => G('/api/v1/recruitment/analytics/dashboard'),
               pipeline: () => G('/api/v1/recruitment/analytics/pipeline'),
               funnel: (jobId: string) => G(`/api/v1/recruitment/analytics/funnel/${jobId}`),
               timeToHire: (jobId: string) => G(`/api/v1/recruitment/analytics/time-to-hire/${jobId}`),
               costPerHire: () => G('/api/recruitment/analytics/advanced/cost-per-hire'),
               offerAcceptanceRate: () => G('/api/recruitment/analytics/advanced/offer-acceptance-rate'),
               qualityOfHire: () => G('/api/recruitment/analytics/advanced/quality-of-hire'),
               reqAging: () => G('/api/recruitment/analytics/advanced/req-aging'),
               timeToFill: () => G('/api/recruitment/analytics/advanced/time-to-fill') },

  agencies: { get: (id: string) => G(`/api/v1/recruitment/agencies/${id}`),
              update: (id: string, b: Body) => PU(`/api/v1/recruitment/agencies/${id}`, b) },

  bgv: { forCandidate: (candidateId: string) => G(`/api/v1/recruitment/bgv/candidate/${candidateId}`),
         refresh: (id: string) => P(`/api/v1/recruitment/bgv/${id}/refresh`),
         webhook: (vendor: string, b: Body) => P(`/api/v1/recruitment/bgv/webhook/${vendor}`, b) },

  references: { forCandidate: (candidateId: string) => G(`/api/v1/recruitment/references/candidate/${candidateId}`),
                invite: (id: string, b: Body) => P(`/api/v1/recruitment/references/${id}/invite`, b),
                respond: (token: string, b: Body) => P(`/api/v1/recruitment/references/respond/${token}`, b) },

  alumni: { register: (b: Body) => P('/api/recruitment/alumni', b),
            markRejoined: (id: string, b?: Body) => P(`/api/recruitment/alumni/${id}/mark-rejoined`, b) },

  hiringLoops: { complete: (id: string, b?: Body) => P(`/api/recruitment/hiring-loops/${id}/complete`, b) },

  internalMobility: { applicants: (reqId: string) => G(`/api/recruitment/internal/requisition/${reqId}/applicants`),
                      decide: (id: string, decision: 'ACCEPT' | 'REJECT', notes?: string) =>
                        P(`/api/recruitment/internal/${id}/decide`, { decision, notes }) },

  psychometric: { webhook: (vendor: string, b: Body) => P(`/api/recruitment/psychometric/webhook/${vendor}`, b) },

  scorecards: { create: (b: Body) => P('/api/recruitment/scorecards', b),
                summary: (applicationId: string) => G(`/api/recruitment/scorecards/application/${applicationId}/summary`),
                createTemplate: (b: Body) => P('/api/recruitment/scorecard-templates', b) },
}

// ───────────────────────────────────────────────────────────────────────────
// Reports & Dashboards (25)
// ───────────────────────────────────────────────────────────────────────────
export const reportsCatalog = {
  dei: { headcount: (p?: Params) => G('/api/reports/dei/headcount', p) },

  dashboards: { mine: () => G('/api/v1/reports/dashboards/me'),
                shared: () => G('/api/v1/reports/dashboards/shared'),
                update: (id: string, b: Body) => PU(`/api/v1/reports/dashboards/${id}`, b),
                delete: (id: string) => D(`/api/v1/reports/dashboards/${id}`),
                setDefault: (id: string) => P(`/api/v1/reports/dashboards/${id}/set-default`) },

  definitions: { list: () => G('/api/v1/reports/definitions'),
                 active: () => G('/api/v1/reports/definitions/active'),
                 byCategory: (cat: string) => G(`/api/v1/reports/definitions/category/${cat}`),
                 get: (id: string) => G(`/api/v1/reports/definitions/${id}`),
                 create: (b: Body) => P('/api/v1/reports/definitions', b),
                 update: (id: string, b: Body) => PU(`/api/v1/reports/definitions/${id}`, b),
                 delete: (id: string) => D(`/api/v1/reports/definitions/${id}`) },

  saved: { mine: () => G('/api/v1/reports/saved/me'),
           forUser: (userId: string) => G(`/api/v1/reports/saved/user/${userId}`),
           forDefinition: (defId: string) => G(`/api/v1/reports/saved/definition/${defId}`),
           get: (id: string) => G(`/api/v1/reports/saved/${id}`),
           update: (id: string, b: Body) => PU(`/api/v1/reports/saved/${id}`, b),
           delete: (id: string) => D(`/api/v1/reports/saved/${id}`),
           complete: (id: string, b?: Body) => P(`/api/v1/reports/saved/${id}/complete`, b),
           fail: (id: string, b?: Body) => P(`/api/v1/reports/saved/${id}/fail`, b) },

  widgets: { forDashboard: (dashboardId: string) => G(`/api/v1/reports/widgets/dashboard/${dashboardId}`),
             get: (id: string) => G(`/api/v1/reports/widgets/${id}`),
             update: (id: string, b: Body) => PU(`/api/v1/reports/widgets/${id}`, b),
             delete: (id: string) => D(`/api/v1/reports/widgets/${id}`) },
}

// ───────────────────────────────────────────────────────────────────────────
// Workflows (25)
// ───────────────────────────────────────────────────────────────────────────
export const workflowsCatalog = {
  definitions: { byEntity: (entity: string) => G(`/api/v1/workflows/definitions/entity/${entity}`),
                 get: (id: string) => G(`/api/v1/workflows/definitions/${id}`),
                 update: (id: string, b: Body) => PU(`/api/v1/workflows/definitions/${id}`, b),
                 delete: (id: string) => D(`/api/v1/workflows/definitions/${id}`),
                 activate: (id: string) => P(`/api/v1/workflows/definitions/${id}/activate`),
                 deactivate: (id: string) => P(`/api/v1/workflows/definitions/${id}/deactivate`) },

  steps: { forWorkflow: (workflowId: string) => G(`/api/v1/workflows/steps/workflow/${workflowId}`),
           get: (id: string) => G(`/api/v1/workflows/steps/${id}`),
           update: (id: string, b: Body) => PU(`/api/v1/workflows/steps/${id}`, b),
           delete: (id: string) => D(`/api/v1/workflows/steps/${id}`) },

  instances: { byEntity: (entityType: string, p?: Params) => G(`/api/v1/workflows/instances/entity/${entityType}`, p),
               byInitiator: (employeeId: string) => G(`/api/v1/workflows/instances/initiator/${employeeId}`),
               byStatus: (status: string) => G(`/api/v1/workflows/instances/status/${status}`),
               start: (b: Body) => P('/api/v1/workflows/instances', b),
               update: (id: string, b: Body) => PU(`/api/v1/workflows/instances/${id}`, b),
               delete: (id: string) => D(`/api/v1/workflows/instances/${id}`),
               approve: (id: string, b?: Body) => P(`/api/v1/workflows/instances/${id}/approve`, b),
               reject: (id: string, b?: Body) => P(`/api/v1/workflows/instances/${id}/reject`, b),
               cancel: (id: string, b?: Body) => P(`/api/v1/workflows/instances/${id}/cancel`, b) },

  delegations: { byDelegate: (employeeId: string) => G(`/api/v1/workflows/delegations/delegate/${employeeId}`),
                 byDelegator: (employeeId: string) => G(`/api/v1/workflows/delegations/delegator/${employeeId}`),
                 get: (id: string) => G(`/api/v1/workflows/delegations/${id}`),
                 update: (id: string, b: Body) => PU(`/api/v1/workflows/delegations/${id}`, b),
                 delete: (id: string) => D(`/api/v1/workflows/delegations/${id}`),
                 deactivate: (id: string) => P(`/api/v1/workflows/delegations/${id}/deactivate`) },
}

// ───────────────────────────────────────────────────────────────────────────
// Compensation (20)
// ───────────────────────────────────────────────────────────────────────────
export const compensationCatalog = {
  benefits: { list: () => G('/api/v1/compensation/benefits'),
              active: () => G('/api/v1/compensation/benefits/active'),
              byType: (type: string) => G(`/api/v1/compensation/benefits/type/${type}`),
              get: (id: string) => G(`/api/v1/compensation/benefits/${id}`),
              create: (b: Body) => P('/api/v1/compensation/benefits', b),
              update: (id: string, b: Body) => PU(`/api/v1/compensation/benefits/${id}`, b),
              delete: (id: string) => D(`/api/v1/compensation/benefits/${id}`) },

  employeeBenefits: { get: (id: string) => G(`/api/v1/compensation/employee-benefits/${id}`),
                      update: (id: string, b: Body) => PU(`/api/v1/compensation/employee-benefits/${id}`, b),
                      delete: (id: string) => D(`/api/v1/compensation/employee-benefits/${id}`) },

  payGrades: { list: () => G('/api/v1/compensation/pay-grades'),
               active: () => G('/api/v1/compensation/pay-grades/active'),
               get: (id: string) => G(`/api/v1/compensation/pay-grades/${id}`),
               create: (b: Body) => P('/api/v1/compensation/pay-grades', b),
               update: (id: string, b: Body) => PU(`/api/v1/compensation/pay-grades/${id}`, b),
               delete: (id: string) => D(`/api/v1/compensation/pay-grades/${id}`) },

  plans: { get: (id: string) => G(`/api/v1/compensation/plans/${id}`),
           update: (id: string, b: Body) => PU(`/api/v1/compensation/plans/${id}`, b),
           delete: (id: string) => D(`/api/v1/compensation/plans/${id}`) },

  benchmark: { bulkUpload: (b: Body) => P('/api/compensation/benchmark/bulk-upload', b) },
}

// ───────────────────────────────────────────────────────────────────────────
// Leaves (20)
// ───────────────────────────────────────────────────────────────────────────
export const leavesCatalog = {
  apply: (b: Body) => P('/api/v1/leaves/apply', b),
  my: () => G('/api/v1/leaves/my'),
  team: () => G('/api/v1/leaves/team'),
  teamCalendar: (p?: Params) => G('/api/v1/leaves/team-calendar', p),
  approve: (id: string, b?: Body) => P(`/api/v1/leaves/${id}/approve`, b),
  reject: (id: string, b?: Body) => P(`/api/v1/leaves/${id}/reject`, b),
  cancel: (id: string, b?: Body) => P(`/api/v1/leaves/${id}/cancel`, b),

  balance: { my: () => G('/api/v1/leaves/balance/my'),
             forEmployee: (employeeId: string) => G(`/api/v1/leaves/balance/${employeeId}`),
             adjust: (b: Body) => P('/api/v1/leaves/balance/adjust', b),
             init: (b: Body) => P('/api/v1/leaves/balance/init', b) },

  types: { list: () => G('/api/v1/leaves/types'),
           active: () => G('/api/v1/leaves/types/active'),
           get: (id: string) => G(`/api/v1/leaves/types/${id}`),
           create: (b: Body) => P('/api/v1/leaves/types', b),
           update: (id: string, b: Body) => PU(`/api/v1/leaves/types/${id}`, b),
           delete: (id: string) => D(`/api/v1/leaves/types/${id}`) },

  policies: { get: (id: string) => G(`/api/v1/leaves/policies/${id}`),
              update: (id: string, b: Body) => PU(`/api/v1/leaves/policies/${id}`, b),
              delete: (id: string) => D(`/api/v1/leaves/policies/${id}`) },
}

// ───────────────────────────────────────────────────────────────────────────
// Notifications (20)
// ───────────────────────────────────────────────────────────────────────────
export const notificationsCatalog = {
  list: (p?: Params) => G('/api/v1/notifications', p),
  send: (b: Body) => P('/api/v1/notifications', b),
  get: (id: string) => G(`/api/v1/notifications/${id}`),
  update: (id: string, b: Body) => PU(`/api/v1/notifications/${id}`, b),
  delete: (id: string) => D(`/api/v1/notifications/${id}`),
  forEmployee: (employeeId: string) => G(`/api/v1/notifications/employee/${employeeId}`),
  channels: () => G('/api/v1/notifications/channels'),
  sse: () => `${api.defaults.baseURL ?? ''}/api/notifications/sse`,
  sseDashboard: () => `${api.defaults.baseURL ?? ''}/api/notifications/sse/dashboard`,

  announcements: { get: (id: string) => G(`/api/v1/notifications/announcements/${id}`),
                   update: (id: string, b: Body) => PU(`/api/v1/notifications/announcements/${id}`, b),
                   delete: (id: string) => D(`/api/v1/notifications/announcements/${id}`) },

  emailTemplates: { byCode: (code: string) => G(`/api/v1/notifications/email-templates/code/${code}`),
                    get: (id: string) => G(`/api/v1/notifications/email-templates/${id}`),
                    update: (id: string, b: Body) => PU(`/api/v1/notifications/email-templates/${id}`, b),
                    delete: (id: string) => D(`/api/v1/notifications/email-templates/${id}`) },

  preferences: { forEmployee: (employeeId: string) => G(`/api/v1/notifications/preferences/employee/${employeeId}`),
                 get: (id: string) => G(`/api/v1/notifications/preferences/${id}`),
                 create: (b: Body) => P('/api/v1/notifications/preferences', b),
                 update: (id: string, b: Body) => PU(`/api/v1/notifications/preferences/${id}`, b),
                 delete: (id: string) => D(`/api/v1/notifications/preferences/${id}`) },
}

// ───────────────────────────────────────────────────────────────────────────
// Compliance (19)
// ───────────────────────────────────────────────────────────────────────────
export const complianceCatalog = {
  items: { byOwner: (employeeId: string) => G(`/api/v1/compliance/items/owner/${employeeId}`),
           byStatus: (status: string) => G(`/api/v1/compliance/items/status/${status}`),
           get: (id: string) => G(`/api/v1/compliance/items/${id}`),
           update: (id: string, b: Body) => PU(`/api/v1/compliance/items/${id}`, b),
           delete: (id: string) => D(`/api/v1/compliance/items/${id}`),
           markCompliant: (id: string, b?: Body) => P(`/api/v1/compliance/items/${id}/mark-compliant`, b) },

  licenses: { forEmployee: (employeeId: string) => G(`/api/v1/compliance/licenses/employee/${employeeId}`),
              byStatus: (status: string) => G(`/api/v1/compliance/licenses/status/${status}`),
              get: (id: string) => G(`/api/v1/compliance/licenses/${id}`),
              create: (b: Body) => P('/api/v1/compliance/licenses', b),
              update: (id: string, b: Body) => PU(`/api/v1/compliance/licenses/${id}`, b),
              delete: (id: string) => D(`/api/v1/compliance/licenses/${id}`) },

  tasks: { forAssignee: (employeeId: string) => G(`/api/v1/compliance/tasks/assignee/${employeeId}`),
           forItem: (itemId: string) => G(`/api/v1/compliance/tasks/item/${itemId}`),
           get: (id: string) => G(`/api/v1/compliance/tasks/${id}`),
           create: (b: Body) => P('/api/v1/compliance/tasks', b),
           update: (id: string, b: Body) => PU(`/api/v1/compliance/tasks/${id}`, b),
           delete: (id: string) => D(`/api/v1/compliance/tasks/${id}`) },

  reports: { audit: (b: Body) => P('/api/v1/compliance/reports/audit', b) },
}

// ───────────────────────────────────────────────────────────────────────────
// Social — Groups (16)
// ───────────────────────────────────────────────────────────────────────────
export const groupsCatalog = {
  get: (id: string) => G(`/api/v1/groups/${id}`),
  update: (id: string, b: Body) => PU(`/api/v1/groups/${id}`, b),
  delete: (id: string) => D(`/api/v1/groups/${id}`),

  members: { forGroup: (groupId: string) => G(`/api/v1/groups/members/group/${groupId}`),
             forEmployee: (employeeId: string) => G(`/api/v1/groups/members/employee/${employeeId}`),
             get: (id: string) => G(`/api/v1/groups/members/${id}`),
             add: (b: Body) => P('/api/v1/groups/members', b),
             join: (groupId: string) => P('/api/v1/groups/members/join', { groupId }),
             leave: (groupId: string) => D(`/api/v1/groups/members/leave/${groupId}`),
             update: (id: string, b: Body) => PU(`/api/v1/groups/members/${id}`, b),
             remove: (id: string) => D(`/api/v1/groups/members/${id}`) },

  events: { forGroup: (groupId: string) => G(`/api/v1/groups/events/group/${groupId}`),
            forOrganizer: (employeeId: string) => G(`/api/v1/groups/events/organizer/${employeeId}`),
            get: (id: string) => G(`/api/v1/groups/events/${id}`),
            update: (id: string, b: Body) => PU(`/api/v1/groups/events/${id}`, b),
            delete: (id: string) => D(`/api/v1/groups/events/${id}`) },
}

// ───────────────────────────────────────────────────────────────────────────
// Social — Posts (15)
// ───────────────────────────────────────────────────────────────────────────
export const postsCatalog = {
  list: (p?: Params) => G('/api/v1/posts', p),
  create: (b: Body) => P('/api/v1/posts', b),
  get: (id: string) => G(`/api/v1/posts/${id}`),
  update: (id: string, b: Body) => PU(`/api/v1/posts/${id}`, b),
  delete: (id: string) => D(`/api/v1/posts/${id}`),
  byAuthor: (employeeId: string) => G(`/api/v1/posts/author/${employeeId}`),

  comments: { list: (p?: Params) => G('/api/v1/posts/comments', p),
              forPost: (postId: string) => G(`/api/v1/posts/comments/post/${postId}`),
              get: (id: string) => G(`/api/v1/posts/comments/${id}`),
              create: (b: Body) => P('/api/v1/posts/comments', b),
              update: (id: string, b: Body) => PU(`/api/v1/posts/comments/${id}`, b),
              delete: (id: string) => D(`/api/v1/posts/comments/${id}`) },

  likes: { forPost: (postId: string) => G(`/api/v1/posts/likes/post/${postId}`),
           toggle: (postId: string) => P('/api/v1/posts/likes', { postId }),
           remove: (id: string) => D(`/api/v1/posts/likes/${id}`) },
}

// ───────────────────────────────────────────────────────────────────────────
// Engagement (13)
// ───────────────────────────────────────────────────────────────────────────
export const engagementCatalog = {
  kudos: { create: (b: Body) => P('/api/v1/engagement/kudos', b) },
  polls: { create: (b: Body) => P('/api/v1/engagement/polls', b) },
  surveys: { create: (b: Body) => P('/api/v1/engagement/surveys', b),
             launch: (id: string) => P(`/api/v1/engagement/surveys/${id}/launch`) },
  pulse: { me: () => G('/api/engagement/pulse/me') },
  stay: { forEmployee: (employeeId: string) => G(`/api/engagement/stay/employee/${employeeId}`),
          createQuestion: (b: Body) => P('/api/engagement/stay/questions', b) },
  wellness: { logs: (p?: Params) => G('/api/engagement/wellness/logs', p) },
  rewards: { createBudget: (b: Body) => P('/api/engagement/rewards/budgets', b),
             spendBudget: (id: string, b: Body) => P(`/api/engagement/rewards/budgets/${id}/spend`, b),
             createCatalogItem: (b: Body) => P('/api/engagement/rewards/catalog', b),
             fulfillRedemption: (id: string, b?: Body) => P(`/api/engagement/rewards/redemptions/${id}/fulfill`, b) },
  suggestions: { deAnonymise: (id: string) => P(`/api/engagement/suggestions/${id}/de-anonymise`) },
}

// ───────────────────────────────────────────────────────────────────────────
// Expenses (11)
// ───────────────────────────────────────────────────────────────────────────
export const expensesCatalog = {
  items: { create: (b: Body) => P('/api/v1/expenses/items', b),
           update: (id: string, b: Body) => PU(`/api/v1/expenses/items/${id}`, b),
           forReport: (reportId: string) => G(`/api/v1/expenses/items/report/${reportId}`) },

  reports: { create: (b: Body) => P('/api/v1/expenses/reports', b),
             get: (id: string) => G(`/api/v1/expenses/reports/${id}`),
             update: (id: string, b: Body) => PU(`/api/v1/expenses/reports/${id}`, b),
             delete: (id: string) => D(`/api/v1/expenses/reports/${id}`),
             forEmployee: (employeeId: string) => G(`/api/v1/expenses/reports/employee/${employeeId}`),
             submit: (id: string) => P(`/api/v1/expenses/reports/${id}/submit`),
             approve: (id: string, b?: Body) => P(`/api/v1/expenses/reports/${id}/approve`, b),
             reject: (id: string, b?: Body) => P(`/api/v1/expenses/reports/${id}/reject`, b) },
}

// ───────────────────────────────────────────────────────────────────────────
// Workplace (10)
// ───────────────────────────────────────────────────────────────────────────
export const workplaceCatalog = {
  rooms: { create: (b: Body) => P('/api/v1/workplace/rooms', b),
           atLocation: (locationId: string) => G(`/api/v1/workplace/rooms/at/${locationId}`) },

  bookings: { create: (b: Body) => P('/api/v1/workplace/bookings', b),
              forRoom: (roomId: string, p?: Params) => G(`/api/v1/workplace/bookings/room/${roomId}`, p) },

  cabs: { create: (b: Body) => P('/api/v1/workplace/cabs', b),
          mine: () => G('/api/v1/workplace/cabs/mine') },

  visitors: { create: (b: Body) => P('/api/v1/workplace/visitors', b),
              inBuilding: () => G('/api/v1/workplace/visitors/in-building'),
              checkIn: (id: string) => P(`/api/v1/workplace/visitors/${id}/check-in`),
              checkOut: (id: string) => P(`/api/v1/workplace/visitors/${id}/check-out`) },
}

// ───────────────────────────────────────────────────────────────────────────
// One-on-ones (8)
// ───────────────────────────────────────────────────────────────────────────
export const oneOnOnesCatalog = {
  between: (managerId: string, employeeId: string) => G('/api/v1/one-on-ones/between', { managerId, employeeId }),
  forEmployee: (employeeId: string) => G(`/api/v1/one-on-ones/employee/${employeeId}`),
  forManager: (managerId: string) => G(`/api/v1/one-on-ones/manager/${managerId}`),
  meAsEmployee: () => G('/api/v1/one-on-ones/me/as-employee'),
  meAsManager: () => G('/api/v1/one-on-ones/me/as-manager'),
  get: (id: string) => G(`/api/v1/one-on-ones/${id}`),
  update: (id: string, b: Body) => PU(`/api/v1/one-on-ones/${id}`, b),
  complete: (id: string, b?: Body) => P(`/api/v1/one-on-ones/${id}/complete`, b),
}

// ───────────────────────────────────────────────────────────────────────────
// SCIM v2 (7)
// ───────────────────────────────────────────────────────────────────────────
export const scimCatalog = {
  serviceProviderConfig: () => G('/scim/v2/ServiceProviderConfig'),
  listUsers: (p?: Params) => G('/scim/v2/Users', p),
  getUser: (id: string) => G(`/scim/v2/Users/${id}`),
  createUser: (b: Body) => P('/scim/v2/Users', b),
  updateUser: (id: string, b: Body) => PU(`/scim/v2/Users/${id}`, b),
  patchUser: (id: string, b: Body) => PA(`/scim/v2/Users/${id}`, b),
  deleteUser: (id: string) => D(`/scim/v2/Users/${id}`),
}

// ───────────────────────────────────────────────────────────────────────────
// File Vault (7)
// ───────────────────────────────────────────────────────────────────────────
export const fileVaultCatalog = {
  mine: () => G('/api/v1/file-vault/mine'),
  folders: (parentId?: string) => G('/api/v1/file-vault/folders/children', parentId ? { parentId } : undefined),
  createFolder: (b: Body) => P('/api/v1/file-vault/folders', b),
  create: (b: Body) => P('/api/v1/file-vault', b),
  upload: (file: File, metadata?: Body) => {
    const fd = new FormData(); fd.append('file', file)
    if (metadata) fd.append('metadata', JSON.stringify(metadata))
    return P('/api/v1/file-vault/upload', fd as unknown as Body)
  },
  download: (id: string) => G(`/api/v1/file-vault/${id}/download`),
  share: (id: string, b: Body) => P(`/api/v1/file-vault/${id}/share`, b),
}

// ───────────────────────────────────────────────────────────────────────────
// Me / Self-service (7)
// ───────────────────────────────────────────────────────────────────────────
export const meCatalog = {
  dashboard: () => G('/api/v1/me/dashboard'),
  approvals: () => G('/api/v1/me/approvals'),
  team: () => G('/api/v1/me/team'),
  data: () => G('/api/v1/me/data'),
  consent: (b: Body) => P('/api/v1/me/consent', b),
  erase: (b?: Body) => P('/api/v1/me/erase', b),
  restrict: (b?: Body) => P('/api/v1/me/restrict', b),
}

// ───────────────────────────────────────────────────────────────────────────
// Auth (7)
// ───────────────────────────────────────────────────────────────────────────
export const authCatalog = {
  signup: (b: Body) => P('/api/v1/auth/signup', b),
  login: (b: Body) => P('/api/v1/auth/login', b),
  logout: () => P('/api/v1/auth/logout'),
  logoutAll: () => P('/api/v1/auth/logout-all'),
  changePassword: (b: Body) => P('/api/v1/auth/change-password', b),
  otp: { send: (b: Body) => P('/api/v1/auth/otp/send', b),
         verify: (b: Body) => P('/api/v1/auth/otp/verify', b) },
}

// ───────────────────────────────────────────────────────────────────────────
// Competencies (6)
// ───────────────────────────────────────────────────────────────────────────
export const competenciesCatalog = {
  active: () => G('/api/v1/competencies/active'),
  get: (id: string) => G(`/api/v1/competencies/${id}`),
  update: (id: string, b: Body) => PU(`/api/v1/competencies/${id}`, b),
  roleMappings: { forRole: (roleId: string) => G(`/api/v1/competencies/role-mappings/role/${roleId}`),
                  create: (b: Body) => P('/api/v1/competencies/role-mappings', b),
                  delete: (id: string) => D(`/api/v1/competencies/role-mappings/${id}`) },
}

// ───────────────────────────────────────────────────────────────────────────
// Files (6)
// ───────────────────────────────────────────────────────────────────────────
export const filesCatalog = {
  upload: (b: Body) => P('/api/v1/files', b),
  delete: (key: string) => D('/api/v1/files', { key }),
  metadata: (key: string) => G('/api/v1/files/metadata', { key }),
  download: (key: string) => G('/api/v1/files/download', { key }),
  presignedDownload: (key: string) => G<string>('/api/v1/files/presigned-download', { key }),
  presignedUpload: (filename: string, contentType?: string) =>
    P<{ url: string; key: string }>('/api/v1/files/presigned-upload', { filename, contentType }),
}

// ───────────────────────────────────────────────────────────────────────────
// Roles (6)
// ───────────────────────────────────────────────────────────────────────────
export const rolesCatalog = {
  get: (id: string) => G(`/api/v1/roles/${id}`),
  update: (id: string, b: Body) => PU(`/api/v1/roles/${id}`, b),
  delete: (id: string) => D(`/api/v1/roles/${id}`),
  forUser: (userId: string) => G(`/api/v1/roles/users/${userId}`),
  assign: (b: Body) => P('/api/v1/roles/assign', b),
  unassign: (userId: string, roleId: string) => D(`/api/v1/roles/users/${userId}/roles/${roleId}`),
}

// ───────────────────────────────────────────────────────────────────────────
// Payroll runs (6)
// ───────────────────────────────────────────────────────────────────────────
export const payrollRunsCatalog = {
  list: (p?: Params) => G('/api/v1/payroll/runs', p),
  get: (id: string) => G(`/api/v1/payroll/runs/${id}`),
  create: (b: Body) => P('/api/v1/payroll/runs', b),
  process: (id: string) => P(`/api/v1/payroll/runs/${id}/process`),
  lock: (id: string) => P(`/api/v1/payroll/runs/${id}/lock`),
  markPaid: (id: string) => P(`/api/v1/payroll/runs/${id}/mark-paid`),
}

// ───────────────────────────────────────────────────────────────────────────
// Holidays (5)
// ───────────────────────────────────────────────────────────────────────────
export const holidaysCatalog = {
  list: (p?: Params) => G('/api/v1/holidays', p),
  get: (id: string) => G(`/api/v1/holidays/${id}`),
  byLocation: (locationId: string) => G('/api/v1/holidays/by-location', { locationId }),
  create: (b: Body) => P('/api/v1/holidays', b),
  update: (id: string, b: Body) => PU(`/api/v1/holidays/${id}`, b),
}

// ───────────────────────────────────────────────────────────────────────────
// Payslips (5)
// ───────────────────────────────────────────────────────────────────────────
export const payslipsCatalog = {
  mine: () => G('/api/v1/payslips/me'),
  myOne: (id: string) => G(`/api/v1/payslips/me/${id}`),
  myPdf: (id: string) => G<Blob>(`/api/v1/payslips/me/${id}/pdf`),
  forRun: (runId: string) => G(`/api/v1/payslips/run/${runId}`),
  get: (id: string) => G(`/api/v1/payslips/${id}`),
}

// ───────────────────────────────────────────────────────────────────────────
// PIP Plans (5)
// ───────────────────────────────────────────────────────────────────────────
export const pipPlansCatalog = {
  mine: () => G('/api/v1/pip-plans/me'),
  forEmployee: (employeeId: string) => G(`/api/v1/pip-plans/employee/${employeeId}`),
  get: (id: string) => G(`/api/v1/pip-plans/${id}`),
  update: (id: string, b: Body) => PU(`/api/v1/pip-plans/${id}`, b),
  activate: (id: string) => P(`/api/v1/pip-plans/${id}/activate`),
}

// ───────────────────────────────────────────────────────────────────────────
// Skills (5)
// ───────────────────────────────────────────────────────────────────────────
export const skillsCatalog = {
  forEmployee: (employeeId: string) => G(`/api/v1/skills/employees/${employeeId}`),
  experts: (skillId: string) => G('/api/v1/skills/experts', { skillId }),
  gap: (p?: Params) => G('/api/v1/skills/gap', p),
  addEmployeeSkill: (b: Body) => P('/api/v1/skills/employees', b),
  setRoleRequirement: (b: Body) => P('/api/v1/skills/role-requirements', b),
}

// ───────────────────────────────────────────────────────────────────────────
// Asset (workspace floors, visitors host) (4)
// ───────────────────────────────────────────────────────────────────────────
export const workspaceCatalog = {
  createFloor: (b: Body) => P('/api/asset/workspace/floors', b),
  updateFloor: (id: string, b: Body) => PU(`/api/asset/workspace/floors/${id}`, b),
  checkInBooking: (id: string) => P(`/api/asset/workspace/bookings/${id}/check-in`),
  visitorHost: (employeeId: string) => G(`/api/asset/visitors/host/${employeeId}`),
}

// ───────────────────────────────────────────────────────────────────────────
// Performance — Goal cascade (4)
// ───────────────────────────────────────────────────────────────────────────
export const performanceCatalog = {
  goalCascade: { create: (b: Body) => P('/api/performance/goals/cascade', b),
                 rollup: (id: string) => P(`/api/performance/goals/cascade/${id}/rollup`),
                 subtree: (id: string) => G(`/api/performance/goals/cascade/${id}/subtree`) },
  cycles: { update: (id: string, b: Body) => PU(`/api/v1/performance/cycles/${id}`, b) },
}

// ───────────────────────────────────────────────────────────────────────────
// Workflow — OOO/delegation (4)
// ───────────────────────────────────────────────────────────────────────────
export const oooCatalog = {
  forUser: (userId: string) => G(`/api/v1/workflow/ooo/user/${userId}`),
  delegate: (userId: string) => G(`/api/v1/workflow/ooo/user/${userId}/delegate`),
  create: (b: Body) => P('/api/v1/workflow/ooo', b),
  cancel: (id: string) => P(`/api/v1/workflow/ooo/${id}/cancel`),
}

// ───────────────────────────────────────────────────────────────────────────
// Integrations (4)
// ───────────────────────────────────────────────────────────────────────────
export const integrationsCatalog = {
  slack: { inbound: (b: Body) => P('/api/v1/integrations/slack/inbound', b),
           commands: (b: Body) => P('/api/v1/integrations/slack/inbound/commands', b),
           events: (b: Body) => P('/api/v1/integrations/slack/inbound/events', b) },
  teams: { messages: (b: Body) => P('/api/v1/integrations/teams/inbound/messages', b) },
}

// ───────────────────────────────────────────────────────────────────────────
// Feedback (3)
// ───────────────────────────────────────────────────────────────────────────
export const feedbackCatalog = {
  get: (id: string) => G(`/api/v1/feedback/${id}`),
  delete: (id: string) => D(`/api/v1/feedback/${id}`),
  receivedFor: (employeeId: string) => G(`/api/v1/feedback/employee/${employeeId}/received`),
}

// ───────────────────────────────────────────────────────────────────────────
// Interviews (3)
// ───────────────────────────────────────────────────────────────────────────
export const interviewsCatalog = {
  addPanelist: (interviewId: string, b: Body) => P(`/api/v1/interviews/${interviewId}/panelists`, b),
  removePanelist: (interviewId: string, panelistId: string) => D(`/api/v1/interviews/${interviewId}/panelists/${panelistId}`),
  submitPanelistFeedback: (interviewId: string, panelistId: string, b: Body) =>
    P(`/api/v1/interviews/${interviewId}/panelists/${panelistId}/feedback`, b),
}

// ───────────────────────────────────────────────────────────────────────────
// Audit Log (3)
// ───────────────────────────────────────────────────────────────────────────
export const auditCatalog = {
  byActor: (actorId: string, p?: Params) => G('/api/v1/audit/actor', { actorId, ...(p ?? {}) }),
  byEntity: (entityType: string, entityId: string) => G('/api/v1/audit/entity', { entityType, entityId }),
  byRange: (from: string, to: string, p?: Params) => G('/api/v1/audit/range', { from, to, ...(p ?? {}) }),
}

// ───────────────────────────────────────────────────────────────────────────
// Cases — notes/status (3)
// ───────────────────────────────────────────────────────────────────────────
export const casesCatalog = {
  notes: (caseId: string) => G(`/api/v1/cases/${caseId}/notes`),
  addNote: (caseId: string, b: Body) => P(`/api/v1/cases/${caseId}/notes`, b),
  updateStatus: (caseId: string, b: Body) => P(`/api/v1/cases/${caseId}/status`, b),
}

// ───────────────────────────────────────────────────────────────────────────
// Forms (3)
// ───────────────────────────────────────────────────────────────────────────
export const formsCatalog = {
  byCode: (code: string) => G(`/api/v1/forms/by-code/${code}`),
  create: (b: Body) => P('/api/v1/forms', b),
  publish: (id: string) => P(`/api/v1/forms/${id}/publish`),
}

// ───────────────────────────────────────────────────────────────────────────
// Users (3)
// ───────────────────────────────────────────────────────────────────────────
export const usersCatalog = {
  me: () => G('/api/v1/users/me'),
  get: (id: string) => G(`/api/v1/users/${id}`),
  setStatus: (id: string, status: string) => PU(`/api/v1/users/${id}/status`, { status }),
}

// ───────────────────────────────────────────────────────────────────────────
// Public — Careers / Career site (2)
// ───────────────────────────────────────────────────────────────────────────
export const careersPublicCatalog = {
  apply: (jobId: string, b: Body) => P(`/api/public/v1/careers/${jobId}`, b),
  parseResume: (jobId: string, b: Body) => P(`/api/public/v1/careers/${jobId}/resume/parse`, b),
}

// ───────────────────────────────────────────────────────────────────────────
// API Keys (2)
// ───────────────────────────────────────────────────────────────────────────
export const apiKeysCatalog = {
  revoke: (id: string) => P(`/api/v1/api-keys/${id}/revoke`),
  rotate: (id: string) => P(`/api/v1/api-keys/${id}/rotate`),
}

// ───────────────────────────────────────────────────────────────────────────
// Employees bulk (1)
// ───────────────────────────────────────────────────────────────────────────
export const employeesBulkCatalog = {
  bulkCreate: (b: Body) => P('/api/v1/employees/bulk', b),
}

// ───────────────────────────────────────────────────────────────────────────
// Letters (1)
// ───────────────────────────────────────────────────────────────────────────
export const lettersCatalog = {
  generateEmployment: (employeeId: string, b?: Body) => P(`/api/v1/letters/employment/${employeeId}`, b),
}

// ───────────────────────────────────────────────────────────────────────────
// Tenants (1)
// ───────────────────────────────────────────────────────────────────────────
export const tenantsCatalog = {
  create: (b: Body) => P('/api/v1/tenants', b),
}

// ───────────────────────────────────────────────────────────────────────────
// Grand re-export — single import surface for any consumer
// ───────────────────────────────────────────────────────────────────────────
export const Catalog = {
  courses: coursesCatalog,
  documents: documentsCatalog,
  assets: assetsCatalog,
  onboarding: onboardingCatalog,
  tickets: ticketsCatalog,
  offboarding: offboardingCatalog,
  recruitment: recruitmentCatalog,
  reports: reportsCatalog,
  workflows: workflowsCatalog,
  compensation: compensationCatalog,
  leaves: leavesCatalog,
  notifications: notificationsCatalog,
  compliance: complianceCatalog,
  groups: groupsCatalog,
  posts: postsCatalog,
  engagement: engagementCatalog,
  expenses: expensesCatalog,
  workplace: workplaceCatalog,
  oneOnOnes: oneOnOnesCatalog,
  scim: scimCatalog,
  fileVault: fileVaultCatalog,
  me: meCatalog,
  auth: authCatalog,
  competencies: competenciesCatalog,
  files: filesCatalog,
  roles: rolesCatalog,
  payrollRuns: payrollRunsCatalog,
  holidays: holidaysCatalog,
  payslips: payslipsCatalog,
  pipPlans: pipPlansCatalog,
  skills: skillsCatalog,
  workspace: workspaceCatalog,
  performance: performanceCatalog,
  ooo: oooCatalog,
  integrations: integrationsCatalog,
  feedback: feedbackCatalog,
  interviews: interviewsCatalog,
  audit: auditCatalog,
  cases: casesCatalog,
  forms: formsCatalog,
  users: usersCatalog,
  careersPublic: careersPublicCatalog,
  apiKeys: apiKeysCatalog,
  employeesBulk: employeesBulkCatalog,
  letters: lettersCatalog,
  tenants: tenantsCatalog,
} as const

export default Catalog
