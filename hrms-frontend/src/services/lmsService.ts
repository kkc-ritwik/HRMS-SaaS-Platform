import { api, unwrap } from '@/lib/api'

export interface Course {
  id: string
  title: string
  description?: string
  category?: string
  level?: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED'
  durationHours?: number
  mode?: 'SELF_PACED' | 'INSTRUCTOR_LED' | 'BLENDED'
  thumbnailUri?: string
  contentUri?: string
  active: boolean
}

export interface Enrollment {
  id: string
  courseId: string
  courseTitle?: string
  employeeId: string
  status: 'ENROLLED' | 'IN_PROGRESS' | 'COMPLETED' | 'DROPPED'
  progressPercent: number
  startedAt?: string
  completedAt?: string
  certificateUri?: string
  score?: number
}

export interface Certification {
  id: string
  employeeId: string
  certificateName: string
  issuedBy?: string
  issueDate: string
  expiryDate?: string
  certificateUrl?: string
  status: 'ACTIVE' | 'EXPIRED' | 'REVOKED'
}

export const lmsService = {
  listCourses: async (params: { search?: string; category?: string; page?: number; size?: number } = {}) =>
    unwrap(await api.get('/api/v1/courses', { params })),
  getCourse: async (id: string) => unwrap(await api.get<Course>(`/api/v1/courses/${id}`)),
  createCourse: async (payload: Partial<Course>) => unwrap(await api.post<Course>('/api/v1/courses', payload)),

  myEnrollments: async () => unwrap(await api.get<Enrollment[]>('/api/v1/learning/enrollments/me')),
  enroll: async (courseId: string) => unwrap(await api.post<Enrollment>('/api/v1/learning/enroll', { courseId })),
  updateProgress: async (id: string, progressPercent: number) =>
    unwrap(await api.patch<Enrollment>(`/api/v1/learning/enrollments/${id}/progress`, { progressPercent })),
  complete: async (id: string, score?: number) =>
    unwrap(await api.post<Enrollment>(`/api/v1/learning/enrollments/${id}/complete`, { score })),

  listCertifications: async (employeeId?: string) =>
    unwrap(await api.get<Certification[]>('/api/v1/certifications', { params: { employeeId } })),
  myCertifications: async () => unwrap(await api.get<Certification[]>('/api/v1/certifications/me')),
  uploadCertification: async (payload: Partial<Certification>) =>
    unwrap(await api.post<Certification>('/api/v1/certifications', payload)),
}
