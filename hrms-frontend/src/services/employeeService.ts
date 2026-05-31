import { api, unwrap } from '@/lib/api'

export interface Employee {
  id: string
  employeeId: string
  firstName: string
  lastName: string
  fullName: string
  email: string
  phone?: string
  avatar?: string
  departmentId?: string
  departmentName?: string
  designationId?: string
  designationName?: string
  locationId?: string
  locationName?: string
  managerId?: string
  managerName?: string
  employmentType?: 'FULL_TIME' | 'PART_TIME' | 'CONTRACT' | 'INTERN'
  status: 'ACTIVE' | 'INACTIVE' | 'ON_LEAVE' | 'TERMINATED' | 'PROBATION' | 'NOTICE_PERIOD'
  joinDate?: string
  probationEndDate?: string
  exitDate?: string
  gender?: 'MALE' | 'FEMALE' | 'OTHER'
  dateOfBirth?: string
  address?: string
  city?: string
  state?: string
  country?: string
  pincode?: string
  bankAccountNumber?: string
  bankName?: string
  ifscCode?: string
  panNumber?: string
  aadharNumber?: string
  createdAt?: string
  updatedAt?: string
}

export interface EmployeeAddress {
  id: string
  employeeId: string
  type: 'PERMANENT' | 'CURRENT' | 'EMERGENCY'
  line1: string
  line2?: string
  city: string
  state: string
  country: string
  pincode: string
  isPrimary?: boolean
}

export interface EmployeeEducation {
  id: string
  employeeId: string
  degree: string
  institution: string
  fieldOfStudy?: string
  startYear?: number
  endYear?: number
  grade?: string
}

export interface EmergencyContact {
  id: string
  employeeId: string
  name: string
  relationship: string
  phone: string
  email?: string
  address?: string
}

export interface FamilyMember {
  id: string
  employeeId: string
  name: string
  relationship: string
  dateOfBirth?: string
  occupation?: string
  dependent?: boolean
}

export interface WorkHistory {
  id: string
  employeeId: string
  company: string
  designation: string
  fromDate?: string
  toDate?: string
  location?: string
  reasonForLeaving?: string
}

export interface EmployeeListParams {
  page?: number
  size?: number
  pageSize?: number
  search?: string
  departmentId?: string
  status?: string
  employmentType?: string
  sort?: string
}

