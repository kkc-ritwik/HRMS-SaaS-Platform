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
  // Approvals are workflow instances awaiting the current user.
  myApprovals: async () => unwrap(await api.get<ApprovalRequest[]>('/api/v1/me/approvals')),
  approvalsForMe: async () => unwrap(await api.get<ApprovalRequest[]>('/api/v1/me/approvals')),
  approve: async (id: string, notes?: string) =>
    unwrap(await api.post(`/api/v1/workflows/instances/${id}/approve`, { notes })),
  reject: async (id: string, reason: string) =>
    unwrap(await api.post(`/api/v1/workflows/instances/${id}/reject`, { reason })),
  cancel: async (id: string, reason?: string) =>
    unwrap(await api.post(`/api/v1/workflows/instances/${id}/cancel`, { reason })),
  escalate: async (id: string, reason?: string) =>
    unwrap(await api.post(`/api/v1/workflows/instances/${id}/reject`, { reason, escalated: true })),

  // OOO delegation — Backend: OutOfOfficeController @ /api/v1/workflow/ooo
  myOOO: async (userId: string) =>
    unwrap(await api.get<OutOfOffice[]>(`/api/v1/workflow/ooo/user/${userId}`)),
  scheduleOOO: async (payload: Partial<OutOfOffice>) =>
    unwrap(await api.post<OutOfOffice>('/api/v1/workflow/ooo', payload)),
  cancelOOO: async (id: string) =>
    unwrap(await api.post(`/api/v1/workflow/ooo/${id}/cancel`)),

  // Workflow definitions double as templates.
  templates: async () => unwrap(await api.get('/api/v1/workflows/definitions')),
  saveTemplate: async (payload: Record<string, unknown>) =>
    unwrap(await api.post('/api/v1/workflows/definitions', payload)),
}
