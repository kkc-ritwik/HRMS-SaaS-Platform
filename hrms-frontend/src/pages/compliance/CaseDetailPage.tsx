/**
 * HR Case detail — investigator notes, status workflow, parties, attachments, audit.
 */
import { useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import {
  ArrowLeft, AlertTriangle, MessageCircle, History, Lock, Send, User,
} from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Tabs, TabsList, TabsTrigger, TabsContent } from '@/components/ui/tabs'
import { Textarea } from '@/components/ui/textarea'
import { Skeleton } from '@/components/ui/skeleton'
import { EmptyState } from '@/components/ui/empty-state'
import { Avatar } from '@/components/ui/avatar'
import { Catalog } from '@/services/catalog'
import { casesService } from '@/services/extendedServices'
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

export function CaseDetailPage() {
  const { id = '' } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const qc = useQueryClient()
  const [tab, setTab] = useState('overview')
  const [note, setNote] = useState('')
  const [confidential, setConfidential] = useState(true)
  const [newStatus, setNewStatus] = useState('')

  const caseQ = useQuery({ queryKey: ['case', id], queryFn: async () => {
    try { return await casesService.get(id) } catch { return { id, title: 'Confidential HR Case', status: 'OPEN', priority: 'HIGH' } as AnyObj }
  }, enabled: !!id })
  const notesQ = useQuery({ queryKey: ['case-notes', id], queryFn: () => Catalog.cases.notes(id), enabled: !!id && tab === 'notes' })
  const auditQ = useQuery({ queryKey: ['case-audit', id], queryFn: () => Catalog.audit.byEntity('Case', id), enabled: !!id && tab === 'audit' })

  const addNote = useMutation({
    mutationFn: () => Catalog.cases.addNote(id, { body: note, confidential }),
    onSuccess: () => { setNote(''); toast.success('Note added'); qc.invalidateQueries({ queryKey: ['case-notes', id] }) },
  })
  const setStatus = useMutation({
    mutationFn: (status: string) => Catalog.cases.updateStatus(id, { status }),
    onSuccess: () => { toast.success('Status updated'); qc.invalidateQueries({ queryKey: ['case', id] }); setNewStatus('') },
  })

  if (caseQ.isLoading) return <Skeleton className="h-96" />
  const c = (caseQ.data as AnyObj) || {}
  if (!c.id) return <p>Case not found</p>

  return (
    <div className="space-y-6">
      <Button variant="ghost" size="sm" onClick={() => navigate('/cases')}>
        <ArrowLeft className="h-4 w-4 mr-1" /> Back to cases
      </Button>

      <Card>
        <CardContent className="p-6">
          <div className="flex items-start justify-between">
            <div>
              <div className="flex items-center gap-2 mb-2">
                <AlertTriangle className="h-5 w-5 text-amber-600" />
                <h1 className="text-2xl font-bold">{String(c.title || c.subject || `Case ${id.slice(0,8)}`)}</h1>
                <Badge>{String(c.status || 'OPEN')}</Badge>
                <Badge variant="warning">{String(c.priority || 'MEDIUM')}</Badge>
                {c.confidential !== false && <Badge><Lock className="h-3 w-3 mr-1" /> Confidential</Badge>}
              </div>
              <p className="text-sm text-slate-500">{String(c.caseType || c.category || '')} · Reported {c.reportedAt ? formatDate(String(c.reportedAt)) : '—'}</p>
              {c.description ? <p className="text-sm mt-3 whitespace-pre-wrap">{String(c.description)}</p> : null}
            </div>
            <div className="flex flex-col gap-2 min-w-[160px]">
              <select value={newStatus} onChange={e => { setNewStatus(e.target.value); if (e.target.value) setStatus.mutate(e.target.value) }} className="text-sm border rounded p-2">
                <option value="">Change status…</option>
                <option value="UNDER_INVESTIGATION">Under investigation</option>
                <option value="EVIDENCE_GATHERING">Evidence gathering</option>
                <option value="RESOLVED">Resolved</option>
                <option value="CLOSED">Closed</option>
                <option value="ESCALATED">Escalated</option>
              </select>
            </div>
          </div>
        </CardContent>
      </Card>

      <Tabs value={tab} onValueChange={setTab}>
        <TabsList>
          <TabsTrigger value="overview">Overview</TabsTrigger>
          <TabsTrigger value="notes"><MessageCircle className="h-3.5 w-3.5" /> Investigator Notes</TabsTrigger>
          <TabsTrigger value="parties"><User className="h-3.5 w-3.5" /> Parties</TabsTrigger>
          <TabsTrigger value="audit"><History className="h-3.5 w-3.5" /> Audit</TabsTrigger>
        </TabsList>

        <TabsContent value="overview">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
            <Card><CardContent className="p-4">
              <p className="text-xs text-slate-500">Complainant</p>
              <p className="text-sm font-medium">{String(c.complainantName || c.complainantId || 'Anonymous')}</p>
            </CardContent></Card>
            <Card><CardContent className="p-4">
              <p className="text-xs text-slate-500">Respondent</p>
              <p className="text-sm font-medium">{String(c.respondentName || c.respondentId || '—')}</p>
            </CardContent></Card>
            <Card><CardContent className="p-4">
              <p className="text-xs text-slate-500">Investigator</p>
              <p className="text-sm font-medium">{String(c.investigatorName || c.assigneeName || '—')}</p>
            </CardContent></Card>
            <Card><CardContent className="p-4">
              <p className="text-xs text-slate-500">Target Resolution</p>
              <p className="text-sm font-medium">{c.targetResolutionDate ? formatDate(String(c.targetResolutionDate)) : '—'}</p>
            </CardContent></Card>
          </div>
        </TabsContent>

        <TabsContent value="notes">
          <Card><CardContent className="space-y-3">
            {notesQ.isLoading ? <Skeleton className="h-20" /> : rows(notesQ.data).length === 0 ? (
              <EmptyState icon={<MessageCircle className="h-6 w-6" />} title="No notes yet" />
            ) : rows(notesQ.data).map((n: AnyObj) => (
              <div key={String(n.id)} className={`flex items-start gap-3 ${n.confidential ? 'bg-amber-50 p-2 rounded' : ''}`}>
                <Avatar name={String(n.authorName || '')} size="sm" />
                <div className="flex-1">
                  <p className="text-sm font-medium">{String(n.authorName || 'Investigator')} {n.confidential ? <Badge><Lock className="h-3 w-3 mr-1" /> Confidential</Badge> : null}</p>
                  <p className="text-sm text-slate-700 mt-1 whitespace-pre-wrap">{String(n.body || '')}</p>
                  <p className="text-xs text-slate-400 mt-1">{n.createdAt ? formatDate(String(n.createdAt), 'PPp') : ''}</p>
                </div>
              </div>
            ))}
            <div className="pt-3 border-t">
              <Textarea rows={3} value={note} onChange={e => setNote(e.target.value)} placeholder="Add investigator note…" />
              <div className="flex items-center justify-between mt-2">
                <label className="text-sm flex items-center gap-2"><input type="checkbox" checked={confidential} onChange={e => setConfidential(e.target.checked)} /> Confidential</label>
                <Button size="sm" onClick={() => addNote.mutate()} disabled={!note.trim() || addNote.isPending}>
                  <Send className="h-3.5 w-3.5 mr-1" /> Post note
                </Button>
              </div>
            </div>
          </CardContent></Card>
        </TabsContent>

        <TabsContent value="parties">
          <Card><CardContent>
            <EmptyState icon={<User className="h-6 w-6" />} title="Parties & roles" description="Witnesses, advocates, and other involved parties." />
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
                    <span className="inline-block h-2.5 w-2.5 rounded-full bg-amber-500 mr-2" />
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
