import { MessageCircle, CheckCircle2 } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { ResourcePage } from '@/components/ui/resource-page'
import { oneOnOneService } from '@/services/extendedServices'
import { Catalog } from '@/services/catalog'
import { formatDate } from '@/lib/utils'

interface OneOnOne extends Record<string, unknown> { id: string; managerName?: string; employeeName?: string; scheduledAt?: string; status: string; talkingPoints?: number }

export function OneOnOnePage() {
  return (
    <ResourcePage<OneOnOne>
      title="1-on-1 Meetings"
      description="Manager ↔ employee touchpoints — schedule, complete"
      icon={<MessageCircle className="h-10 w-10" />}
      queryKey={['one-on-ones']}
      fetcher={() => oneOnOneService.list()}
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
  )
}
