/** Compact list pages for the remaining backend modules. */
import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import {
  Plus, FileSpreadsheet, CheckSquare, FileText, ListChecks, Receipt, MessageCircleHeart,
  Briefcase, ShieldCheck, BarChart3, Bookmark, Layers, Megaphone, Mail, Bell,
  DollarSign, Settings as Cog, FileSignature, FolderOpen, Workflow, GitBranch,
  PlugZap, Hash,
} from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Switch } from '@/components/ui/switch'
import { Label } from '@/components/ui/label'
import { Skeleton } from '@/components/ui/skeleton'
import { DataList } from '@/components/ui/data-list'
import { FormDialog } from '@/components/ui/form-dialog'
import { PageHeader } from '@/components/ui/page-header'
import {
  statutoryReturnService, complianceTaskService, courseModuleService, assessmentAdminService,
  expensePolicyService, performanceAnalyticsService, continuousFeedbackService,
  recruitmentAgencyService, recruitmentAnalyticsService, bgvService,
  groupService, eventService, dashboardAdminService, savedReportService,
  announcementAdminService, emailTemplateService, notificationPreferenceService,
  salaryComponentService, employeeSalaryService, taxConfigService,
  ticketCategoryService, documentTemplateAdminService, documentTypeService, fileVaultService,
  workflowInstanceAdminService, delegationRuleService, integrationsService, jobCostService,
} from '@/services/adminServices'
import { toast } from 'sonner'

function rows<T>(d: unknown): T[] {
  return (d as { content?: T[] } | undefined)?.content || (Array.isArray(d) ? d as T[] : [])
}

// ── Compliance ─────────────────────────────────────────────────────────
interface StatReturn { id: string; type: string; period: string; status: string; submittedAt?: string }
export function StatutoryReturnsPage() {
  const { data, isLoading } = useQuery({ queryKey: ['stat-returns'], queryFn: statutoryReturnService.list })
  return (
    <DataList<StatReturn>
      title="Statutory Returns" description="PF ECR, Form 24Q, ESI returns, PT, Form 16 batch"
      data={rows(data)} isLoading={isLoading}
      emptyIcon={<FileSpreadsheet className="h-10 w-10" />} emptyTitle="No returns generated"
      columns={[
        { key: 'type', label: 'Type', render: r => <Badge>{r.type}</Badge> },
        { key: 'period', label: 'Period' },
        { key: 'status', label: 'Status', render: r => <Badge>{r.status}</Badge> },
        { key: 'submittedAt', label: 'Submitted' },
      ]}
    />
  )
}

interface ComplianceTask { id: string; title: string; category?: string; dueDate?: string; assignee?: string; status: string }
export function ComplianceTasksPage() {
  const { data, isLoading } = useQuery({ queryKey: ['compliance-tasks'], queryFn: complianceTaskService.list })
  return (
    <DataList<ComplianceTask>
      title="Compliance Tasks" description="Statutory + audit tasks with deadlines"
      data={rows(data)} isLoading={isLoading}
      emptyIcon={<CheckSquare className="h-10 w-10" />} emptyTitle="No tasks"
      columns={[
        { key: 'title', label: 'Task' }, { key: 'category', label: 'Category' },
        { key: 'dueDate', label: 'Due' }, { key: 'assignee', label: 'Assignee' },
        { key: 'status', label: 'Status', render: t => <Badge>{t.status}</Badge> },
      ]}
    />
  )
}

// ── LMS ────────────────────────────────────────────────────────────────
interface Module { id: string; title: string; type: string; durationMinutes?: number; orderIndex?: number; required?: boolean }
export function CourseModulesPage() {
  const { data, isLoading } = useQuery({ queryKey: ['course-modules-admin'], queryFn: courseModuleService.list('') })
  return (
    <DataList<Module>
      title="Course Modules" description="Per-course lessons / videos / quizzes"
      data={rows(data)} isLoading={isLoading}
      emptyIcon={<Layers className="h-10 w-10" />} emptyTitle="No modules"
      columns={[
        { key: 'orderIndex', label: '#', align: 'right' }, { key: 'title', label: 'Title' },
        { key: 'type', label: 'Type', render: m => <Badge>{m.type}</Badge> },
        { key: 'durationMinutes', label: 'Duration (min)', align: 'right' },
        { key: 'required', label: 'Required', render: m => m.required ? '✓' : '—' },
      ]}
    />
  )
}

interface Assessment { id: string; title: string; courseTitle?: string; passingScore?: number; attempts?: number; type?: string }
export function AssessmentsPage() {
  const { data, isLoading } = useQuery({ queryKey: ['assessments'], queryFn: assessmentAdminService.list() })
  return (
    <DataList<Assessment>
      title="Assessments" description="Quizzes and tests across courses"
      data={rows(data)} isLoading={isLoading}
      emptyIcon={<FileText className="h-10 w-10" />} emptyTitle="No assessments"
      columns={[
        { key: 'title', label: 'Title' }, { key: 'courseTitle', label: 'Course' },
        { key: 'type', label: 'Type' }, { key: 'passingScore', label: 'Pass %' },
        { key: 'attempts', label: 'Attempts' },
      ]}
    />
  )
}

