import { api, unwrap } from '@/lib/api'

export interface Tenant {
  id: string
  name: string
  domain?: string
  status: 'ACTIVE' | 'SUSPENDED' | 'TRIAL'
  plan?: string
  createdAt: string
}

export interface Integration {
  id: string
  provider: string
  type: 'OAUTH2' | 'API_KEY' | 'WEBHOOK' | 'SCIM' | 'SAML'
  status: 'CONNECTED' | 'DISCONNECTED' | 'ERROR'
  lastSyncAt?: string
  metadata?: Record<string, unknown>
}

export interface FeatureFlag {
  key: string
  description?: string
  enabled: boolean
  rolloutPercent?: number
}

export const settingsService = {
  // Tenant / company
  myTenant: async () => unwrap(await api.get<Tenant>('/api/v1/tenants/current')),
  updateTenant: async (payload: Partial<Tenant>) =>
    unwrap(await api.put<Tenant>('/api/v1/tenants/current', payload)),

  // Branding
  branding: async () => unwrap(await api.get('/api/v1/tenants/current/branding')),
  uploadLogo: async (file: File) => {
    const fd = new FormData(); fd.append('file', file)
    return unwrap(await api.post('/api/v1/tenants/current/branding/logo', fd, { headers: { 'Content-Type': 'multipart/form-data' } }))
  },

  // Integrations
  listIntegrations: async () => unwrap(await api.get<Integration[]>('/api/v1/integrations')),
  connectIntegration: async (provider: string, payload: Record<string, unknown>) =>
    unwrap(await api.post<Integration>(`/api/v1/integrations/${provider}/connect`, payload)),
  disconnectIntegration: async (id: string) => api.delete(`/api/v1/integrations/${id}`),
  testIntegration: async (id: string) =>
    unwrap(await api.post(`/api/v1/integrations/${id}/test`)),

  // Feature flags (admin view)
  listFlags: async () => unwrap(await api.get<FeatureFlag[]>('/api/v1/tenants/current/flags')),
  toggleFlag: async (key: string, enabled: boolean) =>
    unwrap(await api.post(`/api/v1/tenants/current/flags/${key}`, { enabled })),

  // Webhooks
  listWebhooks: async () => unwrap(await api.get('/api/v1/integrations/webhooks')),
  createWebhook: async (payload: { url: string; events: string[]; secret?: string }) =>
    unwrap(await api.post('/api/v1/integrations/webhooks', payload)),
  rotateWebhookSecret: async (id: string) =>
    unwrap(await api.post(`/api/v1/integrations/webhooks/${id}/rotate-secret`)),

  // Security
  myMfa: async () => unwrap(await api.get('/api/v1/auth/mfa/status')),
  enrollMfa: async () => unwrap(await api.post('/api/v1/auth/mfa/enroll')),
  verifyMfa: async (code: string) => unwrap(await api.post('/api/v1/auth/mfa/verify', { code })),
  disableMfa: async (code: string) => unwrap(await api.post('/api/v1/auth/mfa/disable', { code })),
  mySessions: async () => unwrap(await api.get('/api/v1/auth/sessions')),
  revokeSession: async (id: string) => api.delete(`/api/v1/auth/sessions/${id}`),
}
