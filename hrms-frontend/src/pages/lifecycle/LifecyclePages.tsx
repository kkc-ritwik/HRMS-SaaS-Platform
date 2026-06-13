/** Lifecycle pages — Probation, Buddy, PreBoard, Templates, ExitInterviews, Checklists, KT. */
import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Plus, UserCheck, UserPlus, ClipboardList, FileCheck, Users, BookOpenCheck } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { DataList } from '@/components/ui/data-list'
import { FormDialog } from '@/components/ui/form-dialog'
import { ResourcePage } from '@/components/ui/resource-page'
import {
  probationReviewService, buddyAssignmentService, preOnboardingService,
  onboardingTemplateService, onboardingTaskAdminService, exitInterviewService,
  exitChecklistService, knowledgeTransferService,
} from '@/services/adminServices'

function makeRows<T>(d: unknown): T[] {
  return (d as { content?: T[] } | undefined)?.content || (Array.isArray(d) ? d as T[] : [])
}

interface Probation extends Record<string, unknown> { id: string; employeeId: string; startDate: string; reviewDate?: string; status: string; decision?: string }
export function ProbationReviewsPage() {
  return (
    <ResourcePage<Probation>
      title="Probation Reviews"
      description="Confirm, extend or terminate probation for new hires"
      icon={<UserCheck className="h-10 w-10" />}
      queryKey={['probation']}
      fetcher={() => probationReviewService.list() as Promise<unknown>}
      filters={{ status: ['PENDING', 'CONFIRMED', 'EXTENDED', 'TERMINATED'] }}
      columns={[
        { key: 'employeeId', label: 'Employee' }, { key: 'startDate', label: 'Started' },
        { key: 'reviewDate', label: 'Review date' },
        { key: 'status', label: 'Status', render: p => <Badge>{String(p.status)}</Badge> },
        { key: 'decision', label: 'Decision', render: p => p.decision ? <Badge>{String(p.decision)}</Badge> : '—' },
      ]}
      formFields={[
        { name: 'employeeId', label: 'Employee ID', type: 'text', required: true },
        { name: 'startDate', label: 'Probation start', type: 'date', required: true },
        { name: 'reviewDate', label: 'Review date', type: 'date' },
      ]}
      onCreate={v => probationReviewService.create(v)}
      onDelete={id => probationReviewService.remove(id)}
      rowActions={p => [
        { label: 'Confirm', show: p.status === 'PENDING', run: () => probationReviewService.decide(p.id, 'CONFIRM') },
        { label: 'Extend', show: p.status === 'PENDING', run: () => probationReviewService.decide(p.id, 'EXTEND') },
        { label: 'Terminate', show: p.status === 'PENDING', destructive: true, confirm: 'Terminate probation?', run: () => probationReviewService.decide(p.id, 'TERMINATE') },
      ]}
    />
  )
}

interface Buddy extends Record<string, unknown> { id: string; newHireName?: string; buddyName?: string; startDate: string; status: string }
export function BuddyAssignmentsPage() {
  return (
    <ResourcePage<Buddy>
      title="Buddy Assignments"
      description="Onboarding buddies paired with new hires"
      icon={<UserPlus className="h-10 w-10" />}
      queryKey={['buddies']}
      fetcher={() => buddyAssignmentService.list() as Promise<unknown>}
      columns={[
        { key: 'newHireName', label: 'New hire' }, { key: 'buddyName', label: 'Buddy' },
        { key: 'startDate', label: 'Start' },
        { key: 'status', label: 'Status', render: b => <Badge>{String(b.status)}</Badge> },
      ]}
      formFields={[
        { name: 'employeeId', label: 'New hire employee ID', type: 'text', required: true },
        { name: 'buddyId', label: 'Buddy employee ID', type: 'text', required: true },
        { name: 'startDate', label: 'Start date', type: 'date', required: true },
      ]}
      createTitle="Assign buddy"
      onCreate={v => buddyAssignmentService.assign(v)}
      onUpdate={(id, v) => buddyAssignmentService.update(id, v)}
      onDelete={id => buddyAssignmentService.remove(id)}
    />
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
  return (
    <ResourcePage<Template & Record<string, unknown>>
      title="Onboarding Templates"
      description="Reusable task lists per role / department"
      icon={<FileCheck className="h-10 w-10" />}
      queryKey={['onb-templates']}
      fetcher={() => onboardingTemplateService.list() as Promise<unknown>}
      columns={[
        { key: 'name', label: 'Template' }, { key: 'type', label: 'Type' },
        { key: 'tasksCount', label: 'Tasks' },
        { key: 'active', label: 'Active', render: t => <Badge>{t.active ? 'Active' : 'Draft'}</Badge> },
      ]}
      formFields={[
        { name: 'name', label: 'Name', type: 'text', required: true, span: 2 },
        { name: 'type', label: 'Type', type: 'select', options: [
          { value: 'NEW_HIRE', label: 'New hire' }, { value: 'INTERNAL_MOVE', label: 'Internal move' }, { value: 'CONTRACTOR', label: 'Contractor' },
        ] },
        { name: 'durationDays', label: 'Duration (days)', type: 'number', defaultValue: 30 },
        { name: 'description', label: 'Description', type: 'textarea', span: 2 },
      ]}
      createTitle="Create template"
      onCreate={v => onboardingTemplateService.create(v)}
      onUpdate={(id, v) => onboardingTemplateService.update(id, v)}
      onDelete={id => onboardingTemplateService.remove(id)}
    />
  )
}

interface OnbTask extends Record<string, unknown> { id: string; title: string; category?: string; assigneeRole?: string; dueOffsetDays?: number; templateName?: string }
export function OnboardingTasksAdminPage() {
  return (
    <ResourcePage<OnbTask>
      title="Onboarding Tasks Library"
      description="Reusable onboarding task definitions"
      icon={<ClipboardList className="h-10 w-10" />}
      queryKey={['onb-tasks-admin']}
      fetcher={() => onboardingTaskAdminService.list() as Promise<unknown>}
      columns={[
        { key: 'title', label: 'Task' }, { key: 'category', label: 'Category' },
        { key: 'assigneeRole', label: 'Assignee role' },
        { key: 'dueOffsetDays', label: 'Due (days after join)' },
        { key: 'templateName', label: 'Template' },
      ]}
      formFields={[
        { name: 'title', label: 'Task title', type: 'text', required: true, span: 2 },
        { name: 'templateId', label: 'Template ID', type: 'text' },
        { name: 'category', label: 'Category', type: 'select', options: [
          { value: 'IT', label: 'IT' }, { value: 'HR', label: 'HR' }, { value: 'FACILITIES', label: 'Facilities' },
          { value: 'COMPLIANCE', label: 'Compliance' }, { value: 'TRAINING', label: 'Training' },
        ] },
        { name: 'assigneeRole', label: 'Assignee role', type: 'text' },
        { name: 'dueOffsetDays', label: 'Due (days after join)', type: 'number' },
      ]}
      onCreate={v => onboardingTaskAdminService.create(v)}
      onUpdate={(id, v) => onboardingTaskAdminService.update(id, v)}
      onDelete={id => onboardingTaskAdminService.remove(id)}
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
