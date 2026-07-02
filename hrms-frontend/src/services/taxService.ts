import { api, unwrap } from '@/lib/api'

export type TaxRegime = 'OLD' | 'NEW'
export type DeclarationStatus = 'DRAFT' | 'SUBMITTED' | 'VERIFIED'
export type DeclarationProofStatus = 'PENDING' | 'APPROVED' | 'REJECTED'

/** Backend: TaxDeclarationDto.ProofSummary */
export interface DeclarationProof {
  id: string
  section: string
  description?: string
  declaredAmount: number
  proofAmount?: number
  proofUrl?: string
  status: DeclarationProofStatus
  verifiedBy?: string
  createdAt?: string
}

/** Backend: TaxDeclarationDto.AddProofRequest */
export interface AddDeclarationProofRequest {
  section: string
  description?: string
  declaredAmount: number
  proofAmount?: number
  proofUrl?: string
}

export interface TaxDeclaration {
  id: string
  employeeId: string
  financialYear: string
  regime: TaxRegime
  status: DeclarationStatus
  section80c: number
  section80d: number
  section80e: number
  section80g: number
  section24b: number
  hraExemptionClaimed: number
  ltaClaimed: number
  otherIncome: number
  previousEmployerIncome: number
  previousEmployerTds: number
  proofs?: DeclarationProof[]
  createdAt?: string
  updatedAt?: string
}

export interface InvestmentProof {
  id: string
  employeeId: string
  declarationId?: string
  financialYear: string
  section: string
  subSection?: string
  instrumentType?: string
  claimedAmount: number
  verifiedAmount?: number
  documentUri: string
  documentType?: string
  documentDate?: string
  notes?: string
  status: 'UPLOADED' | 'UNDER_REVIEW' | 'ACCEPTED' | 'REJECTED' | 'NEEDS_MORE_INFO'
  reviewerComment?: string
}

export interface Loan {
  id: string
  employeeId: string
  loanType: string
  principalAmount: number
  emi: number
  tenureMonths: number
  interestRate?: number
  disbursedOn?: string
  outstandingBalance: number
  status: 'PENDING' | 'APPROVED' | 'DISBURSED' | 'IN_REPAYMENT' | 'CLOSED' | 'REJECTED'
}

/** Backend: TaxDeclarationController @ /api/v1/tax/declarations,
 *  InvestmentProofController @ /api/payroll/investment-proofs,
 *  LoanController @ /api/v1/payroll/loans,
 *  StatutoryReturnController @ /api/v1/compliance/statutory-returns */
export const taxService = {
  // ── Tax declarations (/api/v1/tax/declarations) ─────────────────────────
  myDeclarations: async () =>
    unwrap<TaxDeclaration[]>(await api.get('/api/v1/tax/declarations/me')),
  myDeclaration: async (fy: string): Promise<TaxDeclaration | null> => {
    const list = unwrap<TaxDeclaration[]>(await api.get('/api/v1/tax/declarations/me'))
    return (list || []).find(d => d.financialYear === fy) ?? null
  },
  getDeclaration: async (declarationId: string) =>
    unwrap<TaxDeclaration>(await api.get(`/api/v1/tax/declarations/me/${declarationId}`)),
  saveDeclaration: async (payload: Partial<TaxDeclaration>) =>
    unwrap<TaxDeclaration>(await api.post('/api/v1/tax/declarations', payload)),
  listSubmitted: async (financialYear: string) =>
    unwrap<TaxDeclaration[]>(await api.get('/api/v1/tax/declarations', { params: { financialYear } })),
  verifyDeclaration: async (declarationId: string) =>
    unwrap<TaxDeclaration>(await api.post(`/api/v1/tax/declarations/${declarationId}/verify`)),

  // ── Declaration proofs (nested under a declaration) ─────────────────────
  declarationProofs: async (declarationId: string) =>
    unwrap(await api.get(`/api/v1/tax/declarations/${declarationId}/proofs`)),
  addDeclarationProof: async (declarationId: string, payload: Record<string, unknown>) =>
    unwrap(await api.post(`/api/v1/tax/declarations/${declarationId}/proofs`, payload)),
  approveDeclarationProof: async (declarationId: string, proofId: string) =>
    unwrap(await api.post(`/api/v1/tax/declarations/${declarationId}/proofs/${proofId}/approve`)),
  rejectDeclarationProof: async (declarationId: string, proofId: string) =>
    unwrap(await api.post(`/api/v1/tax/declarations/${declarationId}/proofs/${proofId}/reject`)),

  // ── Investment proofs (/api/payroll/investment-proofs) ──────────────────
  uploadProof: async (file: File, meta: Partial<InvestmentProof> & { employeeId: string; financialYear: string; section: string; claimedAmount: number }) => {
    const fd = new FormData()
    fd.append('file', file)
    fd.append('meta', new Blob([JSON.stringify(meta)], { type: 'application/json' }))
    return unwrap<InvestmentProof>(await api.post('/api/payroll/investment-proofs', fd, { headers: { 'Content-Type': 'multipart/form-data' } }))
  },
  myProofs: async (employeeId: string, financialYear: string) =>
    unwrap<InvestmentProof[]>(await api.get('/api/payroll/investment-proofs/me', { params: { employeeId, financialYear } })),
  proofQueue: async () =>
    unwrap<InvestmentProof[]>(await api.get('/api/payroll/investment-proofs/queue')),
  verifyProof: async (id: string, verifiedAmount: number, verifiedBy: string, comment?: string) =>
    unwrap(await api.post(`/api/payroll/investment-proofs/${id}/verify`, { verifiedAmount, verifiedBy, comment })),
  rejectProof: async (id: string, verifiedBy: string, comment: string) =>
    unwrap(await api.post(`/api/payroll/investment-proofs/${id}/reject`, { verifiedBy, comment })),

  // ── Loans (/api/v1/payroll/loans) ───────────────────────────────────────
  myLoans: async () => unwrap<Loan[]>(await api.get('/api/v1/payroll/loans/me')),
  loansForEmployee: async (employeeId: string) =>
    unwrap<Loan[]>(await api.get(`/api/v1/payroll/loans/employee/${employeeId}`)),
  applyLoan: async (employeeId: string, payload: Partial<Loan>) =>
    unwrap<Loan>(await api.post(`/api/v1/payroll/loans/employee/${employeeId}`, payload)),
  getLoan: async (loanId: string) => unwrap<Loan>(await api.get(`/api/v1/payroll/loans/${loanId}`)),
  loanSchedule: async (loanId: string) => unwrap(await api.get(`/api/v1/payroll/loans/${loanId}/schedule`)),
  myLoanSchedule: async (loanId: string) => unwrap(await api.get(`/api/v1/payroll/loans/me/${loanId}/schedule`)),
  closeLoan: async (loanId: string) => unwrap(await api.post(`/api/v1/payroll/loans/${loanId}/close`)),

  // ── Statutory returns (/api/v1/compliance/statutory-returns) ────────────
  form24Q: async (payload: { financialYear: string; quarter: 'Q1' | 'Q2' | 'Q3' | 'Q4' }) =>
    unwrap(await api.post('/api/v1/compliance/statutory-returns/form24q', payload)),
}
