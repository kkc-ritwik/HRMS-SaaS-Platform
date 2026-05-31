import { api, unwrap } from '@/lib/api'

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

/** Backend: GoalController @ /api/v1/goals, ReviewController @ /api/v1/reviews,
 *  PerformanceCycleController @ /api/v1/performance/cycles,
 *  FeedbackController @ /api/v1/feedback, OneOnOneController @ /api/v1/one-on-ones,
 *  PerformanceAnalyticsController @ /api/v1/performance/analytics */
export const performanceService = {
  // ── Goals (/api/v1/goals) ────────────────────────────────────────────────
  myGoals: async () => unwrap<Goal[]>(await api.get('/api/v1/goals/me')),
  goalsForEmployee: async (employeeId: string) => unwrap<Goal[]>(await api.get(`/api/v1/goals/employee/${employeeId}`)),
  goalsForCycle: async (cycleId: string) => unwrap<Goal[]>(await api.get(`/api/v1/goals/cycle/${cycleId}`)),
  getGoal: async (id: string) => unwrap<Goal>(await api.get(`/api/v1/goals/${id}`)),
  goalUpdates: async (id: string) => unwrap(await api.get(`/api/v1/goals/${id}/updates`)),
  createGoal: async (payload: Partial<Goal>) => unwrap<Goal>(await api.post('/api/v1/goals', payload)),
  updateGoal: async (id: string, payload: Partial<Goal>) => unwrap<Goal>(await api.put(`/api/v1/goals/${id}`, payload)),
  updateGoalProgress: async (id: string, progress: number, note?: string) =>
    unwrap<Goal>(await api.post(`/api/v1/goals/${id}/progress`, { progress, note })),
  deleteGoal: async (id: string) => { await api.delete(`/api/v1/goals/${id}`) },

  // ── Reviews (/api/v1/reviews) ────────────────────────────────────────────
  myReviews: async () => unwrap<Review[]>(await api.get('/api/v1/reviews/me')),
  myPendingReviews: async () => unwrap<Review[]>(await api.get('/api/v1/reviews/me/pending')),
  reviewsForEmployee: async (employeeId: string) => unwrap<Review[]>(await api.get(`/api/v1/reviews/employee/${employeeId}`)),
  reviewsForCycle: async (cycleId: string) => unwrap<Review[]>(await api.get(`/api/v1/reviews/cycle/${cycleId}`)),
  reviewsForTeam: async (cycleId: string, managerId: string) =>
    unwrap<Review[]>(await api.get(`/api/v1/reviews/cycle/${cycleId}/team/${managerId}`)),
  getReview: async (id: string) => unwrap<Review>(await api.get(`/api/v1/reviews/${id}`)),
  createReview: async (payload: Partial<Review>) => unwrap<Review>(await api.post('/api/v1/reviews', payload)),
  submitReview: async (id: string, payload: { selfRating?: number; managerRating?: number; feedback?: string }) =>
    unwrap<Review>(await api.put(`/api/v1/reviews/${id}/submit`, payload)),
  acknowledgeReview: async (id: string) => unwrap<Review>(await api.post(`/api/v1/reviews/${id}/acknowledge`)),
  calibrateReview: async (id: string, payload: Record<string, unknown>) =>
    unwrap<Review>(await api.post(`/api/v1/reviews/${id}/calibrate`, payload)),

  // ── Performance cycles (/api/v1/performance/cycles) ──────────────────────
  listCycles: async () => unwrap(await api.get('/api/v1/performance/cycles')),
  getCycle: async (id: string) => unwrap(await api.get(`/api/v1/performance/cycles/${id}`)),
  createCycle: async (payload: Record<string, unknown>) => unwrap(await api.post('/api/v1/performance/cycles', payload)),
  activateCycle: async (id: string) => unwrap(await api.post(`/api/v1/performance/cycles/${id}/activate`)),
  startSelfReview: async (id: string) => unwrap(await api.post(`/api/v1/performance/cycles/${id}/start-self-review`)),
  startManagerReview: async (id: string) => unwrap(await api.post(`/api/v1/performance/cycles/${id}/start-manager-review`)),
  startCalibration: async (id: string) => unwrap(await api.post(`/api/v1/performance/cycles/${id}/start-calibration`)),
  finalizeCycle: async (id: string) => unwrap(await api.post(`/api/v1/performance/cycles/${id}/finalize`)),
  closeCycle: async (id: string) => unwrap(await api.post(`/api/v1/performance/cycles/${id}/close`)),

  // ── Analytics (/api/v1/performance/analytics) ────────────────────────────
  nineBox: async (cycleId: string) => unwrap(await api.get(`/api/v1/performance/analytics/cycles/${cycleId}/nine-box`)),
  teamSummary: async (cycleId: string, managerId: string) =>
    unwrap(await api.get(`/api/v1/performance/analytics/cycles/${cycleId}/team-summary/${managerId}`)),
  myTeamSummary: async (cycleId: string) =>
    unwrap(await api.get(`/api/v1/performance/analytics/cycles/${cycleId}/my-team-summary`)),

  // ── 360 Feedback (/api/v1/feedback) ──────────────────────────────────────
  feedbackWall: async () => unwrap(await api.get('/api/v1/feedback/wall')),
  myFeedbackReceived: async () => unwrap(await api.get('/api/v1/feedback/me/received')),
  myFeedbackGiven: async () => unwrap(await api.get('/api/v1/feedback/me/given')),
  giveFeedback: async (payload: Record<string, unknown>) => unwrap(await api.post('/api/v1/feedback', payload)),
}
