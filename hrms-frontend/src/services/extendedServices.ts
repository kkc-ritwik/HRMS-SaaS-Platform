/**
 * Thin service stubs for backend modules where a dedicated file would be overkill.
 * Each export is a record of REST calls that map 1:1 to a single backend controller.
 */
import { api, unwrap } from '@/lib/api'

// ── Recruitment depth ─────────────────────────────────────────────────────
export const interviewService = {
  list: async (params: Record<string, unknown> = {}) => unwrap(await api.get('/api/v1/interviews', { params })),
  get: async (id: string) => unwrap(await api.get(`/api/v1/interviews/${id}`)),
  schedule: async (payload: Record<string, unknown>) => unwrap(await api.post('/api/v1/interviews', payload)),
  submitFeedback: async (id: string, payload: Record<string, unknown>) => unwrap(await api.post(`/api/v1/interviews/${id}/feedback`, payload)),
  panellists: async (id: string) => unwrap(await api.get(`/api/v1/interviews/${id}/panellists`)),
}

export const offerService = {
  list: async () => unwrap(await api.get('/api/v1/offers')),
  get: async (id: string) => unwrap(await api.get(`/api/v1/offers/${id}`)),
  create: async (payload: Record<string, unknown>) => unwrap(await api.post('/api/v1/offers', payload)),
  issue: async (id: string) => unwrap(await api.post(`/api/v1/offers/${id}/issue`)),
  withdraw: async (id: string, reason: string) => unwrap(await api.post(`/api/v1/offers/${id}/withdraw`, { reason })),
}

export const hiringLoopService = {
  list: async () => unwrap(await api.get('/api/recruitment/hiring-loops')),
  create: async (payload: Record<string, unknown>) => unwrap(await api.post('/api/recruitment/hiring-loops', payload)),
  schedule: async (id: string, payload: Record<string, unknown>) => unwrap(await api.post(`/api/recruitment/hiring-loops/${id}/schedule`, payload)),
  scorecardTemplates: async () => unwrap(await api.get('/api/recruitment/scorecard-templates')),
}

export const referenceCheckService = {
  list: async (candidateId?: string) => unwrap(await api.get('/api/v1/reference-checks', { params: { candidateId } })),
  invite: async (payload: Record<string, unknown>) => unwrap(await api.post('/api/v1/reference-checks/invite', payload)),
}

// ── Lifecycle ─────────────────────────────────────────────────────────────
export const probationService = {
  list: async () => unwrap(await api.get('/api/v1/onboarding/probation-reviews')),
  decide: async (id: string, decision: 'CONFIRM' | 'EXTEND' | 'TERMINATE', notes?: string) =>
    unwrap(await api.post(`/api/v1/onboarding/probation-reviews/${id}/decide`, { decision, notes })),
}

export const exitInterviewService = {
  list: async () => unwrap(await api.get('/api/v1/offboarding/exit-interviews')),
  schedule: async (separationId: string, scheduledAt: string) =>
    unwrap(await api.post('/api/v1/offboarding/exit-interviews', { separationId, scheduledAt })),
  submit: async (id: string, payload: Record<string, unknown>) =>
    unwrap(await api.post(`/api/v1/offboarding/exit-interviews/${id}/submit`, payload)),
}

// ── Payroll depth ────────────────────────────────────────────────────────
export const salaryStructureService = {
  list: async () => unwrap(await api.get('/api/v1/salary-structures')),
  create: async (payload: Record<string, unknown>) => unwrap(await api.post('/api/v1/salary-structures', payload)),
}

export const loanService = {
  myLoans: async () => unwrap(await api.get('/api/v1/loans/me')),
  list: async () => unwrap(await api.get('/api/v1/loans')),
  apply: async (payload: Record<string, unknown>) => unwrap(await api.post('/api/v1/loans', payload)),
  approve: async (id: string) => unwrap(await api.post(`/api/v1/loans/${id}/approve`)),
  reject: async (id: string, reason: string) => unwrap(await api.post(`/api/v1/loans/${id}/reject`, { reason })),
  repayments: async (id: string) => unwrap(await api.get(`/api/v1/loans/${id}/repayments`)),
}

