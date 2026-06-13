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
  panellists: async (id: string) => unwrap(await api.get(`/api/v1/interviews/${id}/panelists`)),
}

// Backend: OfferController @ /api/v1/offers (offers are created from an application)
export const offerService = {
  list: async () => unwrap(await api.get('/api/v1/offers')),
  get: async (id: string) => unwrap(await api.get(`/api/v1/offers/${id}`)),
  forApplication: async (applicationId: string) => unwrap(await api.get(`/api/v1/offers/application/${applicationId}`)),
  send: async (id: string) => unwrap(await api.post(`/api/v1/offers/${id}/send`)),
  respond: async (id: string, accepted: boolean) => unwrap(await api.post(`/api/v1/offers/${id}/respond`, { accepted })),
  revoke: async (id: string, reason: string) => unwrap(await api.post(`/api/v1/offers/${id}/revoke`, { reason })),
}

export const hiringLoopService = {
  list: async () => unwrap(await api.get('/api/recruitment/hiring-loops')),
  create: async (payload: Record<string, unknown>) => unwrap(await api.post('/api/recruitment/hiring-loops', payload)),
  schedule: async (id: string, payload: Record<string, unknown>) => unwrap(await api.post(`/api/recruitment/hiring-loops/${id}/schedule`, payload)),
  scorecardTemplates: async () => unwrap(await api.get('/api/recruitment/scorecard-templates')),
}

export const referenceCheckService = {
  list: async (candidateId: string) => unwrap(await api.get(`/api/v1/recruitment/references/candidate/${candidateId}`)),
  invite: async (candidateId: string, payload: Record<string, unknown>) => unwrap(await api.post(`/api/v1/recruitment/references/${candidateId}/invite`, payload)),
}

// ── Lifecycle ─────────────────────────────────────────────────────────────
export const probationService = {
  list: async () => unwrap(await api.get('/api/v1/onboarding/probation-reviews')),
  decide: async (id: string, decision: 'CONFIRM' | 'EXTEND' | 'TERMINATE', notes?: string) =>
    unwrap(await api.put(`/api/v1/onboarding/probation-reviews/${id}`, { decision, status: decision, notes })),
}

export const exitInterviewService = {
  list: async () => unwrap(await api.get('/api/v1/offboarding/exit-interviews')),
  schedule: async (separationId: string, scheduledAt: string) =>
    unwrap(await api.post('/api/v1/offboarding/exit-interviews', { separationId, scheduledAt })),
  submit: async (id: string, payload: Record<string, unknown>) =>
    unwrap(await api.post(`/api/v1/offboarding/exit-interviews/${id}/complete`, payload)),
}

// ── Payroll depth ────────────────────────────────────────────────────────
// Backend: SalaryStructureController @ /api/v1/salary/structures
export const salaryStructureService = {
  list: async () => unwrap(await api.get('/api/v1/salary/structures')),
  active: async () => unwrap(await api.get('/api/v1/salary/structures/active')),
  create: async (payload: Record<string, unknown>) => unwrap(await api.post('/api/v1/salary/structures', payload)),
}

