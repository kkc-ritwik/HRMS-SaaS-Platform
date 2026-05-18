import { coreHrClient } from '@/lib/api'

export interface Employee {
  id: string
  employeeId: string
  firstName: string
  lastName: string
  fullName: string
  email: string
  phone?: string
  avatar?: string
  departmentId: string
  departmentName?: string
  designationId: string
  designationName?: string
  locationId?: string
  locationName?: string
  managerId?: string
  managerName?: string
  employmentType: 'FULL_TIME' | 'PART_TIME' | 'CONTRACT' | 'INTERN'
  status: 'ACTIVE' | 'INACTIVE' | 'ON_LEAVE' | 'TERMINATED'
  joinDate: string
  probationEndDate?: string
  exitDate?: string
  gender: 'MALE' | 'FEMALE' | 'OTHER'
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
  emergencyContact?: {
    name: string
    phone: string
    relation: string
  }
  createdAt: string
  updatedAt: string
}

export interface EmployeeListParams {
  page?: number
  pageSize?: number
  search?: string
  departmentId?: string
  status?: string
  employmentType?: string
  sortBy?: string
  sortOrder?: 'asc' | 'desc'
}

export interface EmployeeListResponse {
  data: {
    employees: Employee[]
    total: number
    page: number
    pageSize: number
  }
}

export const employeeService = {
  list: async (params: EmployeeListParams = {}): Promise<EmployeeListResponse> => {
    const response = await coreHrClient.get<EmployeeListResponse>('/api/v1/employees', { params })
    return response.data
  },

  getById: async (id: string): Promise<{ data: Employee }> => {
    const response = await coreHrClient.get<{ data: Employee }>(`/api/v1/employees/${id}`)
    return response.data
  },

  create: async (payload: Partial<Employee>): Promise<{ data: Employee }> => {
    const response = await coreHrClient.post<{ data: Employee }>('/api/v1/employees', payload)
    return response.data
  },

  update: async (id: string, payload: Partial<Employee>): Promise<{ data: Employee }> => {
    const response = await coreHrClient.put<{ data: Employee }>(`/api/v1/employees/${id}`, payload)
    return response.data
  },

  delete: async (id: string): Promise<void> => {
    await coreHrClient.delete(`/api/v1/employees/${id}`)
  },

  getStats: async (): Promise<{
    data: {
      total: number
      active: number
      onLeave: number
      inactive: number
      newThisMonth: number
    }
  }> => {
    const response = await coreHrClient.get('/api/v1/employees/stats')
    return response.data
  },

  getHeadcountByDept: async (): Promise<{
    data: Array<{ department: string; count: number }>
  }> => {
    const response = await coreHrClient.get('/api/v1/employees/headcount-by-dept')
    return response.data
  },

  bulkImport: async (file: File): Promise<{ data: { imported: number; failed: number } }> => {
    const formData = new FormData()
    formData.append('file', file)
    const response = await coreHrClient.post('/api/v1/employees/bulk-import', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    return response.data
  },
}
