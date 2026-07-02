import { api, unwrap } from '@/lib/api'

export type TicketPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'
export type TicketStatus = 'NEW' | 'ASSIGNED' | 'IN_PROGRESS' | 'WAITING_INFO' | 'RESOLVED' | 'CLOSED' | 'REOPENED'

export interface Ticket {
  id: string
  ticketNumber: string
  subject: string
  description: string
  raisedBy: string
  raisedByName?: string
  assignedTo?: string
  assignedToName?: string
  category: string
  priority: TicketPriority
  status: TicketStatus
  slaDueAt?: string
  resolvedAt?: string
  resolutionNotes?: string
  satisfactionRating?: number
  createdAt: string
  updatedAt: string
}

export interface TicketComment {
  id: string
  ticketId: string
  authorId: string
  authorName?: string
  body: string
  isInternalNote: boolean
  createdAt: string
}

export interface KbArticle {
  id: string
  title: string
  slug: string
  category: string
  body: string
  views: number
  helpful: number
  notHelpful: number
  publishedAt?: string
}

export const helpdeskService = {
  listTickets: async (params: { status?: TicketStatus; assignedTo?: string; category?: string; page?: number; size?: number } = {}) =>
    unwrap(await api.get('/api/v1/tickets', { params })),
  ticketsByRequester: async (employeeId: string) => unwrap(await api.get<Ticket[]>(`/api/v1/tickets/requester/${employeeId}`)),
  ticketsByAssignee: async (employeeId: string) => unwrap(await api.get<Ticket[]>(`/api/v1/tickets/assignee/${employeeId}`)),
  getTicket: async (id: string) => unwrap(await api.get<Ticket>(`/api/v1/tickets/${id}`)),
  createTicket: async (payload: Partial<Ticket>) => unwrap(await api.post<Ticket>('/api/v1/tickets', payload)),
  assignTicket: async (id: string, assigneeId: string) =>
    unwrap(await api.post<Ticket>(`/api/v1/tickets/${id}/assign`, { assigneeId })),
  updateStatus: async (id: string, status: TicketStatus, notes?: string) =>
    unwrap(await api.post<Ticket>(`/api/v1/tickets/${id}/status`, { status, notes })),
  addComment: async (id: string, body: string, isInternalNote = false) =>
    unwrap(await api.post<TicketComment>(`/api/v1/tickets/${id}/comments`, { body, isInternalNote })),
  comments: async (id: string) => unwrap(await api.get<TicketComment[]>(`/api/v1/tickets/comments/ticket/${id}`)),
  rate: async (id: string, rating: number, feedback?: string) =>
    unwrap(await api.post(`/api/v1/helpdesk/tickets/${id}/csat`, {}, { params: { score: rating, comment: feedback } })),

  // KB
  listArticles: async () => unwrap(await api.get<KbArticle[]>('/api/v1/tickets/kb-articles/published')),
  getArticle: async (id: string) => unwrap(await api.get<KbArticle>(`/api/v1/tickets/kb-articles/${id}`)),
  feedback: async (id: string, helpful: boolean) =>
    unwrap(await api.post(`/api/v1/tickets/kb-articles/${id}/feedback`, { helpful })),
}
