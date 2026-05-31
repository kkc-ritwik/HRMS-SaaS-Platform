import { api, unwrap } from '@/lib/api'

export interface Document {
  id: string
  name: string
  description?: string
  category?: string
  employeeId?: string
  storageUri: string
  contentType?: string
  sizeBytes?: number
  versionNumber?: number
  expiryDate?: string
  isPublic?: boolean
  uploadedBy?: string
  uploadedAt: string
}

export interface DocumentTemplate {
  id: string
  name: string
  type: string
  body: string
  variables: string[]
  active: boolean
}

export const documentService = {
  list: async (params: { category?: string; employeeId?: string; search?: string; page?: number; size?: number } = {}) =>
    unwrap(await api.get('/api/v1/documents', { params })),
  get: async (id: string) => unwrap(await api.get<Document>(`/api/v1/documents/${id}`)),
  upload: async (file: File, metadata: { name: string; category?: string; employeeId?: string; expiryDate?: string; isPublic?: boolean }) => {
    const fd = new FormData()
    fd.append('file', file)
    fd.append('meta', new Blob([JSON.stringify(metadata)], { type: 'application/json' }))
    return unwrap(await api.post<Document>('/api/v1/documents', fd, { headers: { 'Content-Type': 'multipart/form-data' } }))
  },
  download: async (id: string): Promise<Blob> => {
    const res = await api.get(`/api/v1/documents/${id}/download`, { responseType: 'blob' })
    return res.data
  },
  versions: async (id: string) => unwrap(await api.get(`/api/v1/documents/${id}/versions`)),
  newVersion: async (id: string, file: File, changeNote?: string) => {
    const fd = new FormData()
    fd.append('file', file)
    if (changeNote) fd.append('changeNote', changeNote)
    return unwrap(await api.post(`/api/v1/documents/${id}/versions`, fd, { headers: { 'Content-Type': 'multipart/form-data' } }))
  },
  delete: async (id: string) => api.delete(`/api/v1/documents/${id}`),

  // Templates + letters
  templates: async () => unwrap(await api.get<DocumentTemplate[]>('/api/v1/templates')),
  generateLetter: async (templateId: string, data: Record<string, unknown>) =>
    unwrap(await api.post(`/api/v1/templates/${templateId}/generate`, data)),

  // Employment letters
  generateEmploymentLetter: async (payload: Record<string, unknown>) =>
    unwrap(await api.post('/api/v1/documents/letters/employment', payload)),
  generateSalaryRevisionLetter: async (payload: Record<string, unknown>) =>
    unwrap(await api.post('/api/documents/letters/salary-revision', payload)),

  // E-sign
  requestEsign: async (documentId: string, signers: Array<{ email: string; name: string }>, provider = 'internal') =>
    unwrap(await api.post('/api/v1/esign/requests', { documentId, signers, provider })),
}
