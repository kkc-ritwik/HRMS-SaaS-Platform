import { Banknote, CheckCircle2 } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { ResourcePage } from '@/components/ui/resource-page'
import { expenseService } from '@/services/expenseService'
import { useAuthStore } from '@/store/authStore'
import { formatDate } from '@/lib/utils'

interface Advance extends Record<string, unknown> {
  id: string
  employeeId?: string
  amount?: number
  currency?: string
  purpose?: string
  status: string
  requestedAt?: string
}

export function AdvancesPage() {
  const user = useAuthStore(s => s.user)
  const employeeId = user?.employeeId || user?.id || ''

  return (
    <ResourcePage<Advance>
      title="Cash Advances"
      description="Pre-trip / pre-expense cash advances — request, approve and settle"
      icon={<Banknote className="h-10 w-10" />}
      queryKey={['advances']}
      fetcher={() => expenseService.advances()}
      filters={{ status: ['REQUESTED', 'APPROVED', 'DISBURSED', 'SETTLED', 'REJECTED'] }}
      columns={[
        { key: 'employeeId', label: 'Employee', render: a => String(a.employeeId ?? '—') },
        { key: 'amount', label: 'Amount', align: 'right', render: a => a.amount != null ? `${a.currency || ''} ${Number(a.amount).toLocaleString()}`.trim() : '—' },
        { key: 'purpose', label: 'Purpose' },
        { key: 'status', label: 'Status', render: a => <Badge variant={a.status === 'REJECTED' ? 'destructive' : a.status === 'SETTLED' ? 'success' : a.status === 'APPROVED' || a.status === 'DISBURSED' ? 'default' : 'warning'}>{String(a.status)}</Badge> },
        { key: 'requestedAt', label: 'Requested', render: a => a.requestedAt ? formatDate(String(a.requestedAt)) : '—' },
      ]}
      formFields={[
        { name: 'amount', label: 'Amount', type: 'currency', required: true },
        { name: 'currency', label: 'Currency', type: 'text' },
        { name: 'purpose', label: 'Purpose', type: 'textarea', required: true, span: 2 },
      ]}
      createTitle="Request advance"
      onCreate={v => expenseService.createAdvance({
        employeeId,
        amount: v.amount,
        currency: v.currency || 'USD',
        purpose: v.purpose,
      })}
      onDelete={id => expenseService.deleteAdvance(id)}
      rowActions={a => [
        {
          label: 'Approve',
          icon: <CheckCircle2 className="h-3.5 w-3.5" />,
          show: a.status === 'REQUESTED',
          run: () => expenseService.approveAdvance(a.id, employeeId),
        },
      ]}
    />
  )
}
