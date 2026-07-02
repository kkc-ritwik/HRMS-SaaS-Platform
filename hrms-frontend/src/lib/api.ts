import axios, { type AxiosError, type AxiosInstance, type InternalAxiosRequestConfig } from 'axios'
import { toast } from 'sonner'
import { config } from './config'

/**
 * Single axios instance — all 30+ backend microservices are reached through the API
 * gateway (default :8080). Auth, tenant, request-id, and idempotency-key are attached
 * by interceptors.
 */
export const api: AxiosInstance = axios.create({
  baseURL: config.apiBaseUrl,
  headers: { 'Content-Type': 'application/json' },
  timeout: 30000,
})

// Legacy aliases — all point at the same gateway-routed axios instance
export const authClient = api
export const coreHrClient = api
export const leaveClient = api
export const payrollClient = api
export const recruitmentClient = api
export const performanceClient = api
export const onboardingClient = api
export const offboardingClient = api
export const documentClient = api
export const notificationClient = api
export const expenseClient = api
export const assetClient = api
export const helpdeskClient = api
export const lmsClient = api
export const reportsClient = api
export const engagementClient = api
export const workflowClient = api
export const complianceClient = api
export const socialClient = api
export const filesClient = api
export const formsClient = api
export const skillsClient = api
export const integrationsClient = api
export const workplaceClient = api
export const casesClient = api
export const timesheetClient = api
export const travelClient = api
export const compensationClient = api

let activeTenantId: string = config.defaultTenant
export function setActiveTenant(tenantId: string): void {
  activeTenantId = tenantId || config.defaultTenant
  localStorage.setItem('activeTenant', activeTenantId)
}
const stored = localStorage.getItem('activeTenant')
if (stored) activeTenantId = stored

function newRequestId(): string {
  return `req-${Date.now()}-${Math.random().toString(36).slice(2, 10)}`
}

api.interceptors.request.use(
  (cfg: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('accessToken')
    if (token) cfg.headers.Authorization = `Bearer ${token}`
    cfg.headers['X-Tenant-Id'] = activeTenantId
    cfg.headers['X-Request-Id'] = newRequestId()
    const method = (cfg.method || 'get').toLowerCase()
    if (['post', 'put', 'patch', 'delete'].includes(method) && !cfg.headers['Idempotency-Key']) {
      cfg.headers['Idempotency-Key'] = `${method}-${Date.now()}-${Math.random().toString(36).slice(2, 12)}`
    }
    return cfg
  },
  err => Promise.reject(err)
)

let isRefreshing = false
let refreshQueue: Array<() => void> = []

/**
 * Extract the first complete top-level JSON value from a string. The API gateway can emit a
 * duplicated/concatenated body ({...}{...}) which is invalid JSON; axios then leaves res.data as
 * a raw string. We brace-match the first balanced object/array so parsing still succeeds.
 */
function extractFirstJson(s: string): unknown {
  const str = s.trim()
  try { return JSON.parse(str) } catch { /* fall through to brace-matching */ }
  const open = str[0]
  if (open !== '{' && open !== '[') return null
  const close = open === '{' ? '}' : ']'
  let depth = 0, inStr = false, esc = false
  for (let i = 0; i < str.length; i++) {
    const c = str[i]
    if (inStr) {
      if (esc) esc = false
      else if (c === '\\') esc = true
      else if (c === '"') inStr = false
    } else if (c === '"') inStr = true
    else if (c === open) depth++
    else if (c === close) { depth--; if (depth === 0) { try { return JSON.parse(str.slice(0, i + 1)) } catch { return null } } }
  }
  return null
}

// Normalise any string body (some gateway responses arrive unparsed / duplicated) into JSON.
api.interceptors.response.use(
  res => {
    if (typeof res.data === 'string' && res.data.length && /^[\[{]/.test(res.data.trim())) {
      const parsed = extractFirstJson(res.data)
      if (parsed !== null) res.data = parsed
    }
    return res
  },
  async (error: AxiosError) => {
    const original = error.config as (InternalAxiosRequestConfig & { _retry?: boolean }) | undefined
    if (!original) return Promise.reject(error)

    if (error.response?.status === 401 && !original._retry && !original.url?.includes('/auth/')) {
      if (isRefreshing) {
        return new Promise(resolve => {
          refreshQueue.push(() => resolve(api(original)))
        })
      }
      original._retry = true
      isRefreshing = true
      try {
        const refreshToken = localStorage.getItem('refreshToken')
        if (!refreshToken) throw new Error('No refresh token')
        const { data } = await api.post('/api/v1/auth/refresh', { refreshToken })
        const newToken = data?.data?.accessToken || data?.accessToken
        if (!newToken) throw new Error('Refresh returned no access token')
        localStorage.setItem('accessToken', newToken)
        if (original.headers) original.headers.Authorization = `Bearer ${newToken}`
        refreshQueue.forEach(cb => cb())
        refreshQueue = []
        return api(original)
      } catch {
        localStorage.removeItem('accessToken')
        localStorage.removeItem('refreshToken')
        localStorage.removeItem('auth-storage')
        window.location.href = '/login'
        return Promise.reject(error)
      } finally {
        isRefreshing = false
      }
    }

    if (error.response) {
      const status = error.response.status
      const data = error.response.data as { message?: string; detail?: string; title?: string } | undefined
      const msg = data?.message || data?.detail || data?.title || `Request failed (${status})`
      if (status >= 500) toast.error(msg)
      else if (status === 429) toast.warning('Rate limit exceeded — please slow down.')
      else if (status === 403) toast.error('You do not have permission to perform this action.')
    } else if (error.request) {
      toast.error('Network error — server unreachable.')
    }

    return Promise.reject(error)
  }
)

export type ApiResponse<T> = {
  data: T
  success?: boolean
  pagination?: { page: number; pageSize: number; total: number; totalPages: number }
}

export type PageResponse<T> = {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
  empty?: boolean
}

export function unwrap<T>(res: { data: ApiResponse<T> | T }): T {
  const body = res.data as ApiResponse<T> | T
  if (body && typeof body === 'object' && 'data' in (body as Record<string, unknown>)) {
    return (body as ApiResponse<T>).data
  }
  return body as T
}

export function getErrorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    const data = error.response?.data as { message?: string; detail?: string; error?: string } | undefined
    return data?.message || data?.detail || data?.error || error.message || 'An error occurred'
  }
  if (error instanceof Error) return error.message
  return 'An unexpected error occurred'
}

export default api
