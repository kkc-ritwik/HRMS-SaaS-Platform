/** Final batch of pages closing the remaining backend↔frontend gaps. */
import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import {
  Plus, DollarSign, HeartPulse, TrendingUp, Receipt, Calendar, FileSpreadsheet,
  LayoutGrid, GitBranch, UserCheck, Brain, ArrowLeftRight, FileText, History,
  Smile, Upload, FolderOpen, Send, BookOpen,
} from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Skeleton } from '@/components/ui/skeleton'
import { DataList } from '@/components/ui/data-list'
import { FormDialog } from '@/components/ui/form-dialog'
import { PageHeader } from '@/components/ui/page-header'
import {
  compensationPlanService, employeeBenefitService, marketBenchmarkService,
  expenseCategoryService, leavePolicyService, gstService, dashboardWidgetService,
  workflowStepService, referenceCheckService, psychometricService, internalMobilityService,
  onboardingDocumentService, csatService, employeeBulkImportService, filesService,
  notificationDispatchService, documentVersionService,
} from '@/services/gapServices'
import { toast } from 'sonner'

// (toast imported above is used by GST, Psychometric, BulkImport, Dispatch pages)

function rows<T>(d: unknown): T[] {
  return (d as { content?: T[] } | undefined)?.content || (Array.isArray(d) ? d as T[] : [])
}

// ── Compensation Plans ─────────────────────────────────────────────────
interface Plan { id: string; employeeId: string; ctc: number; basic: number; effectiveFrom: string; status: string }
export function CompensationPlansPage() {
  const qc = useQueryClient()
  const [creating, setCreating] = useState(false)
  const { data, isLoading } = useQuery({ queryKey: ['comp-plans'], queryFn: compensationPlanService.list })
  const create = useMutation({ mutationFn: compensationPlanService.create, onSuccess: () => qc.invalidateQueries({ queryKey: ['comp-plans'] }) })
  return (
    <>
      <DataList<Plan>
        title="Compensation Plans" description="CTC structures assigned to employees"
        action={<Button onClick={() => setCreating(true)}><Plus className="h-4 w-4 mr-1" /> New Plan</Button>}
        data={rows(data)} isLoading={isLoading}
        emptyIcon={<DollarSign className="h-10 w-10" />} emptyTitle="No compensation plans"
        filters={{ status: ['DRAFT', 'ACTIVE', 'EXPIRED'] }}
        columns={[
          { key: 'employeeId', label: 'Employee' },
          { key: 'ctc', label: 'CTC', align: 'right', render: p => `₹${p.ctc?.toLocaleString()}` },
          { key: 'basic', label: 'Basic', align: 'right', render: p => `₹${p.basic?.toLocaleString()}` },
          { key: 'effectiveFrom', label: 'Effective' },
          { key: 'status', label: 'Status', render: p => <Badge>{p.status}</Badge> },
        ]}
      />
      <FormDialog open={creating} onOpenChange={setCreating} title="Create compensation plan"
        onSubmit={v => create.mutateAsync(v)}
        fields={[
          { name: 'employeeId', label: 'Employee ID', type: 'text', required: true },
          { name: 'effectiveFrom', label: 'Effective from', type: 'date', required: true },
          { name: 'ctc', label: 'Total CTC', type: 'currency', required: true },
          { name: 'basic', label: 'Basic', type: 'currency', required: true },
          { name: 'hra', label: 'HRA', type: 'currency' },
          { name: 'special', label: 'Special allowance', type: 'currency' },
          { name: 'variablePay', label: 'Variable pay', type: 'currency' },
        ]}
      />
    </>
  )
}

