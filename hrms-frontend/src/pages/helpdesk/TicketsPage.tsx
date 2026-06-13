import { HelpCircle, CheckCircle2, XCircle } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { ResourcePage } from '@/components/ui/resource-page'
import { helpdeskService, type Ticket } from '@/services/helpdeskService'
import { Catalog } from '@/services/catalog'

const priorityColor: Record<string, string> = {
  LOW: 'bg-slate-100 text-slate-700',
  MEDIUM: 'bg-amber-100 text-amber-700',
  HIGH: 'bg-orange-100 text-orange-700',
  CRITICAL: 'bg-red-100 text-red-700',
}

export function TicketsPage() {
  return (
    <ResourcePage<Ticket & Record<string, unknown>>
      title="Helpdesk Tickets"
      description="Raise, track, resolve and close support tickets"
      icon={<HelpCircle className="h-10 w-10" />}
      queryKey={['tickets']}
      fetcher={() => helpdeskService.listTickets()}
      rowHref={t => `/helpdesk/${t.id}`}
      filters={{ status: ['OPEN', 'IN_PROGRESS', 'ON_HOLD', 'RESOLVED', 'CLOSED'], priority: ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'] }}
      columns={[
        { key: 'ticketNumber', label: '#' },
        { key: 'subject', label: 'Subject' },
        { key: 'category', label: 'Category' },
        { key: 'priority', label: 'Priority', render: t => <Badge className={priorityColor[String(t.priority)]}>{String(t.priority)}</Badge> },
        { key: 'status', label: 'Status', render: t => <Badge>{String(t.status)}</Badge> },
        { key: 'assignedToName', label: 'Assignee' },
      ]}
      formFields={[
        { name: 'subject', label: 'Subject', type: 'text', required: true, span: 2 },
        { name: 'category', label: 'Category', type: 'select', required: true, options: [
          { value: 'IT', label: 'IT support' }, { value: 'HR', label: 'HR query' }, { value: 'PAYROLL', label: 'Payroll' },
          { value: 'FACILITIES', label: 'Facilities' }, { value: 'ACCESS', label: 'Access / security' }, { value: 'OTHER', label: 'Other' },
        ] },
        { name: 'priority', label: 'Priority', type: 'select', required: true, options: [
          { value: 'LOW', label: 'Low' }, { value: 'MEDIUM', label: 'Medium' }, { value: 'HIGH', label: 'High' }, { value: 'CRITICAL', label: 'Critical' },
        ] },
        { name: 'description', label: 'Description', type: 'textarea', required: true, span: 2 },
      ]}
      createTitle="Raise a ticket"
      onCreate={v => helpdeskService.createTicket(v as Partial<Ticket>)}
      rowActions={t => [
        { label: 'Resolve', icon: <CheckCircle2 className="h-3.5 w-3.5" />, show: t.status !== 'RESOLVED' && t.status !== 'CLOSED', run: () => Catalog.tickets.resolve(t.id, { resolutionNotes: 'Resolved' }) },
        { label: 'Close', icon: <XCircle className="h-3.5 w-3.5" />, show: t.status === 'RESOLVED', run: () => Catalog.tickets.close(t.id, {}) },
      ]}
    />
  )
}
