import { performanceClient } from '@/lib/api'

export interface Goal {
  id: string
  title: string
  description?: string
  employeeId: string
  employeeName?: string
  category: 'INDIVIDUAL' | 'TEAM' | 'COMPANY'
  status: 'DRAFT' | 'ACTIVE' | 'COMPLETED' | 'CANCELLED'
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'
  progress: number
  startDate: string
  endDate: string
  assignedById?: string
  createdAt: string
}

export interface Review {
  id: string
  title: string
  employeeId: string
  employeeName?: string
  reviewerId: string
  reviewerName?: string
  period: string
  status: 'SCHEDULED' | 'IN_PROGRESS' | 'COMPLETED'
  overallRating?: number
  selfRating?: number
  managerRating?: number
  feedback?: string
  scheduledDate: string
  completedDate?: string
  createdAt: string
}

export const performanceService = {
  // Goals
  listGoals: async (params?: { employeeId?: string; status?: string; page?: number; pageSize?: number }) => {
    const response = await performanceClient.get('/api/v1/goals', { params })
    return response.data
  },
  createGoal: async (payload: Partial<Goal>) => {
    const response = await performanceClient.post('/api/v1/goals', payload)
    return response.data
  },
  updateGoal: async (id: string, payload: Partial<Goal>) => {
    const response = await performanceClient.put(`/api/v1/goals/${id}`, payload)
    return response.data
  },
  updateGoalProgress: async (id: string, progress: number) => {
    const response = await performanceClient.patch(`/api/v1/goals/${id}/progress`, { progress })
    return response.data
  },
  deleteGoal: async (id: string) => {
    await performanceClient.delete(`/api/v1/goals/${id}`)
  },

  // Reviews
  listReviews: async (params?: { employeeId?: string; status?: string; page?: number; pageSize?: number }) => {
    const response = await performanceClient.get('/api/v1/reviews', { params })
    return response.data
  },
  getReview: async (id: string) => {
    const response = await performanceClient.get(`/api/v1/reviews/${id}`)
    return response.data
  },
  createReview: async (payload: Partial<Review>) => {
    const response = await performanceClient.post('/api/v1/reviews', payload)
    return response.data
  },
  submitReview: async (id: string, payload: { selfRating?: number; managerRating?: number; feedback?: string }) => {
    const response = await performanceClient.post(`/api/v1/reviews/${id}/submit`, payload)
    return response.data
  },
}
