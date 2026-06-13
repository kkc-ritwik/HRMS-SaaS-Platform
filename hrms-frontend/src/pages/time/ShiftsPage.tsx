import { Clock } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { ResourcePage } from '@/components/ui/resource-page'
import { shiftService } from '@/services/extendedServices'

interface Shift extends Record<string, unknown> { id: string; name: string; startTime?: string; endTime?: string; breakMinutes?: number; type?: string }

export function ShiftsPage() {
  return (
    <ResourcePage<Shift>
      title="Shifts"
      description="Work shifts + rotation templates"
      icon={<Clock className="h-10 w-10" />}
      queryKey={['shifts']}
      fetcher={() => shiftService.list()}
      filters={{ type: ['FIXED', 'ROTATIONAL', 'FLEXIBLE', 'NIGHT'] }}
      columns={[
        { key: 'name', label: 'Name' },
        { key: 'type', label: 'Type', render: s => s.type ? <Badge>{String(s.type)}</Badge> : '—' },
        { key: 'startTime', label: 'Start' },
        { key: 'endTime', label: 'End' },
        { key: 'breakMinutes', label: 'Break (min)', align: 'right' },
      ]}
      formFields={[
        { name: 'name', label: 'Shift name', type: 'text', required: true, span: 2 },
        { name: 'type', label: 'Type', type: 'select', options: [
          { value: 'FIXED', label: 'Fixed' }, { value: 'ROTATIONAL', label: 'Rotational' },
          { value: 'FLEXIBLE', label: 'Flexible' }, { value: 'NIGHT', label: 'Night' },
        ] },
        { name: 'startTime', label: 'Start time', type: 'time', required: true },
        { name: 'endTime', label: 'End time', type: 'time', required: true },
        { name: 'breakMinutes', label: 'Break (minutes)', type: 'number' },
        { name: 'graceMinutes', label: 'Grace period (minutes)', type: 'number' },
      ]}
      onCreate={v => shiftService.create(v)}
    />
  )
}
