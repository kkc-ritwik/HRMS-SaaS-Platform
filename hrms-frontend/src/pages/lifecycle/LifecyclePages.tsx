/** Lifecycle pages — Probation, Buddy, PreBoard, Templates, ExitInterviews, Checklists, KT. */
import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Plus, UserCheck, UserPlus, ClipboardList, FileCheck, Users, BookOpenCheck } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { DataList } from '@/components/ui/data-list'
import { FormDialog } from '@/components/ui/form-dialog'
import {
  probationReviewService, buddyAssignmentService, preOnboardingService,
  onboardingTemplateService, onboardingTaskAdminService, exitInterviewService,
  exitChecklistService, knowledgeTransferService,
} from '@/services/adminServices'

function makeRows<T>(d: unknown): T[] {
  return (d as { content?: T[] } | undefined)?.content || (Array.isArray(d) ? d as T[] : [])
}

interface Probation { id: string; employeeId: string; startDate: string; reviewDate?: string; status: string; decision?: string }
export function ProbationReviewsPage() {
  const qc = useQueryClient()
  const { data, isLoading } = useQuery({ queryKey: ['probation'], queryFn: probationReviewService.list })
  const decide = useMutation({
    mutationFn: ({ id, decision }: { id: string; decision: 'CONFIRM' | 'EXTEND' | 'TERMINATE' }) =>
      probationReviewService.decide(id, decision),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['probation'] }),
  })
  return (
    <DataList<Probation>
      title="Probation Reviews" data={makeRows<Probation>(data)} isLoading={isLoading}
      emptyIcon={<UserCheck className="h-10 w-10" />} emptyTitle="No probation reviews"
      columns={[
        { key: 'employeeId', label: 'Employee' }, { key: 'startDate', label: 'Started' },
        { key: 'reviewDate', label: 'Review date' },
        { key: 'status', label: 'Status', render: p => <Badge>{p.status}</Badge> },
        { key: 'decision', label: 'Decision', render: p => p.decision ? <Badge>{p.decision}</Badge> : '—' },
        { key: 'id', label: '', render: p => (
          <div className="flex gap-1">
            <Button size="sm" onClick={() => decide.mutate({ id: p.id, decision: 'CONFIRM' })}>Confirm</Button>
            <Button size="sm" variant="outline" onClick={() => decide.mutate({ id: p.id, decision: 'EXTEND' })}>Extend</Button>
          </div>
        ) },
      ]}
    />
  )
}

interface Buddy { id: string; newHireName?: string; buddyName?: string; startDate: string; status: string }
export function BuddyAssignmentsPage() {
  const qc = useQueryClient()
  const [creating, setCreating] = useState(false)
  const { data, isLoading } = useQuery({ queryKey: ['buddies'], queryFn: buddyAssignmentService.list })
  const create = useMutation({ mutationFn: buddyAssignmentService.assign, onSuccess: () => qc.invalidateQueries({ queryKey: ['buddies'] }) })
  return (
    <>
      <DataList<Buddy>
        title="Buddy Assignments" description="Onboarding buddies paired with new hires"
        action={<Button onClick={() => setCreating(true)}><Plus className="h-4 w-4 mr-1" /> Assign Buddy</Button>}
        data={makeRows<Buddy>(data)} isLoading={isLoading}
        emptyIcon={<UserPlus className="h-10 w-10" />} emptyTitle="No buddy assignments"
        columns={[
          { key: 'newHireName', label: 'New hire' }, { key: 'buddyName', label: 'Buddy' },
          { key: 'startDate', label: 'Start' },
          { key: 'status', label: 'Status', render: b => <Badge>{b.status}</Badge> },
        ]}
      />
      <FormDialog open={creating} onOpenChange={setCreating} title="Assign buddy"
        onSubmit={v => create.mutateAsync(v)}
        fields={[
          { name: 'newHireId', label: 'New hire employee ID', type: 'text', required: true },
          { name: 'buddyId', label: 'Buddy employee ID', type: 'text', required: true },
          { name: 'startDate', label: 'Start date', type: 'date', required: true },
        ]}
      />
    </>
  )
}