// ── Employee Benefits enrolment ─────────────────────────────────────────
interface EmpBenefit { id: string; employeeName?: string; benefitName?: string; status: string; enrolledOn?: string; coverageAmount?: number }
export function EmployeeBenefitsPage() {
  const qc = useQueryClient()
  const [creating, setCreating] = useState(false)
  const { data, isLoading } = useQuery({ queryKey: ['emp-benefits'], queryFn: employeeBenefitService.list })
  const enroll = useMutation({ mutationFn: employeeBenefitService.enroll, onSuccess: () => qc.invalidateQueries({ queryKey: ['emp-benefits'] }) })
  return (
    <>
      <DataList<EmpBenefit>
        title="Benefit Enrolments" description="Employee enrolment in insurance + benefit plans"
        action={<Button onClick={() => setCreating(true)}><Plus className="h-4 w-4 mr-1" /> Enroll</Button>}
        data={rows(data)} isLoading={isLoading}
        emptyIcon={<HeartPulse className="h-10 w-10" />} emptyTitle="No enrolments"
        columns={[
          { key: 'employeeName', label: 'Employee' }, { key: 'benefitName', label: 'Benefit' },
          { key: 'coverageAmount', label: 'Coverage', align: 'right' },
          { key: 'enrolledOn', label: 'Enrolled' },
          { key: 'status', label: 'Status', render: b => <Badge>{b.status}</Badge> },
        ]}
      />
      <FormDialog open={creating} onOpenChange={setCreating} title="Enroll in benefit"
        onSubmit={v => enroll.mutateAsync(v)}
        fields={[
          { name: 'employeeId', label: 'Employee ID', type: 'text', required: true },
          { name: 'benefitId', label: 'Benefit ID', type: 'text', required: true },
          { name: 'coverageAmount', label: 'Coverage amount', type: 'currency' },
          { name: 'startDate', label: 'Start date', type: 'date' },
        ]}
      />
    </>
  )
}

// ── Market Benchmark ────────────────────────────────────────────────────
export function MarketBenchmarkPage() {
  const [roleCode, setRoleCode] = useState('')
  const [country, setCountry] = useState('IN')
  const { data, isLoading, refetch } = useQuery({
    queryKey: ['benchmark', roleCode, country],
    queryFn: () => marketBenchmarkService.lookup(roleCode, country),
    enabled: false,
  })
  const items = rows<{ provider: string; level?: string; p25TotalCash?: number; p50TotalCash?: number; p75TotalCash?: number; surveyDate: string }>(data)
  return (
    <div className="space-y-6">
      <PageHeader title="Market Benchmarks" description="Compa-ratio + percentile bands from Mercer / AON / WTW surveys" />
      <Card>
        <CardContent className="p-4 flex items-end gap-3">
          <div><Label>Role code</Label><Input value={roleCode} onChange={e => setRoleCode(e.target.value)} placeholder="SDE2" /></div>
          <div><Label>Country</Label><Input value={country} onChange={e => setCountry(e.target.value)} className="w-24" /></div>
          <Button onClick={() => refetch()} disabled={!roleCode}><TrendingUp className="h-4 w-4 mr-1" /> Lookup</Button>
        </CardContent>
      </Card>
      {isLoading ? <Skeleton className="h-40" /> : items.length > 0 && (
        <Card><CardContent className="p-0">
          <table className="w-full text-sm">
            <thead className="bg-slate-50 text-xs uppercase text-slate-500"><tr>
              <th className="p-3 text-left">Provider</th><th className="p-3 text-left">Level</th>
              <th className="p-3 text-right">P25</th><th className="p-3 text-right">P50 (median)</th>
              <th className="p-3 text-right">P75</th><th className="p-3 text-left">Survey date</th>
            </tr></thead>
            <tbody>{items.map((b, i) => (
              <tr key={i} className="border-t">
                <td className="p-3"><Badge>{b.provider}</Badge></td><td className="p-3">{b.level || '—'}</td>
                <td className="p-3 text-right">{b.p25TotalCash ? `₹${b.p25TotalCash.toLocaleString()}` : '—'}</td>
                <td className="p-3 text-right font-semibold">{b.p50TotalCash ? `₹${b.p50TotalCash.toLocaleString()}` : '—'}</td>
                <td className="p-3 text-right">{b.p75TotalCash ? `₹${b.p75TotalCash.toLocaleString()}` : '—'}</td>
                <td className="p-3">{b.surveyDate}</td>
              </tr>
            ))}</tbody>
          </table>
        </CardContent></Card>
      )}
    </div>
  )
}