// ── Performance depth ────────────────────────────────────────────────────
export const oneOnOneService = {
  list: async () => unwrap(await api.get('/api/v1/one-on-ones')),
  schedule: async (payload: Record<string, unknown>) => unwrap(await api.post('/api/v1/one-on-ones', payload)),
}

export const competencyService = {
  list: async () => unwrap(await api.get('/api/v1/competencies')),
  create: async (payload: Record<string, unknown>) => unwrap(await api.post('/api/v1/competencies', payload)),
}

export const reviewCycleService = {
  list: async () => unwrap(await api.get('/api/v1/performance/cycles')),
  create: async (payload: Record<string, unknown>) => unwrap(await api.post('/api/v1/performance/cycles', payload)),
  launch: async (id: string) => unwrap(await api.post(`/api/v1/performance/cycles/${id}/launch`)),
}

export const pipService = {
  list: async () => unwrap(await api.get('/api/v1/pip-plans')),
  create: async (payload: Record<string, unknown>) => unwrap(await api.post('/api/v1/pip-plans', payload)),
}

// ── Engagement depth ─────────────────────────────────────────────────────
export const awardService = {
  list: async () => unwrap(await api.get('/api/v1/awards')),
  nominate: async (payload: Record<string, unknown>) => unwrap(await api.post('/api/v1/awards', payload)),
  approve: async (id: string) => unwrap(await api.post(`/api/v1/awards/${id}/approve`)),
}

export const rewardsCatalogService = {
  catalog: async () => unwrap(await api.get('/api/engagement/rewards/catalog')),
  budgets: async () => unwrap(await api.get('/api/engagement/rewards/budgets')),
  redeem: async (payload: Record<string, unknown>) => unwrap(await api.post('/api/engagement/rewards/redeem', payload)),
}

export const wellnessService = {
  programs: async () => unwrap(await api.get('/api/engagement/wellness/programs')),
  createProgram: async (payload: Record<string, unknown>) => unwrap(await api.post('/api/engagement/wellness/programs', payload)),
  log: async (payload: Record<string, unknown>) => unwrap(await api.post('/api/engagement/wellness/logs', payload)),
  leaderboard: async (programId: string, from: string, to: string) =>
    unwrap(await api.get('/api/engagement/wellness/leaderboard', { params: { programId, from, to } })),
}

export const stayInterviewService = {
  list: async () => unwrap(await api.get('/api/engagement/stay/due')),
  schedule: async (payload: Record<string, unknown>) => unwrap(await api.post('/api/engagement/stay', payload)),
  complete: async (id: string, payload: Record<string, unknown>) => unwrap(await api.post(`/api/engagement/stay/${id}/complete`, payload)),
  questions: async () => unwrap(await api.get('/api/engagement/stay/questions')),
}

export const heatmapService = {
  byDept: async (from: string, to: string) =>
    unwrap(await api.get('/api/engagement/heatmap/by-department', { params: { from, to } })),
  byLocation: async (from: string, to: string) =>
    unwrap(await api.get('/api/engagement/heatmap/by-location', { params: { from, to } })),
  byManager: async (from: string, to: string) =>
    unwrap(await api.get('/api/engagement/heatmap/by-manager', { params: { from, to } })),
  byTenure: async (from: string, to: string) =>
    unwrap(await api.get('/api/engagement/heatmap/by-tenure', { params: { from, to } })),
}

export const pollService = {
  list: async () => unwrap(await api.get('/api/v1/polls')),
  create: async (payload: Record<string, unknown>) => unwrap(await api.post('/api/v1/polls', payload)),
  vote: async (pollId: string, optionId: string) => unwrap(await api.post(`/api/v1/polls/${pollId}/vote`, { optionId })),
}

