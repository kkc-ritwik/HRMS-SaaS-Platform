import { api, unwrap } from '@/lib/api'

export interface TripRequest {
  id: string
  employeeId: string
  purpose: string
  tripType: 'DOMESTIC' | 'INTERNATIONAL' | 'LOCAL'
  fromLocation?: string
  toLocation?: string
  departureDate?: string
  returnDate?: string
  estimatedCost?: number
  currency?: string
  status: 'DRAFT' | 'SUBMITTED' | 'APPROVED' | 'REJECTED' | 'CANCELLED' | 'COMPLETED'
  approverId?: string
  approvedAt?: string
  rejectionReason?: string
  clientBillable?: boolean
  projectCode?: string
}

export interface TripItinerary {
  id: string
  tripId: string
  legType?: string
  fromLocation?: string
  toLocation?: string
  departAt?: string
  arriveAt?: string
  carrier?: string
  bookingReference?: string
  cost?: number
}

export interface TravelAdvance {
  id: string
  tripId: string
  employeeId: string
  amount: number
  currency?: string
  status: 'REQUESTED' | 'DISBURSED' | 'SETTLED'
}

export interface MileageClaim {
  id: string
  employeeId: string
  tripId?: string
  distanceKm: number
  ratePerKm: number
  amount?: number
  claimDate?: string
}

export interface PerDiemRate {
  id: string
  country: string
  city?: string
  dailyRate: number
  currency: string
}

/** Backend: TravelController @ /api/v1/travel (trips/itinerary/advances/mileage/per-diem) */
export const travelService = {
  // ── Trips ────────────────────────────────────────────────────────────────
  myTrips: async (employeeId: string): Promise<TripRequest[]> => {
    const res = unwrap<{ content?: TripRequest[] } | TripRequest[]>(await api.get('/api/v1/travel/trips/me', { params: { employeeId } }))
    return Array.isArray(res) ? res : (res?.content ?? [])
  },
  getTrip: async (id: string) => unwrap<TripRequest>(await api.get(`/api/v1/travel/trips/${id}`)),
  createTrip: async (payload: Partial<TripRequest>) =>
    unwrap<TripRequest>(await api.post('/api/v1/travel/trips', payload)),
  submitTrip: async (id: string) => unwrap<TripRequest>(await api.post(`/api/v1/travel/trips/${id}/submit`)),
  approveTrip: async (id: string, approverId: string) =>
    unwrap<TripRequest>(await api.post(`/api/v1/travel/trips/${id}/approve`, null, { params: { approverId } })),
  rejectTrip: async (id: string, reason: string) =>
    unwrap<TripRequest>(await api.post(`/api/v1/travel/trips/${id}/reject`, null, { params: { reason } })),

  // ── Itinerary ──────────────────────────────────────────────────────────────
  itinerary: async (tripId: string) =>
    unwrap<TripItinerary[]>(await api.get(`/api/v1/travel/itinerary/${tripId}`)),
  addLeg: async (payload: Partial<TripItinerary>) =>
    unwrap<TripItinerary>(await api.post('/api/v1/travel/itinerary', payload)),

  // ── Advances ────────────────────────────────────────────────────────────────
  requestAdvance: async (payload: Partial<TravelAdvance>) =>
    unwrap<TravelAdvance>(await api.post('/api/v1/travel/advances', payload)),
  disburseAdvance: async (id: string) =>
    unwrap<TravelAdvance>(await api.post(`/api/v1/travel/advances/${id}/disburse`)),

  // ── Mileage & per-diem ───────────────────────────────────────────────────────
  claimMileage: async (payload: Partial<MileageClaim>) =>
    unwrap<MileageClaim>(await api.post('/api/v1/travel/mileage', payload)),
  perDiemRates: async (country: string) =>
    unwrap<PerDiemRate[]>(await api.get(`/api/v1/travel/per-diem/${country}`)),
}
