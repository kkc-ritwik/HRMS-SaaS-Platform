import { useQuery } from '@tanstack/react-query'
import { MessageCircle } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { DataList } from '@/components/ui/data-list'
import { oneOnOneService } from '@/services/extendedServices'

interface OneOnOne {
  id: string
  managerName?: string
  employeeName?: string
  scheduledAt: string
  status: string
  talkingPoints?: number
}

export function OneOnOnePage() {
  const { data, isLoading } = useQuery({ queryKey: ['one-on-ones'], queryFn: () => oneOnOneService.list() })
  const items: OneOnOne[] = (data as { content?: OneOnOne[] } | undefined)?.content
    || (Array.isArray(data) ? data as OneOnOne[] : [])
  return (
    <DataList<OneOnOne>
      title="1-on-1 Meetings" description="Manager ↔ employee touchpoints"
      data={items} isLoading={isLoading}
      emptyIcon={<MessageCircle className="h-10 w-10" />} emptyTitle="No 1-on-1s scheduled"
      columns={[
        { key: 'scheduledAt', label: 'When' },
        { key: 'managerName', label: 'Manager' },
        { key: 'employeeName', label: 'Employee' },
        { key: 'status', label: 'Status', render: o => <Badge>{o.status}</Badge> },
      ]}
    />
  )
}
