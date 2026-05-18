import { recruitmentClient } from '@/lib/api'

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

export const recruitmentService = {
  // Jobs
  listJobs: async (params?: { status?: string; search?: string; page?: number; pageSize?: number }) => {
    const response = await recruitmentClient.get('/api/v1/jobs', { params })
    return response.data
  },
  getJob: async (id: string) => {
    const response = await recruitmentClient.get(`/api/v1/jobs/${id}`)
    return response.data
  },
  createJob: async (payload: Partial<Job>) => {
    const response = await recruitmentClient.post('/api/v1/jobs', payload)
    return response.data
  },
  updateJob: async (id: string, payload: Partial<Job>) => {
    const response = await recruitmentClient.put(`/api/v1/jobs/${id}`, payload)
    return response.data
  },
  deleteJob: async (id: string) => {
    await recruitmentClient.delete(`/api/v1/jobs/${id}`)
  },

  // Candidates
  listCandidates: async (params?: { search?: string; page?: number; pageSize?: number }) => {
    const response = await recruitmentClient.get('/api/v1/candidates', { params })
    return response.data
  },
  getCandidate: async (id: string) => {
    const response = await recruitmentClient.get(`/api/v1/candidates/${id}`)
    return response.data
  },
  createCandidate: async (payload: Partial<Candidate>) => {
    const response = await recruitmentClient.post('/api/v1/candidates', payload)
    return response.data
  },

  // Applications
  listApplications: async (params?: { jobId?: string; candidateId?: string; stage?: string; page?: number; pageSize?: number }) => {
    const response = await recruitmentClient.get('/api/v1/applications', { params })
    return response.data
  },
  updateApplicationStage: async (id: string, stage: string) => {
    const response = await recruitmentClient.patch(`/api/v1/applications/${id}/stage`, { stage })
    return response.data
  },
}
