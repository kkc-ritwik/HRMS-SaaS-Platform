import { api, unwrap } from '@/lib/api'

export interface ReportDefinition {
  id: string
  name: string
  description?: string
  module: string
  sql?: string
  parameters?: Array<{ name: string; type: string; required: boolean; defaultValue?: string }>
  visualisation?: 'TABLE' | 'BAR' | 'LINE' | 'PIE' | 'STACKED_BAR'
  createdAt: string
}

export interface SavedReport {
  id: string
  definitionId: string
  name: string
  parameters: Record<string, unknown>
  ownerId: string
  isShared: boolean
}

export interface Dashboard {
  id: string
  name: string
  description?: string
  ownerId: string
  widgets: DashboardWidget[]
}

export interface DashboardWidget {
  id: string
  reportId: string
  title: string
  type: 'STAT' | 'CHART' | 'TABLE'
  position: { x: number; y: number; w: number; h: number }
}

export const reportsService = {
  // Definitions
  listDefinitions: async () => unwrap(await api.get<ReportDefinition[]>('/api/v1/reports/definitions')),
  getDefinition: async (id: string) => unwrap(await api.get<ReportDefinition>(`/api/v1/reports/definitions/${id}`)),
  runReport: async (id: string, parameters: Record<string, unknown>) =>
    unwrap(await api.post(`/api/v1/reports/${id}/run`, parameters)),
  exportReport: async (id: string, parameters: Record<string, unknown>, format: 'csv' | 'xlsx' | 'pdf') => {
    const res = await api.post(`/api/v1/reports/${id}/export`, parameters, { params: { format }, responseType: 'blob' })
    return res.data as Blob
  },

  // Saved reports
  mySaved: async () => unwrap(await api.get<SavedReport[]>('/api/v1/reports/saved')),
  saveReport: async (payload: Partial<SavedReport>) =>
    unwrap(await api.post<SavedReport>('/api/v1/reports/saved', payload)),
  shareReport: async (id: string, userIds: string[]) =>
    unwrap(await api.post(`/api/v1/reports/saved/${id}/share`, { userIds })),

  // Dashboards — Backend: DashboardController @ /api/v1/reports/dashboards
  listDashboards: async () => unwrap(await api.get<Dashboard[]>('/api/v1/reports/dashboards/me')),
  sharedDashboards: async () => unwrap(await api.get<Dashboard[]>('/api/v1/reports/dashboards/shared')),
  getDashboard: async (id: string) => unwrap(await api.get<Dashboard>(`/api/v1/reports/dashboards/${id}`)),
  createDashboard: async (payload: Partial<Dashboard>) =>
    unwrap(await api.post<Dashboard>('/api/v1/reports/dashboards', payload)),
  updateDashboard: async (id: string, payload: Partial<Dashboard>) =>
    unwrap(await api.put<Dashboard>(`/api/v1/reports/dashboards/${id}`, payload)),
  deleteDashboard: async (id: string) => { await api.delete(`/api/v1/reports/dashboards/${id}`) },
  setDefaultDashboard: async (id: string) => unwrap(await api.post(`/api/v1/reports/dashboards/${id}/set-default`)),

  // Schedules
  schedule: async (reportId: string, cron: string, recipients: string[]) =>
    unwrap(await api.post(`/api/v1/reports/${reportId}/schedule`, { cronExpression: cron, recipients, format: 'CSV' })),
}
