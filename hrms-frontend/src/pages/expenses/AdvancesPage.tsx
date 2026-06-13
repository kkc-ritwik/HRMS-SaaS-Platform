import { Banknote, CheckCircle2 } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { ResourcePage } from '@/components/ui/resource-page'
import { advanceService } from '@/services/extendedServices'
import { formatDate } from '@/lib/utils'

interface Advance extends Record<string, unknown> { id: string; employeeId?: string; employeeName?: string; amount?: number; reason?: string; status: string; requestedAt?: string }

export function AdvancesPage() {
  return (
    <ResourcePage<Advance>
      title="Cash Advances"
      description="Pre-trip / pre-expense cash advances — request and settle"
      icon={<Banknote className="h-10 w-10" />}
      queryKey={['advances']}
      fetcher={() => advanceService.list()}
      filters={{ status: ['REQUESTED', 'APPROVED', 'DISBURSED', 'SETTLED', 'REJECTED'] }}
      columns={[
        { key: 'employeeName', label: 'Employee', render: a => String(a.employeeName ?? a.employeeId ?? '—') },
        { key: 'amount', label: 'Amount', align: 'right', render: a => a.amount ? `₹${Number(a.amount).toLocaleString()}` : '—' },
        { key: 'reason', label: 'Reason' },
        { key: 'status', label: 'Status', render: a => <Badge variant={a.status === 'REJECTED' ? 'destructive' : a.status === 'SETTLED' ? 'success' : 'warning'}>{String(a.status)}</Badge> },
        { key: 'requestedAt', label: 'Requested', render: a => a.requestedAt ? formatDate(String(a.requestedAt)) : '—' },
      ]}
      formFields={[
        { name: 'amount', label: 'Amount', type: 'currency', required: true },
        { name: 'reason', label: 'Reason', type: 'textarea', required: true, span: 2 },
        { name: 'neededBy', label: 'Needed by', type: 'date' },
      ]}
      createTitle="Request advance"
      onCreate={v => advanceService.request(v)}
      rowActions={a => [
        { label: 'Settle', icon: <CheckCircle2 className="h-3.5 w-3.5" />, show: a.status === 'DISBURSED', run: () => advanceService.settle(a.id, '') },
      ]}
    />
  )
}
