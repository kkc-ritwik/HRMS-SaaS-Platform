/**
 * Centralised runtime config. Read once from Vite-injected env at boot.
 * Override per environment via .env.local / .env.production.
 */
export const config = {
  apiBaseUrl: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  wsUrl: import.meta.env.VITE_WS_URL || 'ws://localhost:8080/ws',
  sseBaseUrl: import.meta.env.VITE_SSE_BASE_URL || 'http://localhost:8080',
  defaultTenant: import.meta.env.VITE_DEFAULT_TENANT || 'demo',
  mockMode: import.meta.env.VITE_ENABLE_MOCK === 'true',
} as const

export type Config = typeof config
