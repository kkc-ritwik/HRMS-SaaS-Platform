import { api, unwrap } from '@/lib/api'

export interface Department {
  id: string
  name: string
  code?: string
  parentId?: string
  parentName?: string
  headId?: string
  headName?: string
  active?: boolean
  employeeCount?: number
}

export interface Designation {
  id: string
  name: string
  code?: string
  level?: number
  departmentId?: string
  active?: boolean
}

export interface Location {
  id: string
  name: string
  code?: string
  addressLine1?: string
  addressLine2?: string
  city?: string
  state?: string
  country?: string
  pincode?: string
  timezone?: string
  active?: boolean
}

export interface LegalEntity {
  id: string
  name: string
  registrationNumber?: string
  taxId?: string
  parentId?: string
  country?: string
  active?: boolean
}

export interface CustomField {
  id: string
  entityName: string
  fieldName: string
  label: string
  fieldType: string
  required?: boolean
  options?: string[]
}

/** Departments @ /api/v1/departments */
export const departmentService = {
  list: async () => unwrap<Department[]>(await api.get('/api/v1/departments')),
  active: async () => unwrap<Department[]>(await api.get('/api/v1/departments/active')),
  get: async (id: string) => unwrap<Department>(await api.get(`/api/v1/departments/${id}`)),
  create: async (payload: Partial<Department>) => unwrap<Department>(await api.post('/api/v1/departments', payload)),
  update: async (id: string, payload: Partial<Department>) => unwrap<Department>(await api.put(`/api/v1/departments/${id}`, payload)),
  remove: async (id: string) => { await api.delete(`/api/v1/departments/${id}`) },
}

/** Designations @ /api/v1/designations */
export const designationService = {
  list: async () => unwrap<Designation[]>(await api.get('/api/v1/designations')),
  active: async () => unwrap<Designation[]>(await api.get('/api/v1/designations/active')),
  get: async (id: string) => unwrap<Designation>(await api.get(`/api/v1/designations/${id}`)),
  create: async (payload: Partial<Designation>) => unwrap<Designation>(await api.post('/api/v1/designations', payload)),
  update: async (id: string, payload: Partial<Designation>) => unwrap<Designation>(await api.put(`/api/v1/designations/${id}`, payload)),
  remove: async (id: string) => { await api.delete(`/api/v1/designations/${id}`) },
}

/** Locations @ /api/v1/locations */
export const locationService = {
  list: async () => unwrap<Location[]>(await api.get('/api/v1/locations')),
  active: async () => unwrap<Location[]>(await api.get('/api/v1/locations/active')),
  get: async (id: string) => unwrap<Location>(await api.get(`/api/v1/locations/${id}`)),
  create: async (payload: Partial<Location>) => unwrap<Location>(await api.post('/api/v1/locations', payload)),
  update: async (id: string, payload: Partial<Location>) => unwrap<Location>(await api.put(`/api/v1/locations/${id}`, payload)),
  remove: async (id: string) => { await api.delete(`/api/v1/locations/${id}`) },
}

/** Legal entities @ /api/v1/legal-entities */
export const legalEntityService = {
  list: async () => unwrap<LegalEntity[]>(await api.get('/api/v1/legal-entities')),
  get: async (id: string) => unwrap<LegalEntity>(await api.get(`/api/v1/legal-entities/${id}`)),
  children: async (parentId: string) => unwrap<LegalEntity[]>(await api.get(`/api/v1/legal-entities/${parentId}/children`)),
  create: async (payload: Partial<LegalEntity>) => unwrap<LegalEntity>(await api.post('/api/v1/legal-entities', payload)),
  update: async (id: string, payload: Partial<LegalEntity>) => unwrap<LegalEntity>(await api.put(`/api/v1/legal-entities/${id}`, payload)),
}

/** Org chart @ /api/v1/org-chart */
export const orgChartService = {
  root: async () => unwrap(await api.get('/api/v1/org-chart')),
  forEmployee: async (employeeId: string) => unwrap(await api.get(`/api/v1/org-chart/${employeeId}`)),
}

/** Cost centers @ /api/corehr/cost-centers */
export const costCenterService = {
  list: async () => unwrap(await api.get('/api/corehr/cost-centers')),
  activeForEmployee: async (employeeId: string) =>
    unwrap(await api.get(`/api/corehr/cost-centers/employee/${employeeId}/active`)),
  create: async (payload: Record<string, unknown>) => unwrap(await api.post('/api/corehr/cost-centers', payload)),
  allocate: async (payload: Record<string, unknown>) => unwrap(await api.post('/api/corehr/cost-centers/allocations', payload)),
}

/** Custom fields @ /api/v1/custom-fields */
export const customFieldService = {
  byEntity: async (entityName: string) => unwrap<CustomField[]>(await api.get(`/api/v1/custom-fields/by-entity/${entityName}`)),
  create: async (payload: Partial<CustomField>) => unwrap<CustomField>(await api.post('/api/v1/custom-fields', payload)),
  update: async (id: string, payload: Partial<CustomField>) => unwrap<CustomField>(await api.put(`/api/v1/custom-fields/${id}`, payload)),
  remove: async (id: string) => { await api.delete(`/api/v1/custom-fields/${id}`) },
}
