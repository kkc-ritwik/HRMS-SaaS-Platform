import { Users2, LogIn, LogOut } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { ResourcePage } from '@/components/ui/resource-page'
import { Catalog } from '@/services/catalog'
import { formatDate } from '@/lib/utils'

interface Visitor extends Record<string, unknown> {
  id: string; fullName: string; company?: string; hostEmployeeId?: string; hostName?: string; expectedArrival?: string; status: string
}

export function VisitorsPage() {
  return (
    <ResourcePage<Visitor>
      title="Visitors"
      description="Pre-register visitors, check them in and out"
      icon={<Users2 className="h-10 w-10" />}
      queryKey={['visitors', 'in-building']}
      fetcher={() => Catalog.workplace.visitors.inBuilding()}
      filters={{ status: ['EXPECTED', 'CHECKED_IN', 'CHECKED_OUT', 'CANCELLED'] }}
      columns={[
        { key: 'fullName', label: 'Name' },
        { key: 'company', label: 'Company' },
        { key: 'hostName', label: 'Host', render: v => String(v.hostName ?? v.hostEmployeeId ?? '—') },
        { key: 'expectedArrival', label: 'Expected', render: v => v.expectedArrival ? formatDate(String(v.expectedArrival), 'PPp') : '—' },
        { key: 'status', label: 'Status', render: v => <Badge variant={v.status === 'CHECKED_IN' ? 'success' : v.status === 'CHECKED_OUT' ? 'secondary' : 'warning'}>{String(v.status)}</Badge> },
      ]}
      formFields={[
        { name: 'fullName', label: 'Visitor name', type: 'text', required: true, span: 2 },
        { name: 'company', label: 'Company', type: 'text' },
        { name: 'email', label: 'Email', type: 'email' },
        { name: 'phone', label: 'Phone', type: 'tel' },
        { name: 'hostEmployeeId', label: 'Host employee ID', type: 'text', required: true },
        { name: 'expectedArrival', label: 'Expected arrival', type: 'datetime-local', required: true },
        { name: 'purpose', label: 'Purpose of visit', type: 'textarea', span: 2 },
      ]}
      createTitle="Pre-register visitor"
      onCreate={v => Catalog.workplace.visitors.create(v)}
      rowActions={v => [
        { label: 'Check in', icon: <LogIn className="h-3.5 w-3.5" />, show: v.status === 'EXPECTED', run: () => Catalog.workplace.visitors.checkIn(v.id) },
        { label: 'Check out', icon: <LogOut className="h-3.5 w-3.5" />, show: v.status === 'CHECKED_IN', run: () => Catalog.workplace.visitors.checkOut(v.id) },
      ]}
    />
  )
}
