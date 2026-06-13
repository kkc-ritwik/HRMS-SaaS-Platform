/**
 * Rich ticket detail page — conversation, KB suggestions, SLA tracker,
 * activity log, and quick actions.
 */
import { useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import {
  ArrowLeft, MessageSquare, Send, BookOpen, Clock, AlertTriangle,
  History, CheckCircle2, XCircle,
} from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Tabs, TabsList, TabsTrigger, TabsContent } from '@/components/ui/tabs'
import { Skeleton } from '@/components/ui/skeleton'
import { Textarea } from '@/components/ui/textarea'
import { Avatar } from '@/components/ui/avatar'
import { EmptyState } from '@/components/ui/empty-state'
import { helpdeskService, type Ticket, type TicketComment } from '@/services/helpdeskService'
import { Catalog } from '@/services/catalog'
import { toast } from 'sonner'
import { formatDate } from '@/lib/utils'

type AnyObj = Record<string, unknown>
function rows<T = AnyObj>(d: unknown): T[] {
  if (Array.isArray(d)) return d as T[]
  const obj = d as { content?: T[]; data?: { content?: T[] } | T[] } | undefined
  if (!obj) return []
  if (Array.isArray(obj.content)) return obj.content
  if (Array.isArray(obj.data)) return obj.data as T[]
  return []
}

function slaState(t: Ticket): { label: string; pct: number; tone: 'success' | 'warning' | 'destructive' } {
  if (t.status === 'RESOLVED' || t.status === 'CLOSED') return { label: 'Met', pct: 100, tone: 'success' }
  const created = new Date(t.createdAt).getTime()
  const slaHours = t.priority === 'CRITICAL' ? 4 : t.priority === 'HIGH' ? 8 : t.priority === 'MEDIUM' ? 24 : 72
  const elapsed = (Date.now() - created) / 3_600_000
  const pct = Math.min(100, Math.round((elapsed / slaHours) * 100))
  if (pct >= 100) return { label: 'Breached', pct: 100, tone: 'destructive' }
  if (pct >= 75) return { label: 'At risk', pct, tone: 'warning' }
  return { label: 'On track', pct, tone: 'success' }
}

