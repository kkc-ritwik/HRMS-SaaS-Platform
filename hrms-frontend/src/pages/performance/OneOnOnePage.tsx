import { useState } from 'react'
import { MessageCircle, CheckCircle2 } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { ResourcePage } from '@/components/ui/resource-page'
import { oneOnOneService } from '@/services/extendedServices'
import { Catalog } from '@/services/catalog'
import { formatDate } from '@/lib/utils'

interface OneOnOne extends Record<string, unknown> { id: string; managerName?: string; employeeName?: string; scheduledAt?: string; status: string; talkingPoints?: number }
type View = 'manager' | 'employee' | 'all'

export function OneOnOnePage() {
  const [view, setView] = useState<View>('manager')

  const fetcher = () => {
    if (view === 'employee') return Catalog.oneOnOnes.meAsEmployee()
    if (view === 'all') return oneOnOneService.list()
    return Catalog.oneOnOnes.meAsManager()
  }

  return (
    <>
      <Tabs value={view} onValueChange={v => setView(v as View)} className="mb-2">
        <TabsList>
          <TabsTrigger value="manager">As manager</TabsTrigger>
          <TabsTrigger value="employee">As employee</TabsTrigger>
          <TabsTrigger value="all">All</TabsTrigger>
        </TabsList>
      </Tabs>
      <ResourcePage<OneOnOne>
        key={view}
        title="1-on-1 Meetings"
        description="Manager ↔ employee touchpoints — schedule, complete"
        icon={<MessageCircle className="h-10 w-10" />}
        queryKey={['one-on-ones', view]}
        fetcher={fetcher}
        filters={{ status: ['SCHEDULED', 'COMPLETED', 'CANCELLED'] }}
        columns={[
          { key: 'scheduledAt', label: 'When', render: o => o.scheduledAt ? formatDate(String(o.scheduledAt), 'PPp') : '—' },
          { key: 'managerName', label: 'Manager' },
          { key: 'employeeName', label: 'Employee' },
          { key: 'status', label: 'Status', render: o => <Badge variant={o.status === 'COMPLETED' ? 'success' : 'warning'}>{String(o.status)}</Badge> },
        ]}
        formFields={[
          { name: 'employeeId', label: 'Employee ID', type: 'text', required: true },
          { name: 'scheduledAt', label: 'Scheduled at', type: 'datetime-local', required: true },
          { name: 'agenda', label: 'Agenda / talking points', type: 'textarea', span: 2 },
        ]}
        createTitle="Schedule 1-on-1"
        onCreate={v => oneOnOneService.schedule(v)}
        rowActions={o => [
          { label: 'Complete', icon: <CheckCircle2 className="h-3.5 w-3.5" />, show: o.status === 'SCHEDULED', run: () => Catalog.oneOnOnes.complete(o.id, {}) },
        ]}
      />
    </>
  )
}
