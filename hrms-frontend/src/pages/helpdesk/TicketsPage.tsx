import { useQuery } from '@tanstack/react-query'
import { Plus, HelpCircle } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { DataList } from '@/components/ui/data-list'
import { helpdeskService, type Ticket } from '@/services/helpdeskService'

const priorityColor: Record<string, string> = {
  LOW: 'bg-slate-100 text-slate-700',
  MEDIUM: 'bg-amber-100 text-amber-700',
  HIGH: 'bg-orange-100 text-orange-700',
  CRITICAL: 'bg-red-100 text-red-700',
}

export function TicketsPage() {
  const { data, isLoading } = useQuery({ queryKey: ['tickets'], queryFn: () => helpdeskService.listTickets() })
  const items: Ticket[] = (data as { content?: Ticket[] } | undefined)?.content
    || (Array.isArray(data) ? data : [])

  return (
    <DataList<Ticket>
      title="Helpdesk Tickets"
      description="Open and recent support tickets"
      action={<Button><Plus className="h-4 w-4 mr-1" /> New Ticket</Button>}
      data={items}
      isLoading={isLoading}
      emptyIcon={<HelpCircle className="h-10 w-10" />}
      emptyTitle="No tickets"
      columns={[
        { key: 'ticketNumber', label: '#' },
        { key: 'subject', label: 'Subject' },
        { key: 'category', label: 'Category' },
        { key: 'priority', label: 'Priority', render: t => <Badge className={priorityColor[t.priority]}>{t.priority}</Badge> },
        { key: 'status', label: 'Status', render: t => <Badge>{t.status}</Badge> },
        { key: 'assignedToName', label: 'Assignee' },
      ]}
    />
  )
}
