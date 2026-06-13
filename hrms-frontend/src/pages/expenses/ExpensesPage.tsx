import { Receipt, Send, Check, X } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { ResourcePage } from '@/components/ui/resource-page'
import { expenseService, type ExpenseClaim } from '@/services/expenseService'
import { formatDate } from '@/lib/utils'

export function ExpensesPage() {
  return (
    <ResourcePage<ExpenseClaim & Record<string, unknown>>
      title="Expense Claims"
      description="Create, submit, approve and reject reimbursement claims"
      icon={<Receipt className="h-10 w-10" />}
      queryKey={['expenses']}
      fetcher={() => expenseService.listClaims()}
      rowHref={c => `/expenses/${c.id}`}
      filters={{ status: ['DRAFT', 'SUBMITTED', 'APPROVED', 'REJECTED', 'REIMBURSED'] }}
      columns={[
        { key: 'claimNumber', label: 'Claim' },
        { key: 'title', label: 'Title' },
        { key: 'totalAmount', label: 'Amount', align: 'right', render: c => `${c.currency || '₹'} ${Number(c.totalAmount ?? 0).toLocaleString()}` },
        { key: 'status', label: 'Status', render: c => <Badge variant={c.status === 'REJECTED' ? 'destructive' : c.status === 'APPROVED' || c.status === 'REIMBURSED' ? 'success' : 'warning'}>{String(c.status)}</Badge> },
        { key: 'submittedAt', label: 'Submitted', render: c => c.submittedAt ? formatDate(String(c.submittedAt)) : '—' },
      ]}
      formFields={[
        { name: 'title', label: 'Title', type: 'text', required: true, span: 2 },
        { name: 'currency', label: 'Currency', type: 'text', defaultValue: 'INR' },
        { name: 'description', label: 'Description', type: 'textarea', span: 2 },
      ]}
      createTitle="New expense claim"
      onCreate={v => expenseService.createClaim(v)}
      onUpdate={(id, v) => expenseService.updateClaim(id, v)}
      onDelete={id => expenseService.deleteClaim(id)}
      rowActions={c => [
        { label: 'Submit', icon: <Send className="h-3.5 w-3.5" />, show: c.status === 'DRAFT', run: () => expenseService.submit(c.id) },
        { label: 'Approve', icon: <Check className="h-3.5 w-3.5" />, show: c.status === 'SUBMITTED', run: () => expenseService.approve(c.id) },
        { label: 'Reject', icon: <X className="h-3.5 w-3.5" />, show: c.status === 'SUBMITTED', destructive: true, confirm: 'Reject this claim?', run: () => expenseService.reject(c.id, 'Rejected') },
      ]}
    />
  )
}
