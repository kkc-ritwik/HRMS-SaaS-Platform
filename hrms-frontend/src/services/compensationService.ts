import { api, unwrap } from '@/lib/api'

export interface PayGrade {
  id: string
  code: string
  name: string
  level?: string
  minSalary?: number
  maxSalary?: number
  midSalary?: number
  currency?: string
  active: boolean
}

export interface CompensationPlan {
  id: string
  employeeId: string
  effectiveFrom: string
  effectiveTo?: string
  ctc: number
  basic: number
  hra: number
  special: number
  variablePay?: number
  bonus?: number
  payGradeId?: string
  status: 'DRAFT' | 'ACTIVE' | 'EXPIRED'
}

export interface Benefit {
  id: string
  code: string
  name: string
  category: string
  premiumAmount?: number
  premiumPaidBy: 'EMPLOYER' | 'EMPLOYEE' | 'SHARED'
  active: boolean
}

export interface SalaryBand {
  id: string
  bandCode: string
  level: string
  jobFamily?: string
  minSalary: number
  midSalary: number
  maxSalary: number
  currency: string
}

export interface MarketBenchmark {
  id: string
  provider: string
  roleCode: string
  level?: string
  country: string
  currency: string
  p25TotalCash?: number
  p50TotalCash?: number
  p75TotalCash?: number
  surveyDate: string
}

export const compensationService = {
  // Pay grades
  listGrades: async () => unwrap(await api.get<PayGrade[]>('/api/v1/compensation/pay-grades')),
  createGrade: async (payload: Partial<PayGrade>) => unwrap(await api.post<PayGrade>('/api/v1/compensation/pay-grades', payload)),

  // Compensation plans
  listPlans: async (employeeId?: string) =>
    unwrap(await api.get<CompensationPlan[]>('/api/v1/compensation/plans', { params: { employeeId } })),
  getPlan: async (id: string) => unwrap(await api.get<CompensationPlan>(`/api/v1/compensation/plans/${id}`)),
  createPlan: async (payload: Partial<CompensationPlan>) =>
    unwrap(await api.post<CompensationPlan>('/api/v1/compensation/plans', payload)),

  // Benefits
  listBenefits: async () => unwrap(await api.get<Benefit[]>('/api/v1/compensation/benefits')),
  enrollBenefit: async (payload: { employeeId: string; benefitId: string }) =>
    unwrap(await api.post('/api/v1/compensation/employee-benefits', payload)),

  // Bands
  listBands: async () => unwrap(await api.get<SalaryBand[]>('/api/v1/compensation/bands')),

  // Market benchmarks
  benchmarkLookup: async (roleCode: string, country = 'IN', level?: string) =>
    unwrap(await api.get<MarketBenchmark[]>('/api/compensation/benchmark', { params: { roleCode, country, level } })),
  compaRatio: async (currentCtc: number, roleCode: string, country = 'IN') =>
    unwrap(await api.get('/api/compensation/benchmark/compa-ratio', { params: { currentCtc, roleCode, country } })),

  // Total rewards statement
  totalRewards: async (employeeId: string, fy: string) =>
    unwrap(await api.get(`/api/v1/compensation/total-rewards/${employeeId}`, { params: { fy } })),
}