// ── Expense Categories ──────────────────────────────────────────────────
interface ExpCat { id: string; name: string; code: string; perDiemLimit?: number; requiresReceipt?: boolean }
export function ExpenseCategoriesPage() {
  const qc = useQueryClient()
  const [creating, setCreating] = useState(false)
  const { data, isLoading } = useQuery({ queryKey: ['exp-cats'], queryFn: expenseCategoryService.list })
  const create = useMutation({ mutationFn: expenseCategoryService.create, onSuccess: () => qc.invalidateQueries({ queryKey: ['exp-cats'] }) })
  return (
    <>
      <DataList<ExpCat>
        title="Expense Categories" description="Travel, meals, lodging, supplies, etc."
        action={<Button onClick={() => setCreating(true)}><Plus className="h-4 w-4 mr-1" /> New Category</Button>}
        data={rows(data)} isLoading={isLoading}
        emptyIcon={<Receipt className="h-10 w-10" />} emptyTitle="No categories"
        columns={[
          { key: 'code', label: 'Code' }, { key: 'name', label: 'Name' },
          { key: 'perDiemLimit', label: 'Per diem', align: 'right' },
          { key: 'requiresReceipt', label: 'Receipt?', render: c => c.requiresReceipt ? '✓' : '—' },
        ]}
      />
      <FormDialog open={creating} onOpenChange={setCreating} title="Create expense category"
        onSubmit={v => create.mutateAsync(v)}
        fields={[
          { name: 'code', label: 'Code', type: 'text', required: true },
          { name: 'name', label: 'Name', type: 'text', required: true },
          { name: 'perDiemLimit', label: 'Per diem limit', type: 'currency' },
          { name: 'requiresReceipt', label: 'Requires receipt', type: 'switch' },
        ]}
      />
    </>
  )
}

// ── Leave Policies ──────────────────────────────────────────────────────
interface LeavePolicy { id: string; name: string; accrualType: string; accrualAmount: number; carryForwardEnabled: boolean; encashmentEnabled: boolean; active: boolean }
export function LeavePoliciesPage() {
  const qc = useQueryClient()
  const [creating, setCreating] = useState(false)
  const { data, isLoading } = useQuery({ queryKey: ['leave-policies'], queryFn: leavePolicyService.list })
  const create = useMutation({ mutationFn: leavePolicyService.create, onSuccess: () => qc.invalidateQueries({ queryKey: ['leave-policies'] }) })
  return (
    <>
      <DataList<LeavePolicy>
        title="Leave Policies" description="Accrual, carry-forward, encashment, sandwich rules"
        action={<Button onClick={() => setCreating(true)}><Plus className="h-4 w-4 mr-1" /> New Policy</Button>}
        data={rows(data)} isLoading={isLoading}
        emptyIcon={<Calendar className="h-10 w-10" />} emptyTitle="No leave policies"
        columns={[
          { key: 'name', label: 'Policy' },
          { key: 'accrualType', label: 'Accrual', render: p => <Badge>{p.accrualType}</Badge> },
          { key: 'accrualAmount', label: 'Days/period', align: 'right' },
          { key: 'carryForwardEnabled', label: 'Carry-forward', render: p => p.carryForwardEnabled ? '✓' : '—' },
          { key: 'encashmentEnabled', label: 'Encashment', render: p => p.encashmentEnabled ? '✓' : '—' },
          { key: 'active', label: 'Active', render: p => <Badge>{p.active ? 'Active' : 'Inactive'}</Badge> },
        ]}
      />
      <FormDialog open={creating} onOpenChange={setCreating} title="Create leave policy"
        onSubmit={v => create.mutateAsync(v)}
        fields={[
          { name: 'name', label: 'Policy name', type: 'text', required: true, span: 2 },
          { name: 'accrualType', label: 'Accrual type', type: 'select', required: true, options: [
            { value: 'MONTHLY', label: 'Monthly' }, { value: 'QUARTERLY', label: 'Quarterly' },
            { value: 'YEARLY', label: 'Yearly' }, { value: 'ON_HIRE_DATE', label: 'On hire date' },
          ] },
          { name: 'accrualAmount', label: 'Days per period', type: 'number', required: true },
          { name: 'maxCarryForward', label: 'Max carry-forward', type: 'number' },
          { name: 'carryForwardEnabled', label: 'Carry-forward enabled', type: 'switch' },
          { name: 'encashmentEnabled', label: 'Encashment enabled', type: 'switch' },
          { name: 'sandwichRuleEnabled', label: 'Sandwich rule', type: 'switch' },
          { name: 'allowHalfDay', label: 'Allow half-day', type: 'switch', defaultValue: true },
        ]}
      />
    </>
  )
}

