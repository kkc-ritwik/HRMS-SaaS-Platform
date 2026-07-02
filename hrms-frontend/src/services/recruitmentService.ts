import { api, unwrap } from '@/lib/api'

export interface Job {
  id: string
  title: string
  departmentId: string
  departmentName?: string
  locationId?: string
  locationName?: string
  employmentType: 'FULL_TIME' | 'PART_TIME' | 'CONTRACT' | 'INTERN'
  experienceMin?: number
  experienceMax?: number
  salaryMin?: number
  salaryMax?: number
  description?: string
  requirements?: string
  status: 'DRAFT' | 'OPEN' | 'CLOSED' | 'ON_HOLD'
  openings: number
  filled: number
  applicationCount?: number
  closingDate?: string
  createdAt: string
}

export interface Candidate {
  id: string
  firstName: string
  lastName: string
  fullName: string
  email: string
  phone?: string
  resume?: string
  skills?: string[]
  experience?: number
  currentCompany?: string
  currentSalary?: number
  expectedSalary?: number
  source?: string
  status: 'NEW' | 'SCREENING' | 'INTERVIEW' | 'OFFER' | 'HIRED' | 'REJECTED'
  createdAt: string
}

export interface Application {
  id: string
  jobId: string
  jobTitle?: string
  candidateId: string
  candidateName?: string
  candidateEmail?: string
  stage: 'APPLIED' | 'SCREENING' | 'PHONE_SCREEN' | 'INTERVIEW' | 'ASSESSMENT' | 'OFFER' | 'HIRED' | 'REJECTED'
  appliedAt: string
  notes?: string
  rating?: number
}

/** Backend: JobController @ /api/v1/jobs, CandidateController @ /api/v1/candidates,
 *  ApplicationController @ /api/v1/applications, InterviewController @ /api/v1/interviews,
 *  OfferController @ /api/v1/offers */
export const recruitmentService = {
  // ── Jobs / Requisitions (/api/v1/jobs) ──────────────────────────────────
  listJobs: async (params?: { status?: string; search?: string; page?: number; size?: number }) =>
    unwrap<Job[]>(await api.get('/api/v1/jobs', { params })),
  getJob: async (id: string) => unwrap<Job>(await api.get(`/api/v1/jobs/${id}`)),
  createJob: async (payload: Partial<Job>) => unwrap<Job>(await api.post('/api/v1/jobs', payload)),
  updateJob: async (id: string, payload: Partial<Job>) => unwrap<Job>(await api.put(`/api/v1/jobs/${id}`, payload)),
  submitJob: async (id: string) => unwrap<Job>(await api.post(`/api/v1/jobs/${id}/submit`)),
  approveJob: async (id: string) => unwrap<Job>(await api.post(`/api/v1/jobs/${id}/approve`)),
  activateJob: async (id: string) => unwrap<Job>(await api.post(`/api/v1/jobs/${id}/activate`)),
  holdJob: async (id: string) => unwrap<Job>(await api.post(`/api/v1/jobs/${id}/hold`)),
  closeJob: async (id: string) => unwrap<Job>(await api.post(`/api/v1/jobs/${id}/close`)),
  cancelJob: async (id: string) => unwrap<Job>(await api.post(`/api/v1/jobs/${id}/cancel`)),

  // ── Candidates (/api/v1/candidates) ──────────────────────────────────────
  listCandidates: async (params?: { search?: string; page?: number; size?: number }) =>
    unwrap<Candidate[]>(await api.get('/api/v1/candidates', { params })),
  getCandidate: async (id: string) => unwrap<Candidate>(await api.get(`/api/v1/candidates/${id}`)),
  createCandidate: async (payload: Partial<Candidate>) => unwrap<Candidate>(await api.post('/api/v1/candidates', payload)),
  updateCandidate: async (id: string, payload: Partial<Candidate>) => unwrap<Candidate>(await api.put(`/api/v1/candidates/${id}`, payload)),
  deleteCandidate: async (id: string) => { await api.delete(`/api/v1/candidates/${id}`) },

  // ── Applications (/api/v1/applications) ──────────────────────────────────
  apply: async (payload: Partial<Application>) => unwrap<Application>(await api.post('/api/v1/applications', payload)),
  getApplication: async (id: string) => unwrap<Application>(await api.get(`/api/v1/applications/${id}`)),
  applicationsByJob: async (jobId: string) => unwrap<Application[]>(await api.get(`/api/v1/applications/job/${jobId}`)),
  applicationsByCandidate: async (candidateId: string) => unwrap<Application[]>(await api.get(`/api/v1/applications/candidate/${candidateId}`)),
  applicationsByStage: async (stage: string) => unwrap<Application[]>(await api.get(`/api/v1/applications/stage/${stage}`)),
  pipeline: async (jobId: string) => unwrap<Array<{ stage: string; count: number }>>(await api.get(`/api/v1/applications/job/${jobId}/pipeline`)),
  moveStage: async (id: string, stage: string, opts?: { rejectionReason?: string; notes?: string }) =>
    unwrap<Application>(await api.post(`/api/v1/applications/${id}/move`, { stage, ...opts })),

  // ── Interviews (/api/v1/interviews) ──────────────────────────────────────
  interviewsForApplication: async (applicationId: string) =>
    unwrap(await api.get(`/api/v1/interviews/application/${applicationId}`)),
  listInterviews: async () => unwrap(await api.get('/api/v1/interviews')),
  getInterview: async (id: string) => unwrap(await api.get(`/api/v1/interviews/${id}`)),
  scheduleInterview: async (payload: Record<string, unknown>) => unwrap(await api.post('/api/v1/interviews', payload)),
  setInterviewStatus: async (id: string, status: string) => unwrap(await api.patch(`/api/v1/interviews/${id}/status`, { status })),
  submitInterviewFeedback: async (id: string, payload: Record<string, unknown>) =>
    unwrap(await api.post(`/api/v1/interviews/${id}/feedback`, payload)),
  addPanelist: async (id: string, payload: { interviewerId: string; role?: string }) =>
    unwrap(await api.post(`/api/v1/interviews/${id}/panelists`, payload)),
  removePanelist: async (id: string, panelistId: string) =>
    unwrap(await api.delete(`/api/v1/interviews/${id}/panelists/${panelistId}`)),
  submitPanelistFeedback: async (id: string, panelistId: string, payload: { rating: number; feedback?: string }) =>
    unwrap(await api.post(`/api/v1/interviews/${id}/panelists/${panelistId}/feedback`, payload)),

  // ── Offers (/api/v1/offers) ──────────────────────────────────────────────
  offersForApplication: async (applicationId: string) => unwrap(await api.get(`/api/v1/offers/application/${applicationId}`)),
  getOffer: async (id: string) => unwrap(await api.get(`/api/v1/offers/${id}`)),
  createOffer: async (payload: Record<string, unknown>) => unwrap(await api.post('/api/v1/offers', payload)),
  sendOffer: async (id: string) => unwrap(await api.post(`/api/v1/offers/${id}/send`)),
  respondOffer: async (id: string, payload: { accepted: boolean; notes?: string }) => unwrap(await api.post(`/api/v1/offers/${id}/respond`, payload)),
  revokeOffer: async (id: string) => unwrap(await api.post(`/api/v1/offers/${id}/revoke`)),
}
