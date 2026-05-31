import { api, unwrap } from '@/lib/api'

export interface Holiday {
  id: string
  name: string
  date: string
  type: 'PUBLIC' | 'RESTRICTED' | 'OPTIONAL' | 'COMPANY'
  locationId?: string
  region?: string
  religion?: string
  description?: string
  active: boolean
}

export const holidayService = {
  list: async (year: number, locationId?: string) =>
    unwrap(await api.get<Holiday[]>('/api/v1/holidays', { params: { year, locationId } })),
  upcoming: async (days = 30) =>
    unwrap(await api.get<Holiday[]>('/api/v1/holidays/upcoming', { params: { days } })),
  create: async (payload: Partial<Holiday>) => unwrap(await api.post<Holiday>('/api/v1/holidays', payload)),
  update: async (id: string, payload: Partial<Holiday>) =>
    unwrap(await api.put<Holiday>(`/api/v1/holidays/${id}`, payload)),
  delete: async (id: string) => api.delete(`/api/v1/holidays/${id}`),

  // Optional holidays — employee picks N from M
  optionalQuota: async (employeeId: string, year: number) =>
    unwrap(await api.get('/api/v1/holidays/optional/quota', { params: { employeeId, year } })),
  selectOptional: async (employeeId: string, holidayIds: string[]) =>
    unwrap(await api.post('/api/v1/holidays/optional/select', { employeeId, holidayIds })),
}
