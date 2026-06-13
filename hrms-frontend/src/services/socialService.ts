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
  updatePost: async (id: string, payload: Partial<Post>) => unwrap(await api.put<Post>(`/api/v1/posts/${id}`, payload)),
  deletePost: async (id: string) => { await api.delete(`/api/v1/posts/${id}`) },
  postsByAuthor: async (authorId: string) => unwrap<Post[]>(await api.get(`/api/v1/posts/author/${authorId}`)),
  deleteComment: async (id: string) => { await api.delete(`/api/v1/posts/comments/${id}`) },
  // Backend: PostLikeController @ /api/v1/posts/likes
  likePost: async (id: string) => unwrap(await api.post('/api/v1/posts/likes', { postId: id })),
  unlikePost: async (id: string) => unwrap(await api.delete(`/api/v1/posts/likes/${id}`)),
  // Backend: PostCommentController @ /api/v1/posts/comments
  comments: async (id: string) => unwrap(await api.get<PostComment[]>(`/api/v1/posts/comments/post/${id}`)),
  comment: async (id: string, body: string) =>
    unwrap(await api.post<PostComment>('/api/v1/posts/comments', { postId: id, body })),

  // Groups — Backend: GroupController @ /api/v1/groups, members @ /api/v1/groups/members
  listGroups: async () => unwrap(await api.get<Group[]>('/api/v1/groups')),
  myGroups: async (employeeId: string) => unwrap(await api.get<Group[]>(`/api/v1/groups/members/employee/${employeeId}`)),
  joinGroup: async (id: string) => unwrap(await api.post('/api/v1/groups/members/join', { groupId: id })),

  // Events — Backend: EventController @ /api/v1/groups/events
  listEvents: async () => unwrap(await api.get<Event[]>('/api/v1/groups/events')),
  rsvp: async (id: string, attending: boolean) =>
    unwrap(await api.post(`/api/v1/groups/events/${id}/rsvp`, { attending })),
}
