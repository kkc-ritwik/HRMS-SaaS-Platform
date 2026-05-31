import { api, unwrap } from '@/lib/api'

export interface Shift {
  id: string
  name: string
  code?: string
  startTime: string
  endTime: string
  breakMinutes?: number
  graceMinutes?: number
  weeklyOffDays?: string[]
  active?: boolean
}

/** Shifts @ /api/v1/shifts */
export const shiftService = {
  list: async () => unwrap<Shift[]>(await api.get('/api/v1/shifts')),
  active: async () => unwrap<Shift[]>(await api.get('/api/v1/shifts/active')),
  get: async (id: string) => unwrap<Shift>(await api.get(`/api/v1/shifts/${id}`)),
  create: async (payload: Partial<Shift>) => unwrap<Shift>(await api.post('/api/v1/shifts', payload)),
  update: async (id: string, payload: Partial<Shift>) => unwrap<Shift>(await api.put(`/api/v1/shifts/${id}`, payload)),
  remove: async (id: string) => { await api.delete(`/api/v1/shifts/${id}`) },
  assign: async (shiftId: string, payload: { employeeIds: string[]; fromDate?: string; toDate?: string }) =>
    unwrap(await api.post(`/api/v1/shifts/${shiftId}/assign`, payload)),
}