// ── Expense ────────────────────────────────────────────────────────────
interface ExpensePolicy { id: string; name: string; category?: string; perDiemLimit?: number; receiptRequired?: boolean }
export function ExpensePoliciesPage() {
  const qc = useQueryClient()
  const [creating, setCreating] = useState(false)
  const { data, isLoading } = useQuery({ queryKey: ['expense-policies'], queryFn: expensePolicyService.list })
  const create = useMutation({ mutationFn: expensePolicyService.create, onSuccess: () => qc.invalidateQueries({ queryKey: ['expense-policies'] }) })
  return (
    <>
      <DataList<ExpensePolicy>
        title="Expense Policies" description="Per-category limits + receipt requirements"
        action={<Button onClick={() => setCreating(true)}><Plus className="h-4 w-4 mr-1" /> New Policy</Button>}
        data={rows(data)} isLoading={isLoading}
        emptyIcon={<ListChecks className="h-10 w-10" />} emptyTitle="No policies"
        columns={[
          { key: 'name', label: 'Policy' }, { key: 'category', label: 'Category' },
          { key: 'perDiemLimit', label: 'Per diem', align: 'right' },
          { key: 'receiptRequired', label: 'Receipt required', render: p => p.receiptRequired ? '✓' : '—' },
        ]}
      />
      <FormDialog open={creating} onOpenChange={setCreating} title="Create expense policy"
        onSubmit={v => create.mutateAsync(v)}
        fields={[
          { name: 'name', label: 'Name', type: 'text', required: true, span: 2 },
          { name: 'category', label: 'Category', type: 'text', required: true },
          { name: 'perDiemLimit', label: 'Per diem (₹)', type: 'currency' },
          { name: 'maxAmount', label: 'Max amount (₹)', type: 'currency' },
          { name: 'receiptRequired', label: 'Receipt required', type: 'switch' },
          { name: 'description', label: 'Description', type: 'textarea', span: 2 },
        ]}
      />
    </>
  )
}

export function ReceiptOcrPage() {
  const [parsed, setParsed] = useState<Record<string, unknown> | null>(null)
  const [loading, setLoading] = useState(false)
  const onChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const f = e.target.files?.[0]; if (!f) return
    setLoading(true)
    try {
      const { receiptOcrService } = await import('@/services/adminServices')
      const r = await receiptOcrService.parse(f); setParsed(r); toast.success('Parsed')
    } catch (err) { toast.error(err instanceof Error ? err.message : 'Parse failed') }
    finally { setLoading(false) }
  }
  return (
    <div className="space-y-6">
      <PageHeader title="Receipt OCR" description="Drop a receipt image — vendor, amount, tax extracted via Tika" />
      <Card>
        <CardContent className="p-6 space-y-4">
          <Label>Upload receipt</Label>
          <input type="file" accept="image/*,application/pdf" onChange={onChange} />
          {loading && <Skeleton className="h-32" />}
          {parsed && (
            <pre className="bg-slate-50 p-4 rounded-lg text-xs overflow-auto">{JSON.stringify(parsed, null, 2)}</pre>
          )}
        </CardContent>
      </Card>
    </div>
  )
}

// ── Performance ────────────────────────────────────────────────────────
export function PerformanceAnalyticsPage() {
  const { data, isLoading } = useQuery({ queryKey: ['perf-analytics'], queryFn: performanceAnalyticsService.distribution })
  return (
    <div className="space-y-6">
      <PageHeader title="Performance Analytics" description="Rating distribution, calibration matrix, trends" />
      <Card>
        <CardContent className="p-6">
          {isLoading ? <Skeleton className="h-48" /> : (
            <pre className="text-xs">{JSON.stringify(data, null, 2)}</pre>
          )}
        </CardContent>
      </Card>
    </div>
  )
}

interface FeedbackItem { id: string; from?: string; to?: string; message: string; type?: string; createdAt: string }
export function ContinuousFeedbackPage() {
  const qc = useQueryClient()
  const [giving, setGiving] = useState(false)
  const { data, isLoading } = useQuery({ queryKey: ['feedback'], queryFn: continuousFeedbackService.feed })
  const give = useMutation({ mutationFn: continuousFeedbackService.give, onSuccess: () => qc.invalidateQueries({ queryKey: ['feedback'] }) })
  return (
    <>
      <DataList<FeedbackItem>
        title="Continuous Feedback" description="Real-time praise + constructive feedback"
        action={<Button onClick={() => setGiving(true)}><Plus className="h-4 w-4 mr-1" /> Give Feedback</Button>}
        data={rows(data)} isLoading={isLoading}
        emptyIcon={<MessageCircleHeart className="h-10 w-10" />} emptyTitle="No feedback yet"
        columns={[
          { key: 'from', label: 'From' }, { key: 'to', label: 'To' },
          { key: 'type', label: 'Type', render: f => f.type ? <Badge>{f.type}</Badge> : '—' },
          { key: 'message', label: 'Message' }, { key: 'createdAt', label: 'When' },
        ]}
      />
      <FormDialog open={giving} onOpenChange={setGiving} title="Give feedback"
        onSubmit={v => give.mutateAsync(v)}
        fields={[
          { name: 'toEmployeeId', label: 'Recipient (employee ID)', type: 'text', required: true },
          { name: 'type', label: 'Type', type: 'select', required: true, options: [
            { value: 'APPRECIATION', label: 'Appreciation' }, { value: 'CONSTRUCTIVE', label: 'Constructive' },
            { value: 'PEER', label: 'Peer feedback' }, { value: 'UPWARD', label: 'Upward feedback' },
          ] },
          { name: 'message', label: 'Message', type: 'textarea', required: true, span: 2 },
        ]}
      />
    </>
  )
}