interface PreOnboard { id: string; candidateName?: string; offerAcceptedOn?: string; joiningDate: string; status: string }
export function PreOnboardingPage() {
  const qc = useQueryClient()
  const [creating, setCreating] = useState(false)
  const { data, isLoading } = useQuery({ queryKey: ['preboard'], queryFn: preOnboardingService.list })
  const create = useMutation({ mutationFn: preOnboardingService.invite, onSuccess: () => qc.invalidateQueries({ queryKey: ['preboard'] }) })
  return (
    <>
      <DataList<PreOnboard>
        title="Pre-Onboarding" description="Future hires going through pre-joining tasks"
        action={<Button onClick={() => setCreating(true)}><Plus className="h-4 w-4 mr-1" /> Invite</Button>}
        data={makeRows<PreOnboard>(data)} isLoading={isLoading}
        emptyIcon={<ClipboardList className="h-10 w-10" />} emptyTitle="No pre-onboarding records"
        columns={[
          { key: 'candidateName', label: 'Candidate' },
          { key: 'offerAcceptedOn', label: 'Offer accepted' },
          { key: 'joiningDate', label: 'Joining' },
          { key: 'status', label: 'Status', render: p => <Badge>{p.status}</Badge> },
        ]}
      />
      <FormDialog open={creating} onOpenChange={setCreating} title="Invite to pre-onboarding"
        onSubmit={v => create.mutateAsync(v)}
        fields={[
          { name: 'candidateId', label: 'Candidate ID', type: 'text', required: true },
          { name: 'joiningDate', label: 'Joining date', type: 'date', required: true },
          { name: 'designation', label: 'Designation', type: 'text' },
          { name: 'inviteEmail', label: 'Send invite email', type: 'switch', defaultValue: true },
        ]}
      />
    </>
  )
}

interface Template { id: string; name: string; type?: string; tasksCount?: number; active: boolean }
export function OnboardingTemplatesPage() {
  const qc = useQueryClient()
  const [creating, setCreating] = useState(false)
  const { data, isLoading } = useQuery({ queryKey: ['onb-templates'], queryFn: onboardingTemplateService.list })
  const create = useMutation({ mutationFn: onboardingTemplateService.create, onSuccess: () => qc.invalidateQueries({ queryKey: ['onb-templates'] }) })
  return (
    <>
      <DataList<Template>
        title="Onboarding Templates" description="Reusable task lists per role / department"
        action={<Button onClick={() => setCreating(true)}><Plus className="h-4 w-4 mr-1" /> New Template</Button>}
        data={makeRows<Template>(data)} isLoading={isLoading}
        emptyIcon={<FileCheck className="h-10 w-10" />} emptyTitle="No templates"
        columns={[
          { key: 'name', label: 'Template' }, { key: 'type', label: 'Type' },
          { key: 'tasksCount', label: 'Tasks' },
          { key: 'active', label: 'Active', render: t => <Badge>{t.active ? 'Active' : 'Draft'}</Badge> },
        ]}
      />
      <FormDialog open={creating} onOpenChange={setCreating} title="Create template"
        onSubmit={v => create.mutateAsync(v)}
        fields={[
          { name: 'name', label: 'Name', type: 'text', required: true, span: 2 },
          { name: 'type', label: 'Type', type: 'select', options: [
            { value: 'NEW_HIRE', label: 'New hire' }, { value: 'INTERNAL_MOVE', label: 'Internal move' },
            { value: 'CONTRACTOR', label: 'Contractor' },
          ] },
          { name: 'durationDays', label: 'Duration (days)', type: 'number', defaultValue: 30 },
        ]}
      />
    </>
  )
}