/** Backend: EmployeeController @ /api/v1/employees (+ nested sub-resources) */
export const employeeService = {
  // ── Core ────────────────────────────────────────────────────────────────
  list: async (params: EmployeeListParams = {}) =>
    unwrap(await api.get('/api/v1/employees', { params })),
  directory: async (params: EmployeeListParams = {}) =>
    unwrap(await api.get('/api/v1/employees/directory', { params })),
  getById: async (id: string) => unwrap<Employee>(await api.get(`/api/v1/employees/${id}`)),
  create: async (payload: Partial<Employee>) =>
    unwrap<Employee>(await api.post('/api/v1/employees', payload)),
  update: async (id: string, payload: Partial<Employee>) =>
    unwrap<Employee>(await api.put(`/api/v1/employees/${id}`, payload)),
  remove: async (id: string) => { await api.delete(`/api/v1/employees/${id}`) },

  team: async (id: string) => unwrap<Employee[]>(await api.get(`/api/v1/employees/${id}/team`)),
  timeline: async (id: string) => unwrap(await api.get(`/api/v1/employees/${id}/timeline`)),
  lifecycle: async (id: string) => unwrap(await api.get(`/api/v1/employees/${id}/lifecycle`)),
  addLifecycleEvent: async (id: string, payload: Record<string, unknown>) =>
    unwrap(await api.post(`/api/v1/employees/${id}/lifecycle-event`, payload)),

  bulkImportCsv: async (file: File) => {
    const fd = new FormData()
    fd.append('file', file)
    return unwrap(await api.post('/api/v1/employees/bulk/import-csv', fd, {
      headers: { 'Content-Type': 'multipart/form-data' },
    }))
  },

  // ── Backward-compatible aliases (used by existing pages) ───────────────────
  delete: async (id: string) => { await api.delete(`/api/v1/employees/${id}`) },
  bulkImport: async (file: File) => employeeService.bulkImportCsv(file),
  getStats: async () => {
    const data = unwrap<{ content?: Employee[] } | Employee[]>(await api.get('/api/v1/employees', { params: { size: 1000 } }))
    const list: Employee[] = Array.isArray(data) ? data : (data?.content ?? [])
    const monthAgo = new Date(); monthAgo.setMonth(monthAgo.getMonth() - 1)
    return {
      data: {
        total: list.length,
        active: list.filter(e => e.status === 'ACTIVE').length,
        onLeave: list.filter(e => e.status === 'ON_LEAVE').length,
        inactive: list.filter(e => e.status === 'INACTIVE' || e.status === 'TERMINATED').length,
        newThisMonth: list.filter(e => e.joinDate && new Date(e.joinDate) >= monthAgo).length,
      },
    }
  },
  getHeadcountByDept: async () => {
    const data = unwrap<{ content?: Employee[] } | Employee[]>(await api.get('/api/v1/employees', { params: { size: 1000 } }))
    const list: Employee[] = Array.isArray(data) ? data : (data?.content ?? [])
    const counts = new Map<string, number>()
    list.forEach(e => { const d = e.departmentName || 'Unassigned'; counts.set(d, (counts.get(d) ?? 0) + 1) })
    return { data: Array.from(counts.entries()).map(([department, count]) => ({ department, count })) }
  },

  // ── Addresses ─────────────────────────────────────────────────────────────
  addresses: async (employeeId: string) =>
    unwrap<EmployeeAddress[]>(await api.get(`/api/v1/employees/${employeeId}/addresses`)),
  addAddress: async (employeeId: string, payload: Partial<EmployeeAddress>) =>
    unwrap<EmployeeAddress>(await api.post(`/api/v1/employees/${employeeId}/addresses`, payload)),
  updateAddress: async (employeeId: string, addressId: string, payload: Partial<EmployeeAddress>) =>
    unwrap<EmployeeAddress>(await api.put(`/api/v1/employees/${employeeId}/addresses/${addressId}`, payload)),
  deleteAddress: async (employeeId: string, addressId: string) => {
    await api.delete(`/api/v1/employees/${employeeId}/addresses/${addressId}`)
  },

  // ── Education ─────────────────────────────────────────────────────────────
  education: async (employeeId: string) =>
    unwrap<EmployeeEducation[]>(await api.get(`/api/v1/employees/${employeeId}/education`)),
  addEducation: async (employeeId: string, payload: Partial<EmployeeEducation>) =>
    unwrap<EmployeeEducation>(await api.post(`/api/v1/employees/${employeeId}/education`, payload)),
  updateEducation: async (employeeId: string, educationId: string, payload: Partial<EmployeeEducation>) =>
    unwrap<EmployeeEducation>(await api.put(`/api/v1/employees/${employeeId}/education/${educationId}`, payload)),
  deleteEducation: async (employeeId: string, educationId: string) => {
    await api.delete(`/api/v1/employees/${employeeId}/education/${educationId}`)
  },

  // ── Emergency contacts ────────────────────────────────────────────────────
  emergencyContacts: async (employeeId: string) =>
    unwrap<EmergencyContact[]>(await api.get(`/api/v1/employees/${employeeId}/emergency-contacts`)),
  addEmergencyContact: async (employeeId: string, payload: Partial<EmergencyContact>) =>
    unwrap<EmergencyContact>(await api.post(`/api/v1/employees/${employeeId}/emergency-contacts`, payload)),
  updateEmergencyContact: async (employeeId: string, contactId: string, payload: Partial<EmergencyContact>) =>
    unwrap<EmergencyContact>(await api.put(`/api/v1/employees/${employeeId}/emergency-contacts/${contactId}`, payload)),
  deleteEmergencyContact: async (employeeId: string, contactId: string) => {
    await api.delete(`/api/v1/employees/${employeeId}/emergency-contacts/${contactId}`)
  },

  // ── Family ────────────────────────────────────────────────────────────────
  family: async (employeeId: string) =>
    unwrap<FamilyMember[]>(await api.get(`/api/v1/employees/${employeeId}/family`)),
  addFamilyMember: async (employeeId: string, payload: Partial<FamilyMember>) =>
    unwrap<FamilyMember>(await api.post(`/api/v1/employees/${employeeId}/family`, payload)),
  updateFamilyMember: async (employeeId: string, memberId: string, payload: Partial<FamilyMember>) =>
    unwrap<FamilyMember>(await api.put(`/api/v1/employees/${employeeId}/family/${memberId}`, payload)),
  deleteFamilyMember: async (employeeId: string, memberId: string) => {
    await api.delete(`/api/v1/employees/${employeeId}/family/${memberId}`)
  },

  // ── Work history ──────────────────────────────────────────────────────────
  workHistory: async (employeeId: string) =>
    unwrap<WorkHistory[]>(await api.get(`/api/v1/employees/${employeeId}/work-history`)),
  addWorkHistory: async (employeeId: string, payload: Partial<WorkHistory>) =>
    unwrap<WorkHistory>(await api.post(`/api/v1/employees/${employeeId}/work-history`, payload)),
  updateWorkHistory: async (employeeId: string, historyId: string, payload: Partial<WorkHistory>) =>
    unwrap<WorkHistory>(await api.put(`/api/v1/employees/${employeeId}/work-history/${historyId}`, payload)),
  deleteWorkHistory: async (employeeId: string, historyId: string) => {
    await api.delete(`/api/v1/employees/${employeeId}/work-history/${historyId}`)
  },
}