// ── Recruitment ────────────────────────────────────────────────────────
interface Agency { id: string; name: string; contactEmail?: string; commissionPercent?: number; active: boolean }
export function RecruitmentAgenciesPage() {
  const qc = useQueryClient()
  const [creating, setCreating] = useState(false)
  const { data, isLoading } = useQuery({ queryKey: ['agencies'], queryFn: recruitmentAgencyService.list })
  const create = useMutation({ mutationFn: recruitmentAgencyService.create, onSuccess: () => qc.invalidateQueries({ queryKey: ['agencies'] }) })
  return (
    <>
      <DataList<Agency>
        title="Recruitment Agencies" description="External hiring partners"
        action={<Button onClick={() => setCreating(true)}><Plus className="h-4 w-4 mr-1" /> Add Agency</Button>}
        data={rows(data)} isLoading={isLoading}
        emptyIcon={<Briefcase className="h-10 w-10" />} emptyTitle="No agencies"
        columns={[
          { key: 'name', label: 'Name' }, { key: 'contactEmail', label: 'Contact' },
          { key: 'commissionPercent', label: 'Commission %', align: 'right' },
          { key: 'active', label: 'Active', render: a => <Badge>{a.active ? 'Active' : 'Inactive'}</Badge> },
        ]}
      />
      <FormDialog open={creating} onOpenChange={setCreating} title="Onboard recruitment agency"
        onSubmit={v => create.mutateAsync(v)}
        fields={[
          { name: 'name', label: 'Agency name', type: 'text', required: true, span: 2 },
          { name: 'contactName', label: 'Contact name', type: 'text' },
          { name: 'contactEmail', label: 'Contact email', type: 'email' },
          { name: 'commissionPercent', label: 'Commission %', type: 'number' },
          { name: 'paymentTermsDays', label: 'Payment terms (days)', type: 'number' },
        ]}
      />
    </>
  )
}

export function RecruitmentAnalyticsPage() {
  const funnel = useQuery({ queryKey: ['recr-funnel'], queryFn: recruitmentAnalyticsService.funnel })
  const sources = useQuery({ queryKey: ['recr-sources'], queryFn: recruitmentAnalyticsService.sourceEffectiveness })
  const tth = useQuery({ queryKey: ['recr-tth'], queryFn: recruitmentAnalyticsService.timeToHire })
  return (
    <div className="space-y-6">
      <PageHeader title="Recruitment Analytics" description="Funnel, source effectiveness, time to hire" />
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <Card><CardHeader><CardTitle>Funnel</CardTitle></CardHeader>
          <CardContent>{funnel.isLoading ? <Skeleton className="h-32" /> : <pre className="text-xs">{JSON.stringify(funnel.data, null, 2)}</pre>}</CardContent>
        </Card>
        <Card><CardHeader><CardTitle>Source effectiveness</CardTitle></CardHeader>
          <CardContent>{sources.isLoading ? <Skeleton className="h-32" /> : <pre className="text-xs">{JSON.stringify(sources.data, null, 2)}</pre>}</CardContent>
        </Card>
        <Card><CardHeader><CardTitle>Time to hire</CardTitle></CardHeader>
          <CardContent>{tth.isLoading ? <Skeleton className="h-32" /> : <pre className="text-xs">{JSON.stringify(tth.data, null, 2)}</pre>}</CardContent>
        </Card>
      </div>
    </div>
  )
}

interface BgvCase { id: string; candidateName?: string; vendor?: string; status: string; verdict?: string; initiatedAt: string }
export function BgvPage() {
  const qc = useQueryClient()
  const [creating, setCreating] = useState(false)
  const { data, isLoading } = useQuery({ queryKey: ['bgv'], queryFn: bgvService.list })
  const create = useMutation({ mutationFn: bgvService.initiate, onSuccess: () => qc.invalidateQueries({ queryKey: ['bgv'] }) })
  return (
    <>
      <DataList<BgvCase>
        title="Background Verification" description="BGV cases sent to verifier vendors"
        action={<Button onClick={() => setCreating(true)}><Plus className="h-4 w-4 mr-1" /> Initiate</Button>}
        data={rows(data)} isLoading={isLoading}
        emptyIcon={<ShieldCheck className="h-10 w-10" />} emptyTitle="No BGV cases"
        columns={[
          { key: 'candidateName', label: 'Candidate' }, { key: 'vendor', label: 'Vendor' },
          { key: 'initiatedAt', label: 'Started' },
          { key: 'status', label: 'Status', render: b => <Badge>{b.status}</Badge> },
          { key: 'verdict', label: 'Verdict', render: b => b.verdict ? <Badge>{b.verdict}</Badge> : '—' },
        ]}
      />
      <FormDialog open={creating} onOpenChange={setCreating} title="Initiate BGV"
        onSubmit={v => create.mutateAsync(v)}
        fields={[
          { name: 'candidateId', label: 'Candidate ID', type: 'text', required: true },
          { name: 'vendor', label: 'Vendor', type: 'select', required: true, options: [
            { value: 'AUTHBRIDGE', label: 'AuthBridge' }, { value: 'FIRST_ADVANTAGE', label: 'First Advantage' },
            { value: 'IDFY', label: 'IDfy' }, { value: 'INTERNAL', label: 'Internal' },
          ] },
          { name: 'scope', label: 'Scope', type: 'text', placeholder: 'EDUCATION, EMPLOYMENT, ADDRESS, CRIMINAL' },
        ]}
      />
    </>
  )
}