// ── Asset depth ──────────────────────────────────────────────────────────
export const assetCategoryService = {
  list: async () => unwrap(await api.get('/api/v1/assets/categories')),
  create: async (payload: Record<string, unknown>) => unwrap(await api.post('/api/v1/assets/categories', payload)),
}

export const assetMaintenanceService = {
  list: async () => unwrap(await api.get('/api/v1/assets/maintenance')),
  schedule: async (payload: Record<string, unknown>) => unwrap(await api.post('/api/v1/assets/maintenance', payload)),
  complete: async (id: string, notes?: string) => unwrap(await api.post(`/api/v1/assets/maintenance/${id}/complete`, { notes })),
}

export const assetRequestService = {
  list: async () => unwrap(await api.get('/api/v1/assets/requests')),
  myRequests: async () => unwrap(await api.get('/api/v1/assets/requests/me')),
  raise: async (payload: Record<string, unknown>) => unwrap(await api.post('/api/v1/assets/requests', payload)),
  approve: async (id: string) => unwrap(await api.post(`/api/v1/assets/requests/${id}/approve`)),
}

export const amcContractService = {
  list: async () => unwrap(await api.get('/api/v1/assets/amc-contracts')),
  create: async (payload: Record<string, unknown>) => unwrap(await api.post('/api/v1/assets/amc-contracts', payload)),
  expiring: async (days = 90) => unwrap(await api.get('/api/v1/assets/amc-contracts/expiring', { params: { days } })),
}

export const vendorService = {
  list: async () => unwrap(await api.get('/api/v1/vendors')),
  get: async (id: string) => unwrap(await api.get(`/api/v1/vendors/${id}`)),
  create: async (payload: Record<string, unknown>) => unwrap(await api.post('/api/v1/vendors', payload)),
}

// ── Expense depth ────────────────────────────────────────────────────────
export const expenseCategoryService = {
  list: async () => unwrap(await api.get('/api/v1/expenses/categories')),
  create: async (payload: Record<string, unknown>) => unwrap(await api.post('/api/v1/expenses/categories', payload)),
}

export const advanceService = {
  myAdvances: async () => unwrap(await api.get('/api/v1/expenses/advances/me')),
  list: async () => unwrap(await api.get('/api/v1/expenses/advances')),
  request: async (payload: Record<string, unknown>) => unwrap(await api.post('/api/v1/expenses/advances', payload)),
  settle: async (id: string, expenseClaimId: string) =>
    unwrap(await api.post(`/api/v1/expenses/advances/${id}/settle`, { expenseClaimId })),
}

// ── Document depth ───────────────────────────────────────────────────────
export const documentTemplateService = {
  list: async () => unwrap(await api.get('/api/v1/document-templates')),
  create: async (payload: Record<string, unknown>) => unwrap(await api.post('/api/v1/document-templates', payload)),
}

export const lettersService = {
  list: async (type?: string) => unwrap(await api.get('/api/v1/letters', { params: { type } })),
  employment: async (payload: Record<string, unknown>) => unwrap(await api.post('/api/v1/documents/letters/employment', payload)),
  salaryRevision: async (payload: Record<string, unknown>) => unwrap(await api.post('/api/documents/letters/salary-revision', payload)),
  exit: async (type: 'RELIEVING' | 'EXPERIENCE' | 'SERVICE' | 'REFERENCE', payload: Record<string, unknown>) =>
    unwrap(await api.post(`/api/documents/letters/exit/${type}`, payload)),
}

export const companyPolicyService = {
  list: async () => unwrap(await api.get('/api/v1/policies')),
  acknowledge: async (id: string) => unwrap(await api.post(`/api/v1/policies/${id}/acknowledge`)),
  acknowledgements: async (id: string) => unwrap(await api.get(`/api/v1/policies/${id}/acknowledgements`)),
}

