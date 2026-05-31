import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { ArrowLeft, Briefcase, Users, CheckSquare } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Tabs, TabsList, TabsTrigger, TabsContent } from '@/components/ui/tabs'
import { Skeleton } from '@/components/ui/skeleton'
import { Avatar } from '@/components/ui/avatar'
import { recruitmentService, type Job, type Application } from '@/services/recruitmentService'
import { toast } from 'sonner'

export function JobDetailPage() {
  const { id = '' } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const qc = useQueryClient()

  const job = useQuery({ queryKey: ['job', id], queryFn: () => recruitmentService.getJob(id), enabled: !!id })
  const apps = useQuery({ queryKey: ['applications', id], queryFn: () => recruitmentService.applicationsByJob(id), enabled: !!id })

  const publish = useMutation({
    mutationFn: () => recruitmentService.activateJob(id),
    onSuccess: () => { toast.success('Published'); qc.invalidateQueries({ queryKey: ['job', id] }) },
  })
  const close = useMutation({
    mutationFn: () => recruitmentService.closeJob(id),
    onSuccess: () => { toast.success('Closed'); qc.invalidateQueries({ queryKey: ['job', id] }) },
  })

  if (job.isLoading) return <Skeleton className="h-96" />
  const j = job.data as Job
  if (!j) return <p className="text-sm text-slate-500">Job not found</p>

  const applications: Application[] = (apps.data as Application[]) || []

  return (
    <div className="space-y-6">
      <Button variant="ghost" size="sm" onClick={() => navigate('/jobs')}>
        <ArrowLeft className="h-4 w-4 mr-1" /> Back to jobs
      </Button>

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
            </div>
          </div>
          <div className="flex flex-col gap-2">
            {j.status === 'DRAFT' && <Button size="sm" onClick={() => publish.mutate()}>Publish</Button>}
            {j.status === 'OPEN' && <Button size="sm" variant="outline" onClick={() => close.mutate()}>Close</Button>}
          </div>
        </CardContent>
      </Card>

      <Tabs defaultValue="overview">
        <TabsList>
          <TabsTrigger value="overview">Overview</TabsTrigger>
          <TabsTrigger value="pipeline"><Users className="h-3.5 w-3.5 mr-1" />Pipeline ({applications.length})</TabsTrigger>
          <TabsTrigger value="interviews"><CheckSquare className="h-3.5 w-3.5 mr-1" />Interviews</TabsTrigger>
        </TabsList>
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
        <TabsContent value="pipeline">
          <Card>
            <CardContent className="p-0">
              {applications.length === 0 ? (
                <p className="p-6 text-sm text-slate-500 text-center">No applications yet</p>
              ) : (
                <table className="w-full text-sm">
                  <thead className="bg-slate-50 text-xs uppercase text-slate-500">
                    <tr>
                      <th className="p-3 text-left">Candidate</th>
                      <th className="p-3 text-left">Stage</th>
                      <th className="p-3 text-left">Rating</th>
                      <th className="p-3 text-left">Applied</th>
                    </tr>
                  </thead>
                  <tbody>
                    {applications.map(a => (
                      <tr key={a.id} className="border-t hover:bg-slate-50">
                        <td className="p-3"><div className="flex items-center gap-2"><Avatar name={a.candidateName} size="sm" /> {a.candidateName}</div></td>
                        <td className="p-3"><Badge>{a.stage}</Badge></td>
                        <td className="p-3">{a.rating ? `${a.rating}/5` : '—'}</td>
                        <td className="p-3">{new Date(a.appliedAt).toLocaleDateString()}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </CardContent>
          </Card>
        </TabsContent>
        <TabsContent value="interviews">
          <Card><CardContent className="p-6 text-sm text-slate-500 text-center">Interviews list coming up — manage via /interviews</CardContent></Card>
        </TabsContent>
      </Tabs>
    </div>
  )
}