export function TicketDetailPage() {
  const { id = '' } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const qc = useQueryClient()
  const [tab, setTab] = useState('conversation')
  const [comment, setComment] = useState('')
  const [internal, setInternal] = useState(false)

  const ticketQ = useQuery({ queryKey: ['ticket', id], queryFn: () => helpdeskService.getTicket(id), enabled: !!id })
  const commentsQ = useQuery({ queryKey: ['ticket-comments', id], queryFn: () => helpdeskService.comments(id), enabled: !!id })
  const kbQ = useQuery({ queryKey: ['kb-suggested'], queryFn: () => Catalog.tickets.kb.published(), enabled: tab === 'kb' })
  const auditQ = useQuery({ queryKey: ['ticket-audit', id], queryFn: () => Catalog.audit.byEntity('Ticket', id), enabled: tab === 'audit' })

  const post = useMutation({ mutationFn: () => helpdeskService.addComment(id, comment, internal), onSuccess: () => { setComment(''); qc.invalidateQueries({ queryKey: ['ticket-comments', id] }); toast.success('Posted') } })
  const resolve = useMutation({ mutationFn: () => Catalog.tickets.resolve(id, { resolutionNotes: 'Resolved' }), onSuccess: () => { toast.success('Resolved'); qc.invalidateQueries({ queryKey: ['ticket', id] }) } })
  const close = useMutation({ mutationFn: () => Catalog.tickets.close(id, {}), onSuccess: () => { toast.success('Closed'); qc.invalidateQueries({ queryKey: ['ticket', id] }) } })

  if (ticketQ.isLoading) return <Skeleton className="h-96" />
  const t = ticketQ.data as Ticket
  if (!t) return <p>Ticket not found</p>
  const sla = slaState(t)
  const list = (commentsQ.data as TicketComment[]) || []

  return (
    <div className="space-y-6">
      <Button variant="ghost" size="sm" onClick={() => navigate('/helpdesk')}>
        <ArrowLeft className="h-4 w-4 mr-1" /> Back
      </Button>

      <Card>
        <CardContent className="p-6 flex items-start justify-between">
          <div>
            <p className="text-xs text-slate-500">#{t.ticketNumber}</p>
            <h1 className="text-2xl font-bold mt-1">{t.subject}</h1>
            <div className="flex items-center gap-2 mt-2">
              <Badge>{t.category}</Badge>
              <Badge>{t.priority}</Badge>
              <Badge>{t.status}</Badge>
              <Badge variant={sla.tone}><Clock className="h-3 w-3 mr-1" /> SLA {sla.label}</Badge>
            </div>
            <p className="text-sm mt-3 whitespace-pre-wrap">{t.description}</p>
            <p className="text-xs text-slate-400 mt-2">Raised by {t.raisedByName} on {formatDate(t.createdAt, 'PPp')}</p>
          </div>
          <div className="flex flex-col gap-2">
            {t.status !== 'RESOLVED' && t.status !== 'CLOSED' && <Button onClick={() => resolve.mutate()} disabled={resolve.isPending}><CheckCircle2 className="h-4 w-4 mr-1" /> Resolve</Button>}
            {t.status === 'RESOLVED' && <Button variant="outline" onClick={() => close.mutate()} disabled={close.isPending}><XCircle className="h-4 w-4 mr-1" /> Close</Button>}
          </div>
        </CardContent>
      </Card>

      <Tabs value={tab} onValueChange={setTab}>
        <TabsList>
          <TabsTrigger value="conversation"><MessageSquare className="h-3.5 w-3.5" /> Conversation ({list.length})</TabsTrigger>
          <TabsTrigger value="sla"><Clock className="h-3.5 w-3.5" /> SLA</TabsTrigger>
          <TabsTrigger value="kb"><BookOpen className="h-3.5 w-3.5" /> KB Suggestions</TabsTrigger>
          <TabsTrigger value="audit"><History className="h-3.5 w-3.5" /> Audit</TabsTrigger>
        </TabsList>

        <TabsContent value="conversation">
          <Card>
            <CardHeader><CardTitle className="flex items-center gap-2 text-sm"><MessageSquare className="h-4 w-4" /> Conversation</CardTitle></CardHeader>
            <CardContent className="space-y-3">
              {list.length === 0 ? <EmptyState icon={<MessageSquare className="h-6 w-6" />} title="No comments yet" /> : list.map(c => (
                <div key={c.id} className={`flex items-start gap-3 ${c.isInternalNote ? 'bg-amber-50 p-2 rounded' : ''}`}>
                  <Avatar name={c.authorName} size="sm" />
                  <div className="flex-1">
                    <p className="text-sm font-medium">{c.authorName} {c.isInternalNote && <span className="text-xs text-amber-700 ml-2">Internal</span>}</p>
                    <p className="text-sm text-slate-700 mt-1 whitespace-pre-wrap">{c.body}</p>
                    <p className="text-xs text-slate-400 mt-1">{formatDate(c.createdAt, 'PPp')}</p>
                  </div>
                </div>
              ))}
              <div className="pt-3 border-t">
                <Textarea rows={3} value={comment} onChange={e => setComment(e.target.value)} placeholder="Type your reply..." />
                <div className="flex items-center justify-between mt-2">
                  <label className="text-sm flex items-center gap-2"><input type="checkbox" checked={internal} onChange={e => setInternal(e.target.checked)} />Internal note</label>
                  <Button size="sm" onClick={() => post.mutate()} disabled={!comment.trim() || post.isPending}>
                    <Send className="h-3.5 w-3.5 mr-1" /> Post
                  </Button>
                </div>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="sla">
          <Card>
            <CardHeader><CardTitle className="text-sm">Service Level Agreement</CardTitle></CardHeader>
            <CardContent>
              <div className="space-y-3">
                <div>
                  <p className="text-xs text-slate-500 mb-1">SLA progress · target {t.priority === 'CRITICAL' ? '4h' : t.priority === 'HIGH' ? '8h' : t.priority === 'MEDIUM' ? '24h' : '72h'}</p>
                  <div className="h-3 bg-slate-100 rounded-full overflow-hidden">
                    <div className={`h-3 ${sla.tone === 'destructive' ? 'bg-red-500' : sla.tone === 'warning' ? 'bg-amber-500' : 'bg-green-500'}`} style={{ width: `${sla.pct}%` }} />
                  </div>
                  <p className="text-xs mt-1 flex items-center gap-1">
                    {sla.tone === 'destructive' && <AlertTriangle className="h-3 w-3 text-red-500" />}
                    <span className="text-slate-600">{sla.pct}% elapsed · {sla.label}</span>
                  </p>
                </div>
                <div className="text-sm text-slate-600">
                  <p>Raised: {formatDate(t.createdAt, 'PPp')}</p>
                  <p>Priority: <Badge>{t.priority}</Badge></p>
                  <p>Assigned to: {t.assignedToName || '—'}</p>
                </div>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="kb">
          <Card><CardContent>
            {kbQ.isLoading ? <Skeleton className="h-20" /> : rows(kbQ.data).length === 0 ? (
              <EmptyState icon={<BookOpen className="h-6 w-6" />} title="No KB articles" />
            ) : rows(kbQ.data).slice(0, 10).map((k: AnyObj) => (
              <div key={String(k.id)} className="border-b border-slate-100 last:border-0 py-3">
                <button onClick={() => navigate('/kb')} className="text-left">
                  <p className="text-sm font-medium hover:underline">{String(k.title || k.name || '')}</p>
                  <p className="text-xs text-slate-500 line-clamp-2">{String(k.excerpt || k.body || '').slice(0, 200)}</p>
                </button>
              </div>
            ))}
          </CardContent></Card>
        </TabsContent>

        <TabsContent value="audit">
          <Card><CardContent>
            {auditQ.isLoading ? <Skeleton className="h-20" /> : rows(auditQ.data).length === 0 ? (
              <EmptyState icon={<History className="h-6 w-6" />} title="No audit events" />
            ) : (
              <ul className="border-l-2 border-slate-100 ml-2 space-y-3">
                {rows(auditQ.data).map((e: AnyObj, i: number) => (
                  <li key={String(e.id) || `${i}`} className="ml-3 -translate-x-[7px]">
                    <span className="inline-block h-2.5 w-2.5 rounded-full bg-brand-500 mr-2" />
                    <span className="text-sm">{String(e.action || e.eventType || '')}</span>
                    <span className="block text-xs text-slate-500 ml-4">{e.at ? formatDate(String(e.at)) : ''} {e.actor ? `· by ${String(e.actor)}` : ''}</span>
                  </li>
                ))}
              </ul>
            )}
          </CardContent></Card>
        </TabsContent>
      </Tabs>
    </div>
  )
}
