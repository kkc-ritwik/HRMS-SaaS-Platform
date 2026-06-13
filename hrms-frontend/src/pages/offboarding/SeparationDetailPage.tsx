/**
 * Separation detail — combines all offboarding sub-resources for one separation:
 *   Overview · Checklist · Exit interview · Knowledge transfer · FnF · Audit
 */
import { useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import {
  ArrowLeft, ClipboardCheck, MessageCircle, BookOpen, Banknote, History,
  Check, Circle, AlertTriangle,
} from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Tabs, TabsList, TabsTrigger, TabsContent } from '@/components/ui/tabs'
import { Skeleton } from '@/components/ui/skeleton'
import { EmptyState } from '@/components/ui/empty-state'
import { Catalog } from '@/services/catalog'
import { toast } from 'sonner'
import { cn, formatDate } from '@/lib/utils'

type AnyObj = Record<string, unknown>
function rows<T = AnyObj>(d: unknown): T[] {
  if (Array.isArray(d)) return d as T[]
  const obj = d as { content?: T[]; data?: { content?: T[] } | T[] } | undefined
  if (!obj) return []
  if (Array.isArray(obj.content)) return obj.content
  if (Array.isArray(obj.data)) return obj.data as T[]
  return []
}

export function SeparationDetailPage() {
  const { id = '' } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const qc = useQueryClient()
  const [tab, setTab] = useState('overview')

  const sepQ = useQuery({ queryKey: ['sep', id], queryFn: () => Catalog.offboarding.separations.get(id), enabled: !!id })
  const checklistQ = useQuery({ queryKey: ['sep-chk', id], queryFn: () => Catalog.offboarding.checklists.forSeparation(id), enabled: !!id && tab === 'checklist' })
  const exitQ = useQuery({ queryKey: ['sep-exit', id], queryFn: () => Catalog.offboarding.exitInterviews.forSeparation(id), enabled: !!id && tab === 'exit' })
  const ktQ = useQuery({ queryKey: ['sep-kt', id], queryFn: () => Catalog.offboarding.knowledgeTransfers.forSeparation(id), enabled: !!id && tab === 'kt' })
  const auditQ = useQuery({ queryKey: ['sep-audit', id], queryFn: () => Catalog.audit.byEntity('Separation', id), enabled: !!id && tab === 'audit' })

  const approve = useMutation({ mutationFn: () => Catalog.offboarding.separations.approve(id), onSuccess: () => { toast.success('Separation approved'); qc.invalidateQueries({ queryKey: ['sep', id] }) } })
  const complete = useMutation({ mutationFn: () => Catalog.offboarding.separations.complete(id), onSuccess: () => { toast.success('Separation completed'); qc.invalidateQueries({ queryKey: ['sep', id] }) } })
  const completeTask = useMutation({ mutationFn: (taskId: string) => Catalog.offboarding.checklists.complete(taskId), onSuccess: () => { toast.success('Task completed'); qc.invalidateQueries({ queryKey: ['sep-chk', id] }) } })

  if (sepQ.isLoading) return <Skeleton className="h-96" />
  const s = (sepQ.data as AnyObj) || {}
  if (!s.id) return <p>Separation not found</p>

  return (
    <div className="space-y-6">
      <Button variant="ghost" size="sm" onClick={() => navigate('/offboarding')}>
        <ArrowLeft className="h-4 w-4 mr-1" /> Back
      </Button>

      <Card>
        <CardContent className="p-6">
          <div className="flex items-start justify-between">
            <div>
              <h1 className="text-2xl font-bold">{String(s.employeeName || s.employeeId || 'Separation')}</h1>
              <p className="text-sm text-slate-500 mt-1">{String(s.separationType || '')} · LWD {s.lastWorkingDay ? formatDate(String(s.lastWorkingDay)) : '—'}</p>
              {s.reason ? <p className="text-sm mt-2">{String(s.reason)}</p> : null}
            </div>
            <div className="flex flex-col gap-2 items-end">
              <Badge variant={s.status === 'COMPLETED' ? 'success' : s.status === 'APPROVED' ? 'success' : 'warning'}>{String(s.status || 'PENDING')}</Badge>
              {s.status === 'PENDING' && <Button size="sm" onClick={() => approve.mutate()} disabled={approve.isPending}>Approve</Button>}
              {s.status === 'APPROVED' && <Button size="sm" onClick={() => complete.mutate()} disabled={complete.isPending}>Mark complete</Button>}
            </div>
          </div>
        </CardContent>
      </Card>

      <Tabs value={tab} onValueChange={setTab}>
        <TabsList className="flex flex-wrap h-auto">
          <TabsTrigger value="overview">Overview</TabsTrigger>
          <TabsTrigger value="checklist"><ClipboardCheck className="h-3.5 w-3.5" /> Checklist</TabsTrigger>
          <TabsTrigger value="exit"><MessageCircle className="h-3.5 w-3.5" /> Exit Interview</TabsTrigger>
          <TabsTrigger value="kt"><BookOpen className="h-3.5 w-3.5" /> Knowledge Transfer</TabsTrigger>
          <TabsTrigger value="fnf"><Banknote className="h-3.5 w-3.5" /> Full & Final</TabsTrigger>
          <TabsTrigger value="audit"><History className="h-3.5 w-3.5" /> Audit</TabsTrigger>
        </TabsList>

        <TabsContent value="overview">
          <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
            <Card><CardContent className="p-4">
              <p className="text-xs text-slate-500">Notice Period</p>
              <p className="text-xl font-bold">{String(s.noticePeriodDays || '—')} days</p>
            </CardContent></Card>
            <Card><CardContent className="p-4">
              <p className="text-xs text-slate-500">Resign Date</p>
              <p className="text-xl font-bold">{s.resignationDate ? formatDate(String(s.resignationDate)) : '—'}</p>
            </CardContent></Card>
            <Card><CardContent className="p-4">
              <p className="text-xs text-slate-500">Last Working Day</p>
              <p className="text-xl font-bold">{s.lastWorkingDay ? formatDate(String(s.lastWorkingDay)) : '—'}</p>
            </CardContent></Card>
            <Card><CardContent className="p-4">
              <p className="text-xs text-slate-500">Rehire-able</p>
              <p className="text-xl font-bold">{s.rehireEligible === false ? 'No' : 'Yes'}</p>
            </CardContent></Card>
          </div>
        </TabsContent>

        <TabsContent value="checklist">
          <Card><CardContent>
            {checklistQ.isLoading ? <Skeleton className="h-20" /> : rows(checklistQ.data).length === 0 ? (
              <EmptyState icon={<ClipboardCheck className="h-6 w-6" />} title="No checklist items" action={{ label: 'Configure checklists', onClick: () => navigate('/exit-checklists') }} />
            ) : rows(checklistQ.data).map((c: AnyObj) => (
              <div key={String(c.id)} className={cn('flex items-start gap-3 p-3 rounded-lg border', c.status === 'COMPLETED' ? 'bg-green-50 border-green-200' : 'border-slate-100')}>
                <button onClick={() => c.status !== 'COMPLETED' && completeTask.mutate(String(c.id))}>
                  {c.status === 'COMPLETED'
                    ? <div className="h-5 w-5 rounded-full bg-green-500 text-white flex items-center justify-center"><Check className="h-3 w-3" /></div>
                    : <Circle className="h-5 w-5 text-slate-300 hover:text-brand-500" />}
                </button>
                <div className="flex-1">
                  <p className={cn('text-sm font-medium', c.status === 'COMPLETED' && 'line-through text-slate-500')}>{String(c.title || c.taskName || '')}</p>
                  <p className="text-xs text-slate-500">{String(c.assigneeName || c.assigneeRole || '')} {c.dueDate ? `· Due ${formatDate(String(c.dueDate))}` : ''}</p>
                </div>
                <Badge>{String(c.status || '')}</Badge>
              </div>
            ))}
          </CardContent></Card>
        </TabsContent>

        <TabsContent value="exit">
          <Card><CardContent>
            {exitQ.isLoading ? <Skeleton className="h-20" /> : rows(exitQ.data).length === 0 ? (
              <EmptyState icon={<MessageCircle className="h-6 w-6" />} title="No exit interview scheduled" action={{ label: 'Schedule', onClick: () => navigate('/exit-interviews') }} />
            ) : rows(exitQ.data).map((ei: AnyObj) => (
              <div key={String(ei.id)} className="border-b border-slate-100 last:border-0 py-3">
                <p className="text-sm font-medium">Scheduled {ei.scheduledAt ? formatDate(String(ei.scheduledAt)) : ''} · <Badge>{String(ei.status || '')}</Badge></p>
                {ei.overallRating ? <p className="text-xs text-slate-500">Overall rating: {String(ei.overallRating)}/5</p> : null}
                {ei.summary ? <p className="text-sm mt-1 text-slate-600">{String(ei.summary)}</p> : null}
              </div>
            ))}
          </CardContent></Card>
        </TabsContent>

        <TabsContent value="kt">
          <Card><CardContent>
            {ktQ.isLoading ? <Skeleton className="h-20" /> : rows(ktQ.data).length === 0 ? (
              <EmptyState icon={<BookOpen className="h-6 w-6" />} title="No knowledge transfers" action={{ label: 'Create', onClick: () => navigate('/knowledge-transfers') }} />
            ) : rows(ktQ.data).map((k: AnyObj) => (
              <div key={String(k.id)} className="border-b border-slate-100 last:border-0 py-3">
                <p className="text-sm font-medium">{String(k.area || k.title || 'KT')}</p>
                <p className="text-xs text-slate-500">To: {String(k.toEmployeeName || k.toEmployeeId || '—')} · <Badge>{String(k.status || '')}</Badge></p>
              </div>
            ))}
          </CardContent></Card>
        </TabsContent>

        <TabsContent value="fnf">
          <Card><CardContent>
            <EmptyState icon={<Banknote className="h-6 w-6" />} title="Full & Final Settlement" description="View the FnF calculation, pending reimbursements, and disbursement status." action={{ label: 'Open Payroll FnF', onClick: () => navigate('/pay-runs') }} />
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
                    <span className="text-sm">{String(e.action || '')}</span>
                    <span className="block text-xs text-slate-500 ml-4">{e.at ? formatDate(String(e.at)) : ''} {e.actor ? `· by ${String(e.actor)}` : ''}</span>
                  </li>
                ))}
              </ul>
            )}
          </CardContent></Card>
        </TabsContent>
      </Tabs>
      {s.rehireEligible === false && (
        <div className="flex items-center gap-2 text-xs text-amber-700 bg-amber-50 border border-amber-200 rounded p-3">
          <AlertTriangle className="h-4 w-4" /> Marked NOT eligible for rehire — visible on candidate boomerang screen.
        </div>
      )}
    </div>
  )
}