// Backend: LoanController @ /api/v1/payroll/loans (per-employee; no admin list-all)
export const loanService = {
  myLoans: async () => unwrap(await api.get('/api/v1/payroll/loans/me')),
  list: async () => unwrap(await api.get('/api/v1/payroll/loans/me')),
  forEmployee: async (employeeId: string) => unwrap(await api.get(`/api/v1/payroll/loans/employee/${employeeId}`)),
  get: async (id: string) => unwrap(await api.get(`/api/v1/payroll/loans/${id}`)),
  schedule: async (id: string) => unwrap(await api.get(`/api/v1/payroll/loans/${id}/schedule`)),
  apply: async (payload: Record<string, unknown>) => {
    const employeeId = String(payload.employeeId ?? '')
    return unwrap(await api.post(`/api/v1/payroll/loans/employee/${employeeId}`, payload))
  },
  close: async (id: string) => unwrap(await api.post(`/api/v1/payroll/loans/${id}/close`)),
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

// Backend: PerformanceCycleController @ /api/v1/performance/cycles
export const reviewCycleService = {
  list: async () => unwrap(await api.get('/api/v1/performance/cycles')),
  create: async (payload: Record<string, unknown>) => unwrap(await api.post('/api/v1/performance/cycles', payload)),
  activate: async (id: string) => unwrap(await api.post(`/api/v1/performance/cycles/${id}/activate`)),
  launch: async (id: string) => unwrap(await api.post(`/api/v1/performance/cycles/${id}/activate`)),
  startSelfReview: async (id: string) => unwrap(await api.post(`/api/v1/performance/cycles/${id}/start-self-review`)),
  startManagerReview: async (id: string) => unwrap(await api.post(`/api/v1/performance/cycles/${id}/start-manager-review`)),
  startCalibration: async (id: string) => unwrap(await api.post(`/api/v1/performance/cycles/${id}/start-calibration`)),
  finalize: async (id: string) => unwrap(await api.post(`/api/v1/performance/cycles/${id}/finalize`)),
  close: async (id: string) => unwrap(await api.post(`/api/v1/performance/cycles/${id}/close`)),
}

export const pipService = {
  list: async () => unwrap(await api.get('/api/v1/pip-plans')),
  create: async (payload: Record<string, unknown>) => unwrap(await api.post('/api/v1/pip-plans', payload)),
}

// ── Engagement depth ─────────────────────────────────────────────────────
// Backend: AwardController @ /api/v1/awards (recognition nominations + approvals)
export const awardService = {
  list: async (status?: string) => unwrap(await api.get('/api/v1/awards', { params: { status } })),
  forNominee: async (nomineeId: string) => unwrap(await api.get(`/api/v1/awards/nominee/${nomineeId}`)),
  get: async (id: string) => unwrap(await api.get(`/api/v1/awards/${id}`)),
  nominate: async (payload: Record<string, unknown>) => unwrap(await api.post('/api/v1/awards', payload)),
  approve: async (id: string, notes?: string) => unwrap(await api.post(`/api/v1/awards/${id}/approve`, { notes })),
  reject: async (id: string, notes?: string) => unwrap(await api.post(`/api/v1/awards/${id}/reject`, { notes })),
  remove: async (id: string) => { await api.delete(`/api/v1/awards/${id}`) },
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
  list: async () => unwrap(await api.get('/api/v1/engagement/polls')),
  create: async (payload: Record<string, unknown>) => unwrap(await api.post('/api/v1/engagement/polls', payload)),
  vote: async (pollId: string, optionId: string) => unwrap(await api.post(`/api/v1/engagement/polls/${pollId}/vote`, { optionId })),
  tally: async (pollId: string) => unwrap(await api.get(`/api/v1/engagement/polls/${pollId}/tally`)),
}

// ── Asset depth ──────────────────────────────────────────────────────────
export const assetCategoryService = {
  list: async () => unwrap(await api.get('/api/v1/assets/categories')),
  create: async (payload: Record<string, unknown>) => unwrap(await api.post('/api/v1/assets/categories', payload)),
}

export const assetMaintenanceService = {
  list: async () => unwrap(await api.get('/api/v1/assets/maintenance')),
  schedule: async (payload: Record<string, unknown>) => unwrap(await api.post('/api/v1/assets/maintenance', payload)),
  complete: async (id: string, notes?: string) => unwrap(await api.put(`/api/v1/assets/maintenance/${id}`, { status: 'COMPLETED', notes })),
}

export const assetRequestService = {
  list: async () => unwrap(await api.get('/api/v1/assets/requests')),
  myRequests: async () => unwrap(await api.get('/api/v1/assets/requests/me')),
  raise: async (payload: Record<string, unknown>) => unwrap(await api.post('/api/v1/assets/requests', payload)),
  approve: async (id: string) => unwrap(await api.post(`/api/v1/assets/requests/${id}/approve`)),
}

export const amcContractService = {
  list: async () => unwrap(await api.get('/api/v1/assets/amc-contracts')),
  get: async (id: string) => unwrap(await api.get(`/api/v1/assets/amc-contracts/${id}`)),
  create: async (payload: Record<string, unknown>) => unwrap(await api.post('/api/v1/assets/amc-contracts', payload)),
  update: async (id: string, payload: Record<string, unknown>) => unwrap(await api.put(`/api/v1/assets/amc-contracts/${id}`, payload)),
  remove: async (id: string) => { await api.delete(`/api/v1/assets/amc-contracts/${id}`) },
  expiring: async (days = 90) => unwrap(await api.get('/api/v1/assets/amc-contracts/expiring', { params: { days } })),
}

export const vendorService = {
  list: async (category?: string) => unwrap(await api.get('/api/v1/vendors', { params: { category } })),
  get: async (id: string) => unwrap(await api.get(`/api/v1/vendors/${id}`)),
  create: async (payload: Record<string, unknown>) => unwrap(await api.post('/api/v1/vendors', payload)),
  update: async (id: string, payload: Record<string, unknown>) => unwrap(await api.put(`/api/v1/vendors/${id}`, payload)),
  remove: async (id: string) => { await api.delete(`/api/v1/vendors/${id}`) },
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
// Backend: DocumentTemplateController @ /api/v1/documents/templates
export const documentTemplateService = {
  list: async () => unwrap(await api.get('/api/v1/documents/templates/all')),
  get: async (id: string) => unwrap(await api.get(`/api/v1/documents/templates/${id}`)),
  create: async (payload: Record<string, unknown>) => unwrap(await api.post('/api/v1/documents/templates', payload)),
  update: async (id: string, payload: Record<string, unknown>) => unwrap(await api.put(`/api/v1/documents/templates/${id}`, payload)),
  remove: async (id: string) => { await api.delete(`/api/v1/documents/templates/${id}`) },
}

export const lettersService = {
  list: async (employeeId?: string) => unwrap(await api.get(employeeId ? `/api/v1/documents/letters/employee/${employeeId}` : '/api/v1/documents/letters')),
  employment: async (payload: Record<string, unknown>) => {
    const type = String(payload.letterType ?? 'EMPLOYMENT')
    return unwrap(await api.post(`/api/v1/letters/employment/${type}`, payload))
  },
  salaryRevision: async (payload: Record<string, unknown>) => unwrap(await api.post('/api/documents/letters/salary-revision', payload)),
  exit: async (type: 'RELIEVING' | 'EXPERIENCE' | 'SERVICE' | 'REFERENCE', payload: Record<string, unknown>) =>
    unwrap(await api.post(`/api/documents/letters/exit/${type}`, payload)),
}

export const companyPolicyService = {
  list: async () => unwrap(await api.get('/api/v1/documents/policies/all')),
  get: async (id: string) => unwrap(await api.get(`/api/v1/documents/policies/${id}`)),
  create: async (payload: Record<string, unknown>) => unwrap(await api.post('/api/v1/documents/policies', payload)),
  update: async (id: string, payload: Record<string, unknown>) => unwrap(await api.put(`/api/v1/documents/policies/${id}`, payload)),
}

// ── Helpdesk depth ───────────────────────────────────────────────────────
// Backend: KbArticleController @ /api/v1/tickets/kb-articles
export const kbService = {
  list: async () => unwrap(await api.get('/api/v1/tickets/kb-articles')),
  published: async () => unwrap(await api.get('/api/v1/tickets/kb-articles/published')),
  get: async (id: string) => unwrap(await api.get(`/api/v1/tickets/kb-articles/${id}`)),
  create: async (payload: Record<string, unknown>) => unwrap(await api.post('/api/v1/tickets/kb-articles', payload)),
  update: async (id: string, payload: Record<string, unknown>) => unwrap(await api.put(`/api/v1/tickets/kb-articles/${id}`, payload)),
  publish: async (id: string) => unwrap(await api.post(`/api/v1/tickets/kb-articles/${id}/publish`)),
  view: async (id: string) => unwrap(await api.post(`/api/v1/tickets/kb-articles/${id}/view`)),
}

// ── Compliance / Cases / GDPR ────────────────────────────────────────────
// Backend: GdprController @ /api/v1/me (operates on the current authenticated user)
export const gdprService = {
  exportEmployee: async () => unwrap(await api.get('/api/v1/me/data')),
  myData: async () => unwrap(await api.get('/api/v1/me/data')),
  eraseEmployee: async (reason: string) => unwrap(await api.post('/api/v1/me/erase', { reason })),
  restrict: async (payload: Record<string, unknown> = {}) => unwrap(await api.post('/api/v1/me/restrict', payload)),
  recordConsent: async (payload: Record<string, unknown>) =>
    unwrap(await api.post('/api/v1/me/consent', payload)),
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
  markDone: async (id: string) => unwrap(await api.post(`/api/v1/compliance/items/${id}/mark-compliant`)),
}

export const licenseService = {
  list: async () => unwrap(await api.get('/api/v1/compliance/licenses')),
  byStatus: async (status: string) => unwrap(await api.get(`/api/v1/compliance/licenses/status/${status}`)),
}

// ── Org / Skills / Travel / Forms / Shifts / CostCenters ────────────────
export const orgChartService = {
  tree: async () => unwrap(await api.get('/api/v1/org-chart')),
  reportsTo: async (employeeId: string) => unwrap(await api.get(`/api/v1/org-chart/${employeeId}`)),
}

// Backend: SkillsController @ /api/v1/skills (employee skills, gap, experts, role reqs)
export const skillsService = {
  gap: async () => unwrap(await api.get('/api/v1/skills/gap')),
  experts: async (skillId: string) => unwrap(await api.get('/api/v1/skills/experts', { params: { skillId } })),
  forEmployee: async (employeeId: string) => unwrap(await api.get(`/api/v1/skills/employees/${employeeId}`)),
  myMatrix: async (employeeId: string) => unwrap(await api.get(`/api/v1/skills/employees/${employeeId}`)),
  rate: async (payload: Record<string, unknown>) => unwrap(await api.post('/api/v1/skills/employees', payload)),
  setRoleRequirement: async (payload: Record<string, unknown>) => unwrap(await api.post('/api/v1/skills/role-requirements', payload)),
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
  byEntity: async (entityType: string) => unwrap(await api.get(`/api/v1/workflows/definitions/entity/${entityType}`)),
  create: async (payload: Record<string, unknown>) => unwrap(await api.post('/api/v1/workflows/definitions', payload)),
  activate: async (id: string) => unwrap(await api.post(`/api/v1/workflows/definitions/${id}/activate`)),
  deactivate: async (id: string) => unwrap(await api.post(`/api/v1/workflows/definitions/${id}/deactivate`)),
}

export const workflowInstanceService = {
  list: async () => unwrap(await api.get('/api/v1/workflows/instances')),
  get: async (id: string) => unwrap(await api.get(`/api/v1/workflows/instances/${id}`)),
}

// Backend: WorkflowDelegationController @ /api/v1/workflows/delegations
export const delegationRuleService = {
  list: async () => unwrap(await api.get('/api/v1/workflows/delegations')),
  byDelegator: async (employeeId: string) => unwrap(await api.get(`/api/v1/workflows/delegations/delegator/${employeeId}`)),
  create: async (payload: Record<string, unknown>) => unwrap(await api.post('/api/v1/workflows/delegations', payload)),
  deactivate: async (id: string) => unwrap(await api.post(`/api/v1/workflows/delegations/${id}/deactivate`)),
}

// ── Onboarding ───────────────────────────────────────────────────────────
export const onboardingTemplateService = {
  list: async () => unwrap(await api.get('/api/v1/onboarding/templates')),
  create: async (payload: Record<string, unknown>) => unwrap(await api.post('/api/v1/onboarding/templates', payload)),
}

export const onboardingTaskService = {
  forEmployee: async (employeeId: string) => unwrap(await api.get(`/api/v1/onboarding/tasks/employee/${employeeId}`)),
  complete: async (taskId: string, payload?: Record<string, unknown>) =>
    unwrap(await api.put(`/api/v1/onboarding/tasks/${taskId}`, { status: 'COMPLETED', ...(payload || {}) })),
}

// Backend: BuddyAssignmentController @ /api/v1/onboarding/buddy-assignments
export const buddyService = {
  forEmployee: async (employeeId: string) => unwrap(await api.get(`/api/v1/onboarding/buddy-assignments/employee/${employeeId}`)),
  get: async (id: string) => unwrap(await api.get(`/api/v1/onboarding/buddy-assignments/${id}`)),
  assign: async (newHireId: string, buddyId: string) =>
    unwrap(await api.post('/api/v1/onboarding/buddy-assignments', { employeeId: newHireId, buddyId })),
}

// ── LMS depth ────────────────────────────────────────────────────────────
// Backend: CertificationController @ /api/v1/courses/certifications
export const certificationService = {
  myCerts: async () => unwrap(await api.get('/api/v1/courses/certifications')),
  list: async () => unwrap(await api.get('/api/v1/courses/certifications')),
  forEmployee: async (employeeId: string) => unwrap(await api.get(`/api/v1/courses/certifications/employee/${employeeId}`)),
  upload: async (payload: Record<string, unknown>) => unwrap(await api.post('/api/v1/courses/certifications', payload)),
  revoke: async (id: string, reason?: string) => unwrap(await api.post(`/api/v1/courses/certifications/${id}/revoke`, { reason })),
}

// Backend: AssessmentController @ /api/v1/courses/assessments
export const assessmentService = {
  forCourse: async (courseId: string) => unwrap(await api.get(`/api/v1/courses/assessments/course/${courseId}`)),
  list: async (courseId: string) => unwrap(await api.get(`/api/v1/courses/assessments/course/${courseId}`)),
  get: async (id: string) => unwrap(await api.get(`/api/v1/courses/assessments/${id}`)),
}

// ── Self-service ── Backend: SelfServiceController @ /api/v1/me ────────────
export const selfServiceService = {
  myEmployee: async () => unwrap(await api.get('/api/v1/me/dashboard')),
  dashboard: async () => unwrap(await api.get('/api/v1/me/dashboard')),
  myTeam: async () => unwrap(await api.get('/api/v1/me/team')),
  myApprovals: async () => unwrap(await api.get('/api/v1/me/approvals')),
}

// ── Background verification ── Backend: BgvController @ /api/v1/recruitment/bgv
export const bgvService = {
  forCandidate: async (candidateId: string) => unwrap(await api.get(`/api/v1/recruitment/bgv/candidate/${candidateId}`)),
  list: async (candidateId?: string) => candidateId
    ? unwrap(await api.get(`/api/v1/recruitment/bgv/candidate/${candidateId}`))
    : [],
  initiate: async (candidateId: string, payload: Record<string, unknown>) =>
    unwrap(await api.post('/api/v1/recruitment/bgv/initiate', { candidateId, ...payload })),
  refresh: async (caseId: string) => unwrap(await api.post(`/api/v1/recruitment/bgv/${caseId}/refresh`)),
}