// ── Social ─────────────────────────────────────────────────────────────
interface Group { id: string; name: string; description?: string; memberCount?: number; privacy: string }
export function GroupsPage() {
  const qc = useQueryClient()
  const [creating, setCreating] = useState(false)
  const { data, isLoading } = useQuery({ queryKey: ['groups'], queryFn: groupService.list })
  const create = useMutation({ mutationFn: groupService.create, onSuccess: () => qc.invalidateQueries({ queryKey: ['groups'] }) })
  return (
    <>
      <DataList<Group>
        title="Groups" description="Interest groups + ERG (Employee Resource Groups)"
        action={<Button onClick={() => setCreating(true)}><Plus className="h-4 w-4 mr-1" /> New Group</Button>}
        data={rows(data)} isLoading={isLoading}
        emptyIcon={<Hash className="h-10 w-10" />} emptyTitle="No groups"
        columns={[
          { key: 'name', label: 'Group' }, { key: 'description', label: 'About' },
          { key: 'privacy', label: 'Privacy', render: g => <Badge>{g.privacy}</Badge> },
          { key: 'memberCount', label: 'Members', align: 'right' },
        ]}
      />
      <FormDialog open={creating} onOpenChange={setCreating} title="Create group"
        onSubmit={v => create.mutateAsync(v)}
        fields={[
          { name: 'name', label: 'Group name', type: 'text', required: true, span: 2 },
          { name: 'description', label: 'Description', type: 'textarea', span: 2 },
          { name: 'privacy', label: 'Privacy', type: 'select', required: true, options: [
            { value: 'PUBLIC', label: 'Public' }, { value: 'PRIVATE', label: 'Private' },
          ] },
        ]}
      />
    </>
  )
}

interface EventItem { id: string; title: string; startsAt: string; endsAt: string; location?: string; attendeesCount?: number }
export function EventsPage() {
  const qc = useQueryClient()
  const [creating, setCreating] = useState(false)
  const { data, isLoading } = useQuery({ queryKey: ['events'], queryFn: eventService.list })
  const create = useMutation({ mutationFn: eventService.create, onSuccess: () => qc.invalidateQueries({ queryKey: ['events'] }) })
  return (
    <>
      <DataList<EventItem>
        title="Events" description="Town halls, meetups, training events"
        action={<Button onClick={() => setCreating(true)}><Plus className="h-4 w-4 mr-1" /> New Event</Button>}
        data={rows(data)} isLoading={isLoading}
        emptyIcon={<Bookmark className="h-10 w-10" />} emptyTitle="No events"
        columns={[
          { key: 'title', label: 'Event' }, { key: 'startsAt', label: 'Starts' },
          { key: 'endsAt', label: 'Ends' }, { key: 'location', label: 'Location' },
          { key: 'attendeesCount', label: 'RSVPs', align: 'right' },
        ]}
      />
      <FormDialog open={creating} onOpenChange={setCreating} title="Create event"
        onSubmit={v => create.mutateAsync(v)}
        fields={[
          { name: 'title', label: 'Title', type: 'text', required: true, span: 2 },
          { name: 'startsAt', label: 'Starts at', type: 'datetime-local', required: true },
          { name: 'endsAt', label: 'Ends at', type: 'datetime-local', required: true },
          { name: 'location', label: 'Location', type: 'text' },
          { name: 'meetingLink', label: 'Meeting link', type: 'url' },
          { name: 'description', label: 'Description', type: 'textarea', span: 2 },
        ]}
      />
    </>
  )
}

// ── Reports ────────────────────────────────────────────────────────────
interface Dash { id: string; name: string; description?: string; ownerName?: string; widgetCount?: number }
export function DashboardsPage() {
  const { data, isLoading } = useQuery({ queryKey: ['dashboards'], queryFn: dashboardAdminService.list })
  return (
    <DataList<Dash>
      title="Dashboards" description="Custom dashboards built from saved reports"
      data={rows(data)} isLoading={isLoading}
      emptyIcon={<BarChart3 className="h-10 w-10" />} emptyTitle="No dashboards"
      columns={[
        { key: 'name', label: 'Dashboard' }, { key: 'description', label: 'About' },
        { key: 'ownerName', label: 'Owner' }, { key: 'widgetCount', label: 'Widgets', align: 'right' },
      ]}
    />
  )
}

