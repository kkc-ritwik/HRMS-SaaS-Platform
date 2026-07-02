import { useState } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { HelpCircle, CheckCircle2, XCircle, UserPlus } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogBody } from '@/components/ui/dialog'
import { ResourcePage } from '@/components/ui/resource-page'
import { helpdeskService, type Ticket } from '@/services/helpdeskService'
import { Catalog } from '@/services/catalog'
import { useAuthStore } from '@/store/authStore'
import { getErrorMessage } from '@/lib/api'
import { toast } from 'sonner'

const priorityColor: Record<string, string> = {
  LOW: 'bg-slate-100 text-slate-700',
  MEDIUM: 'bg-amber-100 text-amber-700',
  HIGH: 'bg-orange-100 text-orange-700',
  CRITICAL: 'bg-red-100 text-red-700',
}
type View = 'all' | 'mine' | 'assigned'

export function TicketsPage() {
  const qc = useQueryClient()
  const user = useAuthStore(s => s.user)
  const employeeId = user?.employeeId || user?.id || ''
  const [view, setView] = useState<View>('all')
  const [assignFor, setAssignFor] = useState<(Ticket & Record<string, unknown>) | null>(null)
  const [assignee, setAssignee] = useState('')

  const fetcher = () => {
    if (view === 'mine') return helpdeskService.ticketsByRequester(employeeId)
    if (view === 'assigned') return helpdeskService.ticketsByAssignee(employeeId)
    return helpdeskService.listTickets()
  }

  const assign = useMutation({
    mutationFn: () => helpdeskService.assignTicket(assignFor!.id, assignee),
    onSuccess: () => { toast.success('Ticket assigned'); setAssignFor(null); setAssignee(''); qc.invalidateQueries({ queryKey: ['tickets'] }) },
    onError: e => toast.error(getErrorMessage(e)),
  })

  return (
    <>
      <Tabs value={view} onValueChange={v => setView(v as View)} className="mb-2">
        <TabsList>
          <TabsTrigger value="all">All tickets</TabsTrigger>
          <TabsTrigger value="mine">Raised by me</TabsTrigger>
          <TabsTrigger value="assigned">Assigned to me</TabsTrigger>
        </TabsList>
      </Tabs>
      <ResourcePage<Ticket & Record<string, unknown>>
        key={view}
        title="Helpdesk Tickets"
        description="Raise, track, assign, resolve and close support tickets"
        icon={<HelpCircle className="h-10 w-10" />}
        queryKey={['tickets', view]}
        fetcher={fetcher}
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
          { label: 'Assign', icon: <UserPlus className="h-3.5 w-3.5" />, show: t.status !== 'CLOSED', run: () => { setAssignFor(t); return undefined } },
          { label: 'Resolve', icon: <CheckCircle2 className="h-3.5 w-3.5" />, show: t.status !== 'RESOLVED' && t.status !== 'CLOSED', run: () => Catalog.tickets.resolve(t.id, { resolutionNotes: 'Resolved' }) },
          { label: 'Close', icon: <XCircle className="h-3.5 w-3.5" />, show: t.status === 'RESOLVED', run: () => Catalog.tickets.close(t.id, {}) },
        ]}
      />

      <Dialog open={!!assignFor} onOpenChange={o => { if (!o) { setAssignFor(null); setAssignee('') } }}>
        <DialogContent className="max-w-sm">
          <DialogHeader><DialogTitle className="flex items-center gap-2"><UserPlus className="h-5 w-5" /> Assign ticket</DialogTitle></DialogHeader>
          <DialogBody className="space-y-3">
            <p className="text-sm text-slate-500">Assign {assignFor?.ticketNumber ? `#${assignFor.ticketNumber}` : 'this ticket'} to an agent.</p>
            <div>
              <Label className="text-xs">Assignee employee ID</Label>
              <Input placeholder="employee UUID" value={assignee} onChange={e => setAssignee(e.target.value)} />
            </div>
            <Button onClick={() => assign.mutate()} loading={assign.isPending} disabled={!assignee.trim()}>Assign</Button>
          </DialogBody>
        </DialogContent>
      </Dialog>
    </>
  )
}
