import { api, unwrap } from '@/lib/api'

export interface Kudos {
  id: string
  giverId: string
  recipientId: string
  recipientName?: string
  value?: string
  message: string
  isPublic?: boolean
  points?: number
  createdAt: string
}

export interface Suggestion {
  id: string
  title: string
  description: string
  category: 'PROCESS' | 'CULTURE' | 'FACILITY' | 'BENEFITS' | 'TECHNOLOGY' | 'COMPLAINT' | 'IDEA' | 'OTHER'
  status: 'OPEN' | 'UNDER_REVIEW' | 'ACCEPTED' | 'REJECTED' | 'IMPLEMENTED' | 'ARCHIVED'
  votesUp: number
  votesDown: number
  commentsCount: number
  resolution?: string
}

export interface MoodCheckIn {
  id: string
  score: number
  comment?: string
  category: 'DAILY' | 'WEEKLY' | 'POST_ONBOARDING' | 'POST_PROMOTION' | 'POST_REVIEW' | 'EXIT'
  anonymous?: boolean
  theme?: string
  checkInDate: string
}

export interface Poll {
  id: string
  question: string
  options: { id: string; text: string; voteCount: number }[]
  startsAt: string
  endsAt: string
  totalVotes: number
  isActive: boolean
}

export interface Survey {
  id: string
  title: string
  description?: string
  status: 'DRAFT' | 'ACTIVE' | 'CLOSED' | 'ARCHIVED'
  startsAt?: string
  endsAt?: string
  responseCount: number
}

export interface WellnessProgram {
  id: string
  code: string
  name: string
  description?: string
  category: string
  startDate?: string
  endDate?: string
  goalMetric?: string
  goalTarget?: number
  active: boolean
}

/** Backend: EngagementController @ /api/v1/engagement (kudos/polls/surveys),
 *  SuggestionController/MoodCheckInController/RewardsController/StayInterviewController/
 *  WellnessController/EngagementHeatmapController @ /api/engagement/* */