interface Saved { id: string; name: string; ownerName?: string; sharedWith?: number; lastRunAt?: string }
export function SavedReportsPage() {
  const { data, isLoading } = useQuery({ queryKey: ['saved-reports'], queryFn: savedReportService.list })
  return (
    <DataList<Saved>
      title="Saved Reports" description="Bookmarked report queries (yours + shared)"
      data={rows(data)} isLoading={isLoading}
      emptyIcon={<Bookmark className="h-10 w-10" />} emptyTitle="No saved reports"
      columns={[
        { key: 'name', label: 'Name' }, { key: 'ownerName', label: 'Owner' },
        { key: 'sharedWith', label: 'Shared with', align: 'right' },
        { key: 'lastRunAt', label: 'Last run' },
      ]}
    />
  )
}

// ── Notifications ──────────────────────────────────────────────────────
interface Ann { id: string; title: string; audience: string; publishedAt?: string; expiresAt?: string }
export function AnnouncementsAdminPage() {
  const qc = useQueryClient()
  const [creating, setCreating] = useState(false)
  const { data, isLoading } = useQuery({ queryKey: ['announcements'], queryFn: announcementAdminService.list })
  const create = useMutation({ mutationFn: announcementAdminService.publish, onSuccess: () => qc.invalidateQueries({ queryKey: ['announcements'] }) })
  return (
    <>
      <DataList<Ann>
        title="Announcements" description="Company-wide and targeted announcements"
        action={<Button onClick={() => setCreating(true)}><Plus className="h-4 w-4 mr-1" /> New</Button>}
        data={rows(data)} isLoading={isLoading}
        emptyIcon={<Megaphone className="h-10 w-10" />} emptyTitle="No announcements"
        columns={[
          { key: 'title', label: 'Title' },
          { key: 'audience', label: 'Audience', render: a => <Badge>{a.audience}</Badge> },
          { key: 'publishedAt', label: 'Published' }, { key: 'expiresAt', label: 'Expires' },
        ]}
      />
      <FormDialog open={creating} onOpenChange={setCreating} title="Publish announcement"
        onSubmit={v => create.mutateAsync(v)}
        fields={[
          { name: 'title', label: 'Title', type: 'text', required: true, span: 2 },
          { name: 'body', label: 'Body', type: 'textarea', required: true, span: 2 },
          { name: 'audience', label: 'Audience', type: 'select', required: true, options: [
            { value: 'ALL', label: 'All employees' }, { value: 'DEPARTMENT', label: 'Department' },
            { value: 'LOCATION', label: 'Location' }, { value: 'ROLE', label: 'Role' },
          ] },
          { name: 'expiresAt', label: 'Expires at', type: 'datetime-local' },
        ]}
      />
    </>
  )
}

interface EmailTpl { id: string; key: string; subject: string; channel: string; locale?: string; active: boolean }
export function EmailTemplatesPage() {
  const { data, isLoading } = useQuery({ queryKey: ['email-templates'], queryFn: emailTemplateService.list })
  return (
    <DataList<EmailTpl>
      title="Email Templates" description="System-generated email copy library"
      data={rows(data)} isLoading={isLoading}
      emptyIcon={<Mail className="h-10 w-10" />} emptyTitle="No templates"
      columns={[
        { key: 'key', label: 'Key' }, { key: 'subject', label: 'Subject' },
        { key: 'channel', label: 'Channel', render: t => <Badge>{t.channel}</Badge> },
        { key: 'locale', label: 'Locale' },
        { key: 'active', label: 'Active', render: t => <Badge>{t.active ? 'Active' : 'Inactive'}</Badge> },
      ]}
    />
  )
}

export function NotificationPreferencesPage() {
  const qc = useQueryClient()
  const { data, isLoading } = useQuery({ queryKey: ['notif-prefs'], queryFn: notificationPreferenceService.mine })
  const upd = useMutation({ mutationFn: notificationPreferenceService.update, onSuccess: () => qc.invalidateQueries({ queryKey: ['notif-prefs'] }) })
  const prefs = (data as Record<string, boolean> | undefined) || {}
  if (isLoading) return <Skeleton className="h-64" />
  return (
    <div className="space-y-6 max-w-2xl">
      <PageHeader title="Notification Preferences" description="Choose what you want to be notified about" />
      <Card>
        <CardContent className="p-6 space-y-4">
          {['emailDigest', 'leaveApprovals', 'payslipPublished', 'announcements', 'birthdaysAndAnniversaries',
            'helpdeskUpdates', 'goalReviewReminders', 'pulsePrompts', 'kudos', 'workflowAssignments'].map(key => (
            <div key={key} className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium">{key.replace(/([A-Z])/g, ' $1').replace(/^./, c => c.toUpperCase())}</p>
              </div>
              <Switch
                checked={!!prefs[key]}
                onCheckedChange={v => upd.mutate({ ...prefs, [key]: v })}
              />
            </div>
          ))}
        </CardContent>
      </Card>
    </div>
  )
}