interface OnbTask { id: string; title: string; category?: string; assigneeRole?: string; dueOffsetDays?: number; templateName?: string }
export function OnboardingTasksAdminPage() {
  const { data, isLoading } = useQuery({ queryKey: ['onb-tasks-admin'], queryFn: onboardingTaskAdminService.list })
  return (
    <DataList<OnbTask>
      title="Onboarding Tasks Library" data={makeRows<OnbTask>(data)} isLoading={isLoading}
      emptyIcon={<ClipboardList className="h-10 w-10" />} emptyTitle="No tasks defined"
      columns={[
        { key: 'title', label: 'Task' }, { key: 'category', label: 'Category' },
        { key: 'assigneeRole', label: 'Assignee role' },
        { key: 'dueOffsetDays', label: 'Due (days after join)' },
        { key: 'templateName', label: 'Template' },
      ]}
    />
  )
}

interface ExitInterview { id: string; employeeName?: string; lastWorkingDay?: string; status: string; satisfactionRating?: number }
export function ExitInterviewsPage() {
  const qc = useQueryClient()
  const [creating, setCreating] = useState(false)
  const { data, isLoading } = useQuery({ queryKey: ['exit-int'], queryFn: exitInterviewService.list })
  const create = useMutation({ mutationFn: exitInterviewService.schedule, onSuccess: () => qc.invalidateQueries({ queryKey: ['exit-int'] }) })
  return (
    <>
      <DataList<ExitInterview>
        title="Exit Interviews" data={makeRows<ExitInterview>(data)} isLoading={isLoading}
        action={<Button onClick={() => setCreating(true)}><Plus className="h-4 w-4 mr-1" /> Schedule</Button>}
        emptyIcon={<Users className="h-10 w-10" />} emptyTitle="No exit interviews"
        columns={[
          { key: 'employeeName', label: 'Employee' }, { key: 'lastWorkingDay', label: 'LWD' },
          { key: 'satisfactionRating', label: 'Rating', render: e => e.satisfactionRating ? `${e.satisfactionRating}/5` : '—' },
          { key: 'status', label: 'Status', render: e => <Badge>{e.status}</Badge> },
        ]}
      />
      <FormDialog open={creating} onOpenChange={setCreating} title="Schedule exit interview"
        onSubmit={v => create.mutateAsync(v)}
        fields={[
          { name: 'separationId', label: 'Separation ID', type: 'text', required: true },
          { name: 'scheduledAt', label: 'Scheduled date & time', type: 'datetime-local', required: true },
        ]}
      />
    </>
  )
}

interface ExitChecklist { id: string; separationId: string; department: string; status: string; clearedBy?: string }
export function ExitChecklistsPage() {
  const { data, isLoading } = useQuery({ queryKey: ['exit-checklists'], queryFn: exitChecklistService.list })
  return (
    <DataList<ExitChecklist>
      title="Exit Clearance Checklists" data={makeRows<ExitChecklist>(data)} isLoading={isLoading}
      emptyIcon={<FileCheck className="h-10 w-10" />} emptyTitle="No clearance items"
      columns={[
        { key: 'separationId', label: 'Separation' }, { key: 'department', label: 'Department' },
        { key: 'status', label: 'Status', render: c => <Badge>{c.status}</Badge> },
        { key: 'clearedBy', label: 'Cleared by' },
      ]}
    />
  )
}

interface KT { id: string; outgoingName?: string; incomingName?: string; topic: string; status: string; completedOn?: string }
export function KnowledgeTransferPage() {
  const { data, isLoading } = useQuery({ queryKey: ['kt'], queryFn: knowledgeTransferService.list })
  return (
    <DataList<KT>
      title="Knowledge Transfers"
      description="Pending and completed KTs as employees exit"
      data={makeRows<KT>(data)} isLoading={isLoading}
      emptyIcon={<BookOpenCheck className="h-10 w-10" />} emptyTitle="No KTs"
      columns={[
        { key: 'outgoingName', label: 'Outgoing' }, { key: 'incomingName', label: 'Incoming' },
        { key: 'topic', label: 'Topic' },
        { key: 'status', label: 'Status', render: k => <Badge>{k.status}</Badge> },
        { key: 'completedOn', label: 'Completed' },
      ]}
    />
  )
}
