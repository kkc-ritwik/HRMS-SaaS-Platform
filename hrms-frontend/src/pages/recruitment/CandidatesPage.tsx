import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Plus, UserPlus } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Avatar } from '@/components/ui/avatar'
import { Badge } from '@/components/ui/badge'
import { DataList } from '@/components/ui/data-list'
import { FormDialog } from '@/components/ui/form-dialog'
import { recruitmentService, type Candidate } from '@/services/recruitmentService'

export function CandidatesPage() {
  const qc = useQueryClient()
  const [creating, setCreating] = useState(false)
  const { data, isLoading } = useQuery({ queryKey: ['candidates'], queryFn: () => recruitmentService.listCandidates() })
  const candidates: Candidate[] = (data as { data?: Candidate[]; content?: Candidate[] } | undefined)?.data
    || (data as { content?: Candidate[] } | undefined)?.content
    || (Array.isArray(data) ? data : [])

  const create = useMutation({
    mutationFn: (v: Record<string, unknown>) => recruitmentService.createCandidate(v as Partial<Candidate>),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['candidates'] }),
  })

  return (
    <>
      <DataList<Candidate>
        title="Candidates"
        description="Talent pool across requisitions"
        action={<Button onClick={() => setCreating(true)}><Plus className="h-4 w-4 mr-1" /> Add Candidate</Button>}
        data={candidates}
        isLoading={isLoading}
        emptyIcon={<UserPlus className="h-10 w-10" />}
        emptyTitle="No candidates yet"
        emptyDescription="Add a candidate to start tracking"
        filters={{ status: ['NEW', 'SCREENING', 'INTERVIEW', 'OFFER', 'HIRED', 'REJECTED'] }}
        columns={[
          { key: 'fullName', label: 'Candidate', render: c => <div className="flex items-center gap-2"><Avatar name={c.fullName} size="sm" /> {c.fullName}</div> },
          { key: 'email', label: 'Email' },
          { key: 'phone', label: 'Phone' },
          { key: 'experience', label: 'Exp (yrs)', align: 'right' },
          { key: 'expectedSalary', label: 'Expected', align: 'right', render: c => c.expectedSalary ? `₹${c.expectedSalary.toLocaleString()}` : '—' },
          { key: 'status', label: 'Status', render: c => <Badge>{c.status}</Badge> },
          { key: 'source', label: 'Source' },
        ]}
      />
      <FormDialog
        open={creating}
        onOpenChange={setCreating}
        title="Add candidate"
        onSubmit={v => create.mutateAsync(v)}
        fields={[
          { name: 'firstName', label: 'First name', type: 'text', required: true },
          { name: 'lastName', label: 'Last name', type: 'text', required: true },
          { name: 'email', label: 'Email', type: 'email', required: true },
          { name: 'phone', label: 'Phone', type: 'tel' },
          { name: 'currentCompany', label: 'Current company', type: 'text' },
          { name: 'experience', label: 'Experience (years)', type: 'number' },
          { name: 'currentSalary', label: 'Current CTC', type: 'currency' },
          { name: 'expectedSalary', label: 'Expected CTC', type: 'currency' },
          { name: 'source', label: 'Source', type: 'select', options: [
            { value: 'CAREER_SITE', label: 'Career site' }, { value: 'LINKEDIN', label: 'LinkedIn' },
            { value: 'REFERRAL', label: 'Referral' }, { value: 'AGENCY', label: 'Agency' },
            { value: 'JOB_BOARD', label: 'Job board' }, { value: 'OTHER', label: 'Other' },
          ] },
        ]}
      />
    </>
  )
}
