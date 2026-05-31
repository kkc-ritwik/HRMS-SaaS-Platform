import { api, unwrap } from '@/lib/api'

export interface Separation {
  id: string
  employeeId: string
  type: 'RESIGNATION' | 'TERMINATION' | 'RETIREMENT' | 'END_OF_CONTRACT' | 'DEATH'
  reason?: string
  resignationDate: string
  lastWorkingDay: string
  noticePeriodDays: number
  noticeServedDays?: number
  status: 'INITIATED' | 'NOTICE_PERIOD' | 'CLEARANCE' | 'FNF_PROCESSING' | 'COMPLETED' | 'WITHDRAWN'
  withdrawnAt?: string
  exitInterviewStatus?: 'SCHEDULED' | 'COMPLETED' | 'SKIPPED'
}

export interface ClearanceItem {
  department: string
  status: 'PENDING' | 'CLEARED' | 'BLOCKED'
  blockerReason?: string
  clearedBy?: string
  clearedAt?: string
}

export const offboardingService = {
  list: async () => unwrap(await api.get<Separation[]>('/api/v1/offboarding/separations')),
  get: async (id: string) => unwrap(await api.get<Separation>(`/api/v1/offboarding/separations/${id}`)),
  initiate: async (payload: Partial<Separation>) =>
    unwrap(await api.post<Separation>('/api/v1/offboarding/separations', payload)),
  withdraw: async (id: string) =>
    unwrap(await api.post(`/api/v1/offboarding/separations/${id}/withdraw`)),
  clearance: async (id: string) =>
    unwrap(await api.get<ClearanceItem[]>(`/api/v1/offboarding/separations/${id}/clearance`)),
  markCleared: async (id: string, department: string, notes?: string) =>
    unwrap(await api.post(`/api/v1/offboarding/separations/${id}/clearance/${department}`, { notes })),

  // Notice-period buyout
  buyoutCalc: async (payload: {
    resignationDate: string; lastWorkingDay: string; contractualNoticeDays: number;
    monthlyGross: number; waiverPercent?: number; capMonths?: number;
  }) => unwrap(await api.post('/api/v1/offboarding/notice-buyout/calc', payload)),

  // Exit letters
  generateExitLetter: async (type: 'RELIEVING' | 'EXPERIENCE' | 'SERVICE' | 'REFERENCE', payload: Record<string, unknown>) =>
    unwrap(await api.post(`/api/documents/letters/exit/${type}`, payload)),
}
