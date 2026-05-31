import { useQuery } from '@tanstack/react-query'
import { Clock } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { DataList } from '@/components/ui/data-list'
import { shiftService } from '@/services/extendedServices'

interface Shift { id: string; name: string; startTime: string; endTime: string; breakMinutes?: number; type?: string }

export function ShiftsPage() {
  const { data, isLoading } = useQuery({ queryKey: ['shifts'], queryFn: () => shiftService.list() })
  const items: Shift[] = (data as { content?: Shift[] } | undefined)?.content
    || (Array.isArray(data) ? data as Shift[] : [])
  return (
    <DataList<Shift>
      title="Shifts" description="Work shifts + rotation templates"
      data={items} isLoading={isLoading}
      emptyIcon={<Clock className="h-10 w-10" />} emptyTitle="No shifts defined"
      columns={[
        { key: 'name', label: 'Name' },
        { key: 'type', label: 'Type', render: s => s.type ? <Badge>{s.type}</Badge> : '—' },
        { key: 'startTime', label: 'Start' },
        { key: 'endTime', label: 'End' },
        { key: 'breakMinutes', label: 'Break (min)' },
      ]}
    />
  )
}
