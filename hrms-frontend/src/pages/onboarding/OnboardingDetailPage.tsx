/**
 * Rich onboarding-workflow detail page — 6 tabs covering Zoho-parity surface:
 *   Overview · Tasks · Buddy · Documents · Probation · Pre-Onboarding Portal
 */
import { useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import {
  ArrowLeft, Check, Circle, FileText, ClipboardCheck, Users, Award,
  Send, BookOpen, Star,
} from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Tabs, TabsList, TabsTrigger, TabsContent } from '@/components/ui/tabs'
import { Skeleton } from '@/components/ui/skeleton'
import { Progress } from '@/components/ui/progress'
import { EmptyState } from '@/components/ui/empty-state'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { onboardingService, type OnboardingWorkflow, type OnboardingTask } from '@/services/onboardingService'
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

export function OnboardingDetailPage() {
  const { id = '' } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const qc = useQueryClient()
  const [tab, setTab] = useState('overview')
  const [inviteEmail, setInviteEmail] = useState('')

  const wfQ = useQuery({ queryKey: ['onboarding', id], queryFn: () => onboardingService.getWorkflow(id), enabled: !!id })
  const tasksQ = useQuery({ queryKey: ['onboarding-tasks', id], queryFn: () => onboardingService.listTasks(id), enabled: !!id })
  const w = wfQ.data as OnboardingWorkflow | undefined
  const employeeId = w?.employeeId || ''

  // Lazy per-tab
  const buddyQ = useQuery({ queryKey: ['onb-buddy', employeeId], queryFn: () => Catalog.onboarding.buddies.forEmployee(employeeId), enabled: !!employeeId && tab === 'buddy' })
  const docsQ = useQuery({ queryKey: ['onb-docs', employeeId], queryFn: () => Catalog.documents.forEmployee(employeeId), enabled: !!employeeId && tab === 'documents' })
  const probationQ = useQuery({ queryKey: ['onb-probation', employeeId], queryFn: () => Catalog.onboarding.probation.forEmployee(employeeId), enabled: !!employeeId && tab === 'probation' })

  const complete = useMutation({ mutationFn: (taskId: string) => onboardingService.completeTask(taskId), onSuccess: () => { toast.success('Task completed'); qc.invalidateQueries({ queryKey: ['onboarding-tasks', id] }) } })
  const sendInvite = useMutation({
    mutationFn: () => Catalog.onboarding.preboard.invite({ email: inviteEmail, candidateId: id }),
    onSuccess: () => { toast.success('Pre-onboarding invite sent'); setInviteEmail('') },
    onError: () => toast.error('Failed to send invite'),
  })

  if (wfQ.isLoading) return <Skeleton className="h-96" />
  if (!w) return <p>Workflow not found</p>
  const taskList = (tasksQ.data as OnboardingTask[]) || []

  const byCategory = taskList.reduce<Record<string, OnboardingTask[]>>((acc, t) => {
    const cat = t.category || 'Other'
    if (!acc[cat]) acc[cat] = []
    acc[cat].push(t)
    return acc
  }, {})

  return (
    <div className="space-y-6">
      <Button variant="ghost" size="sm" onClick={() => navigate('/onboarding')}>
        <ArrowLeft className="h-4 w-4 mr-1" /> Back
      </Button>

      <Card>
        <CardContent className="p-6">
          <div className="flex items-start justify-between">
            <div>
              <h1 className="text-2xl font-bold">Onboarding · {w.employeeId.slice(0, 8)}…</h1>
              <p className="text-sm text-slate-500 mt-1">Started {w.startDate} · Expected {w.expectedCompletion || 'TBD'}</p>
            </div>
            <Badge>{w.status}</Badge>
          </div>
          <div className="mt-4">
            <div className="flex justify-between text-xs text-slate-500 mb-1"><span>Progress</span><span>{w.progressPercent}%</span></div>
            <Progress value={w.progressPercent} />
          </div>
        </CardContent>
      </Card>

      <Tabs value={tab} onValueChange={setTab}>
        <TabsList className="flex flex-wrap h-auto">
          <TabsTrigger value="overview">Overview</TabsTrigger>
          <TabsTrigger value="tasks"><ClipboardCheck className="h-3.5 w-3.5" /> Tasks ({taskList.length})</TabsTrigger>
          <TabsTrigger value="buddy"><Users className="h-3.5 w-3.5" /> Buddy</TabsTrigger>
          <TabsTrigger value="documents"><FileText className="h-3.5 w-3.5" /> Documents</TabsTrigger>
          <TabsTrigger value="probation"><Star className="h-3.5 w-3.5" /> Probation</TabsTrigger>
          <TabsTrigger value="preboard"><Send className="h-3.5 w-3.5" /> Pre-Onboarding</TabsTrigger>
        </TabsList>

        {/* Overview */}
        <TabsContent value="overview">
          <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
            <Card><CardContent className="p-4">
              <p className="text-xs text-slate-500">Total tasks</p>
              <p className="text-2xl font-bold">{taskList.length}</p>
            </CardContent></Card>
            <Card><CardContent className="p-4">
              <p className="text-xs text-slate-500">Completed</p>
              <p className="text-2xl font-bold text-green-600">{taskList.filter(t => t.status === 'COMPLETED').length}</p>
            </CardContent></Card>
            <Card><CardContent className="p-4">
              <p className="text-xs text-slate-500">Pending</p>
              <p className="text-2xl font-bold text-amber-600">{taskList.filter(t => t.status !== 'COMPLETED').length}</p>
            </CardContent></Card>
            <Card><CardContent className="p-4">
              <p className="text-xs text-slate-500">Progress</p>
              <p className="text-2xl font-bold">{w.progressPercent}%</p>
            </CardContent></Card>
          </div>
        </TabsContent>

        {/* Tasks */}
        <TabsContent value="tasks">
          <div className="space-y-4">
            {Object.entries(byCategory).map(([cat, items]) => (
              <Card key={cat}>
                <CardHeader><CardTitle className="flex items-center gap-2 text-sm"><ClipboardCheck className="h-4 w-4" /> {cat}</CardTitle></CardHeader>
                <CardContent className="space-y-2">
                  {items.map(t => (
                    <div key={t.id} className={cn('flex items-start gap-3 p-3 rounded-lg border', t.status === 'COMPLETED' ? 'bg-green-50 border-green-200' : 'border-slate-100')}>
                      <button onClick={() => t.status !== 'COMPLETED' && complete.mutate(t.id)} className="mt-0.5">
                        {t.status === 'COMPLETED'
                          ? <div className="h-5 w-5 rounded-full bg-green-500 text-white flex items-center justify-center"><Check className="h-3 w-3" /></div>
                          : <Circle className="h-5 w-5 text-slate-300 hover:text-brand-500" />}
                      </button>
                      <div className="flex-1">
                        <p className={cn('text-sm font-medium', t.status === 'COMPLETED' && 'line-through text-slate-500')}>{t.title}</p>
                        {t.description && <p className="text-xs text-slate-500 mt-0.5">{t.description}</p>}
                        <div className="flex items-center gap-3 mt-1 text-xs text-slate-400">
                          {t.assigneeRole && <span>👤 {t.assigneeRole}</span>}
                          {t.dueDate && <span>📅 Due {formatDate(t.dueDate)}</span>}
                          {t.documentUri && <a className="text-brand-600 hover:underline flex items-center gap-1" href={t.documentUri} target="_blank" rel="noreferrer"><FileText className="h-3 w-3" />Document</a>}
                        </div>
                      </div>
                      <Badge>{t.status}</Badge>
                    </div>
                  ))}
                </CardContent>
              </Card>
            ))}
            {taskList.length === 0 && <EmptyState icon={<ClipboardCheck className="h-6 w-6" />} title="No tasks configured" />}
          </div>
        </TabsContent>

        {/* Buddy */}
        <TabsContent value="buddy">
          <Card><CardContent>
            {buddyQ.isLoading ? <Skeleton className="h-20" /> : rows(buddyQ.data).length === 0 ? (
              <EmptyState icon={<Users className="h-6 w-6" />} title="No buddy assigned" action={{ label: 'Assign buddy', onClick: () => navigate('/buddy-assignments') }} />
            ) : rows(buddyQ.data).map((b: AnyObj) => (
              <div key={String(b.id)} className="border-b border-slate-100 last:border-0 py-3">
                <p className="text-sm font-medium">{String(b.buddyName || b.buddyId || '')}</p>
                <p className="text-xs text-slate-500">Assigned {b.assignedAt ? formatDate(String(b.assignedAt)) : ''} · <Badge>{String(b.status || 'ACTIVE')}</Badge></p>
              </div>
            ))}
          </CardContent></Card>
        </TabsContent>

        {/* Documents */}
        <TabsContent value="documents">
          <Card><CardContent>
            {docsQ.isLoading ? <Skeleton className="h-20" /> : rows(docsQ.data).length === 0 ? (
              <EmptyState icon={<FileText className="h-6 w-6" />} title="No documents uploaded yet" />
            ) : rows(docsQ.data).map((d: AnyObj) => (
              <div key={String(d.id)} className="flex items-center justify-between border-b border-slate-100 last:border-0 py-2">
                <div><p className="text-sm font-medium">{String(d.documentName || d.name || '')}</p><p className="text-xs text-slate-500">{String(d.documentType || '')}</p></div>
                <Badge>{String(d.status || 'PENDING')}</Badge>
              </div>
            ))}
          </CardContent></Card>
        </TabsContent>

        {/* Probation */}
        <TabsContent value="probation">
          <Card><CardContent>
            {probationQ.isLoading ? <Skeleton className="h-20" /> : rows(probationQ.data).length === 0 ? (
              <EmptyState icon={<Award className="h-6 w-6" />} title="No probation reviews scheduled" action={{ label: 'Schedule review', onClick: () => navigate('/probation-reviews') }} />
            ) : rows(probationQ.data).map((p: AnyObj) => (
              <div key={String(p.id)} className="border-b border-slate-100 last:border-0 py-3">
                <p className="text-sm font-medium">Review on {p.reviewDate ? formatDate(String(p.reviewDate)) : '—'}</p>
                <p className="text-xs text-slate-500">Decision: {String(p.decision || 'PENDING')} {p.rating ? `· Rating ${p.rating}/5` : ''}</p>
                {p.notes ? <p className="text-sm mt-1 text-slate-600">{String(p.notes)}</p> : null}
              </div>
            ))}
          </CardContent></Card>
        </TabsContent>

        {/* Pre-Onboarding */}
        <TabsContent value="preboard">
          <Card>
            <CardHeader><CardTitle className="text-sm">Send pre-onboarding portal invite</CardTitle></CardHeader>
            <CardContent className="space-y-3">
              <div>
                <Label>Candidate email</Label>
                <Input value={inviteEmail} onChange={e => setInviteEmail(e.target.value)} placeholder="name@example.com" />
              </div>
              <Button onClick={() => sendInvite.mutate()} disabled={!inviteEmail || sendInvite.isPending}>
                <Send className="h-4 w-4 mr-1" /> Send invite
              </Button>
              <p className="text-xs text-slate-500">
                The candidate will receive an email with a one-time link to upload documents,
                fill personal details, and acknowledge the employee handbook before Day 1.
              </p>
              <Button variant="ghost" onClick={() => navigate('/pre-onboarding')}><BookOpen className="h-4 w-4 mr-1" /> View all pre-onboarding portals</Button>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  )
}
