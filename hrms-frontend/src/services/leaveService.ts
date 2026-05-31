import { leaveClient } from '@/lib/api'

export interface LeaveType {
  id: string
  name: string
  code: string
  description?: string
  daysAllowed: number
  carryForward: boolean
  carryForwardLimit?: number
  isPaid: boolean
  applicableGender?: 'MALE' | 'FEMALE' | 'ALL'
  requiresApproval: boolean
  color?: string
  createdAt: string
}

export interface LeaveBalance {
  id: string
  employeeId: string
  employeeName?: string
  leaveTypeId: string
  leaveTypeName: string
  year: number
  allocated: number
  used: number
  pending: number
  remaining: number
}

export interface LeaveApplication {
  id: string
  employeeId: string
  employeeName?: string
  employeeAvatar?: string
  leaveTypeId: string
  leaveTypeName: string
  startDate: string
  endDate: string
  days: number
  reason: string
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED'
  approverId?: string
  approverName?: string
  approvedAt?: string
  rejectionReason?: string
  createdAt: string
}

export interface Holiday {
  id: string
  name: string
  date: string
  type: 'NATIONAL' | 'REGIONAL' | 'OPTIONAL' | 'PUBLIC' | 'RESTRICTED' | 'COMPANY'
  description?: string
}

/** Backend: LeaveApplicationController @ /api/v1/leaves, LeaveTypeController @ /api/v1/leaves/types,
 *  LeaveBalanceController @ /api/v1/leaves/balance, HolidayController @ /api/v1/holidays */
export const leaveService = {
  // ── Leave Types (/api/v1/leaves/types) ──────────────────────────────────
  listLeaveTypes: async () => (await leaveClient.get('/api/v1/leaves/types')).data,
  listActiveLeaveTypes: async () => (await leaveClient.get('/api/v1/leaves/types/active')).data,
  getLeaveType: async (id: string) => (await leaveClient.get(`/api/v1/leaves/types/${id}`)).data,
  createLeaveType: async (payload: Partial<LeaveType>) => (await leaveClient.post('/api/v1/leaves/types', payload)).data,
  updateLeaveType: async (id: string, payload: Partial<LeaveType>) => (await leaveClient.put(`/api/v1/leaves/types/${id}`, payload)).data,
  deleteLeaveType: async (id: string) => { await leaveClient.delete(`/api/v1/leaves/types/${id}`) },

  // ── Leave Balances (/api/v1/leaves/balance) ─────────────────────────────
  myBalances: async () => (await leaveClient.get('/api/v1/leaves/balance/my')).data,
  balancesForEmployee: async (employeeId: string) => (await leaveClient.get(`/api/v1/leaves/balance/${employeeId}`)).data,
  initBalance: async (payload: Record<string, unknown>) => (await leaveClient.post('/api/v1/leaves/balance/init', payload)).data,
  adjustBalance: async (payload: Record<string, unknown>) => (await leaveClient.post('/api/v1/leaves/balance/adjust', payload)).data,

  // ── Leave Applications (/api/v1/leaves) ─────────────────────────────────
  myLeaves: async () => (await leaveClient.get('/api/v1/leaves/my')).data,
  teamLeaves: async () => (await leaveClient.get('/api/v1/leaves/team')).data,
  teamCalendar: async (from?: string, to?: string) =>
    (await leaveClient.get('/api/v1/leaves/team-calendar', { params: { from, to } })).data,
  applyLeave: async (payload: { leaveTypeId: string; startDate: string; endDate: string; reason: string; halfDay?: boolean }) =>
    (await leaveClient.post('/api/v1/leaves/apply', payload)).data,
  approveLeave: async (id: string, comment?: string) =>
    (await leaveClient.post(`/api/v1/leaves/${id}/approve`, { comment })).data,
  rejectLeave: async (id: string, reason: string) =>
    (await leaveClient.post(`/api/v1/leaves/${id}/reject`, { reason })).data,
  cancelLeave: async (id: string) =>
    (await leaveClient.post(`/api/v1/leaves/${id}/cancel`)).data,

  // Aliases used by older list pages
  listApplications: async (params?: { status?: string; employeeId?: string; page?: number; pageSize?: number }) =>
    (await leaveClient.get('/api/v1/leaves/team', { params })).data,
  listBalances: async (_params?: { employeeId?: string; page?: number; pageSize?: number }) =>
    (await leaveClient.get('/api/v1/leaves/balance/my')).data,

  // ── Holidays (/api/v1/holidays) ─────────────────────────────────────────
  listHolidays: async (params?: { year?: number }) => (await leaveClient.get('/api/v1/holidays', { params })).data,
  holidaysByLocation: async (locationId: string, year?: number) =>
    (await leaveClient.get('/api/v1/holidays/by-location', { params: { locationId, year } })).data,
  getHoliday: async (id: string) => (await leaveClient.get(`/api/v1/holidays/${id}`)).data,
  createHoliday: async (payload: Partial<Holiday>) => (await leaveClient.post('/api/v1/holidays', payload)).data,
  updateHoliday: async (id: string, payload: Partial<Holiday>) => (await leaveClient.put(`/api/v1/holidays/${id}`, payload)).data,
  deleteHoliday: async (id: string) => { await leaveClient.delete(`/api/v1/holidays/${id}`) },

  // ── Leave Policies (/api/v1/leaves/policies) ────────────────────────────
  listPolicies: async () => (await leaveClient.get('/api/v1/leaves/policies')).data,
  getPolicy: async (id: string) => (await leaveClient.get(`/api/v1/leaves/policies/${id}`)).data,
  createPolicy: async (payload: Record<string, unknown>) => (await leaveClient.post('/api/v1/leaves/policies', payload)).data,
  updatePolicy: async (id: string, payload: Record<string, unknown>) => (await leaveClient.put(`/api/v1/leaves/policies/${id}`, payload)).data,
  deletePolicy: async (id: string) => { await leaveClient.delete(`/api/v1/leaves/policies/${id}`) },
}