// ── GST Returns ─────────────────────────────────────────────────────────
export function GstReturnsPage() {
  const gstr1 = useMutation({ mutationFn: () => gstService.gstr1({ period: new Date().toISOString().slice(0, 7) }), onSuccess: () => toast.success('GSTR-1 generated') })
  const gstr3b = useMutation({ mutationFn: () => gstService.gstr3b({ period: new Date().toISOString().slice(0, 7) }), onSuccess: () => toast.success('GSTR-3B generated') })
  return (
    <div className="space-y-6">
      <PageHeader title="GST Returns" description="Generate GSTR-1 (outward supplies) and GSTR-3B (summary)" />
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <Card><CardHeader><CardTitle className="flex items-center gap-2"><FileSpreadsheet className="h-5 w-5" /> GSTR-1</CardTitle></CardHeader>
          <CardContent><p className="text-sm text-slate-500 mb-4">Outward supplies for the current month.</p>
            <Button onClick={() => gstr1.mutate()} disabled={gstr1.isPending}>Generate GSTR-1</Button></CardContent></Card>
        <Card><CardHeader><CardTitle className="flex items-center gap-2"><FileSpreadsheet className="h-5 w-5" /> GSTR-3B</CardTitle></CardHeader>
          <CardContent><p className="text-sm text-slate-500 mb-4">Monthly summary return.</p>
            <Button onClick={() => gstr3b.mutate()} disabled={gstr3b.isPending}>Generate GSTR-3B</Button></CardContent></Card>
      </div>
    </div>
  )
}

// ── Dashboard Widgets ───────────────────────────────────────────────────
interface Widget { id: string; title: string; type: string; reportId?: string; dashboardName?: string }
export function DashboardWidgetsPage() {
  const { data, isLoading } = useQuery({ queryKey: ['widgets'], queryFn: () => dashboardWidgetService.list() })
  return (
    <DataList<Widget>
      title="Dashboard Widgets" description="Reusable widgets bound to saved reports"
      data={rows(data)} isLoading={isLoading}
      emptyIcon={<LayoutGrid className="h-10 w-10" />} emptyTitle="No widgets"
      columns={[
        { key: 'title', label: 'Widget' },
        { key: 'type', label: 'Type', render: w => <Badge>{w.type}</Badge> },
        { key: 'dashboardName', label: 'Dashboard' },
      ]}
    />
  )
}

// ── Workflow Steps ──────────────────────────────────────────────────────
interface Step { id: string; definitionName?: string; name: string; type: string; orderIndex?: number; approverRole?: string }
export function WorkflowStepsPage() {
  const { data, isLoading } = useQuery({ queryKey: ['wf-steps'], queryFn: () => workflowStepService.list() })
  return (
    <DataList<Step>
      title="Workflow Steps" description="Step definitions across workflow definitions"
      data={rows(data)} isLoading={isLoading}
      emptyIcon={<GitBranch className="h-10 w-10" />} emptyTitle="No steps"
      columns={[
        { key: 'definitionName', label: 'Workflow' }, { key: 'orderIndex', label: '#', align: 'right' },
        { key: 'name', label: 'Step' }, { key: 'type', label: 'Type', render: s => <Badge>{s.type}</Badge> },
        { key: 'approverRole', label: 'Approver role' },
      ]}
    />
  )
}

