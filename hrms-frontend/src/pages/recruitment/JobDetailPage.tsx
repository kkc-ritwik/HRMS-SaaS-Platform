/**
 * Rich job-requisition detail page — 6 tabs covering the full hiring funnel:
 *   Overview · Pipeline · Funnel · Analytics · Interviews · Hiring Loops
 */
import { useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import {
  ArrowLeft, Briefcase, Users, CheckSquare, BarChart3, GitBranch, Clock,
  TrendingUp, DollarSign,
} from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Tabs, TabsList, TabsTrigger, TabsContent } from '@/components/ui/tabs'
import { Skeleton } from '@/components/ui/skeleton'
import { Avatar } from '@/components/ui/avatar'
import { EmptyState } from '@/components/ui/empty-state'
import { StatCard } from '@/components/ui/stat-card'
import { recruitmentService, type Job, type Application } from '@/services/recruitmentService'
import { hiringLoopService } from '@/services/extendedServices'
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

const STAGES = ['APPLIED', 'SCREENING', 'PHONE_SCREEN', 'INTERVIEW', 'ASSESSMENT', 'OFFER', 'HIRED', 'REJECTED'] as const

export function JobDetailPage() {
  const { id = '' } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const qc = useQueryClient()
  const [tab, setTab] = useState('overview')

  const jobQ = useQuery({ queryKey: ['job', id], queryFn: () => recruitmentService.getJob(id), enabled: !!id })
  const appsQ = useQuery({ queryKey: ['job-apps', id], queryFn: () => recruitmentService.applicationsByJob(id), enabled: !!id })
  const funnelQ = useQuery({ queryKey: ['job-funnel', id], queryFn: () => Catalog.recruitment.analytics.funnel(id), enabled: !!id && tab === 'funnel' })
  const tthQ = useQuery({ queryKey: ['job-tth', id], queryFn: () => Catalog.recruitment.analytics.timeToHire(id), enabled: !!id && tab === 'analytics' })
  const loopsQ = useQuery({
    queryKey: ['job-loops', id],
    queryFn: async () => { try { return await hiringLoopService.list() } catch { return [] } },
    enabled: !!id && tab === 'loops',
  })

  const publish = useMutation({ mutationFn: () => recruitmentService.activateJob(id), onSuccess: () => { toast.success('Published'); qc.invalidateQueries({ queryKey: ['job', id] }) } })
  const close = useMutation({ mutationFn: () => recruitmentService.closeJob(id), onSuccess: () => { toast.success('Closed'); qc.invalidateQueries({ queryKey: ['job', id] }) } })
  const hold = useMutation({ mutationFn: () => recruitmentService.holdJob(id), onSuccess: () => { toast.success('On hold'); qc.invalidateQueries({ queryKey: ['job', id] }) } })

  if (jobQ.isLoading) return <Skeleton className="h-96" />
  const j = jobQ.data as Job
  if (!j) return <p className="text-sm text-slate-500">Job not found</p>

  const applications: Application[] = (appsQ.data as Application[]) || []
  const stageCounts: Record<string, number> = {}
  STAGES.forEach(s => { stageCounts[s] = 0 })
  applications.forEach(a => { stageCounts[a.stage] = (stageCounts[a.stage] ?? 0) + 1 })

  return (
    <div className="space-y-6">
      <Button variant="ghost" size="sm" onClick={() => navigate('/jobs')}>
        <ArrowLeft className="h-4 w-4 mr-1" /> Back to jobs
      </Button>

      {/* Header */}
      <Card>
        <CardContent className="p-6 flex items-start justify-between">
          <div>
            <div className="flex items-center gap-2 mb-2">
              <Briefcase className="h-5 w-5 text-violet-600" />
              <h1 className="text-2xl font-bold">{j.title}</h1>
              <Badge>{j.status}</Badge>
            </div>
            <p className="text-sm text-slate-500">{j.departmentName} · {j.locationName} · {j.employmentType}</p>
            <div className="flex items-center gap-4 mt-3 text-sm">
              <div><span className="text-slate-500">Openings:</span> <strong>{j.openings}</strong></div>
              <div><span className="text-slate-500">Filled:</span> <strong>{j.filled}</strong></div>
              {j.salaryMin && j.salaryMax && (
                <div><span className="text-slate-500">Salary:</span> <strong>₹{j.salaryMin.toLocaleString()}–₹{j.salaryMax.toLocaleString()}</strong></div>
              )}
              {j.closingDate && <div className="flex items-center gap-1 text-slate-500"><Clock className="h-3.5 w-3.5" /> Closes {formatDate(j.closingDate)}</div>}
            </div>
          </div>
          <div className="flex flex-col gap-2">
            {j.status === 'DRAFT' && <Button size="sm" onClick={() => publish.mutate()} disabled={publish.isPending}>Publish</Button>}
            {j.status === 'OPEN' && <>
              <Button size="sm" variant="outline" onClick={() => hold.mutate()} disabled={hold.isPending}>Hold</Button>
              <Button size="sm" variant="outline" onClick={() => close.mutate()} disabled={close.isPending}>Close</Button>
            </>}
            {j.status === 'ON_HOLD' && <Button size="sm" onClick={() => publish.mutate()}>Resume</Button>}
          </div>
        </CardContent>
      </Card>

      {/* Funnel snapshot stat cards */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
        <StatCard title="Applied" value={applications.length} icon={<Users className="h-5 w-5 text-brand-600" />} />
        <StatCard title="In Pipeline" value={applications.filter(a => !['HIRED', 'REJECTED'].includes(a.stage)).length} icon={<GitBranch className="h-5 w-5 text-brand-600" />} />
        <StatCard title="Offers Out" value={stageCounts.OFFER || 0} icon={<DollarSign className="h-5 w-5 text-brand-600" />} />
        <StatCard title="Hired" value={stageCounts.HIRED || 0} icon={<CheckSquare className="h-5 w-5 text-brand-600" />} />
      </div>

      <Tabs value={tab} onValueChange={setTab}>
        <TabsList className="flex flex-wrap h-auto">
          <TabsTrigger value="overview">Overview</TabsTrigger>
          <TabsTrigger value="pipeline"><Users className="h-3.5 w-3.5" /> Pipeline ({applications.length})</TabsTrigger>
          <TabsTrigger value="funnel"><BarChart3 className="h-3.5 w-3.5" /> Funnel</TabsTrigger>
          <TabsTrigger value="analytics"><TrendingUp className="h-3.5 w-3.5" /> Analytics</TabsTrigger>
          <TabsTrigger value="interviews"><CheckSquare className="h-3.5 w-3.5" /> Interviews</TabsTrigger>
          <TabsTrigger value="loops"><GitBranch className="h-3.5 w-3.5" /> Hiring Loops</TabsTrigger>
        </TabsList>

        {/* Overview */}
        <TabsContent value="overview" className="space-y-4">
          <Card>
            <CardHeader><CardTitle>Description</CardTitle></CardHeader>
            <CardContent><p className="text-sm whitespace-pre-wrap">{j.description || 'No description provided.'}</p></CardContent>
          </Card>
          <Card>
            <CardHeader><CardTitle>Requirements</CardTitle></CardHeader>
            <CardContent><p className="text-sm whitespace-pre-wrap">{j.requirements || 'No requirements specified.'}</p></CardContent>
          </Card>
        </TabsContent>

        {/* Pipeline */}
        <TabsContent value="pipeline">
          <Card><CardContent className="p-0">
            {applications.length === 0 ? <EmptyState icon={<Users className="h-6 w-6" />} title="No applications yet" /> : (
              <table className="w-full text-sm">
                <thead className="bg-slate-50 text-xs uppercase text-slate-500">
                  <tr><th className="p-3 text-left">Candidate</th><th className="p-3 text-left">Stage</th><th className="p-3 text-left">Rating</th><th className="p-3 text-left">Applied</th></tr>
                </thead>
                <tbody>{applications.map(a => (
                  <tr key={a.id} className="border-t hover:bg-slate-50 cursor-pointer" onClick={() => navigate(`/candidates/${a.candidateId}`)}>
                    <td className="p-3"><div className="flex items-center gap-2"><Avatar name={a.candidateName} size="sm" /> {a.candidateName}</div></td>
                    <td className="p-3"><Badge>{a.stage}</Badge></td>
                    <td className="p-3">{a.rating ? `${a.rating}/5` : '—'}</td>
                    <td className="p-3">{formatDate(a.appliedAt)}</td>
                  </tr>
                ))}</tbody>
              </table>
            )}
          </CardContent></Card>
        </TabsContent>

        {/* Funnel (stage breakdown) */}
        <TabsContent value="funnel">
          <Card>
            <CardHeader><CardTitle>Funnel — applications by stage</CardTitle></CardHeader>
            <CardContent>
              {funnelQ.isLoading ? <Skeleton className="h-24" /> : (
                <div className="space-y-2">
                  {STAGES.map(s => {
                    const count = stageCounts[s] || 0
                    const max = Math.max(1, ...Object.values(stageCounts))
                    const pct = Math.round((count / max) * 100)
                    return (
                      <div key={s} className="flex items-center gap-3">
                        <span className="w-28 text-xs uppercase text-slate-500">{s}</span>
                        <div className="flex-1 bg-slate-100 rounded h-6 relative">
                          <div className="absolute inset-y-0 left-0 bg-brand-500 rounded transition-all" style={{ width: `${pct}%` }} />
                          <span className="absolute inset-0 flex items-center px-2 text-xs font-medium">{count}</span>
                        </div>
                      </div>
                    )
                  })}
                </div>
              )}
              {!!funnelQ.data && (
                <pre className="mt-4 p-3 bg-slate-50 text-xs rounded overflow-auto max-h-40">{JSON.stringify(funnelQ.data, null, 2)}</pre>
              )}
            </CardContent>
          </Card>
        </TabsContent>

        {/* Analytics */}
        <TabsContent value="analytics">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <Card>
              <CardHeader><CardTitle className="text-sm">Time to Hire</CardTitle></CardHeader>
              <CardContent>
                {tthQ.isLoading ? <Skeleton className="h-20" /> : tthQ.data ? (
                  <pre className="p-3 bg-slate-50 text-xs rounded overflow-auto">{JSON.stringify(tthQ.data, null, 2)}</pre>
                ) : <EmptyState icon={<TrendingUp className="h-6 w-6" />} title="No data yet" />}
              </CardContent>
            </Card>
            <Card>
              <CardHeader><CardTitle className="text-sm">Pipeline Health</CardTitle></CardHeader>
              <CardContent className="space-y-2 text-sm">
                <div>Total applications: <strong>{applications.length}</strong></div>
                <div>Active pipeline: <strong>{applications.filter(a => !['HIRED', 'REJECTED'].includes(a.stage)).length}</strong></div>
                <div>Rejection rate: <strong>{applications.length ? Math.round(((stageCounts.REJECTED || 0) / applications.length) * 100) : 0}%</strong></div>
                <div>Hire conversion: <strong>{applications.length ? Math.round(((stageCounts.HIRED || 0) / applications.length) * 100) : 0}%</strong></div>
              </CardContent>
            </Card>
          </div>
        </TabsContent>

        {/* Interviews */}
        <TabsContent value="interviews">
          <Card><CardContent className="p-6 text-sm text-slate-500 text-center">
            Manage interviews for applications in this job from the <button onClick={() => navigate('/interviews')} className="text-brand-600 hover:underline">Interviews page</button>.
          </CardContent></Card>
        </TabsContent>

        {/* Hiring Loops */}
        <TabsContent value="loops">
          <Card><CardContent>
            {loopsQ.isLoading ? <Skeleton className="h-24" /> : rows(loopsQ.data).length === 0 ? (
              <EmptyState icon={<GitBranch className="h-6 w-6" />} title="No hiring loop configured" action={{ label: 'Configure', onClick: () => navigate('/hiring-loops') }} />
            ) : rows(loopsQ.data).map((l: AnyObj) => (
              <div key={String(l.id)} className="border-b border-slate-100 last:border-0 py-3">
                <p className="text-sm font-medium">{String(l.name || '')}</p>
                <p className="text-xs text-slate-500">{String(l.stageCount || 0)} rounds · <Badge>{String(l.status || 'ACTIVE')}</Badge></p>
              </div>
            ))}
          </CardContent></Card>
        </TabsContent>
      </Tabs>
    </div>
  )
}
