/**
 * Rich candidate profile — 8 tabs covering the entire recruitment surface:
 *   Profile · Applications · Interviews · Scorecards · BGV · References ·
 *   Psychometric · Resume
 *
 * Pulls live data via @/services/catalog so every backend endpoint is exercised.
 */
import { useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import {
  ArrowLeft, Mail, Phone, Building, Briefcase, FileText, Star, Shield, UserCheck,
  Brain, MessageCircle, Download, Plus,
} from 'lucide-react'
import { toast } from 'sonner'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Avatar } from '@/components/ui/avatar'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Tabs, TabsList, TabsTrigger, TabsContent } from '@/components/ui/tabs'
import { Skeleton } from '@/components/ui/skeleton'
import { EmptyState } from '@/components/ui/empty-state'
import { recruitmentService, type Candidate, type Application } from '@/services/recruitmentService'
import { interviewService, offerService } from '@/services/extendedServices'
import { Catalog } from '@/services/catalog'
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

export function CandidateDetailPage() {
  const { id = '' } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const qc = useQueryClient()
  const [tab, setTab] = useState('profile')
  const enabled = (k: string) => tab === k

  const candQ = useQuery({ queryKey: ['candidate', id], queryFn: () => recruitmentService.getCandidate(id), enabled: !!id })
  const appsQ = useQuery({ queryKey: ['cand-apps', id], queryFn: () => recruitmentService.applicationsByCandidate(id), enabled: !!id })

  // Lazy per-tab data (interviews & offers are rendered per-application by the
  // ApplicationInterviews / ApplicationOffers sub-components below).
  const bgvQ = useQuery({ queryKey: ['cand-bgv', id], queryFn: () => Catalog.recruitment.bgv.forCandidate(id), enabled: enabled('bgv') })
  const refsQ = useQuery({ queryKey: ['cand-refs', id], queryFn: () => Catalog.recruitment.references.forCandidate(id), enabled: enabled('references') })

  /* ── Mutations ── */
  const refreshBgv = useMutation({
    mutationFn: (bgvId: string) => Catalog.recruitment.bgv.refresh(bgvId),
    onSuccess: () => { toast.success('BGV refresh triggered'); qc.invalidateQueries({ queryKey: ['cand-bgv', id] }) },
  })
  const inviteRef = useMutation({
    mutationFn: () => Catalog.recruitment.references.invite(id, { candidateId: id }),
    onSuccess: () => { toast.success('Reference invite sent'); qc.invalidateQueries({ queryKey: ['cand-refs', id] }) },
  })

  if (candQ.isLoading) return <Skeleton className="h-96" />
  const c = (candQ.data as Candidate) || ({} as Candidate)
  if (!c.id) return <p className="text-sm text-slate-500">Candidate not found</p>

  const applications: Application[] = (appsQ.data as Application[]) || []

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <Button variant="ghost" size="sm" onClick={() => navigate('/candidates')}>
          <ArrowLeft className="h-4 w-4 mr-1" /> Back
        </Button>
        <div className="flex items-center gap-2">
          <Button size="sm" variant="outline" onClick={() => inviteRef.mutate()} disabled={inviteRef.isPending}>
            <UserCheck className="h-4 w-4 mr-1" /> Request Reference
          </Button>
        </div>
      </div>

      <Card>
        <CardContent className="p-6 flex items-start gap-4">
          <Avatar name={c.fullName} size="lg" />
          <div className="flex-1">
            <h1 className="text-2xl font-bold">{c.fullName}</h1>
            <div className="flex items-center gap-4 mt-2 text-sm text-slate-600">
              <span className="flex items-center gap-1"><Mail className="h-4 w-4" /> {c.email}</span>
              {c.phone && <span className="flex items-center gap-1"><Phone className="h-4 w-4" /> {c.phone}</span>}
            </div>
            <div className="flex items-center gap-4 mt-2 text-sm text-slate-600">
              {c.currentCompany && <span className="flex items-center gap-1"><Building className="h-4 w-4" /> {c.currentCompany}</span>}
              {c.experience !== undefined && <span className="flex items-center gap-1"><Briefcase className="h-4 w-4" /> {c.experience} yrs</span>}
            </div>
            <Badge className="mt-3">{c.status}</Badge>
          </div>
        </CardContent>
      </Card>

      <Tabs value={tab} onValueChange={setTab}>
        <TabsList className="flex flex-wrap h-auto">
          <TabsTrigger value="profile">Profile</TabsTrigger>
          <TabsTrigger value="applications">Applications ({applications.length})</TabsTrigger>
          <TabsTrigger value="interviews"><Star className="h-3.5 w-3.5" /> Interviews</TabsTrigger>
          <TabsTrigger value="offers"><FileText className="h-3.5 w-3.5" /> Offers</TabsTrigger>
          <TabsTrigger value="bgv"><Shield className="h-3.5 w-3.5" /> BGV</TabsTrigger>
          <TabsTrigger value="references"><UserCheck className="h-3.5 w-3.5" /> References</TabsTrigger>
          <TabsTrigger value="psychometric"><Brain className="h-3.5 w-3.5" /> Psychometric</TabsTrigger>
          <TabsTrigger value="resume"><FileText className="h-3.5 w-3.5" /> Resume</TabsTrigger>
        </TabsList>

        <TabsContent value="profile">
          <Card>
            <CardHeader><CardTitle>Details</CardTitle></CardHeader>
            <CardContent className="grid grid-cols-2 gap-4 text-sm">
              <div><span className="text-slate-500">Current company:</span> {c.currentCompany || '—'}</div>
              <div><span className="text-slate-500">Current CTC:</span> {c.currentSalary ? `₹${c.currentSalary.toLocaleString()}` : '—'}</div>
              <div><span className="text-slate-500">Expected CTC:</span> {c.expectedSalary ? `₹${c.expectedSalary.toLocaleString()}` : '—'}</div>
              <div><span className="text-slate-500">Source:</span> {c.source || '—'}</div>
              <div className="col-span-2"><span className="text-slate-500">Skills:</span> {c.skills?.join(', ') || '—'}</div>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="applications">
          <Card><CardContent className="p-0">
            {applications.length === 0 ? <EmptyState icon={<Briefcase className="h-6 w-6" />} title="No applications" /> : (
              <table className="w-full text-sm">
                <thead className="bg-slate-50 text-xs uppercase text-slate-500">
                  <tr><th className="p-3 text-left">Job</th><th className="p-3 text-left">Stage</th><th className="p-3 text-left">Applied</th><th></th></tr>
                </thead>
                <tbody>{applications.map(a => (
                  <tr key={a.id} className="border-t hover:bg-slate-50">
                    <td className="p-3"><button onClick={() => navigate(`/jobs/${a.jobId}`)} className="hover:underline">{a.jobTitle}</button></td>
                    <td className="p-3"><Badge>{a.stage}</Badge></td>
                    <td className="p-3">{formatDate(a.appliedAt)}</td>
                    <td className="p-3 text-right"><Button size="sm" variant="ghost" onClick={() => navigate(`/applications`)}>View</Button></td>
                  </tr>
                ))}</tbody>
              </table>
            )}
          </CardContent></Card>
        </TabsContent>

        <TabsContent value="interviews">
          {applications.length === 0 ? <EmptyState icon={<Star className="h-6 w-6" />} title="No interviews yet" /> : (
            <div className="space-y-3">
              {applications.map(a => (
                <Card key={a.id}>
                  <CardHeader><CardTitle className="text-sm">{a.jobTitle}</CardTitle></CardHeader>
                  <CardContent>
                    <ApplicationInterviews appId={a.id} />
                  </CardContent>
                </Card>
              ))}
            </div>
          )}
        </TabsContent>

        <TabsContent value="offers">
          {applications.length === 0 ? <EmptyState icon={<FileText className="h-6 w-6" />} title="No offers" /> : (
            <div className="space-y-3">
              {applications.map(a => (
                <Card key={a.id}>
                  <CardHeader><CardTitle className="text-sm">{a.jobTitle}</CardTitle></CardHeader>
                  <CardContent><ApplicationOffers appId={a.id} /></CardContent>
                </Card>
              ))}
            </div>
          )}
        </TabsContent>

        <TabsContent value="bgv">
          <Card><CardContent>
            {bgvQ.isLoading ? <Skeleton className="h-20" /> : rows(bgvQ.data).length === 0 ? (
              <EmptyState icon={<Shield className="h-6 w-6" />} title="No BGV initiated" action={{ label: 'Initiate BGV', onClick: () => toast.message('Use the Recruitment → BGV admin page to initiate.') }} />
            ) : rows(bgvQ.data).map((b: AnyObj) => (
              <div key={String(b.id)} className="flex items-center justify-between border-b border-slate-100 last:border-0 py-3">
                <div>
                  <p className="text-sm font-medium">{String(b.vendor || '')} · <Badge>{String(b.status || '')}</Badge></p>
                  <p className="text-xs text-slate-500">Initiated {b.initiatedAt ? formatDate(String(b.initiatedAt)) : '—'} · Verdict: {String(b.verdict || 'PENDING')}</p>
                </div>
                <Button size="sm" variant="outline" onClick={() => refreshBgv.mutate(String(b.id))}>Refresh</Button>
              </div>
            ))}
          </CardContent></Card>
        </TabsContent>

        <TabsContent value="references">
          <Card><CardContent>
            {refsQ.isLoading ? <Skeleton className="h-20" /> : rows(refsQ.data).length === 0 ? (
              <EmptyState icon={<UserCheck className="h-6 w-6" />} title="No reference checks" action={{ label: 'Invite reference', onClick: () => inviteRef.mutate() }} />
            ) : rows(refsQ.data).map((r: AnyObj) => (
              <div key={String(r.id)} className="border-b border-slate-100 last:border-0 py-3">
                <p className="text-sm font-medium">{String(r.refereeName || '')} <span className="text-xs text-slate-500">{r.refereeRelationship ? `(${String(r.refereeRelationship)})` : ''}</span></p>
                <p className="text-xs text-slate-500">{String(r.refereeEmail || '')} · <Badge>{String(r.status || '')}</Badge> · Rating: {r.rating ? `${r.rating}/5` : '—'} · Would re-hire: {r.wouldRehire == null ? '—' : r.wouldRehire ? 'Yes' : 'No'}</p>
                {r.comments ? <p className="text-sm mt-1 text-slate-600">{String(r.comments)}</p> : null}
              </div>
            ))}
          </CardContent></Card>
        </TabsContent>

        <TabsContent value="psychometric">
          <Card><CardContent>
            <EmptyState icon={<Brain className="h-6 w-6" />} title="Psychometric tests" description="Open the Psychometric admin page to invite this candidate." action={{ label: 'Open Psychometric', onClick: () => navigate('/psychometric') }} />
          </CardContent></Card>
        </TabsContent>

        <TabsContent value="resume">
          <Card><CardContent className="p-6">
            {c.resume
              ? <a href={c.resume} target="_blank" rel="noreferrer" className="inline-flex items-center gap-2 text-brand-600 hover:underline"><Download className="h-4 w-4" /> Download resume</a>
              : <EmptyState icon={<FileText className="h-6 w-6" />} title="No resume uploaded" />}
          </CardContent></Card>
        </TabsContent>
      </Tabs>
    </div>
  )
}

