import { api, unwrap } from '@/lib/api'

export interface ExpenseClaim {
  id: string
  employeeId: string
  claimNumber: string
  title: string
  description?: string
  totalAmount: number
  currency: string
  categoryId?: string
  status: 'DRAFT' | 'SUBMITTED' | 'APPROVED' | 'REJECTED' | 'REIMBURSED' | 'CANCELLED'
  submittedAt?: string
  approvedBy?: string
  approvedAt?: string
  rejectedReason?: string
  reimbursedAt?: string
  paymentReference?: string
  items: ExpenseItem[]
}

export interface ExpenseItem {
  id: string
  reportId?: string
  categoryId?: string
  description: string
  spentOn: string
  amount: number
  receiptUri?: string
  vendor?: string
  taxAmount?: number
  paymentMethod?: string
}

export interface ExpenseCategory {
  id: string
  name: string
  code: string
  perDiemLimit?: number
  requiresReceipt?: boolean
  parentCategoryId?: string
}

/** Backend: ExpenseReportController @ /api/v1/expenses/reports,
 *  ExpenseItemController @ /api/v1/expenses/items,
 *  ExpenseCategoryController @ /api/v1/expenses/categories */
export const expenseService = {
  listClaims: async (params: { status?: string; page?: number; size?: number } = {}) =>
    unwrap(await api.get('/api/v1/expenses/reports', { params })),
  myClaims: async (employeeId: string) =>
    unwrap(await api.get<ExpenseClaim[]>(`/api/v1/expenses/reports/employee/${employeeId}`)),
  getClaim: async (id: string) => unwrap(await api.get<ExpenseClaim>(`/api/v1/expenses/reports/${id}`)),
  createClaim: async (payload: Partial<ExpenseClaim>) =>
    unwrap(await api.post<ExpenseClaim>('/api/v1/expenses/reports', payload)),
  updateClaim: async (id: string, payload: Partial<ExpenseClaim>) =>
    unwrap(await api.put<ExpenseClaim>(`/api/v1/expenses/reports/${id}`, payload)),
  deleteClaim: async (id: string) => { await api.delete(`/api/v1/expenses/reports/${id}`) },
  submit: async (id: string) =>
    unwrap(await api.post<ExpenseClaim>(`/api/v1/expenses/reports/${id}/submit`)),
  approve: async (id: string, notes?: string) =>
    unwrap(await api.post<ExpenseClaim>(`/api/v1/expenses/reports/${id}/approve`, { notes })),
  reject: async (id: string, reason: string) =>
    unwrap(await api.post<ExpenseClaim>(`/api/v1/expenses/reports/${id}/reject`, { reason })),

  // Items (/api/v1/expenses/items)
  itemsForReport: async (reportId: string) => unwrap(await api.get<ExpenseItem[]>(`/api/v1/expenses/items/report/${reportId}`)),
  addItem: async (payload: Partial<ExpenseItem>) => unwrap(await api.post<ExpenseItem>('/api/v1/expenses/items', payload)),
  updateItem: async (id: string, payload: Partial<ExpenseItem>) => unwrap(await api.put<ExpenseItem>(`/api/v1/expenses/items/${id}`, payload)),
  deleteItem: async (id: string) => { await api.delete(`/api/v1/expenses/items/${id}`) },

  listAllItems: async () => unwrap<ExpenseItem[]>(await api.get('/api/v1/expenses/items')),
  getItem: async (id: string) => unwrap<ExpenseItem>(await api.get(`/api/v1/expenses/items/${id}`)),

  // Categories (/api/v1/expenses/categories)
  categories: async () => unwrap(await api.get<ExpenseCategory[]>('/api/v1/expenses/categories')),
  categoriesAll: async () => unwrap<ExpenseCategory[]>(await api.get('/api/v1/expenses/categories/all')),
  getCategory: async (id: string) => unwrap<ExpenseCategory>(await api.get(`/api/v1/expenses/categories/${id}`)),
  createCategory: async (payload: Partial<ExpenseCategory>) =>
    unwrap(await api.post<ExpenseCategory>('/api/v1/expenses/categories', payload)),
  updateCategory: async (id: string, payload: Partial<ExpenseCategory>) =>
    unwrap<ExpenseCategory>(await api.put(`/api/v1/expenses/categories/${id}`, payload)),
  deleteCategory: async (id: string) => { await api.delete(`/api/v1/expenses/categories/${id}`) },

  // Advances (/api/v1/expenses/advances)
  advances: async () => unwrap(await api.get('/api/v1/expenses/advances')),
  advancesForEmployee: async (employeeId: string) => unwrap(await api.get(`/api/v1/expenses/advances/employee/${employeeId}`)),
  getAdvance: async (id: string) => unwrap(await api.get(`/api/v1/expenses/advances/${id}`)),
  createAdvance: async (payload: Record<string, unknown>) => unwrap(await api.post('/api/v1/expenses/advances', payload)),
  updateAdvance: async (id: string, payload: Record<string, unknown>) => unwrap(await api.put(`/api/v1/expenses/advances/${id}`, payload)),
  deleteAdvance: async (id: string) => { await api.delete(`/api/v1/expenses/advances/${id}`) },
  approveAdvance: async (id: string) => unwrap(await api.post(`/api/v1/expenses/advances/${id}/approve`)),

  // Policies (/api/v1/expenses/policies)
  policies: async () => unwrap(await api.get('/api/v1/expenses/policies')),
  policiesAll: async () => unwrap(await api.get('/api/v1/expenses/policies/all')),
  getPolicy: async (id: string) => unwrap(await api.get(`/api/v1/expenses/policies/${id}`)),
  createPolicy: async (payload: Record<string, unknown>) => unwrap(await api.post('/api/v1/expenses/policies', payload)),
  updatePolicy: async (id: string, payload: Record<string, unknown>) => unwrap(await api.put(`/api/v1/expenses/policies/${id}`, payload)),
  deletePolicy: async (id: string) => { await api.delete(`/api/v1/expenses/policies/${id}`) },

  // Receipt OCR (/api/v1/expenses/ocr)
  parseReceipt: async (file: File) => {
    const fd = new FormData(); fd.append('file', file)
    return unwrap(await api.post('/api/v1/expenses/ocr', fd, { headers: { 'Content-Type': 'multipart/form-data' } }))
  },
  parseReceiptByUri: async (uri: string) =>
    unwrap(await api.post('/api/v1/expenses/ocr/by-uri', { uri })),
}
