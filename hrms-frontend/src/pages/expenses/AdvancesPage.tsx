import { useQuery } from '@tanstack/react-query'
import { Banknote } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { DataList } from '@/components/ui/data-list'
import { advanceService } from '@/services/extendedServices'

interface Advance { id: string; employeeId: string; amount: number; reason: string; status: string; requestedAt: string }

export function AdvancesPage() {
  const { data, isLoading } = useQuery({ queryKey: ['advances'], queryFn: () => advanceService.list() })
  const items: Advance[] = (data as { content?: Advance[] } | undefined)?.content
    || (Array.isArray(data) ? data as Advance[] : [])
  return (
    <DataList<Advance>
      title="Cash Advances" description="Pre-trip / pre-expense cash advances"
      data={items} isLoading={isLoading}
      emptyIcon={<Banknote className="h-10 w-10" />} emptyTitle="No advances"
      columns={[
        { key: 'employeeId', label: 'Employee' },
        { key: 'amount', label: 'Amount', align: 'right' },
        { key: 'reason', label: 'Reason' },
        { key: 'status', label: 'Status', render: a => <Badge>{a.status}</Badge> },
        { key: 'requestedAt', label: 'Requested' },
      ]}
    />
  )
}
