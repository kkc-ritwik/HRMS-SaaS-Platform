import { useParams, useNavigate } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { ArrowLeft, Mail, Phone, Building, Briefcase } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Avatar } from '@/components/ui/avatar'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Tabs, TabsList, TabsTrigger, TabsContent } from '@/components/ui/tabs'
import { Skeleton } from '@/components/ui/skeleton'
import { recruitmentService, type Candidate, type Application } from '@/services/recruitmentService'

export function CandidateDetailPage() {
  const { id = '' } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const cand = useQuery({ queryKey: ['candidate', id], queryFn: () => recruitmentService.getCandidate(id), enabled: !!id })
  const apps = useQuery({ queryKey: ['cand-apps', id], queryFn: () => recruitmentService.applicationsByCandidate(id), enabled: !!id })

  if (cand.isLoading) return <Skeleton className="h-96" />
  const c = cand.data as Candidate
  if (!c) return <p>Candidate not found</p>

  const applications: Application[] = (apps.data as Application[]) || []

  return (
    <div className="space-y-6">
      <Button variant="ghost" size="sm" onClick={() => navigate('/candidates')}>
        <ArrowLeft className="h-4 w-4 mr-1" /> Back
      </Button>

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

      <Tabs defaultValue="profile">
        <TabsList>
          <TabsTrigger value="profile">Profile</TabsTrigger>
          <TabsTrigger value="applications">Applications ({applications.length})</TabsTrigger>
          <TabsTrigger value="resume">Resume</TabsTrigger>
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
          <Card>
            <CardContent className="p-0">
              {applications.length === 0 ? (
                <p className="p-6 text-sm text-slate-500 text-center">No applications</p>
              ) : (
                <table className="w-full text-sm">
                  <thead className="bg-slate-50 text-xs uppercase text-slate-500">
                    <tr>
                      <th className="p-3 text-left">Job</th>
                      <th className="p-3 text-left">Stage</th>
                      <th className="p-3 text-left">Applied</th>
                    </tr>
                  </thead>
                  <tbody>
                    {applications.map(a => (
                      <tr key={a.id} className="border-t hover:bg-slate-50">
                        <td className="p-3">{a.jobTitle}</td>
                        <td className="p-3"><Badge>{a.stage}</Badge></td>
                        <td className="p-3">{new Date(a.appliedAt).toLocaleDateString()}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </CardContent>
          </Card>
        </TabsContent>
        <TabsContent value="resume">
          <Card>
            <CardContent className="p-6">
              {c.resume ? <a href={c.resume} target="_blank" rel="noreferrer" className="text-violet-600 hover:underline">Download resume</a>
                : <p className="text-sm text-slate-500">No resume uploaded</p>}
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  )
}
