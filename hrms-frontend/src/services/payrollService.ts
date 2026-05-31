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
  status: 'DRAFT' | 'PROCESSING' | 'COMPLETED' | 'LOCKED' | 'PAID' | 'FAILED'
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

/** Backend: PayrollRunController @ /api/v1/payroll/runs, PayslipController @ /api/v1/payslips,
 *  SalaryStructureController @ /api/v1/salary/structures */
export const payrollService = {
  // ── Salary Structures (/api/v1/salary/structures) ───────────────────────
  listSalaryStructures: async () => (await payrollClient.get('/api/v1/salary/structures')).data,
  listActiveStructures: async () => (await payrollClient.get('/api/v1/salary/structures/active')).data,
  createSalaryStructure: async (payload: Partial<SalaryStructure>) => (await payrollClient.post('/api/v1/salary/structures', payload)).data,
  updateSalaryStructure: async (id: string, payload: Partial<SalaryStructure>) => (await payrollClient.put(`/api/v1/salary/structures/${id}`, payload)).data,
  addComponent: async (id: string, payload: Partial<SalaryComponent>) => (await payrollClient.post(`/api/v1/salary/structures/${id}/components`, payload)).data,
  deleteSalaryStructure: async (id: string) => { await payrollClient.delete(`/api/v1/salary/structures/${id}`) },

  // ── Pay Runs (/api/v1/payroll/runs) ─────────────────────────────────────
  listPayRuns: async (params?: { year?: number }) => (await payrollClient.get('/api/v1/payroll/runs', { params })).data,
  getPayRun: async (id: string) => (await payrollClient.get(`/api/v1/payroll/runs/${id}`)).data,
  createPayRun: async (payload: { month: number; year: number; name?: string }) => (await payrollClient.post('/api/v1/payroll/runs', payload)).data,
  processPayRun: async (id: string) => (await payrollClient.post(`/api/v1/payroll/runs/${id}/process`)).data,
  lockPayRun: async (id: string) => (await payrollClient.post(`/api/v1/payroll/runs/${id}/lock`)).data,
  markPaid: async (id: string) => (await payrollClient.post(`/api/v1/payroll/runs/${id}/mark-paid`)).data,

  // ── Payslips (/api/v1/payslips) ─────────────────────────────────────────
  listPayslips: async () => (await payrollClient.get('/api/v1/payslips/me')).data,
  payslipsForRun: async (runId: string) => (await payrollClient.get(`/api/v1/payslips/run/${runId}`)).data,
  getPayslip: async (id: string) => (await payrollClient.get(`/api/v1/payslips/${id}`)).data,
  getMyPayslip: async (id: string) => (await payrollClient.get(`/api/v1/payslips/me/${id}`)).data,
  downloadPayslip: async (id: string): Promise<Blob> => {
    const res = await payrollClient.get(`/api/v1/payslips/me/${id}/pdf`, { responseType: 'blob' })
    return res.data
  },
}
