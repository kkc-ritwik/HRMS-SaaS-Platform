import { api, unwrap } from '@/lib/api'

export type AttendanceStatus = 'PRESENT' | 'ABSENT' | 'HALF_DAY' | 'ON_LEAVE' | 'HOLIDAY' | 'WEEK_OFF'

export interface PunchResponse {
  punchId: string
  employeeId: string
  punchTime: string
  type: 'CHECK_IN' | 'CHECK_OUT'
  source?: string
  currentStatus: AttendanceStatus
  firstCheckIn?: string
  lastCheckOut?: string
  effectiveHoursToday?: number
  lateByMins?: number
  message?: string
}

export interface AttendanceRecord {
  id: string
  employeeId: string
  attendanceDate: string
  shiftName?: string
  firstCheckIn?: string
  lastCheckOut?: string
  totalHours?: number
  effectiveHours?: number
  breakHours?: number
  overtimeHours?: number
  status: AttendanceStatus
  lateByMins?: number
  earlyLeavingMins?: number
  source?: string
  regularized?: boolean
}

export interface MonthSummary {
  year: number
  month: number
  employeeId: string
  presentDays: number
  absentDays: number
  halfDays: number
  leaveDays: number
  holidayDays: number
  weekOffDays: number
  totalOvertimeHours?: number
  records: AttendanceRecord[]
}

export interface Regularization {
  id: string
  employeeId: string
  attendanceDate: string
  requestedCheckIn?: string
  requestedCheckOut?: string
  reason: string
  status: 'PENDING' | 'APPROVED' | 'REJECTED'
}

/** Backend: AttendanceController @ /api/v1/attendance */
export const attendanceService = {
  // ── Punch ────────────────────────────────────────────────────────────────
  punch: async (payload?: { source?: string; latitude?: number; longitude?: number }) =>
    unwrap<PunchResponse>(await api.post('/api/v1/attendance/punch', payload ?? { source: 'WEB' })),

  // ── My attendance ──────────────────────────────────────────────────────────
  myLog: async (year: number, month: number) =>
    unwrap<MonthSummary>(await api.get('/api/v1/attendance/my-log', { params: { year, month } })),
  myRecords: async (params?: { page?: number; size?: number }) =>
    unwrap<AttendanceRecord[]>(await api.get('/api/v1/attendance/my-records', { params })),

  // ── Team / dashboard ─────────────────────────────────────────────────────────
  team: async (employeeIds: string[], date?: string) =>
    unwrap(await api.get('/api/v1/attendance/team', { params: { employeeIds, date } })),
  dashboard: async (date?: string) =>
    unwrap(await api.get('/api/v1/attendance/dashboard', { params: { date } })),

  // ── Regularization ─────────────────────────────────────────────────────────
  myRegularizations: async (params?: { page?: number; size?: number }) =>
    unwrap<Regularization[]>(await api.get('/api/v1/attendance/regularize', { params })),
  pendingRegularizations: async () =>
    unwrap<Regularization[]>(await api.get('/api/v1/attendance/regularize/pending')),
  requestRegularization: async (payload: Partial<Regularization>) =>
    unwrap<Regularization>(await api.post('/api/v1/attendance/regularize', payload)),
  approveRegularization: async (id: string) =>
    unwrap<Regularization>(await api.post(`/api/v1/attendance/regularize/${id}/approve`)),
  rejectRegularization: async (id: string, reason?: string) =>
    unwrap<Regularization>(await api.post(`/api/v1/attendance/regularize/${id}/reject`, { reason })),

  // ── Biometric ingestion (device integrations) ──────────────────────────────
  biometricPunch: async (payload: Record<string, unknown>) =>
    unwrap(await api.post('/api/v1/attendance/biometric/punch', payload)),
  biometricPunchBulk: async (payload: Record<string, unknown>[]) =>
    unwrap(await api.post('/api/v1/attendance/biometric/punch/bulk', payload)),
}
