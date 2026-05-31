import { useState, useEffect } from 'react'
import { useQuery } from '@tanstack/react-query'
import { CheckSquare } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { DataList } from '@/components/ui/data-list'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { recruitmentService, type Application, type Job } from '@/services/recruitmentService'

export function ApplicationsPage() {
  const [jobId, setJobId] = useState<string>('')
  const jobs = useQuery({ queryKey: ['jobs', 'all'], queryFn: () => recruitmentService.listJobs() })
  const jobList = (jobs.data as Job[]) || []

  useEffect(() => {
    if (!jobId && jobList.length > 0) setJobId(jobList[0].id)
  }, [jobId, jobList])

  const { data, isLoading } = useQuery({
    queryKey: ['applications', 'job', jobId],
    queryFn: () => recruitmentService.applicationsByJob(jobId),
    enabled: !!jobId,
  })
  const items = (data as Application[]) || []

  return (
    <DataList<Application>
      title="Applications" description="Candidate applications for a requisition"
      data={items} isLoading={isLoading || jobs.isLoading}
      emptyIcon={<CheckSquare className="h-10 w-10" />} emptyTitle="No applications"
      action={
        <Select value={jobId} onValueChange={setJobId}>
          <SelectTrigger className="w-64"><SelectValue placeholder="Select requisition" /></SelectTrigger>
          <SelectContent>
            {jobList.map(j => <SelectItem key={j.id} value={j.id}>{j.title}</SelectItem>)}
          </SelectContent>
        </Select>
      }
      columns={[
        { key: 'candidateName', label: 'Candidate' },
        { key: 'stage', label: 'Stage', render: a => <Badge>{a.stage}</Badge> },
        { key: 'rating', label: 'Rating', render: a => a.rating ? `${a.rating}/5` : '—' },
        { key: 'appliedAt', label: 'Applied' },
      ]}
    />
  )
}