// ── Payroll depth ──────────────────────────────────────────────────────
interface SalComp { id: string; code: string; name: string; type: string; calculationType: string; taxable: boolean }
export function SalaryComponentsPage() {
  const qc = useQueryClient()
  const [creating, setCreating] = useState(false)
  const { data, isLoading } = useQuery({ queryKey: ['salary-components'], queryFn: salaryComponentService.list })
  const create = useMutation({ mutationFn: salaryComponentService.create, onSuccess: () => qc.invalidateQueries({ queryKey: ['salary-components'] }) })
  return (
    <>
      <DataList<SalComp>
        title="Salary Components" description="Earnings, deductions, statutory components"
        action={<Button onClick={() => setCreating(true)}><Plus className="h-4 w-4 mr-1" /> New Component</Button>}
        data={rows(data)} isLoading={isLoading}
        emptyIcon={<DollarSign className="h-10 w-10" />} emptyTitle="No components"
        columns={[
          { key: 'code', label: 'Code' }, { key: 'name', label: 'Name' },
          { key: 'type', label: 'Type', render: c => <Badge>{c.type}</Badge> },
          { key: 'calculationType', label: 'Calculation' },
          { key: 'taxable', label: 'Taxable', render: c => c.taxable ? '✓' : '—' },
        ]}
      />
      <FormDialog open={creating} onOpenChange={setCreating} title="Add salary component"
        onSubmit={v => create.mutateAsync(v)}
        fields={[
          { name: 'code', label: 'Code', type: 'text', required: true },
          { name: 'name', label: 'Display name', type: 'text', required: true },
          { name: 'type', label: 'Type', type: 'select', required: true, options: [
            { value: 'EARNING', label: 'Earning' }, { value: 'DEDUCTION', label: 'Deduction' }, { value: 'BENEFIT', label: 'Benefit' },
          ] },
          { name: 'calculationType', label: 'Calculation', type: 'select', required: true, options: [
            { value: 'FIXED', label: 'Fixed amount' }, { value: 'PERCENTAGE', label: 'Percentage of base' },
          ] },
          { name: 'value', label: 'Value', type: 'number', required: true },
          { name: 'taxable', label: 'Taxable', type: 'switch' },
        ]}
      />
    </>
  )
}

interface EmpSalary { id: string; employeeName?: string; structureName?: string; basicSalary: number; ctc: number; effectiveFrom: string }
export function EmployeeSalaryPage() {
  const { data, isLoading } = useQuery({ queryKey: ['emp-salary'], queryFn: employeeSalaryService.list })
  return (
    <DataList<EmpSalary>
      title="Employee Salaries" description="Salary assignments + history per employee"
      data={rows(data)} isLoading={isLoading}
      emptyIcon={<DollarSign className="h-10 w-10" />} emptyTitle="No salary assignments"
      columns={[
        { key: 'employeeName', label: 'Employee' }, { key: 'structureName', label: 'Structure' },
        { key: 'basicSalary', label: 'Basic', align: 'right', render: e => `₹${e.basicSalary?.toLocaleString()}` },
        { key: 'ctc', label: 'CTC', align: 'right', render: e => `₹${e.ctc?.toLocaleString()}` },
        { key: 'effectiveFrom', label: 'Effective from' },
      ]}
    />
  )
}

interface TaxCfg { id: string; financialYear: string; regime: string; slabs: number; surchargeEnabled?: boolean }
export function TaxConfigPage() {
  const { data, isLoading } = useQuery({ queryKey: ['tax-config'], queryFn: taxConfigService.list })
  return (
    <DataList<TaxCfg>
      title="Tax Configuration" description="Income-tax slabs per FY × regime + surcharge/cess rules"
      data={rows(data)} isLoading={isLoading}
      emptyIcon={<Cog className="h-10 w-10" />} emptyTitle="No tax config"
      columns={[
        { key: 'financialYear', label: 'FY' },
        { key: 'regime', label: 'Regime', render: t => <Badge>{t.regime}</Badge> },
        { key: 'slabs', label: 'Slabs', align: 'right' },
        { key: 'surchargeEnabled', label: 'Surcharge', render: t => t.surchargeEnabled ? '✓' : '—' },
      ]}
    />
  )
}

// ── Helpdesk depth ─────────────────────────────────────────────────────
interface TicketCat { id: string; name: string; slaHours?: number; assignedTeam?: string }
export function TicketCategoriesPage() {
  const qc = useQueryClient()
  const [creating, setCreating] = useState(false)
  const { data, isLoading } = useQuery({ queryKey: ['ticket-cats'], queryFn: ticketCategoryService.list })
  const create = useMutation({ mutationFn: ticketCategoryService.create, onSuccess: () => qc.invalidateQueries({ queryKey: ['ticket-cats'] }) })
  return (
    <>
      <DataList<TicketCat>
        title="Ticket Categories" description="Helpdesk routing + SLA per category"
        action={<Button onClick={() => setCreating(true)}><Plus className="h-4 w-4 mr-1" /> New Category</Button>}
        data={rows(data)} isLoading={isLoading}
        emptyIcon={<Hash className="h-10 w-10" />} emptyTitle="No categories"
        columns={[
          { key: 'name', label: 'Category' }, { key: 'slaHours', label: 'SLA (hours)' },
          { key: 'assignedTeam', label: 'Default team' },
        ]}
      />
      <FormDialog open={creating} onOpenChange={setCreating} title="Add ticket category"
        onSubmit={v => create.mutateAsync(v)}
        fields={[
          { name: 'name', label: 'Name', type: 'text', required: true, span: 2 },
          { name: 'slaHours', label: 'SLA (hours)', type: 'number' },
          { name: 'assignedTeam', label: 'Default team', type: 'text' },
          { name: 'description', label: 'Description', type: 'textarea', span: 2 },
        ]}
      />
    </>
  )
}

