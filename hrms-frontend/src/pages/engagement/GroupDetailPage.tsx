import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { ArrowLeft, Hash, Users, Calendar, LogIn, LogOut } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { PageHeader } from '@/components/ui/page-header'
import { Skeleton } from '@/components/ui/skeleton'
import { EmptyState } from '@/components/ui/empty-state'
import { Avatar } from '@/components/ui/avatar'
import { CrudSection } from '@/components/ui/crud-section'
import { groupsCatalog } from '@/services/catalog'
import { toast } from 'sonner'
import { formatDate } from '@/lib/utils'

type AnyObj = Record<string, unknown>
function rows<T = AnyObj>(d: unknown): T[] {
  if (Array.isArray(d)) return d as T[]
  const o = d as { content?: T[] } | undefined
  return Array.isArray(o?.content) ? o!.content : []
}

export function GroupDetailPage() {
  const { id = '' } = useParams()
  const navigate = useNavigate()
  const qc = useQueryClient()

  const groupQ = useQuery({ queryKey: ['group', id], queryFn: () => groupsCatalog.get(id), enabled: !!id })
  const membersQ = useQuery({ queryKey: ['group-members', id], queryFn: () => groupsCatalog.members.forGroup(id), enabled: !!id })
  const eventsQ = useQuery({ queryKey: ['group-events', id], queryFn: () => groupsCatalog.events.forGroup(id), enabled: !!id })
  const g = (groupQ.data ?? {}) as AnyObj

  const join = useMutation({ mutationFn: () => groupsCatalog.members.join(id), onSuccess: () => { toast.success('Joined'); qc.invalidateQueries({ queryKey: ['group-members', id] }) } })
  const leave = useMutation({ mutationFn: () => groupsCatalog.members.leave(id), onSuccess: () => { toast.success('Left group'); qc.invalidateQueries({ queryKey: ['group-members', id] }) } })

  return (
    <div className="space-y-5 animate-fade-in">
      <Button variant="ghost" size="sm" onClick={() => navigate('/groups')}><ArrowLeft className="h-4 w-4 mr-1" /> Back to groups</Button>
      <PageHeader
        title={groupQ.isLoading ? 'Loading…' : String(g.name ?? 'Group')}
        description={String(g.description ?? '')}
        action={
          <div className="flex gap-2">
            <Button size="sm" onClick={() => join.mutate()}><LogIn className="h-4 w-4 mr-1" /> Join</Button>
            <Button size="sm" variant="outline" onClick={() => leave.mutate()}><LogOut className="h-4 w-4 mr-1" /> Leave</Button>
          </div>
        }
      />

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
        <Card>
          <CardHeader><CardTitle className="text-sm flex items-center gap-2"><Users className="h-4 w-4" /> Members ({rows(membersQ.data).length})</CardTitle></CardHeader>
          <CardContent>
            {membersQ.isLoading ? <Skeleton className="h-32" /> : rows(membersQ.data).length === 0 ? (
              <EmptyState icon={<Hash className="h-6 w-6" />} title="No members yet" />
            ) : (
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
                {rows<AnyObj>(membersQ.data).map(m => (
                  <div key={String(m.id)} className="flex items-center gap-2 p-2 rounded border">
                    <Avatar name={String(m.employeeName ?? m.memberName ?? '')} size="xs" />
                    <span className="text-sm truncate">{String(m.employeeName ?? m.memberName ?? m.employeeId ?? '')}</span>
                    {m.role ? <Badge variant="secondary" className="ml-auto">{String(m.role)}</Badge> : null}
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>

        <CrudSection
          title="Events" icon={<Calendar className="h-6 w-6" />}
          items={rows(eventsQ.data)} loading={eventsQ.isLoading}
          emptyText="No events scheduled" queryKey={['group-events', id]}
          fields={[
            { name: 'title', label: 'Event title', type: 'text', required: true, span: 2 },
            { name: 'startTime', label: 'Starts', type: 'datetime-local', required: true },
            { name: 'endTime', label: 'Ends', type: 'datetime-local' },
            { name: 'location', label: 'Location', type: 'text' },
            { name: 'virtualLink', label: 'Virtual link', type: 'url', span: 2 },
          ]}
          onCreate={v => groupsCatalog.events.create({ ...v, groupId: id })}
          onUpdate={(eid, v) => groupsCatalog.events.update(eid, v)}
          onDelete={eid => groupsCatalog.events.delete(eid)}
          renderItem={(e: AnyObj) => (<>
            <p className="text-sm font-medium">{String(e.title ?? '')}</p>
            <p className="text-xs text-slate-500">{e.startTime ? formatDate(String(e.startTime), 'PPp') : ''}{e.location ? ` · ${String(e.location)}` : ''}</p>
          </>)}
        />
      </div>
    </div>
  )
}
