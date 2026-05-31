import { api, unwrap } from '@/lib/api'

export interface AuditLog {
  id: string
  entityName: string
  entityId: string
  action: string
  actorId?: string
  actorEmail?: string
  ipAddress?: string
  userAgent?: string
  requestId?: string
  beforeValue?: Record<string, unknown>
  afterValue?: Record<string, unknown>
  changedFields?: Record<string, unknown>
  createdAt: string
}

export interface PoshSummary {
  totalReceived: number
  disposed: number
  pendingMoreThan90Days: number
  critical: number
  anonymous: number
  byStatus: Record<string, number>
}

export interface DiversityHeadcount { group: string; count: number; percent: number }

export const complianceService = {
  // Audit
  auditByEntity: async (entityName: string, entityId: string) =>
    unwrap(await api.get<AuditLog[]>('/api/v1/audit', { params: { entityName, entityId } })),
  auditByActor: async (actorId: string, from?: string, to?: string) =>
    unwrap(await api.get<AuditLog[]>('/api/v1/audit/actor', { params: { actorId, from, to } })),
  searchAudit: async (params: { from?: string; to?: string; entityName?: string; actorId?: string; page?: number; size?: number }) =>
    unwrap(await api.get('/api/v1/audit/search', { params })),

  // POSH
  poshPreview: async (year: number) =>
    unwrap(await api.get<PoshSummary>('/api/cases/posh/annual-return/preview', { params: { year } })),
  poshGenerate: async (payload: { year: number; employerName: string; employerAddress: string; iccChairperson: string; iccMembers: string[]; workshopsConducted: number; workshopAttendance: number; natureOfAction: string }) =>
    unwrap(await api.post('/api/cases/posh/annual-return/generate', payload)),

  // GDPR
  exportEmployee: async (employeeId: string) =>
    unwrap(await api.get(`/api/v1/gdpr/export/${employeeId}`)),
  eraseEmployee: async (employeeId: string, reason: string) =>
    unwrap(await api.post(`/api/v1/gdpr/erase/${employeeId}`, { reason })),
  restrictProcessing: async (employeeId: string) =>
    unwrap(await api.post(`/api/v1/gdpr/restrict/${employeeId}`)),
  recordConsent: async (employeeId: string, payload: Record<string, unknown>) =>
    unwrap(await api.post(`/api/v1/gdpr/consent/${employeeId}`, payload)),

  // D&I
  deiHeadcount: async (by: 'gender' | 'ethnicity' | 'generation' | 'disability' | 'nationality') =>
    unwrap(await api.get<DiversityHeadcount[]>('/api/reports/dei/headcount', { params: { by } })),
  deiLeadership: async () => unwrap(await api.get('/api/reports/dei/leadership-representation')),
  deiHiringFunnel: async (year: number) =>
    unwrap(await api.get('/api/reports/dei/hiring-funnel', { params: { year } })),
  deiAttrition: async (year: number, by = 'gender') =>
    unwrap(await api.get('/api/reports/dei/attrition-by-group', { params: { year, by } })),
  deiPayGap: async (by = 'gender') =>
    unwrap(await api.get('/api/reports/dei/pay-gap', { params: { by } })),

  // Cases (grievance, POSH)
  listCases: async (params: { status?: string; type?: string; page?: number; size?: number } = {}) =>
    unwrap(await api.get('/api/v1/cases', { params })),
  createCase: async (payload: Record<string, unknown>) =>
    unwrap(await api.post('/api/v1/cases', payload)),
}