export const engagementService = {
  // Kudos (/api/v1/engagement/kudos)
  giveKudos: async (payload: { recipientId: string; message: string; value?: string; isPublic?: boolean }) =>
    unwrap(await api.post<Kudos>('/api/v1/engagement/kudos', payload)),
  myKudosFeed: async (): Promise<Kudos[]> => {
    const res = unwrap<{ content?: Kudos[] } | Kudos[]>(await api.get('/api/v1/engagement/kudos/feed'))
    return Array.isArray(res) ? res : (res?.content ?? [])
  },
  kudosReceived: async (recipientId: string): Promise<Kudos[]> => {
    const res = unwrap<{ content?: Kudos[] } | Kudos[]>(await api.get('/api/v1/engagement/kudos/received', { params: { recipientId } }))
    return Array.isArray(res) ? res : (res?.content ?? [])
  },

  // Suggestions
  listSuggestions: async (params: { category?: string; status?: string; page?: number; size?: number } = {}) =>
    unwrap(await api.get('/api/engagement/suggestions', { params })),
  submitSuggestion: async (payload: { title: string; description: string; category: string; includeIdentity?: boolean; employeeId?: string }) =>
    unwrap(await api.post('/api/engagement/suggestions', payload)),
  voteSuggestion: async (id: string, voterEmployeeId: string, direction: 'UP' | 'DOWN') =>
    unwrap(await api.post(`/api/engagement/suggestions/${id}/vote`, { voterEmployeeId, direction })),
  resolveSuggestion: async (id: string, payload: { status: string; resolution: string }) =>
    unwrap(await api.post(`/api/engagement/suggestions/${id}/resolve`, payload)),

  // Pulse / Mood
  submitPulse: async (payload: { employeeId: string; score: number; comment?: string; theme?: string; anonymous?: boolean; category?: string }) =>
    unwrap(await api.post('/api/engagement/pulse', payload)),
  myPulse: async (employeeId: string, from: string, to: string) =>
    unwrap(await api.get<MoodCheckIn[]>('/api/engagement/pulse/me', { params: { employeeId, from, to } })),
  pulseAggregate: async (from: string, to: string) =>
    unwrap(await api.get('/api/engagement/pulse/aggregate', { params: { from, to } })),
  pulseTrend: async (days = 30) =>
    unwrap(await api.get('/api/engagement/pulse/trend', { params: { days } })),

  // Heatmap
  heatmap: async (by: 'department' | 'location' | 'manager' | 'tenure', from: string, to: string) =>
    unwrap(await api.get(`/api/engagement/heatmap/by-${by}`, { params: { from, to } })),

  // Polls (/api/v1/engagement/polls)
  listPolls: async (): Promise<Poll[]> => {
    const res = unwrap<{ content?: Poll[] } | Poll[]>(await api.get('/api/v1/engagement/polls'))
    return Array.isArray(res) ? res : (res?.content ?? [])
  },
  createPoll: async (payload: Partial<Poll>) => unwrap(await api.post<Poll>('/api/v1/engagement/polls', payload)),
  // backend PollVote expects selectedOptions: List<Integer> (option indexes)
  votePoll: async (pollId: string, optionIndexes: number[]) =>
    unwrap(await api.post(`/api/v1/engagement/polls/${pollId}/vote`, { selectedOptions: optionIndexes })),
  pollTally: async (pollId: string) =>
    unwrap<Record<number, number>>(await api.get(`/api/v1/engagement/polls/${pollId}/tally`)),

  // Surveys (/api/v1/engagement/surveys)
  listSurveys: async (): Promise<Survey[]> => {
    const res = unwrap<{ content?: Survey[] } | Survey[]>(await api.get('/api/v1/engagement/surveys'))
    return Array.isArray(res) ? res : (res?.content ?? [])
  },
  createSurvey: async (payload: Partial<Survey>) => unwrap(await api.post<Survey>('/api/v1/engagement/surveys', payload)),
  launchSurvey: async (id: string) => unwrap(await api.post<Survey>(`/api/v1/engagement/surveys/${id}/launch`)),
  submitSurveyResponse: async (id: string, payload: Record<string, unknown>) =>
    unwrap(await api.post(`/api/v1/engagement/surveys/${id}/responses`, payload)),
  surveyResponses: async (id: string) => unwrap(await api.get(`/api/v1/engagement/surveys/${id}/responses`)),
  surveyEnps: async (id: string, key = 'score') =>
    unwrap(await api.get(`/api/v1/engagement/surveys/${id}/enps`, { params: { key } })),

  // Stay interviews
  scheduleStayInterview: async (payload: Record<string, unknown>) =>
    unwrap(await api.post('/api/engagement/stay', payload)),
  dueStayInterviews: async () => unwrap(await api.get('/api/engagement/stay/due')),

  // Rewards catalog + redemption
  rewardsCatalog: async () => unwrap(await api.get('/api/engagement/rewards/catalog')),
  redeem: async (payload: { catalogItemId: string; employeeId: string; quantity?: number; availablePoints: number; shippingAddress?: string }) =>
    unwrap(await api.post('/api/engagement/rewards/redeem', payload)),
  myRedemptions: async (employeeId: string) =>
    unwrap(await api.get(`/api/engagement/rewards/redemptions/employee/${employeeId}`)),

  // Wellness
  wellnessPrograms: async () => unwrap(await api.get<WellnessProgram[]>('/api/engagement/wellness/programs')),
  logWellness: async (payload: { employeeId: string; programId: string; logDate: string; metricValue: number; source?: string }) =>
    unwrap(await api.post('/api/engagement/wellness/logs', payload)),
  leaderboard: async (programId: string, from: string, to: string) =>
    unwrap(await api.get('/api/engagement/wellness/leaderboard', { params: { programId, from, to } })),
}