// ── Documents depth ────────────────────────────────────────────────────
interface DocTpl { id: string; name: string; type: string; variables?: string[] }
export function DocumentTemplatesAdminPage() {
  const { data, isLoading } = useQuery({ queryKey: ['doc-tpl'], queryFn: documentTemplateAdminService.list })
  return (
    <DataList<DocTpl>
      title="Document Templates" description="Letter and contract templates library"
      data={rows(data)} isLoading={isLoading}
      emptyIcon={<FileSignature className="h-10 w-10" />} emptyTitle="No templates"
      columns={[
        { key: 'name', label: 'Template' },
        { key: 'type', label: 'Type', render: t => <Badge>{t.type}</Badge> },
        { key: 'variables', label: 'Variables', render: t => <Badge>{t.variables?.length || 0}</Badge> },
      ]}
    />
  )
}

interface DocType { id: string; code: string; name: string; category?: string; requiredFor?: string }
export function DocumentTypesPage() {
  const qc = useQueryClient()
  const [creating, setCreating] = useState(false)
  const { data, isLoading } = useQuery({ queryKey: ['doc-types'], queryFn: documentTypeService.list })
  const create = useMutation({ mutationFn: documentTypeService.create, onSuccess: () => qc.invalidateQueries({ queryKey: ['doc-types'] }) })
  return (
    <>
      <DataList<DocType>
        title="Document Types" description="Master list of document classifications"
        action={<Button onClick={() => setCreating(true)}><Plus className="h-4 w-4 mr-1" /> New Type</Button>}
        data={rows(data)} isLoading={isLoading}
        emptyIcon={<FileText className="h-10 w-10" />} emptyTitle="No document types"
        columns={[
          { key: 'code', label: 'Code' }, { key: 'name', label: 'Name' },
          { key: 'category', label: 'Category' }, { key: 'requiredFor', label: 'Required for' },
        ]}
      />
      <FormDialog open={creating} onOpenChange={setCreating} title="Create document type"
        onSubmit={v => create.mutateAsync(v)}
        fields={[
          { name: 'code', label: 'Code', type: 'text', required: true },
          { name: 'name', label: 'Display name', type: 'text', required: true },
          { name: 'category', label: 'Category', type: 'text' },
          { name: 'requiredFor', label: 'Required for', type: 'select', options: [
            { value: 'ONBOARDING', label: 'Onboarding' }, { value: 'VISA', label: 'Visa' },
            { value: 'OFFBOARDING', label: 'Offboarding' }, { value: 'PAYROLL', label: 'Payroll' },
          ] },
        ]}
      />
    </>
  )
}

interface VaultFile { id: string; name: string; category?: string; sizeBytes?: number; ownerName?: string; uploadedAt: string }
export function FileVaultPage() {
  const { data, isLoading } = useQuery({ queryKey: ['file-vault'], queryFn: fileVaultService.list })
  return (
    <DataList<VaultFile>
      title="File Vault" description="Secure long-term file storage"
      data={rows(data)} isLoading={isLoading}
      emptyIcon={<FolderOpen className="h-10 w-10" />} emptyTitle="Empty vault"
      columns={[
        { key: 'name', label: 'File' }, { key: 'category', label: 'Category' },
        { key: 'sizeBytes', label: 'Size', align: 'right', render: f => f.sizeBytes ? `${Math.round(f.sizeBytes / 1024)} KB` : '—' },
        { key: 'ownerName', label: 'Owner' }, { key: 'uploadedAt', label: 'Uploaded' },
      ]}
    />
  )
}

// ── Workflow depth ─────────────────────────────────────────────────────
interface Instance { id: string; definitionName?: string; subjectId?: string; status: string; currentStep?: string; startedAt: string }
export function WorkflowInstancesPage() {
  const { data, isLoading } = useQuery({ queryKey: ['wf-instances'], queryFn: workflowInstanceAdminService.list })
  return (
    <DataList<Instance>
      title="Workflow Instances" description="Active and completed workflow runs"
      data={rows(data)} isLoading={isLoading}
      emptyIcon={<Workflow className="h-10 w-10" />} emptyTitle="No instances"
      columns={[
        { key: 'definitionName', label: 'Workflow' }, { key: 'subjectId', label: 'Subject' },
        { key: 'currentStep', label: 'Current step' }, { key: 'startedAt', label: 'Started' },
        { key: 'status', label: 'Status', render: i => <Badge>{i.status}</Badge> },
      ]}
    />
  )
}