// ── Helpdesk depth ───────────────────────────────────────────────────────
export const kbService = {
  list: async (category?: string, search?: string) => unwrap(await api.get('/api/v1/kb', { params: { category, search } })),
  get: async (id: string) => unwrap(await api.get(`/api/v1/kb/${id}`)),
  create: async (payload: Record<string, unknown>) => unwrap(await api.post('/api/v1/kb', payload)),
}

// ── Compliance / Cases / GDPR ────────────────────────────────────────────
export const gdprService = {
  exportEmployee: async (employeeId: string) => unwrap(await api.get(`/api/v1/gdpr/export/${employeeId}`)),
  eraseEmployee: async (employeeId: string, reason: string) => unwrap(await api.post(`/api/v1/gdpr/erase/${employeeId}`, { reason })),
  consents: async (employeeId: string) => unwrap(await api.get(`/api/v1/gdpr/consents/${employeeId}`)),
  recordConsent: async (employeeId: string, payload: Record<string, unknown>) =>
    unwrap(await api.post(`/api/v1/gdpr/consent/${employeeId}`, payload)),
}

export const casesService = {
  list: async (params: Record<string, unknown> = {}) => unwrap(await api.get('/api/v1/cases', { params })),
  get: async (id: string) => unwrap(await api.get(`/api/v1/cases/${id}`)),
  create: async (payload: Record<string, unknown>) => unwrap(await api.post('/api/v1/cases', payload)),
  poshAnnualReturn: async (year: number) => unwrap(await api.get('/api/cases/posh/annual-return/preview', { params: { year } })),
}

export const complianceItemService = {
  list: async () => unwrap(await api.get('/api/v1/compliance/items')),
  create: async (payload: Record<string, unknown>) => unwrap(await api.post('/api/v1/compliance/items', payload)),
  markDone: async (id: string) => unwrap(await api.post(`/api/v1/compliance/items/${id}/done`)),
}

export const licenseService = {
  list: async () => unwrap(await api.get('/api/v1/compliance/licenses')),
  expiring: async (days = 90) => unwrap(await api.get('/api/v1/compliance/licenses/expiring', { params: { days } })),
}

// ── Org / Skills / Travel / Forms / Shifts / CostCenters ────────────────
export const orgChartService = {
  tree: async () => unwrap(await api.get('/api/v1/org-chart/tree')),
  reportsTo: async (employeeId: string) => unwrap(await api.get(`/api/v1/org-chart/${employeeId}`)),
}

export const skillsService = {
  list: async () => unwrap(await api.get('/api/v1/skills')),
  myMatrix: async (employeeId: string) => unwrap(await api.get(`/api/v1/skills/employee/${employeeId}`)),
  rate: async (payload: Record<string, unknown>) => unwrap(await api.post('/api/v1/skills/rate', payload)),
}

export const formService = {
  list: async () => unwrap(await api.get('/api/v1/forms')),
  get: async (id: string) => unwrap(await api.get(`/api/v1/forms/${id}`)),
  submit: async (id: string, payload: Record<string, unknown>) => unwrap(await api.post(`/api/v1/forms/${id}/submissions`, payload)),
  submissions: async (id: string) => unwrap(await api.get(`/api/v1/forms/${id}/submissions`)),
}

export const shiftService = {
  list: async () => unwrap(await api.get('/api/v1/shifts')),
  create: async (payload: Record<string, unknown>) => unwrap(await api.post('/api/v1/shifts', payload)),
  swapRequest: async (payload: Record<string, unknown>) => unwrap(await api.post('/api/v1/shifts/swap', payload)),
  mySchedule: async (from: string, to: string) => unwrap(await api.get('/api/v1/shifts/me', { params: { from, to } })),
}

export const costCenterService = {
  list: async () => unwrap(await api.get('/api/corehr/cost-centers')),
  create: async (payload: Record<string, unknown>) => unwrap(await api.post('/api/corehr/cost-centers', payload)),
  allocations: async (employeeId: string, on?: string) =>
    unwrap(await api.get(`/api/corehr/cost-centers/employee/${employeeId}/active`, { params: { on } })),
}

