import axios, { type AxiosError, type InternalAxiosRequestConfig } from 'axios'

const API_GATEWAY = 'http://localhost:8080'
const TENANT_ID = 'demo'

// All requests go through the API Gateway — it handles CORS and routing
export const api = axios.create({
  baseURL: API_GATEWAY,
  headers: {
    'Content-Type': 'application/json',
    'X-Tenant-ID': TENANT_ID,
  },
  timeout: 30000,
})

// All service clients point to the gateway (not direct service ports)
export const authClient = api
export const coreHrClient = api
export const leaveClient = api
export const payrollClient = api
export const recruitmentClient = api
export const performanceClient = api
export const onboardingClient = api
export const documentClient = api
export const notificationClient = api
export const expenseClient = api
export const assetClient = api
export const helpdeskClient = api
export const lmsClient = api
export const reportsClient = api

const allClients = [api]

// Add auth interceptors to all clients
allClients.forEach(client => {
  client.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {
      const token = localStorage.getItem('accessToken')
      if (token) {
        config.headers.Authorization = `Bearer ${token}`
      }
      config.headers['X-Tenant-ID'] = TENANT_ID
      return config
    },
    error => Promise.reject(error)
  )

  client.interceptors.response.use(
    response => response,
    async (error: AxiosError) => {
      const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean }

      if (error.response?.status === 401 && !originalRequest._retry) {
        originalRequest._retry = true

        try {
          const refreshToken = localStorage.getItem('refreshToken')
          if (!refreshToken) {
            throw new Error('No refresh token')
          }

          const response = await authClient.post('/api/v1/auth/refresh', {
            refreshToken,
          })

          const { accessToken } = response.data.data
          localStorage.setItem('accessToken', accessToken)

          if (originalRequest.headers) {
            originalRequest.headers.Authorization = `Bearer ${accessToken}`
          }

          return client(originalRequest)
        } catch {
          localStorage.removeItem('accessToken')
          localStorage.removeItem('refreshToken')
          localStorage.removeItem('user')
          window.location.href = '/login'
          return Promise.reject(error)
        }
      }

      return Promise.reject(error)
    }
  )
})

export type ApiError = {
  message: string
  code?: string
  details?: unknown
}

export function getErrorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    return (
      error.response?.data?.message ||
      error.response?.data?.error ||
      error.message ||
      'An error occurred'
    )
  }
  if (error instanceof Error) {
    return error.message
  }
  return 'An unexpected error occurred'
}

export default api
