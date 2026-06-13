import { Repeat, Play, Square, CheckCircle2, Users } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { ResourcePage } from '@/components/ui/resource-page'
import { reviewCycleService } from '@/services/extendedServices'
import { Catalog } from '@/services/catalog'
import { formatDate } from '@/lib/utils'

interface Cycle extends Record<string, unknown> { id: string; name: string; type?: string; status: string; startDate?: string; endDate?: string }

export function ReviewCyclesPage() {
  return (
    <ResourcePage<Cycle>
      title="Review Cycles"
      description="Appraisal cycles — self-review, manager review, calibration, finalisation"
      icon={<Repeat className="h-10 w-10" />}
      queryKey={['review-cycles']}
      fetcher={() => reviewCycleService.list()}
      filters={{ status: ['DRAFT', 'ACTIVE', 'SELF_REVIEW', 'MANAGER_REVIEW', 'CALIBRATION', 'CLOSED'] }}
      columns={[
        { key: 'name', label: 'Cycle' },
        { key: 'type', label: 'Type', render: c => c.type ? <Badge>{String(c.type)}</Badge> : '—' },
        { key: 'startDate', label: 'Start', render: c => c.startDate ? formatDate(String(c.startDate)) : '—' },
        { key: 'endDate', label: 'End', render: c => c.endDate ? formatDate(String(c.endDate)) : '—' },
        { key: 'status', label: 'Status', render: c => <Badge>{String(c.status)}</Badge> },
      ]}
      formFields={[
        { name: 'name', label: 'Cycle name', type: 'text', required: true, span: 2 },
        { name: 'type', label: 'Type', type: 'select', options: [
          { value: 'ANNUAL', label: 'Annual' }, { value: 'HALF_YEARLY', label: 'Half-yearly' },
          { value: 'QUARTERLY', label: 'Quarterly' }, { value: 'PROBATION', label: 'Probation' },
        ] },
        { name: 'startDate', label: 'Start date', type: 'date', required: true },
        { name: 'endDate', label: 'End date', type: 'date', required: true },
        { name: 'description', label: 'Description', type: 'textarea', span: 2 },
      ]}
      onCreate={v => reviewCycleService.create(v)}
      onUpdate={(id, v) => Catalog.performance.cycles.update(id, v)}
      rowActions={c => [
        { label: 'Activate', icon: <Play className="h-3.5 w-3.5" />, show: c.status === 'DRAFT', run: () => reviewCycleService.activate(c.id) },
        { label: 'Start self-review', icon: <Users className="h-3.5 w-3.5" />, show: c.status === 'ACTIVE', run: () => reviewCycleService.startSelfReview(c.id) },
        { label: 'Start manager review', icon: <Users className="h-3.5 w-3.5" />, show: c.status === 'SELF_REVIEW', run: () => reviewCycleService.startManagerReview(c.id) },
        { label: 'Start calibration', icon: <Square className="h-3.5 w-3.5" />, show: c.status === 'MANAGER_REVIEW', run: () => reviewCycleService.startCalibration(c.id) },
        { label: 'Finalise', icon: <CheckCircle2 className="h-3.5 w-3.5" />, show: c.status === 'CALIBRATION', confirm: 'Finalise this cycle? Ratings become read-only.', run: () => reviewCycleService.finalize(c.id) },
      ]}
    />
  )
}
