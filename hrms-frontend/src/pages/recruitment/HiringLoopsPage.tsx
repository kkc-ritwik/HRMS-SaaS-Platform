import { Users, Calendar, CheckCircle2 } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { ResourcePage } from '@/components/ui/resource-page'
import { hiringLoopService } from '@/services/extendedServices'
import { Catalog } from '@/services/catalog'
import { formatDate } from '@/lib/utils'

interface HiringLoop extends Record<string, unknown> { id: string; candidateId?: string; candidateName?: string; loopDate?: string; status: string; outcome?: string; format?: string }

export function HiringLoopsPage() {
  return (
    <ResourcePage<HiringLoop>
      title="Hiring Loops"
      description="Coordinated on-site/remote interview panels — create, schedule, complete"
      icon={<Users className="h-10 w-10" />}
      queryKey={['hiring-loops']}
      fetcher={() => hiringLoopService.list()}
      filters={{ status: ['DRAFT', 'SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'] }}
      columns={[
        { key: 'candidateName', label: 'Candidate', render: h => String(h.candidateName ?? h.candidateId ?? '—') },
        { key: 'loopDate', label: 'Date', render: h => h.loopDate ? formatDate(String(h.loopDate)) : '—' },
        { key: 'format', label: 'Format' },
        { key: 'status', label: 'Status', render: h => <Badge>{String(h.status)}</Badge> },
        { key: 'outcome', label: 'Outcome', render: h => h.outcome ? <Badge>{String(h.outcome)}</Badge> : '—' },
      ]}
      formFields={[
        { name: 'applicationId', label: 'Application ID', type: 'text', required: true },
        { name: 'format', label: 'Format', type: 'select', options: [
          { value: 'ONSITE', label: 'On-site' }, { value: 'REMOTE', label: 'Remote' }, { value: 'HYBRID', label: 'Hybrid' },
        ] },
        { name: 'loopDate', label: 'Loop date', type: 'date' },
      ]}
      createTitle="Create hiring loop"
      onCreate={v => hiringLoopService.create(v)}
      rowActions={h => [
        { label: 'Schedule', icon: <Calendar className="h-3.5 w-3.5" />, show: h.status === 'DRAFT', run: () => hiringLoopService.schedule(h.id, {}) },
        { label: 'Complete', icon: <CheckCircle2 className="h-3.5 w-3.5" />, show: h.status === 'SCHEDULED' || h.status === 'IN_PROGRESS', run: () => Catalog.recruitment.hiringLoops.complete(h.id, {}) },
      ]}
    />
  )
}
