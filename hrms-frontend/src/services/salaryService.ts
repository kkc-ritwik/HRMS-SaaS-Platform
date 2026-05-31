import { api, unwrap } from '@/lib/api'

export interface SalaryComponent {
  id: string
  name: string
  code?: string
  type: 'EARNING' | 'DEDUCTION' | 'BENEFIT' | 'REIMBURSEMENT'
  calculationType: 'FIXED' | 'PERCENTAGE' | 'FORMULA'
  value?: number
  percentageOf?: string
  formula?: string
  taxable?: boolean
  active?: boolean
}

export interface SalaryStructure {
  id: string
  name: string
  description?: string
  active?: boolean
  components?: Array<SalaryComponent & { amount?: number }>
}

export interface EmployeeSalary {
  id: string
  employeeId: string
  structureId?: string
  structureName?: string
  ctc: number
  effectiveFrom: string
  effectiveTo?: string
  current?: boolean
  components?: Array<{ name: string; amount: number; type: string }>
}

/** Salary components @ /api/v1/salary/components */
export const salaryComponentService = {
  list: async () => unwrap<SalaryComponent[]>(await api.get('/api/v1/salary/components')),
  active: async () => unwrap<SalaryComponent[]>(await api.get('/api/v1/salary/components/active')),
  get: async (id: string) => unwrap<SalaryComponent>(await api.get(`/api/v1/salary/components/${id}`)),
  create: async (payload: Partial<SalaryComponent>) => unwrap<SalaryComponent>(await api.post('/api/v1/salary/components', payload)),
  update: async (id: string, payload: Partial<SalaryComponent>) => unwrap<SalaryComponent>(await api.put(`/api/v1/salary/components/${id}`, payload)),
  remove: async (id: string) => { await api.delete(`/api/v1/salary/components/${id}`) },
}

/** Salary structures @ /api/v1/salary/structures */
export const salaryStructureService = {
  list: async () => unwrap<SalaryStructure[]>(await api.get('/api/v1/salary/structures')),
  active: async () => unwrap<SalaryStructure[]>(await api.get('/api/v1/salary/structures/active')),
  get: async (id: string) => unwrap<SalaryStructure>(await api.get(`/api/v1/salary/structures/${id}`)),
  create: async (payload: Partial<SalaryStructure>) => unwrap<SalaryStructure>(await api.post('/api/v1/salary/structures', payload)),
  update: async (id: string, payload: Partial<SalaryStructure>) => unwrap<SalaryStructure>(await api.put(`/api/v1/salary/structures/${id}`, payload)),
  remove: async (id: string) => { await api.delete(`/api/v1/salary/structures/${id}`) },
  addComponent: async (id: string, payload: Record<string, unknown>) =>
    unwrap(await api.post(`/api/v1/salary/structures/${id}/components`, payload)),
  removeComponent: async (id: string, componentId: string) => {
    await api.delete(`/api/v1/salary/structures/${id}/components/${componentId}`)
  },
}

/** Employee salary @ /api/v1/salary/employee */
export const employeeSalaryService = {
  forEmployee: async (employeeId: string) => unwrap<EmployeeSalary[]>(await api.get(`/api/v1/salary/employee/${employeeId}`)),
  current: async (employeeId: string) => unwrap<EmployeeSalary>(await api.get(`/api/v1/salary/employee/${employeeId}/current`)),
  history: async (employeeId: string) => unwrap<EmployeeSalary[]>(await api.get(`/api/v1/salary/employee/${employeeId}/history`)),
  get: async (employeeId: string, salaryId: string) =>
    unwrap<EmployeeSalary>(await api.get(`/api/v1/salary/employee/${employeeId}/salary/${salaryId}`)),
  assign: async (employeeId: string, payload: Partial<EmployeeSalary>) =>
    unwrap<EmployeeSalary>(await api.post(`/api/v1/salary/employee/${employeeId}`, payload)),
}

/** Statutory tax config @ /api/v1/salary/tax-config */
export const taxConfigService = {
  esi: async () => unwrap(await api.get('/api/v1/salary/tax-config/esi')),
  pf: async () => unwrap(await api.get('/api/v1/salary/tax-config/pf')),
  pt: async (state: string) => unwrap(await api.get(`/api/v1/salary/tax-config/pt/${state}`)),
  saveEsi: async (payload: Record<string, unknown>) => unwrap(await api.post('/api/v1/salary/tax-config/esi', payload)),
  savePf: async (payload: Record<string, unknown>) => unwrap(await api.post('/api/v1/salary/tax-config/pf', payload)),
  savePt: async (payload: Record<string, unknown>) => unwrap(await api.post('/api/v1/salary/tax-config/pt', payload)),
  deletePt: async (id: string) => { await api.delete(`/api/v1/salary/tax-config/pt/${id}`) },
}
