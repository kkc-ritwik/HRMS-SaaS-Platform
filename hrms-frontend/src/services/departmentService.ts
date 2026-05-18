import { coreHrClient } from '@/lib/api'

export interface Department {
  id: string
  name: string
  code?: string
  description?: string
  parentId?: string
  parentName?: string
  headId?: string
  headName?: string
  employeeCount?: number
  createdAt: string
  updatedAt: string
}

export interface Designation {
  id: string
  name: string
  code?: string
  level?: number
  description?: string
  departmentId?: string
  departmentName?: string
  employeeCount?: number
  createdAt: string
  updatedAt: string
}

export interface Location {
  id: string
  name: string
  code?: string
  address?: string
  city?: string
  state?: string
  country?: string
  pincode?: string
  phone?: string
  employeeCount?: number
  createdAt: string
  updatedAt: string
}

export const departmentService = {
  list: async (params?: { search?: string; page?: number; pageSize?: number }) => {
    const response = await coreHrClient.get('/api/v1/departments', { params })
    return response.data
  },
  getById: async (id: string) => {
    const response = await coreHrClient.get(`/api/v1/departments/${id}`)
    return response.data
  },
  create: async (payload: Partial<Department>) => {
    const response = await coreHrClient.post('/api/v1/departments', payload)
    return response.data
  },
  update: async (id: string, payload: Partial<Department>) => {
    const response = await coreHrClient.put(`/api/v1/departments/${id}`, payload)
    return response.data
  },
  delete: async (id: string) => {
    await coreHrClient.delete(`/api/v1/departments/${id}`)
  },
}

export const designationService = {
  list: async (params?: { search?: string; page?: number; pageSize?: number }) => {
    const response = await coreHrClient.get('/api/v1/designations', { params })
    return response.data
  },
  create: async (payload: Partial<Designation>) => {
    const response = await coreHrClient.post('/api/v1/designations', payload)
    return response.data
  },
  update: async (id: string, payload: Partial<Designation>) => {
    const response = await coreHrClient.put(`/api/v1/designations/${id}`, payload)
    return response.data
  },
  delete: async (id: string) => {
    await coreHrClient.delete(`/api/v1/designations/${id}`)
  },
}

export const locationService = {
  list: async (params?: { search?: string; page?: number; pageSize?: number }) => {
    const response = await coreHrClient.get('/api/v1/locations', { params })
    return response.data
  },
  create: async (payload: Partial<Location>) => {
    const response = await coreHrClient.post('/api/v1/locations', payload)
    return response.data
  },
  update: async (id: string, payload: Partial<Location>) => {
    const response = await coreHrClient.put(`/api/v1/locations/${id}`, payload)
    return response.data
  },
  delete: async (id: string) => {
    await coreHrClient.delete(`/api/v1/locations/${id}`)
  },
}