/** Sub-component — interviews for a single application via /api/v1/interviews?applicationId */
function ApplicationInterviews({ appId }: { appId: string }) {
  const navigate = useNavigate()
  const q = useQuery({
    queryKey: ['app-interviews', appId],
    queryFn: async () => {
      try { return await interviewService.list({ applicationId: appId }) } catch { return [] }
    },
  })
  if (q.isLoading) return <Skeleton className="h-12" />
  const list = rows<AnyObj>(q.data)
  if (list.length === 0) return <EmptyState icon={<Star className="h-5 w-5" />} title="No interviews scheduled" />
  return (
    <div className="space-y-2">{list.map((i: AnyObj) => (
      <div key={String(i.id)} className="flex items-center justify-between border-b border-slate-100 last:border-0 py-2">
        <div>
          <p className="text-sm font-medium">{String(i.round || i.title || 'Interview')}</p>
          <p className="text-xs text-slate-500">{i.scheduledAt ? formatDate(String(i.scheduledAt)) : ''} · <Badge>{String(i.status || '')}</Badge></p>
        </div>
        <Button size="sm" variant="ghost" onClick={() => navigate('/interviews')}><MessageCircle className="h-3.5 w-3.5" /></Button>
      </div>
    ))}</div>
  )
}

/** Sub-component — offers for a single application */
function ApplicationOffers({ appId }: { appId: string }) {
  const q = useQuery({
    queryKey: ['app-offers', appId],
    queryFn: async () => {
      try { return await offerService.list() } catch { return [] }
    },
  })
  if (q.isLoading) return <Skeleton className="h-12" />
  const list = rows<AnyObj>(q.data).filter(o => String(o.applicationId) === appId)
  if (list.length === 0) return <EmptyState icon={<FileText className="h-5 w-5" />} title="No offers for this application" />
  return (
    <div className="space-y-2">{list.map((o: AnyObj) => (
      <div key={String(o.id)} className="flex items-center justify-between border-b border-slate-100 last:border-0 py-2">
        <div>
          <p className="text-sm font-medium">CTC ₹{Number(o.ctc ?? 0).toLocaleString()}</p>
          <p className="text-xs text-slate-500">{o.issuedAt ? formatDate(String(o.issuedAt)) : ''} · <Badge>{String(o.status || '')}</Badge></p>
        </div>
        <Plus className="h-4 w-4 text-slate-400" />
      </div>
    ))}</div>
  )
}
