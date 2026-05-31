import { api, unwrap } from '@/lib/api'

export interface Notification {
  id: string
  userId: string
  type: string
  title: string
  body: string
  link?: string
  iconUrl?: string
  read: boolean
  readAt?: string
  createdAt: string
  payload?: Record<string, unknown>
}

export interface Announcement {
  id: string
  title: string
  body: string
  category?: string
  publishedAt?: string
  expiresAt?: string
  audience?: 'ALL' | 'DEPARTMENT' | 'LOCATION' | 'ROLE'
  imageUrl?: string
}

export const notificationService = {
  listInbox: async (params: { read?: boolean; page?: number; size?: number } = {}) =>
    unwrap(await api.get('/api/v1/notifications', { params })),
  unreadCount: async () => unwrap(await api.get<{ count: number }>('/api/v1/notifications/unread-count')),
  markRead: async (id: string) => unwrap(await api.post(`/api/v1/notifications/${id}/read`)),
  markAllRead: async () => unwrap(await api.post('/api/v1/notifications/read-all')),
  preferences: async () => unwrap(await api.get('/api/v1/notifications/preferences')),
  updatePreferences: async (payload: Record<string, boolean>) =>
    unwrap(await api.put('/api/v1/notifications/preferences', payload)),

  listAnnouncements: async () => unwrap(await api.get<Announcement[]>('/api/v1/announcements')),
  publishAnnouncement: async (payload: Partial<Announcement>) =>
    unwrap(await api.post<Announcement>('/api/v1/announcements', payload)),
}
