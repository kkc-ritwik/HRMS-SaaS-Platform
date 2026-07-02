import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Plus, Briefcase } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { DataList } from '@/components/ui/data-list'
import { FormDialog } from '@/components/ui/form-dialog'
import { recruitmentService, type Job } from '@/services/recruitmentService'
import { getErrorMessage } from '@/lib/api'
import { toast } from 'sonner'
import { useNavigate } from 'react-router-dom'

export function JobsPage() {
  const navigate = useNavigate()
  const qc = useQueryClient()
  const [creating, setCreating] = useState(false)

  const { data, isLoading } = useQuery({ queryKey: ['jobs'], queryFn: () => recruitmentService.listJobs() })
  const jobs: Job[] = (data as { data?: Job[]; content?: Job[] } | undefined)?.data
    || (data as { content?: Job[] } | undefined)?.content
    || (Array.isArray(data) ? data : [])

  const create = useMutation({
    mutationFn: (v: Record<string, unknown>) => recruitmentService.createJob(v as Partial<Job>),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['jobs'] }),
  })
  const act = useMutation({
    mutationFn: ({ id, action }: { id: string; action: 'submit' | 'approve' | 'cancel' }) =>
      action === 'submit' ? recruitmentService.submitJob(id)
        : action === 'approve' ? recruitmentService.approveJob(id)
          : recruitmentService.cancelJob(id),
    onSuccess: (_d, v) => { toast.success(`Requisition ${v.action}${v.action === 'cancel' ? 'led' : v.action === 'submit' ? 'ted' : 'd'}`); qc.invalidateQueries({ queryKey: ['jobs'] }) },
    onError: e => toast.error(getErrorMessage(e)),
  })

  return (
    <>
      <DataList<Job>
        title="Job Openings"
        description="Open requisitions across the org"
        action={<Button onClick={() => setCreating(true)}><Plus className="h-4 w-4 mr-1" /> New Job</Button>}
        data={jobs}
        isLoading={isLoading}
        emptyIcon={<Briefcase className="h-10 w-10" />}
        emptyTitle="No open positions"
        emptyDescription="Create your first job opening to start hiring"
        onRowClick={j => navigate(`/jobs/${j.id}`)}
        filters={{
          status: ['DRAFT', 'OPEN', 'CLOSED', 'ON_HOLD'],
          employmentType: ['FULL_TIME', 'PART_TIME', 'CONTRACT', 'INTERN'],
        }}
        columns={[
          { key: 'title', label: 'Title' },
          { key: 'departmentName', label: 'Department' },
          { key: 'locationName', label: 'Location' },
          { key: 'employmentType', label: 'Type', render: j => <Badge>{j.employmentType}</Badge> },
          { key: 'openings', label: 'Openings', align: 'right' },
          { key: 'applicationCount', label: 'Applicants', align: 'right', render: j => j.applicationCount ?? 0 },
          { key: 'status', label: 'Status', render: j => <Badge>{j.status}</Badge> },
          { key: 'closingDate', label: 'Closes' },
          { key: 'id', label: '', align: 'right', sortable: false, render: j => {
            const s = String(j.status)
            return (
              <div className="flex justify-end gap-1" onClick={e => e.stopPropagation()}>
                {s === 'DRAFT' && <Button size="sm" variant="outline" className="h-7" onClick={() => act.mutate({ id: j.id, action: 'submit' })}>Submit</Button>}
                {(s === 'PENDING_APPROVAL' || s === 'SUBMITTED') && <Button size="sm" className="h-7" onClick={() => act.mutate({ id: j.id, action: 'approve' })}>Approve</Button>}
                {s !== 'CANCELLED' && s !== 'CLOSED' && <Button size="sm" variant="ghost" className="h-7 text-red-500" onClick={() => act.mutate({ id: j.id, action: 'cancel' })}>Cancel</Button>}
              </div>
            )
          } },
        ]}
      />
      <FormDialog
        open={creating}
        onOpenChange={setCreating}
        title="Create job opening"
        onSubmit={v => create.mutateAsync(v)}
        fields={[
          { name: 'title', label: 'Title', type: 'text', required: true, span: 2 },
          { name: 'employmentType', label: 'Employment type', type: 'select', required: true, options: [
            { value: 'FULL_TIME', label: 'Full time' }, { value: 'PART_TIME', label: 'Part time' },
            { value: 'CONTRACT', label: 'Contract' }, { value: 'INTERN', label: 'Internship' },
          ] },
          { name: 'openings', label: 'Number of openings', type: 'number', required: true, defaultValue: 1 },
          { name: 'experienceMin', label: 'Min experience (years)', type: 'number' },
          { name: 'experienceMax', label: 'Max experience (years)', type: 'number' },
          { name: 'salaryMin', label: 'Min salary', type: 'currency' },
          { name: 'salaryMax', label: 'Max salary', type: 'currency' },
          { name: 'closingDate', label: 'Closing date', type: 'date' },
          { name: 'description', label: 'Description', type: 'textarea', span: 2 },
          { name: 'requirements', label: 'Requirements', type: 'textarea', span: 2 },
        ]}
      />
    </>
  )
}
