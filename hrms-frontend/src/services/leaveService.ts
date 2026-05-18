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
  employeeName: string
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
  employeeName: string
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
  type: 'NATIONAL' | 'REGIONAL' | 'OPTIONAL'
  description?: string
}

export const leaveService = {
  // Leave Types
  listLeaveTypes: async () => {
    const response = await leaveClient.get('/api/v1/leave-types')
    return response.data
  },
  createLeaveType: async (payload: Partial<LeaveType>) => {
    const response = await leaveClient.post('/api/v1/leave-types', payload)
    return response.data
  },
  updateLeaveType: async (id: string, payload: Partial<LeaveType>) => {
    const response = await leaveClient.put(`/api/v1/leave-types/${id}`, payload)
    return response.data
  },
  deleteLeaveType: async (id: string) => {
    await leaveClient.delete(`/api/v1/leave-types/${id}`)
  },

  // Leave Balances
  listBalances: async (params?: { employeeId?: string; year?: number; page?: number; pageSize?: number }) => {
    const response = await leaveClient.get('/api/v1/leave-balances', { params })
    return response.data
  },

  // Leave Applications
  listApplications: async (params?: {
    status?: string
    employeeId?: string
    startDate?: string
    endDate?: string
    page?: number
    pageSize?: number
  }) => {
    const response = await leaveClient.get('/api/v1/leave-applications', { params })
    return response.data
  },
  applyLeave: async (payload: {
    leaveTypeId: string
    startDate: string
    endDate: string
    reason: string
  }) => {
    const response = await leaveClient.post('/api/v1/leave-applications', payload)
    return response.data
  },
  approveLeave: async (id: string, comment?: string) => {
    const response = await leaveClient.post(`/api/v1/leave-applications/${id}/approve`, { comment })
    return response.data
  },
  rejectLeave: async (id: string, reason: string) => {
    const response = await leaveClient.post(`/api/v1/leave-applications/${id}/reject`, { reason })
    return response.data
  },
  cancelLeave: async (id: string) => {
    const response = await leaveClient.post(`/api/v1/leave-applications/${id}/cancel`)
    return response.data
  },

  // Holidays
  listHolidays: async (params?: { year?: number }) => {
    const response = await leaveClient.get('/api/v1/holidays', { params })
    return response.data
  },
  createHoliday: async (payload: Partial<Holiday>) => {
    const response = await leaveClient.post('/api/v1/holidays', payload)
    return response.data
  },
  deleteHoliday: async (id: string) => {
    await leaveClient.delete(`/api/v1/holidays/${id}`)
  },
}