export const customFieldService = {
  list: async (entity: string) => unwrap(await api.get('/api/v1/custom-fields', { params: { entity } })),
  create: async (payload: Record<string, unknown>) => unwrap(await api.post('/api/v1/custom-fields', payload)),
}

// ── Workflows ────────────────────────────────────────────────────────────
export const workflowDefinitionService = {
  list: async () => unwrap(await api.get('/api/v1/workflows/definitions')),
  create: async (payload: Record<string, unknown>) => unwrap(await api.post('/api/v1/workflows/definitions', payload)),
  publish: async (id: string) => unwrap(await api.post(`/api/v1/workflows/definitions/${id}/publish`)),
}

export const workflowInstanceService = {
  list: async () => unwrap(await api.get('/api/v1/workflows/instances')),
  get: async (id: string) => unwrap(await api.get(`/api/v1/workflows/instances/${id}`)),
}

export const delegationRuleService = {
  list: async () => unwrap(await api.get('/api/v1/workflow/delegation-rules')),
  create: async (payload: Record<string, unknown>) => unwrap(await api.post('/api/v1/workflow/delegation-rules', payload)),
}

// ── Onboarding ───────────────────────────────────────────────────────────
export const onboardingTemplateService = {
  list: async () => unwrap(await api.get('/api/v1/onboarding/templates')),
  create: async (payload: Record<string, unknown>) => unwrap(await api.post('/api/v1/onboarding/templates', payload)),
}

export const onboardingTaskService = {
  list: async (workflowId: string) => unwrap(await api.get(`/api/v1/onboarding/workflows/${workflowId}/tasks`)),
  complete: async (taskId: string, payload?: Record<string, unknown>) =>
    unwrap(await api.post(`/api/v1/onboarding/tasks/${taskId}/complete`, payload || {})),
}

export const buddyService = {
  list: async () => unwrap(await api.get('/api/v1/onboarding/buddies')),
  assign: async (newHireId: string, buddyId: string) =>
    unwrap(await api.post('/api/v1/onboarding/buddies', { newHireId, buddyId })),
}

// ── LMS depth ────────────────────────────────────────────────────────────
export const certificationService = {
  myCerts: async () => unwrap(await api.get('/api/v1/certifications/me')),
  list: async (employeeId?: string) => unwrap(await api.get('/api/v1/certifications', { params: { employeeId } })),
  upload: async (payload: Record<string, unknown>) => unwrap(await api.post('/api/v1/certifications', payload)),
}

export const assessmentService = {
  list: async (courseId: string) => unwrap(await api.get(`/api/v1/courses/${courseId}/assessments`)),
  attempt: async (assessmentId: string, answers: Record<string, unknown>) =>
    unwrap(await api.post(`/api/v1/assessments/${assessmentId}/attempt`, answers)),
}

// ── Self-service ─────────────────────────────────────────────────────────
export const selfServiceService = {
  myEmployee: async () => unwrap(await api.get('/api/v1/self-service/me')),
  myTeam: async () => unwrap(await api.get('/api/v1/self-service/my-team')),
  myManager: async () => unwrap(await api.get('/api/v1/self-service/my-manager')),
  raiseUpdate: async (payload: Record<string, unknown>) =>
    unwrap(await api.post('/api/v1/self-service/profile-update', payload)),
}

// ── Background verification ──────────────────────────────────────────────
export const bgvService = {
  list: async () => unwrap(await api.get('/api/v1/recruitment/bgv')),
  initiate: async (candidateId: string, payload: Record<string, unknown>) =>
    unwrap(await api.post('/api/v1/recruitment/bgv/initiate', { candidateId, ...payload })),
  status: async (caseId: string) => unwrap(await api.get(`/api/v1/recruitment/bgv/${caseId}`)),
}
