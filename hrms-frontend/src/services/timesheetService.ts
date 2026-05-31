import { api, unwrap } from '@/lib/api'

export interface WeeklyTimesheet {
  id: string
  employeeId: string
  weekStart: string
  totalHours: number
  billableHours: number
  status: 'DRAFT' | 'SUBMITTED' | 'APPROVED' | 'REJECTED'
  submittedAt?: string
  approverId?: string
  approvedAt?: string
}

export interface TimesheetEntry {
  id: string
  employeeId: string
  projectId: string
  taskId?: string
  workDate: string
  hours: number
  isBillable?: boolean
  notes?: string
  status: 'DRAFT' | 'SUBMITTED' | 'APPROVED' | 'REJECTED'
}

export interface Project {
  id: string
  code: string
  name: string
  clientName?: string
  startDate?: string
  endDate?: string
  status: 'PLANNING' | 'ACTIVE' | 'ON_HOLD' | 'COMPLETED' | 'CANCELLED'
  budgetHours?: number
  consumedHours?: number
  hourlyRate?: number
  currency?: string
}

export interface ProjectTask {
  id: string
  projectId: string
  name: string
  billable?: boolean
}

/** Backend: TimesheetController @ /api/v1/timesheet (singular),
 *  JobCostController @ /api/v1/timesheet/reports/job-cost */
export const timesheetService = {
  // ── Entries ─────────────────────────────────────────────────────────────
  logEntry: async (payload: Partial<TimesheetEntry>) =>
    unwrap<TimesheetEntry>(await api.post('/api/v1/timesheet/entries', payload)),
  entries: async (employeeId: string, from: string, to: string) =>
    unwrap<TimesheetEntry[]>(await api.get('/api/v1/timesheet/entries', { params: { employeeId, from, to } })),

  // ── Weekly submission / approval ─────────────────────────────────────────
  submitWeek: async (employeeId: string, weekStart: string) =>
    unwrap<WeeklyTimesheet>(await api.post('/api/v1/timesheet/weeks/submit', null, { params: { employeeId, weekStart } })),
  approveWeek: async (id: string, approverId: string) =>
    unwrap<WeeklyTimesheet>(await api.post(`/api/v1/timesheet/weeks/${id}/approve`, null, { params: { approverId } })),

  // ── Projects & tasks (create only) ───────────────────────────────────────
  createProject: async (payload: Partial<Project>) =>
    unwrap<Project>(await api.post('/api/v1/timesheet/projects', payload)),
  createTask: async (payload: Partial<ProjectTask>) =>
    unwrap<ProjectTask>(await api.post('/api/v1/timesheet/tasks', payload)),

  // ── Job-cost reports ─────────────────────────────────────────────────────
  jobCostForProject: async (projectId: string) =>
    unwrap(await api.get(`/api/v1/timesheet/reports/job-cost/project/${projectId}`)),
  jobCostTenantSummary: async () =>
    unwrap(await api.get('/api/v1/timesheet/reports/job-cost/tenant-summary')),
}
