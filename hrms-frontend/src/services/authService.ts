import { authClient } from '@/lib/api'
import type { User } from '@/store/authStore'

export interface LoginPayload {
  email: string
  password: string
  tenantId?: string
}

export interface LoginResponse {
  data: {
    accessToken: string
    refreshToken: string
    user: User
  }
}

export const authService = {
  login: async (payload: LoginPayload): Promise<LoginResponse> => {
    const response = await authClient.post<LoginResponse>('/api/v1/auth/login', {
      ...payload,
      tenantId: payload.tenantId || 'demo',
    })
    return response.data
  },

  logout: async () => {
    try {
      await authClient.post('/api/v1/auth/logout')
    } catch {
      // Ignore errors on logout
    }
  },

  refresh: async (refreshToken: string) => {
    const response = await authClient.post('/api/v1/auth/refresh', { refreshToken })
    return response.data
  },

  me: async () => {
    const response = await authClient.get('/api/v1/auth/me')
    return response.data
  },

  changePassword: async (payload: { currentPassword: string; newPassword: string }) => {
    const response = await authClient.post('/api/v1/auth/change-password', payload)
    return response.data
  },

  forgotPassword: async (email: string) => {
    const response = await authClient.post('/api/v1/auth/forgot-password', { email })
    return response.data
  },
}
