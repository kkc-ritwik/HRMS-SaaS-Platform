import { api, unwrap } from '@/lib/api'

export interface Post {
  id: string
  authorId: string
  authorName?: string
  authorAvatar?: string
  body: string
  imageUri?: string
  videoUri?: string
  hashtags?: string[]
  likesCount: number
  commentsCount: number
  sharesCount: number
  visibility: 'PUBLIC' | 'DEPARTMENT' | 'GROUP'
  groupId?: string
  createdAt: string
  isLikedByMe?: boolean
}

export interface PostComment {
  id: string
  postId: string
  authorId: string
  authorName?: string
  body: string
  createdAt: string
}

export interface Group {
  id: string
  name: string
  description?: string
  privacy: 'PUBLIC' | 'PRIVATE'
  memberCount: number
  coverUri?: string
}

export interface Event {
  id: string
  title: string
  description?: string
  location?: string
  meetingLink?: string
  startsAt: string
  endsAt: string
  rsvpDeadline?: string
  attendeesCount: number
  isAttending?: boolean
}

export const socialService = {
  feed: async (page = 0, size = 20) =>
    unwrap(await api.get('/api/v1/posts/feed', { params: { page, size } })),
  createPost: async (payload: Partial<Post>) => unwrap(await api.post<Post>('/api/v1/posts', payload)),
  likePost: async (id: string) => unwrap(await api.post(`/api/v1/posts/${id}/like`)),
  unlikePost: async (id: string) => unwrap(await api.delete(`/api/v1/posts/${id}/like`)),
  comments: async (id: string) => unwrap(await api.get<PostComment[]>(`/api/v1/posts/${id}/comments`)),
  comment: async (id: string, body: string) =>
    unwrap(await api.post<PostComment>(`/api/v1/posts/${id}/comments`, { body })),

  // Groups
  listGroups: async () => unwrap(await api.get<Group[]>('/api/v1/groups')),
  myGroups: async () => unwrap(await api.get<Group[]>('/api/v1/groups/me')),
  joinGroup: async (id: string) => unwrap(await api.post(`/api/v1/groups/${id}/join`)),

  // Events
  listEvents: async () => unwrap(await api.get<Event[]>('/api/v1/events')),
  rsvp: async (id: string, attending: boolean) =>
    unwrap(await api.post(`/api/v1/events/${id}/rsvp`, { attending })),
}