interface DelegationRule { id: string; module: string; fromUserId: string; toUserId: string; startDate: string; endDate?: string; active: boolean }
export function DelegationRulesPage() {
  const qc = useQueryClient()
  const [creating, setCreating] = useState(false)
  const { data, isLoading } = useQuery({ queryKey: ['delegation-rules'], queryFn: delegationRuleService.list })
  const create = useMutation({ mutationFn: delegationRuleService.create, onSuccess: () => qc.invalidateQueries({ queryKey: ['delegation-rules'] }) })
  return (
    <>
      <DataList<DelegationRule>
        title="Delegation Rules" description="Permanent delegation of approvals between roles"
        action={<Button onClick={() => setCreating(true)}><Plus className="h-4 w-4 mr-1" /> New Rule</Button>}
        data={rows(data)} isLoading={isLoading}
        emptyIcon={<GitBranch className="h-10 w-10" />} emptyTitle="No delegation rules"
        columns={[
          { key: 'module', label: 'Module' },
          { key: 'fromUserId', label: 'From' }, { key: 'toUserId', label: 'To' },
          { key: 'startDate', label: 'Start' }, { key: 'endDate', label: 'End' },
          { key: 'active', label: 'Active', render: r => <Badge>{r.active ? 'Active' : 'Inactive'}</Badge> },
        ]}
      />
      <FormDialog open={creating} onOpenChange={setCreating} title="Create delegation rule"
        onSubmit={v => create.mutateAsync(v)}
        fields={[
          { name: 'module', label: 'Module', type: 'select', required: true, options: [
            { value: 'LEAVE', label: 'Leave' }, { value: 'EXPENSE', label: 'Expense' },
            { value: 'TIMESHEET', label: 'Timesheet' }, { value: 'ALL', label: 'All' },
          ] },
          { name: 'fromUserId', label: 'From user ID', type: 'text', required: true },
          { name: 'toUserId', label: 'To user ID', type: 'text', required: true },
          { name: 'startDate', label: 'Start date', type: 'date', required: true },
          { name: 'endDate', label: 'End date', type: 'date' },
        ]}
      />
    </>
  )
}

// ── Integrations depth ─────────────────────────────────────────────────
interface Webhook { id: string; url: string; events: string[]; active: boolean; lastDeliveredAt?: string }
export function WebhooksPage() {
  const qc = useQueryClient()
  const [creating, setCreating] = useState(false)
  const { data, isLoading } = useQuery({ queryKey: ['webhooks'], queryFn: integrationsService.webhooks })
  const create = useMutation({ mutationFn: integrationsService.registerWebhook, onSuccess: () => qc.invalidateQueries({ queryKey: ['webhooks'] }) })
  const rotate = useMutation({ mutationFn: (id: string) => integrationsService.rotateSecret(id), onSuccess: () => qc.invalidateQueries({ queryKey: ['webhooks'] }) })
  return (
    <>
      <DataList<Webhook>
        title="Webhooks" description="Outbound HTTP delivery on platform events"
        action={<Button onClick={() => setCreating(true)}><Plus className="h-4 w-4 mr-1" /> New Webhook</Button>}
        data={rows(data)} isLoading={isLoading}
        emptyIcon={<PlugZap className="h-10 w-10" />} emptyTitle="No webhooks"
        columns={[
          { key: 'url', label: 'URL', render: w => <code className="text-xs">{w.url}</code> },
          { key: 'events', label: 'Events', render: w => <Badge>{w.events?.length || 0}</Badge> },
          { key: 'lastDeliveredAt', label: 'Last delivered' },
          { key: 'active', label: 'Active', render: w => <Badge>{w.active ? 'Active' : 'Inactive'}</Badge> },
          { key: 'id', label: '', render: w => <Button size="sm" variant="outline" onClick={() => rotate.mutate(w.id)}>Rotate secret</Button> },
        ]}
      />
      <FormDialog open={creating} onOpenChange={setCreating} title="Register webhook"
        onSubmit={v => create.mutateAsync({ ...v, events: String(v.events).split(',').map(x => x.trim()) })}
        fields={[
          { name: 'url', label: 'Endpoint URL', type: 'url', required: true, span: 2 },
          { name: 'events', label: 'Events (comma-separated)', type: 'text', required: true, span: 2,
            placeholder: 'employee.created,leave.approved,payroll.published' },
        ]}
      />
    </>
  )
}

// ── Job-cost reports ───────────────────────────────────────────────────
interface JobCost { project: string; billableHours: number; nonBillableHours: number; cost: number; revenue?: number }
export function JobCostReportsPage() {
  const { data, isLoading } = useQuery({ queryKey: ['job-cost'], queryFn: jobCostService.list })
  return (
    <DataList<JobCost>
      title="Job Cost Reports" description="Project profitability from timesheet hours × rate"
      data={rows(data)} isLoading={isLoading}
      emptyIcon={<BarChart3 className="h-10 w-10" />} emptyTitle="No job-cost data"
      columns={[
        { key: 'project', label: 'Project' },
        { key: 'billableHours', label: 'Billable', align: 'right' },
        { key: 'nonBillableHours', label: 'Non-billable', align: 'right' },
        { key: 'cost', label: 'Cost', align: 'right', render: r => `₹${r.cost?.toLocaleString()}` },
        { key: 'revenue', label: 'Revenue', align: 'right', render: r => r.revenue ? `₹${r.revenue.toLocaleString()}` : '—' },
      ]}
    />
  )
}

// ── User notification preferences referenced in inbox link ─────────────
export { Bell as _Bell, Receipt as _Receipt }
