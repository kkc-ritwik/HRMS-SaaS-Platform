import { api, unwrap } from '@/lib/api'

export interface Asset {
  id: string
  assetTag: string
  name: string
  description?: string
  categoryId?: string
  categoryName?: string
  serialNumber?: string
  model?: string
  manufacturer?: string
  purchaseDate?: string
  purchaseCost?: number
  warrantyExpiry?: string
  status: 'AVAILABLE' | 'ASSIGNED' | 'IN_REPAIR' | 'RETIRED' | 'LOST' | 'STOLEN'
  conditionStatus?: string
  assignedToEmployeeId?: string
  assignedToEmployeeName?: string
  assignedAt?: string
  locationId?: string
  qrCodeUri?: string
  imageUri?: string
}

export interface AssetAssignment {
  id: string
  assetId: string
  employeeId: string
  assignedAt: string
  returnedAt?: string
  conditionOnAssign?: string
  conditionOnReturn?: string
  handoverNotes?: string
}

export interface AssetRequest {
  id: string
  requestedBy: string
  category: string
  reason: string
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'FULFILLED'
  approvedBy?: string
  fulfilledWithAssetId?: string
  requestedAt: string
}

export const assetService = {
  list: async (params: { status?: string; categoryId?: string; search?: string; page?: number; size?: number } = {}) =>
    unwrap(await api.get('/api/v1/assets', { params })),
  get: async (id: string) => unwrap(await api.get<Asset>(`/api/v1/assets/${id}`)),
  create: async (payload: Partial<Asset>) => unwrap(await api.post<Asset>('/api/v1/assets', payload)),
  update: async (id: string, payload: Partial<Asset>) => unwrap(await api.put<Asset>(`/api/v1/assets/${id}`, payload)),
  retire: async (id: string, reason: string) =>
    unwrap(await api.put(`/api/v1/assets/${id}`, { status: 'RETIRED', retireReason: reason })),

  // Assignment / handover
  assign: async (id: string, payload: { employeeId: string; conditionOnAssign?: string; handoverNotes?: string }) =>
    unwrap(await api.post<AssetAssignment>(`/api/v1/assets/${id}/assign`, payload)),
  returnAsset: async (id: string, payload: { conditionOnReturn?: string; notes?: string }) =>
    unwrap(await api.post<AssetAssignment>(`/api/v1/assets/${id}/return`, payload)),

  // Categories
  categories: async () => unwrap(await api.get('/api/v1/assets/categories')),

  // Requests
  myRequests: async (employeeId: string) => unwrap(await api.get<AssetRequest[]>(`/api/v1/assets/requests/employee/${employeeId}`)),
  raiseRequest: async (payload: Partial<AssetRequest>) =>
    unwrap(await api.post<AssetRequest>('/api/v1/assets/requests', payload)),

  // Workspace (desk + floor)
  desks: async (floorId?: string) => unwrap(await api.get('/api/asset/workspace/desks', { params: { floorId } })),
  deskAvailability: async (date: string, floorId?: string) =>
    unwrap(await api.get('/api/asset/workspace/desks/availability', { params: { date, floorId } })),
  bookDesk: async (payload: { deskId: string; employeeId: string; date: string; purpose?: string }) =>
    unwrap(await api.post('/api/asset/workspace/bookings', payload)),
  myBookings: async (employeeId: string, from: string, to: string) =>
    unwrap(await api.get('/api/asset/workspace/bookings/mine', { params: { employeeId, from, to } })),
  cancelBooking: async (id: string) =>
    unwrap(await api.post(`/api/asset/workspace/bookings/${id}/cancel`)),
  floors: async (officeId?: string) =>
    unwrap(await api.get('/api/asset/workspace/floors', { params: { officeId } })),
  seatingMap: async (floorId: string, date: string) =>
    unwrap(await api.get(`/api/asset/workspace/floors/${floorId}/seating-map`, { params: { date } })),

  // Visitors
  visitorsToday: async () => unwrap(await api.get('/api/asset/visitors/today')),
  preRegisterVisitor: async (payload: Record<string, unknown>) =>
    unwrap(await api.post('/api/asset/visitors/pre-register', payload)),
  visitorCheckIn: async (payload: Record<string, unknown>) =>
    unwrap(await api.post('/api/asset/visitors/check-in', payload)),
  visitorCheckOut: async (id: string) =>
    unwrap(await api.post(`/api/asset/visitors/${id}/check-out`)),
}