// ── Reference Checks ────────────────────────────────────────────────────
interface RefCheck { id: string; candidateId: string; refereeName: string; status: string; rating?: number; wouldRehire?: boolean }
export function ReferenceChecksPage() {
  const qc = useQueryClient()
  const [creating, setCreating] = useState(false)
  const { data, isLoading } = useQuery({ queryKey: ['ref-checks'], queryFn: referenceCheckService.list })
  const create = useMutation({ mutationFn: referenceCheckService.invite, onSuccess: () => qc.invalidateQueries({ queryKey: ['ref-checks'] }) })
  return (
    <>
      <DataList<RefCheck>
        title="Reference Checks" description="Referee feedback collected for candidates"
        action={<Button onClick={() => setCreating(true)}><Plus className="h-4 w-4 mr-1" /> Request Reference</Button>}
        data={rows(data)} isLoading={isLoading}
        emptyIcon={<UserCheck className="h-10 w-10" />} emptyTitle="No reference checks"
        columns={[
          { key: 'candidateId', label: 'Candidate' }, { key: 'refereeName', label: 'Referee' },
          { key: 'rating', label: 'Rating', render: r => r.rating ? `${r.rating}/5` : '—' },
          { key: 'wouldRehire', label: 'Would rehire?', render: r => r.wouldRehire == null ? '—' : r.wouldRehire ? 'Yes' : 'No' },
          { key: 'status', label: 'Status', render: r => <Badge>{r.status}</Badge> },
        ]}
      />
      <FormDialog open={creating} onOpenChange={setCreating} title="Request reference check"
        onSubmit={v => create.mutateAsync(v)}
        fields={[
          { name: 'candidateId', label: 'Candidate ID', type: 'text', required: true },
          { name: 'refereeName', label: 'Referee name', type: 'text', required: true },
          { name: 'refereeEmail', label: 'Referee email', type: 'email', required: true },
          { name: 'refereeRelationship', label: 'Relationship', type: 'text' },
          { name: 'refereeCompany', label: 'Company', type: 'text' },
        ]}
      />
    </>
  )
}

// ── Psychometric Tests ──────────────────────────────────────────────────
interface Psych { id: string; candidateId: string; vendor: string; testName?: string; status: string; overallScore?: number; percentile?: number }
export function PsychometricPage() {
  const qc = useQueryClient()
  const [creating, setCreating] = useState(false)
  const [candidateId, setCandidateId] = useState('')
  const { data, isLoading, refetch } = useQuery({
    queryKey: ['psych', candidateId], queryFn: () => psychometricService.forCandidate(candidateId), enabled: false,
  })
  const invite = useMutation({ mutationFn: psychometricService.invite, onSuccess: () => { toast.success('Test invite sent'); qc.invalidateQueries({ queryKey: ['psych'] }) } })
  return (
    <div className="space-y-4">
      <PageHeader title="Psychometric Assessments" description="Aptitude + personality tests (Mettl, SHL, Codility, AON)"
        action={<Button onClick={() => setCreating(true)}><Plus className="h-4 w-4 mr-1" /> Invite Candidate</Button>} />
      <Card><CardContent className="p-4 flex items-end gap-3">
        <div><Label>Candidate ID</Label><Input value={candidateId} onChange={e => setCandidateId(e.target.value)} /></div>
        <Button onClick={() => refetch()} disabled={!candidateId}><Brain className="h-4 w-4 mr-1" /> View Results</Button>
      </CardContent></Card>
      <DataList<Psych>
        title="" data={rows(data)} isLoading={isLoading} searchable={false} exportable={false}
        emptyIcon={<Brain className="h-10 w-10" />} emptyTitle="Enter a candidate ID to view their assessments"
        columns={[
          { key: 'vendor', label: 'Vendor', render: p => <Badge>{p.vendor}</Badge> },
          { key: 'testName', label: 'Test' },
          { key: 'overallScore', label: 'Score', render: p => p.overallScore ?? '—' },
          { key: 'percentile', label: 'Percentile', render: p => p.percentile ? `${p.percentile}th` : '—' },
          { key: 'status', label: 'Status', render: p => <Badge>{p.status}</Badge> },
        ]}
      />
      <FormDialog open={creating} onOpenChange={setCreating} title="Invite candidate to test"
        onSubmit={v => invite.mutateAsync(v)}
        fields={[
          { name: 'candidateId', label: 'Candidate ID', type: 'text', required: true },
          { name: 'candidateEmail', label: 'Candidate email', type: 'email', required: true },
          { name: 'vendor', label: 'Vendor', type: 'select', required: true, options: [
            { value: 'METTL', label: 'Mettl' }, { value: 'SHL', label: 'SHL' },
            { value: 'CODILITY', label: 'Codility' }, { value: 'HACKEREARTH', label: 'HackerEarth' },
            { value: 'AON', label: 'AON' }, { value: 'INTERNAL', label: 'Internal' },
          ] },
          { name: 'testCode', label: 'Test code', type: 'text', required: true },
          { name: 'testName', label: 'Test name', type: 'text' },
        ]}
      />
    </div>
  )
}

