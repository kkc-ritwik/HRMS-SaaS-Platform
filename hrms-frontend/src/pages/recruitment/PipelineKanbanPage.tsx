import { useState, useEffect } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { GripVertical, Users } from 'lucide-react'
import { Card, CardContent } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { Avatar } from '@/components/ui/avatar'
import { Badge } from '@/components/ui/badge'
import { PageHeader } from '@/components/ui/page-header'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { recruitmentService, type Application, type Job } from '@/services/recruitmentService'
import { toast } from 'sonner'

const STAGES: Array<Application['stage']> = ['APPLIED', 'SCREENING', 'PHONE_SCREEN', 'INTERVIEW', 'ASSESSMENT', 'OFFER', 'HIRED', 'REJECTED']
const STAGE_COLORS: Record<string, string> = {
  APPLIED: 'bg-slate-100 text-slate-700',
  SCREENING: 'bg-blue-100 text-blue-700',
  PHONE_SCREEN: 'bg-indigo-100 text-indigo-700',
  INTERVIEW: 'bg-violet-100 text-violet-700',
  ASSESSMENT: 'bg-amber-100 text-amber-700',
  OFFER: 'bg-emerald-100 text-emerald-700',
  HIRED: 'bg-green-100 text-green-700',
  REJECTED: 'bg-red-100 text-red-700',
}

export function PipelineKanbanPage() {
  const qc = useQueryClient()
  const [draggingId, setDraggingId] = useState<string | null>(null)
  const [jobId, setJobId] = useState<string>('')

  const jobs = useQuery({ queryKey: ['jobs', 'all'], queryFn: () => recruitmentService.listJobs() })
  const jobList = (jobs.data as Job[]) || []
  useEffect(() => { if (!jobId && jobList.length > 0) setJobId(jobList[0].id) }, [jobId, jobList])

  const { data, isLoading } = useQuery({
    queryKey: ['applications-pipeline', jobId],
    queryFn: () => recruitmentService.applicationsByJob(jobId),
    enabled: !!jobId,
  })
  const items: Application[] = (data as Application[]) || []

  const moveStage = useMutation({
    mutationFn: ({ id, stage }: { id: string; stage: Application['stage'] }) => recruitmentService.moveStage(id, stage),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['applications-pipeline', jobId] }); toast.success('Stage updated') },
  })

  const grouped = STAGES.reduce<Record<string, Application[]>>((acc, s) => {
    acc[s] = items.filter(a => a.stage === s)
    return acc
  }, {})

  return (
    <div className="space-y-6">
      <PageHeader title="Recruitment Pipeline" description="Drag candidates across stages — Kanban view" action={
        <Select value={jobId} onValueChange={setJobId}>
          <SelectTrigger className="w-64"><SelectValue placeholder="Select requisition" /></SelectTrigger>
          <SelectContent>
            {jobList.map(j => <SelectItem key={j.id} value={j.id}>{j.title}</SelectItem>)}
          </SelectContent>
        </Select>
      } />
      {isLoading ? <Skeleton className="h-96" /> : (
        <div className="flex gap-3 overflow-x-auto pb-2">
          {STAGES.map(stage => (
            <div
              key={stage}
              className="flex-shrink-0 w-64 bg-slate-50 rounded-lg p-2"
              onDragOver={e => e.preventDefault()}
              onDrop={() => {
                if (draggingId) {
                  const app = items.find(a => a.id === draggingId)
                  if (app && app.stage !== stage) moveStage.mutate({ id: app.id, stage })
                  setDraggingId(null)
                }
              }}
            >
              <div className="flex items-center justify-between px-2 py-1.5 mb-2">
                <div className="flex items-center gap-2">
                  <Badge className={STAGE_COLORS[stage]}>{stage}</Badge>
                </div>
                <span className="text-xs text-slate-500">{grouped[stage].length}</span>
              </div>
              <div className="space-y-2 min-h-[100px]">
                {grouped[stage].length === 0 ? (
                  <p className="text-xs text-slate-400 text-center py-8 flex flex-col items-center gap-2">
                    <Users className="h-6 w-6 text-slate-300" /> No candidates
                  </p>
                ) : (
                  grouped[stage].map(app => (
                    <Card
                      key={app.id}
                      draggable
                      onDragStart={() => setDraggingId(app.id)}
                      onDragEnd={() => setDraggingId(null)}
                      className={`cursor-grab active:cursor-grabbing ${draggingId === app.id ? 'opacity-50' : ''}`}
                    >
                      <CardContent className="p-3">
                        <div className="flex items-start gap-2">
                          <GripVertical className="h-4 w-4 text-slate-300 flex-shrink-0 mt-0.5" />
                          <Avatar name={app.candidateName} size="sm" />
                          <div className="flex-1 min-w-0">
                            <p className="text-sm font-medium truncate">{app.candidateName}</p>
                            <p className="text-xs text-slate-500 truncate">{app.jobTitle}</p>
                            {app.rating && <p className="text-xs text-amber-600 mt-1">★ {app.rating}/5</p>}
                          </div>
                        </div>
                      </CardContent>
                    </Card>
                  ))
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
