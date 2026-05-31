import { api, unwrap } from '@/lib/api'

export type ApprovalState = 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED' | 'ESCALATED' | 'AUTO_APPROVED'

export interface ApprovalRequest {
  id: string
  type: string                       // LEAVE / EXPENSE / TIMESHEET / TICKET / etc.
  subjectId: string
  initiatorId: string
  initiatorName?: string
  currentApproverId?: string
  currentApproverName?: string
  state: ApprovalState
  slaDueAt?: string
  createdAt: string
  approvedAt?: string
  payload?: Record<string, unknown>
}

export interface OutOfOffice {
  id: string
  userId: string
  delegateId: string
  reason?: string
  startDate: string
  endDate: string
  autoReplyMsg?: string
  active: boolean
}

export const workflowService = {
  myApprovals: async () => unwrap(await api.get<ApprovalRequest[]>('/api/v1/approvals/me')),
  approvalsForMe: async () => unwrap(await api.get<ApprovalRequest[]>('/api/v1/approvals/pending')),
  approve: async (id: string, notes?: string) =>
    unwrap(await api.post(`/api/v1/approvals/${id}/approve`, { notes })),
  reject: async (id: string, reason: string) =>
    unwrap(await api.post(`/api/v1/approvals/${id}/reject`, { reason })),
  escalate: async (id: string, toUserId: string) =>
    unwrap(await api.post(`/api/v1/approvals/${id}/escalate`, { toUserId })),

  // OOO delegation
  myOOO: async (userId: string) =>
    unwrap(await api.get<OutOfOffice[]>('/api/v1/workflow/out-of-office', { params: { userId } })),
  scheduleOOO: async (payload: Partial<OutOfOffice>) =>
    unwrap(await api.post<OutOfOffice>('/api/v1/workflow/out-of-office', payload)),
  cancelOOO: async (id: string) =>
    unwrap(await api.delete(`/api/v1/workflow/out-of-office/${id}`)),

  // Templates
  templates: async () => unwrap(await api.get('/api/v1/workflows/templates')),
  saveTemplate: async (payload: Record<string, unknown>) =>
    unwrap(await api.post('/api/v1/workflows/templates', payload)),
}