// ── Internal Mobility ───────────────────────────────────────────────────
interface Opening { id: string; title: string; location?: string; status: string; positionsCount?: number }
export function InternalMobilityPage() {
  const { data, isLoading } = useQuery({ queryKey: ['internal-openings'], queryFn: internalMobilityService.openings })
  const alumni = useQuery({ queryKey: ['boomerang'], queryFn: internalMobilityService.boomerang })
  return (
    <div className="space-y-6">
      <DataList<Opening>
        title="Internal Job Board" description="Open positions employees can apply to internally"
        data={rows(data)} isLoading={isLoading}
        emptyIcon={<ArrowLeftRight className="h-10 w-10" />} emptyTitle="No internal openings"
        columns={[
          { key: 'title', label: 'Role' }, { key: 'location', label: 'Location' },
          { key: 'positionsCount', label: 'Openings', align: 'right' },
          { key: 'status', label: 'Status', render: o => <Badge>{o.status}</Badge> },
        ]}
      />
      <Card>
        <CardHeader><CardTitle>Boomerang-eligible alumni</CardTitle></CardHeader>
        <CardContent>
          {alumni.isLoading ? <Skeleton className="h-24" /> : (
            <div className="flex flex-wrap gap-2">
              {(rows<{ id: string; fullName: string; lastDesignation?: string }>(alumni.data)).map(a => (
                <Badge key={a.id}>{a.fullName} {a.lastDesignation ? `· ${a.lastDesignation}` : ''}</Badge>
              ))}
              {rows(alumni.data).length === 0 && <p className="text-sm text-slate-500">No boomerang candidates</p>}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  )
}

// ── Onboarding Documents ────────────────────────────────────────────────
interface OnbDoc { id: string; employeeId: string; documentType: string; status: string; uploadedAt?: string }
export function OnboardingDocumentsPage() {
  const { data, isLoading } = useQuery({ queryKey: ['onb-docs'], queryFn: onboardingDocumentService.list })
  return (
    <DataList<OnbDoc>
      title="Onboarding Documents" description="Document collection status per new hire"
      data={rows(data)} isLoading={isLoading}
      emptyIcon={<FileText className="h-10 w-10" />} emptyTitle="No onboarding documents"
      columns={[
        { key: 'employeeId', label: 'Employee' }, { key: 'documentType', label: 'Document' },
        { key: 'uploadedAt', label: 'Uploaded' },
        { key: 'status', label: 'Status', render: d => <Badge>{d.status}</Badge> },
      ]}
    />
  )
}

// ── Document Versions ───────────────────────────────────────────────────
interface DocVersion { id: string; versionNumber: number; changeNote?: string; uploadedBy?: string; createdAt: string; sizeBytes?: number }
export function DocumentVersionsPage() {
  const [docId, setDocId] = useState('')
  const { data, isLoading, refetch } = useQuery({
    queryKey: ['doc-versions', docId], queryFn: () => documentVersionService.list(docId), enabled: false,
  })
  return (
    <div className="space-y-4">
      <PageHeader title="Document Versions" description="Version history for any document" />
      <Card><CardContent className="p-4 flex items-end gap-3">
        <div className="flex-1"><Label>Document ID</Label><Input value={docId} onChange={e => setDocId(e.target.value)} /></div>
        <Button onClick={() => refetch()} disabled={!docId}><History className="h-4 w-4 mr-1" /> Load versions</Button>
      </CardContent></Card>
      <DataList<DocVersion>
        title="" data={rows(data)} isLoading={isLoading} searchable={false} exportable={false}
        emptyIcon={<History className="h-10 w-10" />} emptyTitle="Enter a document ID"
        columns={[
          { key: 'versionNumber', label: 'Version', align: 'right' },
          { key: 'changeNote', label: 'Change note' }, { key: 'uploadedBy', label: 'By' },
          { key: 'createdAt', label: 'When' },
          { key: 'sizeBytes', label: 'Size', align: 'right', render: v => v.sizeBytes ? `${Math.round(v.sizeBytes / 1024)} KB` : '—' },
        ]}
      />
    </div>
  )
}

// ── CSAT dashboard ──────────────────────────────────────────────────────
export function CsatDashboardPage() {
  const { data, isLoading } = useQuery({ queryKey: ['csat'], queryFn: csatService.summary })
  return (
    <div className="space-y-6">
      <PageHeader title="Helpdesk CSAT" description="Customer satisfaction scores for resolved tickets" />
      <Card><CardContent className="p-6">
        {isLoading ? <Skeleton className="h-40" /> : (
          <div className="flex items-center gap-2"><Smile className="h-6 w-6 text-green-500" />
            <pre className="text-xs">{JSON.stringify(data, null, 2)}</pre>
          </div>
        )}
      </CardContent></Card>
    </div>
  )
}

// ── Employee Bulk Import wizard ─────────────────────────────────────────
export function EmployeeBulkImportPage() {
  const [result, setResult] = useState<Record<string, unknown> | null>(null)
  const [loading, setLoading] = useState(false)
  const onFile = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const f = e.target.files?.[0]; if (!f) return
    setLoading(true)
    try { setResult(await employeeBulkImportService.importCsv(f)); toast.success('Import complete') }
    catch (err) { toast.error(err instanceof Error ? err.message : 'Import failed') }
    finally { setLoading(false) }
  }
  return (
    <div className="space-y-6 max-w-2xl">
      <PageHeader title="Bulk Import Employees" description="Upload a CSV to onboard many employees at once" />
      <Card><CardContent className="p-6 space-y-4">
        <div className="border-2 border-dashed rounded-xl p-10 text-center">
          <Upload className="h-10 w-10 mx-auto text-slate-400 mb-3" />
          <p className="text-sm text-slate-500 mb-4">CSV columns: firstName, lastName, email, departmentCode, designationCode, joinDate</p>
          <input id="csv" type="file" accept=".csv" hidden onChange={onFile} />
          <Button onClick={() => document.getElementById('csv')?.click()}>Choose CSV file</Button>
        </div>
        {loading && <Skeleton className="h-20" />}
        {result && <pre className="bg-slate-50 p-4 rounded-lg text-xs overflow-auto">{JSON.stringify(result, null, 2)}</pre>}
      </CardContent></Card>
    </div>
  )
}

// ── File browser ────────────────────────────────────────────────────────
interface VFile { id: string; name: string; folder?: string; sizeBytes?: number; contentType?: string; uploadedAt: string }
export function FilesPage() {
  const { data, isLoading } = useQuery({ queryKey: ['files'], queryFn: () => filesService.list() })
  return (
    <DataList<VFile>
      title="Files" description="All files across the platform"
      data={rows(data)} isLoading={isLoading}
      emptyIcon={<FolderOpen className="h-10 w-10" />} emptyTitle="No files"
      columns={[
        { key: 'name', label: 'File' }, { key: 'folder', label: 'Folder' },
        { key: 'contentType', label: 'Type' },
        { key: 'sizeBytes', label: 'Size', align: 'right', render: f => f.sizeBytes ? `${Math.round(f.sizeBytes / 1024)} KB` : '—' },
        { key: 'uploadedAt', label: 'Uploaded' },
      ]}
    />
  )
}

// ── Notification Dispatch (admin manual send) ───────────────────────────
export function NotificationDispatchPage() {
  const send = useMutation({
    mutationFn: (v: Record<string, unknown>) => notificationDispatchService.send(v),
    onSuccess: () => toast.success('Notification dispatched'),
  })
  const [open, setOpen] = useState(false)
  const { data, isLoading } = useQuery({ queryKey: ['dispatch-history'], queryFn: notificationDispatchService.history })
  return (
    <>
      <DataList<{ id: string; channel: string; subject: string; audience: string; sentAt: string; deliveredCount?: number }>
        title="Notification Dispatch" description="Manually broadcast a message across channels"
        action={<Button onClick={() => setOpen(true)}><Send className="h-4 w-4 mr-1" /> New Dispatch</Button>}
        data={rows(data)} isLoading={isLoading}
        emptyIcon={<Send className="h-10 w-10" />} emptyTitle="No dispatches"
        columns={[
          { key: 'channel', label: 'Channel', render: d => <Badge>{d.channel}</Badge> },
          { key: 'subject', label: 'Subject' }, { key: 'audience', label: 'Audience' },
          { key: 'deliveredCount', label: 'Delivered', align: 'right' },
          { key: 'sentAt', label: 'Sent' },
        ]}
      />
      <FormDialog open={open} onOpenChange={setOpen} title="Dispatch notification"
        onSubmit={v => send.mutateAsync(v)}
        fields={[
          { name: 'channel', label: 'Channel', type: 'select', required: true, options: [
            { value: 'EMAIL', label: 'Email' }, { value: 'SMS', label: 'SMS' },
            { value: 'PUSH', label: 'Push' }, { value: 'IN_APP', label: 'In-app' }, { value: 'SLACK', label: 'Slack' },
          ] },
          { name: 'audience', label: 'Audience', type: 'select', required: true, options: [
            { value: 'ALL', label: 'All' }, { value: 'DEPARTMENT', label: 'Department' }, { value: 'ROLE', label: 'Role' },
          ] },
          { name: 'subject', label: 'Subject', type: 'text', required: true, span: 2 },
          { name: 'body', label: 'Message', type: 'textarea', required: true, span: 2 },
        ]}
      />
    </>
  )
}

// ── SCORM player ────────────────────────────────────────────────────────
export function ScormPlayerPage() {
  return (
    <div className="space-y-6">
      <PageHeader title="SCORM Player" description="Launches SCORM 1.2 / 2004 course packages with runtime tracking" />
      <Card><CardContent className="p-0">
        <div className="aspect-video bg-slate-900 rounded-lg flex items-center justify-center text-white">
          <div className="text-center">
            <BookOpen className="h-16 w-16 mx-auto opacity-50" />
            <p className="text-sm opacity-70 mt-2">SCORM content iframe mounts here.</p>
            <p className="text-xs opacity-50 mt-1">Runtime API (LMSInitialize / LMSCommit / LMSFinish) bridges to /api/v1/lms/scorm/runtime</p>
          </div>
        </div>
      </CardContent></Card>
    </div>
  )
}

// ── Public Career Site preview ──────────────────────────────────────────
export function CareerSitePage() {
  const tenant = localStorage.getItem('activeTenant') || 'demo'
  const { data, isLoading } = useQuery({
    queryKey: ['careers', tenant],
    queryFn: async () => {
      const { api } = await import('@/lib/api')
      return (await api.get(`/api/public/v1/careers/${tenant}/jobs`)).data
    },
  })
  const jobs = rows<{ id: string; title: string; location?: string; employmentType?: string }>(data)
  return (
    <div className="space-y-6">
      <PageHeader title="Career Site" description={`Public careers page preview · /careers/${tenant}`} />
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {isLoading ? Array.from({ length: 4 }).map((_, i) => <Skeleton key={i} className="h-28" />) :
          jobs.length === 0 ? <p className="text-sm text-slate-500">No published openings</p> :
          jobs.map(j => (
            <Card key={j.id}><CardContent className="p-4">
              <h3 className="font-semibold">{j.title}</h3>
              <p className="text-xs text-slate-500 mt-1">{j.location} · {j.employmentType}</p>
              <Button size="sm" className="mt-3">Apply now</Button>
            </CardContent></Card>
          ))}
      </div>
    </div>
  )
}
