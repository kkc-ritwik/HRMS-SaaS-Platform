import { payrollClient } from '@/lib/api'

export interface SalaryStructure {
  id: string
  name: string
  description?: string
  components: SalaryComponent[]
  createdAt: string
}

export interface SalaryComponent {
  id: string
  name: string
  type: 'EARNING' | 'DEDUCTION' | 'BENEFIT'
  calculationType: 'FIXED' | 'PERCENTAGE'
  value: number
  percentageOf?: string
  taxable: boolean
}

export interface PayRun {
  id: string
  name: string
  period: string
  month: number
  year: number
  status: 'DRAFT' | 'PROCESSING' | 'COMPLETED' | 'FAILED'
  employeeCount: number
  totalGross: number
  totalDeductions: number
  totalNet: number
  processedAt?: string
  createdAt: string
}

export interface Payslip {
  id: string
  payRunId: string
  employeeId: string
  employeeName: string
  employeeId_code: string
  period: string
  month: number
  year: number
  basicSalary: number
  grossSalary: number
  totalDeductions: number
  netSalary: number
  earnings: Array<{ name: string; amount: number }>
  deductions: Array<{ name: string; amount: number }>
  status: 'GENERATED' | 'PAID'
  paidAt?: string
  createdAt: string
}

export const payrollService = {
  // Salary Structures
  listSalaryStructures: async () => {
    const response = await payrollClient.get('/api/v1/salary-structures')
    return response.data
  },
  createSalaryStructure: async (payload: Partial<SalaryStructure>) => {
    const response = await payrollClient.post('/api/v1/salary-structures', payload)
    return response.data
  },
  updateSalaryStructure: async (id: string, payload: Partial<SalaryStructure>) => {
    const response = await payrollClient.put(`/api/v1/salary-structures/${id}`, payload)
    return response.data
  },
  deleteSalaryStructure: async (id: string) => {
    await payrollClient.delete(`/api/v1/salary-structures/${id}`)
  },

  // Pay Runs
  listPayRuns: async (params?: { page?: number; pageSize?: number; year?: number }) => {
    const response = await payrollClient.get('/api/v1/pay-runs', { params })
    return response.data
  },
  createPayRun: async (payload: { month: number; year: number; name?: string }) => {
    const response = await payrollClient.post('/api/v1/pay-runs', payload)
    return response.data
  },
  processPayRun: async (id: string) => {
    const response = await payrollClient.post(`/api/v1/pay-runs/${id}/process`)
    return response.data
  },
  getPayRun: async (id: string) => {
    const response = await payrollClient.get(`/api/v1/pay-runs/${id}`)
    return response.data
  },

  // Payslips
  listPayslips: async (params?: { employeeId?: string; page?: number; pageSize?: number; year?: number }) => {
    const response = await payrollClient.get('/api/v1/payslips', { params })
    return response.data
  },
  getPayslip: async (id: string) => {
    const response = await payrollClient.get(`/api/v1/payslips/${id}`)
    return response.data
  },
  downloadPayslip: async (id: string) => {
    const response = await payrollClient.get(`/api/v1/payslips/${id}/download`, {
      responseType: 'blob',
    })
    return response.data
  },
}
