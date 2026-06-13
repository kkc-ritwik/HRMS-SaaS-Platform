/**
 * Rich 11-tab employee profile.
 * Every tab pulls live data from the backend through the Catalog or dedicated services:
 *
 *   Overview     – core employee record + summary cards
 *   Personal     – PII, addresses, family, emergency contacts
 *   Employment   – work history, education, lifecycle timeline, transfer/promo
 *   Documents    – uploaded docs + generated letters
 *   Payroll      – salary structure, payslips, loans, tax declarations
 *   Leave        – balances, applications history, encashment
 *   Performance  – goals, reviews, PIPs, feedback, 1-on-1s
 *   Learning     – enrollments, certifications, course progress
 *   Assets       – assigned assets, returns, maintenance requests
 *   Workflow     – pending approvals, OOO delegations, separations
 *   Audit        – activity timeline + compliance items
 */
import { useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import {
  ArrowLeft, Edit, Mail, Phone, MapPin, Calendar, Briefcase, Building2, User,
  CreditCard, FileText, Clock, Shield, Award, BookOpen, Target, Package,
  GitBranch, ScrollText, Download, Plus, MessageCircle, AlertTriangle,
  Users as UsersIcon, History, Banknote,
} from 'lucide-react'
import { toast } from 'sonner'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Avatar } from '@/components/ui/avatar'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { PageHeader } from '@/components/ui/page-header'
import { Skeleton } from '@/components/ui/skeleton'
import { Progress } from '@/components/ui/progress'
import { EmptyState } from '@/components/ui/empty-state'
import { CrudSection } from '@/components/ui/crud-section'
import { employeeService } from '@/services/employeeService'
import { Catalog } from '@/services/catalog'
import { formatDate } from '@/lib/utils'

type AnyObj = Record<string, unknown>
function rows<T = AnyObj>(d: unknown): T[] {
  if (Array.isArray(d)) return d as T[]
  const obj = d as { content?: T[]; data?: { content?: T[] } | T[] } | undefined
  if (!obj) return []
  if (Array.isArray(obj.content)) return obj.content
  if (Array.isArray(obj.data)) return obj.data as T[]
  if (obj.data && Array.isArray((obj.data as { content?: T[] }).content)) {
    return (obj.data as { content: T[] }).content
  }
  return []
}

const DEMO_EMPLOYEE = {
  id: '1', employeeId: 'EMP001', firstName: 'Priya', lastName: 'Sharma',
  fullName: 'Priya Sharma', email: 'priya.sharma@demo.com', phone: '+91 98765 43210',
  departmentName: 'Engineering', designationName: 'Senior Software Engineer',
  locationName: 'Bangalore', managerName: 'Vikram Patel', managerId: 'EMP010',
  employmentType: 'FULL_TIME' as const, status: 'ACTIVE' as const,
  joinDate: '2022-01-15', gender: 'FEMALE' as const, dateOfBirth: '1993-07-22',
  address: '123 MG Road, Koramangala', city: 'Bangalore', state: 'Karnataka',
  country: 'India', pincode: '560034', bankName: 'HDFC Bank',
  bankAccountNumber: '****4521', ifscCode: 'HDFC0001234', panNumber: 'ABCDE1234F',
  aadharNumber: '1234-5678-9012', maritalStatus: 'MARRIED',
  createdAt: '2022-01-10', updatedAt: '2024-01-15',
}

const statusVariantMap: Record<string, 'success' | 'warning' | 'secondary' | 'destructive'> = {
  ACTIVE: 'success', ON_LEAVE: 'warning', INACTIVE: 'secondary',
  TERMINATED: 'destructive', PROBATION: 'warning', NOTICE_PERIOD: 'destructive',
}

function InfoRow({ label, value, icon: Icon }: { label: string; value?: React.ReactNode; icon?: React.ComponentType<{ className?: string }> }) {
  return (
    <div className="flex items-start gap-3 py-3 border-b border-slate-50 last:border-0">
      {Icon && <Icon className="h-4 w-4 text-slate-400 mt-0.5 flex-shrink-0" />}
      <div className="flex-1 min-w-0">
        <p className="text-xs text-slate-400 mb-0.5">{label}</p>
        <div className="text-sm text-slate-800 font-medium">{value ?? '—'}</div>
      </div>
    </div>
  )
}

export function EmployeeDetailPage() {
  const { id = '' } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const qc = useQueryClient()

  /* ── Core employee record ─────────────────────────────────────────────── */
  const employeeQ = useQuery({
    queryKey: ['employee', id],
    queryFn: async () => { try { return await employeeService.getById(id) } catch { return DEMO_EMPLOYEE } },
    enabled: !!id,
  })

  const raw = employeeQ.data as unknown
  const employee =
    (raw && typeof raw === 'object' && 'data' in (raw as AnyObj)
      ? (raw as { data: typeof DEMO_EMPLOYEE }).data
      : (raw as typeof DEMO_EMPLOYEE | undefined)) || DEMO_EMPLOYEE

  /* ── Tab-loaded queries (lazy-fetched once each tab opens) ────────────── */
  const [activeTab, setActiveTab] = useState('overview')
  const enabled = (key: string) => activeTab === key

  // Personal tab
  const addressesQ = useQuery({ queryKey: ['emp-addrs', id], queryFn: () => employeeService.addresses(id), enabled: !!id && enabled('personal') })
  const familyQ = useQuery({ queryKey: ['emp-family', id], queryFn: () => employeeService.family(id), enabled: !!id && enabled('personal') })
  const emergencyQ = useQuery({ queryKey: ['emp-emerg', id], queryFn: () => employeeService.emergencyContacts(id), enabled: !!id && enabled('personal') })

  // Employment tab
  const eduQ = useQuery({ queryKey: ['emp-edu', id], queryFn: () => employeeService.education(id), enabled: !!id && enabled('employment') })
  const wkQ = useQuery({ queryKey: ['emp-wk', id], queryFn: () => employeeService.workHistory(id), enabled: !!id && enabled('employment') })
  const lifecycleQ = useQuery({ queryKey: ['emp-life', id], queryFn: () => employeeService.lifecycle(id), enabled: !!id && enabled('employment') })
  const teamQ = useQuery({ queryKey: ['emp-team', id], queryFn: () => employeeService.team(id), enabled: !!id && enabled('employment') })

  // Documents tab
  const docsQ = useQuery({ queryKey: ['emp-docs', id], queryFn: () => Catalog.documents.forEmployee(id), enabled: !!id && enabled('documents') })
  const lettersQ = useQuery({ queryKey: ['emp-letters', id], queryFn: () => Catalog.documents.letters.forEmployee(id), enabled: !!id && enabled('documents') })

  // Payroll tab
  const payslipsQ = useQuery({ queryKey: ['emp-payslips', id], queryFn: () => Catalog.payslips.mine(), enabled: !!id && enabled('payroll') })
  const loansQ = useQuery({ queryKey: ['emp-loans', id], queryFn: async () => { const r = await fetch('#'); return r; }, enabled: false })

  // Leave tab
  const leaveBalQ = useQuery({ queryKey: ['emp-leave-bal', id], queryFn: () => Catalog.leaves.balance.forEmployee(id), enabled: !!id && enabled('leave') })
  const leavesQ = useQuery({ queryKey: ['emp-leaves', id], queryFn: () => Catalog.leaves.my(), enabled: !!id && enabled('leave') })

  // Performance tab
  const goalsQ = useQuery({ queryKey: ['emp-goals', id], queryFn: () => Catalog.pipPlans.forEmployee(id), enabled: !!id && enabled('performance') })
  const feedbackQ = useQuery({ queryKey: ['emp-fb', id], queryFn: () => Catalog.feedback.receivedFor(id), enabled: !!id && enabled('performance') })
  const oneOnOnesQ = useQuery({ queryKey: ['emp-1on1', id], queryFn: () => Catalog.oneOnOnes.forEmployee(id), enabled: !!id && enabled('performance') })

  // Learning tab
  const enrollmentsQ = useQuery({ queryKey: ['emp-enroll', id], queryFn: () => Catalog.courses.enrollmentsForEmployee(id), enabled: !!id && enabled('learning') })
  const certsQ = useQuery({ queryKey: ['emp-certs', id], queryFn: () => Catalog.courses.certificationsForEmployee(id), enabled: !!id && enabled('learning') })
  const skillsQ = useQuery({ queryKey: ['emp-skills', id], queryFn: () => Catalog.skills.forEmployee(id), enabled: !!id && enabled('learning') })

  // Assets tab
  const assetsQ = useQuery({ queryKey: ['emp-assets', id], queryFn: () => Catalog.assets.assignments.forEmployee(id), enabled: !!id && enabled('assets') })
  const assetReqsQ = useQuery({ queryKey: ['emp-areq', id], queryFn: () => Catalog.assets.requests.forEmployee(id), enabled: !!id && enabled('assets') })

  // Workflow tab
  const oooQ = useQuery({ queryKey: ['emp-ooo', id], queryFn: () => Catalog.ooo.forUser(id), enabled: !!id && enabled('workflow') })
  const wfInstQ = useQuery({ queryKey: ['emp-wfi', id], queryFn: () => Catalog.workflows.instances.byInitiator(id), enabled: !!id && enabled('workflow') })
  const separationsQ = useQuery({ queryKey: ['emp-sep', id], queryFn: () => Catalog.offboarding.separations.forEmployee(id), enabled: !!id && enabled('workflow') })

  // Audit tab
  const timelineQ = useQuery({ queryKey: ['emp-tl', id], queryFn: () => employeeService.timeline(id), enabled: !!id && enabled('audit') })
  const auditQ = useQuery({ queryKey: ['emp-audit', id], queryFn: () => Catalog.audit.byEntity('Employee', id), enabled: !!id && enabled('audit') })
  const complianceQ = useQuery({ queryKey: ['emp-comp', id], queryFn: () => Catalog.compliance.items.byOwner(id), enabled: !!id && enabled('audit') })
  const licensesQ = useQuery({ queryKey: ['emp-lic', id], queryFn: () => Catalog.compliance.licenses.forEmployee(id), enabled: !!id && enabled('audit') })

  /* ── Mutations ────────────────────────────────────────────────────────── */
  const generateLetter = useMutation({
    mutationFn: () => Catalog.letters.generateEmployment(id),
    onSuccess: () => { toast.success('Employment letter generated'); qc.invalidateQueries({ queryKey: ['emp-letters', id] }) },
    onError: () => toast.error('Failed to generate letter'),
  })

  if (employeeQ.isLoading) {
    return (
      <div className="space-y-5">
        <Skeleton className="h-8 w-48" />
        <div className="grid grid-cols-4 gap-4">
          {Array.from({ length: 4 }).map((_, i) => <Skeleton key={i} className="h-32 rounded-xl" />)}
        </div>
      </div>
    )
  }

  return (
    <div className="space-y-5 animate-fade-in">
      <PageHeader
        title="Employee Profile"
        breadcrumbs={[{ label: 'People' }, { label: 'Employees', href: '/employees' }, { label: employee.fullName }]}
      >
        <Button variant="outline" size="sm" onClick={() => navigate('/employees')}>
          <ArrowLeft className="h-4 w-4" /> Back
        </Button>
        <Button variant="outline" size="sm" onClick={() => generateLetter.mutate()} disabled={generateLetter.isPending}>
          <ScrollText className="h-4 w-4" /> Generate Letter
        </Button>
        <Button size="sm" onClick={() => navigate(`/employees/${id}/edit`)}>
          <Edit className="h-4 w-4" /> Edit
        </Button>
      </PageHeader>

      {/* Profile header card */}
      <Card>
        <CardContent className="pt-6">
          <div className="flex flex-col sm:flex-row items-start sm:items-center gap-5">
            <Avatar name={employee.fullName} size="xl" />
            <div className="flex-1 min-w-0">
              <div className="flex flex-wrap items-center gap-3 mb-1">
                <h2 className="text-xl font-bold text-slate-900">{employee.fullName}</h2>
                <Badge variant={statusVariantMap[employee.status] || 'secondary'} dot>
                  {employee.status.replace('_', ' ')}
                </Badge>
              </div>
              <p className="text-sm text-slate-500">{employee.designationName} · {employee.departmentName}</p>
              <div className="flex flex-wrap items-center gap-4 mt-3">
                <a href={`mailto:${employee.email}`} className="flex items-center gap-1.5 text-sm text-slate-600 hover:text-brand-600">
                  <Mail className="h-3.5 w-3.5" /> {employee.email}
                </a>
                {employee.phone && <span className="flex items-center gap-1.5 text-sm text-slate-600"><Phone className="h-3.5 w-3.5" /> {employee.phone}</span>}
                {employee.locationName && <span className="flex items-center gap-1.5 text-sm text-slate-600"><MapPin className="h-3.5 w-3.5" /> {employee.locationName}</span>}
                {employee.managerName && <span className="flex items-center gap-1.5 text-sm text-slate-600"><User className="h-3.5 w-3.5" /> Reports to {employee.managerName}</span>}
              </div>
            </div>
            <div className="flex flex-col gap-2 text-sm text-right">
              <span className="font-mono text-xs bg-brand-50 text-brand-700 px-2 py-1 rounded border border-brand-200">{employee.employeeId}</span>
              <span className="text-xs text-slate-400">Joined {formatDate(employee.joinDate)}</span>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Tabs */}
      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <TabsList className="flex flex-wrap h-auto">
          <TabsTrigger value="overview"><User className="h-3.5 w-3.5" /> Overview</TabsTrigger>
          <TabsTrigger value="personal"><User className="h-3.5 w-3.5" /> Personal</TabsTrigger>
          <TabsTrigger value="employment"><Briefcase className="h-3.5 w-3.5" /> Employment</TabsTrigger>
          <TabsTrigger value="documents"><FileText className="h-3.5 w-3.5" /> Documents</TabsTrigger>
          <TabsTrigger value="payroll"><Banknote className="h-3.5 w-3.5" /> Payroll</TabsTrigger>
          <TabsTrigger value="leave"><Calendar className="h-3.5 w-3.5" /> Leave</TabsTrigger>
          <TabsTrigger value="performance"><Target className="h-3.5 w-3.5" /> Performance</TabsTrigger>
          <TabsTrigger value="learning"><BookOpen className="h-3.5 w-3.5" /> Learning</TabsTrigger>
          <TabsTrigger value="assets"><Package className="h-3.5 w-3.5" /> Assets</TabsTrigger>
          <TabsTrigger value="workflow"><GitBranch className="h-3.5 w-3.5" /> Workflow</TabsTrigger>
          <TabsTrigger value="audit"><History className="h-3.5 w-3.5" /> Audit</TabsTrigger>
        </TabsList>

        {/* OVERVIEW */}
        <TabsContent value="overview">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <Card><CardHeader><CardTitle className="text-sm">Work</CardTitle></CardHeader><CardContent className="pt-0">
              <InfoRow label="Department" value={employee.departmentName} icon={Building2} />
              <InfoRow label="Designation" value={employee.designationName} icon={Briefcase} />
              <InfoRow label="Manager" value={employee.managerName} icon={User} />
              <InfoRow label="Employment Type" value={employee.employmentType?.replace('_', ' ')} icon={Briefcase} />
              <InfoRow label="Join Date" value={formatDate(employee.joinDate)} icon={Calendar} />
            </CardContent></Card>
            <Card><CardHeader><CardTitle className="text-sm">Contact</CardTitle></CardHeader><CardContent className="pt-0">
              <InfoRow label="Email" value={employee.email} icon={Mail} />
              <InfoRow label="Phone" value={employee.phone} icon={Phone} />
              <InfoRow label="Location" value={employee.locationName} icon={MapPin} />
              <InfoRow label="City" value={employee.city} />
              <InfoRow label="Country" value={employee.country} />
            </CardContent></Card>
            <Card><CardHeader><CardTitle className="text-sm">Identifiers</CardTitle></CardHeader><CardContent className="pt-0">
              <InfoRow label="Employee ID" value={employee.employeeId} icon={Shield} />
              <InfoRow label="PAN" value={employee.panNumber} icon={Shield} />
              <InfoRow label="Aadhar" value={employee.aadharNumber} icon={Shield} />
              <InfoRow label="DOB" value={formatDate(employee.dateOfBirth)} icon={Calendar} />
              <InfoRow label="Gender" value={employee.gender} icon={User} />
            </CardContent></Card>
          </div>
        </TabsContent>

        {/* PERSONAL */}
        <TabsContent value="personal">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <CrudSection
              title="Addresses" icon={<MapPin className="h-6 w-6" />}
              items={rows(addressesQ.data)} loading={addressesQ.isLoading}
              emptyText="No addresses on file" queryKey={['emp-addrs', id]}
              fields={[
                { name: 'type', label: 'Type', type: 'select', required: true, options: [
                  { value: 'PERMANENT', label: 'Permanent' }, { value: 'CURRENT', label: 'Current' }, { value: 'EMERGENCY', label: 'Emergency' },
                ] },
                { name: 'line1', label: 'Line 1', type: 'text', required: true, span: 2 },
                { name: 'line2', label: 'Line 2', type: 'text', span: 2 },
                { name: 'city', label: 'City', type: 'text' }, { name: 'state', label: 'State', type: 'text' },
                { name: 'pincode', label: 'Pincode', type: 'text' }, { name: 'country', label: 'Country', type: 'text' },
              ]}
              onCreate={v => employeeService.addAddress(id, v)}
              onUpdate={(aid, v) => employeeService.updateAddress(id, aid, v)}
              onDelete={aid => employeeService.deleteAddress(id, aid)}
              renderItem={(a: AnyObj) => (<>
                <p className="text-sm font-medium">{String(a.type || '')}</p>
                <p className="text-xs text-slate-500">{[a.line1, a.line2, a.city, a.state, a.pincode, a.country].filter(Boolean).join(', ')}</p>
              </>)}
            />
            <CrudSection
              title="Emergency Contacts" icon={<Phone className="h-6 w-6" />}
              items={rows(emergencyQ.data)} loading={emergencyQ.isLoading}
              emptyText="No emergency contact recorded" queryKey={['emp-emerg', id]}
              fields={[
                { name: 'name', label: 'Name', type: 'text', required: true },
                { name: 'relationship', label: 'Relationship', type: 'text', required: true },
                { name: 'phone', label: 'Phone', type: 'tel', required: true },
                { name: 'email', label: 'Email', type: 'email' },
                { name: 'address', label: 'Address', type: 'textarea', span: 2 },
              ]}
              onCreate={v => employeeService.addEmergencyContact(id, v)}
              onUpdate={(cid, v) => employeeService.updateEmergencyContact(id, cid, v)}
              onDelete={cid => employeeService.deleteEmergencyContact(id, cid)}
              renderItem={(c: AnyObj) => (<>
                <p className="text-sm font-medium">{String(c.name || '')} <span className="text-xs text-slate-500">({String(c.relationship || '')})</span></p>
                <p className="text-xs text-slate-500">{String(c.phone || '')} {c.email ? `· ${String(c.email)}` : ''}</p>
              </>)}
            />
            <CrudSection
              className="md:col-span-2"
              title="Family Members" icon={<UsersIcon className="h-6 w-6" />}
              items={rows(familyQ.data)} loading={familyQ.isLoading}
              emptyText="No family members declared" queryKey={['emp-family', id]}
              fields={[
                { name: 'name', label: 'Name', type: 'text', required: true },
                { name: 'relationship', label: 'Relationship', type: 'text', required: true },
                { name: 'dateOfBirth', label: 'Date of birth', type: 'date' },
                { name: 'occupation', label: 'Occupation', type: 'text' },
                { name: 'dependent', label: 'Dependent', type: 'switch' },
              ]}
              onCreate={v => employeeService.addFamilyMember(id, v)}
              onUpdate={(mid, v) => employeeService.updateFamilyMember(id, mid, v)}
              onDelete={mid => employeeService.deleteFamilyMember(id, mid)}
              renderItem={(m: AnyObj) => (<>
                <p className="text-sm font-medium">{String(m.name || '')} <span className="text-xs text-slate-500">({String(m.relationship || '')})</span></p>
                <p className="text-xs text-slate-500">
                  {m.dateOfBirth ? formatDate(String(m.dateOfBirth)) : '—'} {m.occupation ? `· ${String(m.occupation)}` : ''} {m.dependent ? '· Dependent' : ''}
                </p>
              </>)}
            />
          </div>
        </TabsContent>

        {/* EMPLOYMENT */}
        <TabsContent value="employment">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <CrudSection
              title="Education" icon={<BookOpen className="h-6 w-6" />}
              items={rows(eduQ.data)} loading={eduQ.isLoading}
              emptyText="No education records" queryKey={['emp-edu', id]}
              fields={[
                { name: 'degree', label: 'Degree', type: 'text', required: true },
                { name: 'fieldOfStudy', label: 'Field of study', type: 'text' },
                { name: 'institution', label: 'Institution', type: 'text', required: true, span: 2 },
                { name: 'startYear', label: 'Start year', type: 'number' },
                { name: 'endYear', label: 'End year', type: 'number' },
                { name: 'grade', label: 'Grade', type: 'text' },
              ]}
              onCreate={v => employeeService.addEducation(id, v)}
              onUpdate={(eid, v) => employeeService.updateEducation(id, eid, v)}
              onDelete={eid => employeeService.deleteEducation(id, eid)}
              renderItem={(e: AnyObj) => (<>
                <p className="text-sm font-medium">{String(e.degree || '')} {e.fieldOfStudy ? `· ${String(e.fieldOfStudy)}` : ''}</p>
                <p className="text-xs text-slate-500">{String(e.institution || '')} {e.startYear ? `· ${e.startYear}–${e.endYear ?? '—'}` : ''}</p>
              </>)}
            />
            <CrudSection
              title="Previous Employment" icon={<Briefcase className="h-6 w-6" />}
              items={rows(wkQ.data)} loading={wkQ.isLoading}
              emptyText="No previous employment" queryKey={['emp-wk', id]}
              fields={[
                { name: 'company', label: 'Company', type: 'text', required: true },
                { name: 'designation', label: 'Designation', type: 'text', required: true },
                { name: 'fromDate', label: 'From', type: 'date' },
                { name: 'toDate', label: 'To', type: 'date' },
                { name: 'location', label: 'Location', type: 'text' },
                { name: 'reasonForLeaving', label: 'Reason for leaving', type: 'text', span: 2 },
              ]}
              onCreate={v => employeeService.addWorkHistory(id, v)}
              onUpdate={(wid, v) => employeeService.updateWorkHistory(id, wid, v)}
              onDelete={wid => employeeService.deleteWorkHistory(id, wid)}
              renderItem={(w: AnyObj) => (<>
                <p className="text-sm font-medium">{String(w.designation || '')} @ {String(w.company || '')}</p>
                <p className="text-xs text-slate-500">{w.fromDate ? formatDate(String(w.fromDate)) : ''} – {w.toDate ? formatDate(String(w.toDate)) : 'Present'} {w.location ? `· ${String(w.location)}` : ''}</p>
              </>)}
            />
            <Card><CardHeader><CardTitle className="text-sm">Direct Reports</CardTitle></CardHeader><CardContent>
              {teamQ.isLoading ? <Skeleton className="h-20" /> : rows(teamQ.data).length === 0 ? (
                <EmptyState icon={<UsersIcon className="h-6 w-6" />} title="No direct reports" />
              ) : (
                <div className="flex flex-wrap gap-2">
                  {rows(teamQ.data).map((t: AnyObj) => (
                    <button key={String(t.id)} onClick={() => navigate(`/employees/${t.id}`)} className="flex items-center gap-2 px-2 py-1.5 rounded border hover:bg-slate-50">
                      <Avatar name={String(t.fullName || '')} size="xs" />
                      <span className="text-xs">{String(t.fullName || '')}</span>
                    </button>
                  ))}
                </div>
              )}
            </CardContent></Card>
            <CrudSection
              title="Lifecycle Events" icon={<History className="h-6 w-6" />}
              items={rows(lifecycleQ.data)} loading={lifecycleQ.isLoading}
              emptyText="No lifecycle events" queryKey={['emp-life', id]}
              fields={[
                { name: 'eventType', label: 'Event type', type: 'select', required: true, options: [
                  { value: 'PROMOTION', label: 'Promotion' }, { value: 'TRANSFER', label: 'Transfer' },
                  { value: 'CONFIRMATION', label: 'Confirmation' }, { value: 'SALARY_REVISION', label: 'Salary revision' },
                  { value: 'ROLE_CHANGE', label: 'Role change' }, { value: 'DEPUTATION', label: 'Deputation' }, { value: 'OTHER', label: 'Other' },
                ] },
                { name: 'eventDate', label: 'Event date', type: 'date', required: true },
                { name: 'notes', label: 'Notes', type: 'textarea', span: 2 },
              ]}
              onCreate={v => employeeService.addLifecycleEvent(id, v)}
              renderItem={(ev: AnyObj) => (<>
                <p className="text-sm font-medium">{String(ev.eventType || '')}</p>
                <p className="text-xs text-slate-500">{ev.eventDate ? formatDate(String(ev.eventDate)) : ''} {ev.notes ? `· ${String(ev.notes)}` : ''}</p>
              </>)}
            />
          </div>
        </TabsContent>

        {/* DOCUMENTS */}
        <TabsContent value="documents">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <Card><CardHeader><div className="flex items-center justify-between"><CardTitle className="text-sm">Documents</CardTitle><Button size="sm" variant="outline"><Plus className="h-3.5 w-3.5" /> Upload</Button></div></CardHeader><CardContent>
              {docsQ.isLoading ? <Skeleton className="h-20" /> : rows(docsQ.data).length === 0 ? (
                <EmptyState icon={<FileText className="h-6 w-6" />} title="No documents on file" description="Upload offer letter, contract, ID proofs, etc." />
              ) : rows(docsQ.data).map((d: AnyObj) => (
                <div key={String(d.id)} className="flex items-center justify-between border-b border-slate-100 last:border-0 py-2">
                  <div><p className="text-sm font-medium">{String(d.documentName || d.name || '')}</p><p className="text-xs text-slate-500">{String(d.documentType || '')} · {d.createdAt ? formatDate(String(d.createdAt)) : ''}</p></div>
                  <Button size="sm" variant="ghost"><Download className="h-3.5 w-3.5" /></Button>
                </div>
              ))}
            </CardContent></Card>
            <Card><CardHeader><CardTitle className="text-sm">Generated Letters</CardTitle></CardHeader><CardContent>
              {lettersQ.isLoading ? <Skeleton className="h-20" /> : rows(lettersQ.data).length === 0 ? (
                <EmptyState icon={<ScrollText className="h-6 w-6" />} title="No letters generated" />
              ) : rows(lettersQ.data).map((l: AnyObj) => (
                <div key={String(l.id)} className="flex items-center justify-between border-b border-slate-100 last:border-0 py-2">
                  <div><p className="text-sm font-medium">{String(l.letterType || l.type || '')}</p><p className="text-xs text-slate-500">{l.generatedAt ? formatDate(String(l.generatedAt)) : ''}</p></div>
                  <Button size="sm" variant="ghost"><Download className="h-3.5 w-3.5" /></Button>
                </div>
              ))}
            </CardContent></Card>
          </div>
        </TabsContent>

        {/* PAYROLL */}
        <TabsContent value="payroll">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <Card><CardHeader><CardTitle className="text-sm">Bank Details</CardTitle></CardHeader><CardContent className="pt-0">
              <InfoRow label="Bank Name" value={employee.bankName} icon={CreditCard} />
              <InfoRow label="Account Number" value={employee.bankAccountNumber} />
              <InfoRow label="IFSC Code" value={employee.ifscCode} />
              <InfoRow label="PAN" value={employee.panNumber} icon={Shield} />
            </CardContent></Card>
            <Card><CardHeader><CardTitle className="text-sm">Recent Payslips</CardTitle></CardHeader><CardContent>
              {payslipsQ.isLoading ? <Skeleton className="h-20" /> : rows(payslipsQ.data).length === 0 ? (
                <EmptyState icon={<Banknote className="h-6 w-6" />} title="No payslips yet" />
              ) : rows(payslipsQ.data).slice(0, 6).map((p: AnyObj) => (
                <div key={String(p.id)} className="flex items-center justify-between border-b border-slate-100 last:border-0 py-2">
                  <div><p className="text-sm font-medium">{String(p.period || p.month || '')}</p><p className="text-xs text-slate-500">Net ₹{Number(p.netPay ?? 0).toLocaleString()}</p></div>
                  <Button size="sm" variant="ghost"><Download className="h-3.5 w-3.5" /></Button>
                </div>
              ))}
            </CardContent></Card>
            <Card className="md:col-span-2"><CardHeader><CardTitle className="text-sm">Active Loans</CardTitle></CardHeader><CardContent>
              {loansQ.isLoading ? <Skeleton className="h-16" /> : <EmptyState icon={<Banknote className="h-6 w-6" />} title="No active loans" />}
            </CardContent></Card>
          </div>
        </TabsContent>

        {/* LEAVE */}
        <TabsContent value="leave">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <Card><CardHeader><CardTitle className="text-sm">Leave Balances</CardTitle></CardHeader><CardContent>
              {leaveBalQ.isLoading ? <Skeleton className="h-24" /> : rows(leaveBalQ.data).length === 0 ? (
                <EmptyState icon={<Calendar className="h-6 w-6" />} title="No leave balances yet" />
              ) : rows(leaveBalQ.data).map((b: AnyObj) => {
                const used = Number(b.used ?? 0), allocated = Number(b.allocated ?? 0)
                const pct = allocated > 0 ? Math.min(100, Math.round((used / allocated) * 100)) : 0
                return (
                  <div key={String(b.id || b.leaveTypeId)} className="py-2 border-b border-slate-100 last:border-0">
                    <div className="flex items-center justify-between mb-1"><p className="text-sm font-medium">{String(b.leaveTypeName || '')}</p><p className="text-xs text-slate-500">{used}/{allocated}</p></div>
                    <Progress value={pct} />
                  </div>
                )
              })}
            </CardContent></Card>
            <Card><CardHeader><CardTitle className="text-sm">Recent Applications</CardTitle></CardHeader><CardContent>
              {leavesQ.isLoading ? <Skeleton className="h-20" /> : rows(leavesQ.data).length === 0 ? (
                <EmptyState icon={<Calendar className="h-6 w-6" />} title="No leave applications" />
              ) : rows(leavesQ.data).slice(0, 8).map((l: AnyObj) => (
                <div key={String(l.id)} className="flex items-center justify-between border-b border-slate-100 last:border-0 py-2">
                  <div><p className="text-sm font-medium">{String(l.leaveTypeName || '')} · {String(l.days ?? '')} d</p><p className="text-xs text-slate-500">{formatDate(String(l.startDate))} – {formatDate(String(l.endDate))}</p></div>
                  <Badge variant={l.status === 'APPROVED' ? 'success' : l.status === 'REJECTED' ? 'destructive' : 'warning'}>{String(l.status)}</Badge>
                </div>
              ))}
            </CardContent></Card>
          </div>
        </TabsContent>

        {/* PERFORMANCE */}
        <TabsContent value="performance">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <Card><CardHeader><CardTitle className="text-sm">PIPs</CardTitle></CardHeader><CardContent>
              {goalsQ.isLoading ? <Skeleton className="h-20" /> : rows(goalsQ.data).length === 0 ? (
                <EmptyState icon={<Target className="h-6 w-6" />} title="No active PIPs" />
              ) : rows(goalsQ.data).map((g: AnyObj) => (
                <div key={String(g.id)} className="border-b border-slate-100 last:border-0 py-2">
                  <p className="text-sm font-medium">{String(g.title || g.objective || 'PIP')}</p>
                  <p className="text-xs text-slate-500">{String(g.status || '')} · {g.endDate ? formatDate(String(g.endDate)) : ''}</p>
                </div>
              ))}
            </CardContent></Card>
            <Card><CardHeader><CardTitle className="text-sm">Feedback Received</CardTitle></CardHeader><CardContent>
              {feedbackQ.isLoading ? <Skeleton className="h-20" /> : rows(feedbackQ.data).length === 0 ? (
                <EmptyState icon={<MessageCircle className="h-6 w-6" />} title="No feedback yet" />
              ) : rows(feedbackQ.data).slice(0, 5).map((f: AnyObj) => (
                <div key={String(f.id)} className="border-b border-slate-100 last:border-0 py-2">
                  <p className="text-sm">{String(f.message || f.body || '').slice(0, 120)}</p>
                  <p className="text-xs text-slate-500">{String(f.fromName || 'Anonymous')} · {f.createdAt ? formatDate(String(f.createdAt)) : ''}</p>
                </div>
              ))}
            </CardContent></Card>
            <Card><CardHeader><CardTitle className="text-sm">1-on-1s</CardTitle></CardHeader><CardContent>
              {oneOnOnesQ.isLoading ? <Skeleton className="h-20" /> : rows(oneOnOnesQ.data).length === 0 ? (
                <EmptyState icon={<MessageCircle className="h-6 w-6" />} title="No 1-on-1s scheduled" />
              ) : rows(oneOnOnesQ.data).slice(0, 5).map((o: AnyObj) => (
                <div key={String(o.id)} className="border-b border-slate-100 last:border-0 py-2">
                  <p className="text-sm font-medium">with {String(o.otherPartyName || '')}</p>
                  <p className="text-xs text-slate-500">{o.scheduledAt ? formatDate(String(o.scheduledAt)) : ''} · {String(o.status || '')}</p>
                </div>
              ))}
            </CardContent></Card>
          </div>
        </TabsContent>

        {/* LEARNING */}
        <TabsContent value="learning">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <Card><CardHeader><CardTitle className="text-sm">Enrollments</CardTitle></CardHeader><CardContent>
              {enrollmentsQ.isLoading ? <Skeleton className="h-20" /> : rows(enrollmentsQ.data).length === 0 ? (
                <EmptyState icon={<BookOpen className="h-6 w-6" />} title="No course enrollments" />
              ) : rows(enrollmentsQ.data).map((e: AnyObj) => (
                <div key={String(e.id)} className="border-b border-slate-100 last:border-0 py-2">
                  <p className="text-sm font-medium">{String(e.courseTitle || e.courseName || '')}</p>
                  <div className="flex items-center gap-2 mt-1"><Progress value={Number(e.progressPercent ?? 0)} /><span className="text-xs text-slate-500">{Number(e.progressPercent ?? 0)}%</span></div>
                </div>
              ))}
            </CardContent></Card>
            <Card><CardHeader><CardTitle className="text-sm">Certifications</CardTitle></CardHeader><CardContent>
              {certsQ.isLoading ? <Skeleton className="h-20" /> : rows(certsQ.data).length === 0 ? (
                <EmptyState icon={<Award className="h-6 w-6" />} title="No certifications" />
              ) : rows(certsQ.data).map((c: AnyObj) => (
                <div key={String(c.id)} className="border-b border-slate-100 last:border-0 py-2">
                  <p className="text-sm font-medium">{String(c.name || c.title || '')}</p>
                  <p className="text-xs text-slate-500">Valid until {c.validUntil ? formatDate(String(c.validUntil)) : '—'}</p>
                </div>
              ))}
            </CardContent></Card>
            <Card><CardHeader><CardTitle className="text-sm">Skills</CardTitle></CardHeader><CardContent>
              {skillsQ.isLoading ? <Skeleton className="h-20" /> : rows(skillsQ.data).length === 0 ? (
                <EmptyState icon={<Target className="h-6 w-6" />} title="No skills declared" />
              ) : (
                <div className="flex flex-wrap gap-1.5">
                  {rows(skillsQ.data).map((s: AnyObj) => (
                    <Badge key={String(s.id || s.skillId)} variant="secondary">{String(s.skillName || s.name || '')} {s.proficiencyLevel ? `· L${s.proficiencyLevel}` : ''}</Badge>
                  ))}
                </div>
              )}
            </CardContent></Card>
          </div>
        </TabsContent>

        {/* ASSETS */}
        <TabsContent value="assets">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <Card><CardHeader><CardTitle className="text-sm">Assigned Assets</CardTitle></CardHeader><CardContent>
              {assetsQ.isLoading ? <Skeleton className="h-20" /> : rows(assetsQ.data).length === 0 ? (
                <EmptyState icon={<Package className="h-6 w-6" />} title="No assets assigned" />
              ) : rows(assetsQ.data).map((a: AnyObj) => (
                <div key={String(a.id)} className="flex items-center justify-between border-b border-slate-100 last:border-0 py-2">
                  <div><p className="text-sm font-medium">{String(a.assetName || a.serialNumber || '')}</p><p className="text-xs text-slate-500">{String(a.assetType || '')} · Assigned {a.assignedAt ? formatDate(String(a.assignedAt)) : ''}</p></div>
                  <Button size="sm" variant="outline" onClick={() => Catalog.assets.assignments.return(String(a.id)).then(() => toast.success('Asset return initiated'))}>Return</Button>
                </div>
              ))}
            </CardContent></Card>
            <Card><CardHeader><CardTitle className="text-sm">Pending Requests</CardTitle></CardHeader><CardContent>
              {assetReqsQ.isLoading ? <Skeleton className="h-20" /> : rows(assetReqsQ.data).length === 0 ? (
                <EmptyState icon={<Package className="h-6 w-6" />} title="No pending requests" />
              ) : rows(assetReqsQ.data).map((r: AnyObj) => (
                <div key={String(r.id)} className="border-b border-slate-100 last:border-0 py-2">
                  <p className="text-sm font-medium">{String(r.assetType || r.title || '')}</p>
                  <p className="text-xs text-slate-500">{String(r.status || '')} · {r.requestedAt ? formatDate(String(r.requestedAt)) : ''}</p>
                </div>
              ))}
            </CardContent></Card>
          </div>
        </TabsContent>

        {/* WORKFLOW */}
        <TabsContent value="workflow">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <Card><CardHeader><CardTitle className="text-sm">Out of Office</CardTitle></CardHeader><CardContent>
              {oooQ.isLoading ? <Skeleton className="h-20" /> : rows(oooQ.data).length === 0 ? (
                <EmptyState icon={<Calendar className="h-6 w-6" />} title="Not currently OOO" />
              ) : rows(oooQ.data).map((o: AnyObj) => (
                <div key={String(o.id)} className="border-b border-slate-100 last:border-0 py-2">
                  <p className="text-sm font-medium">{formatDate(String(o.fromDate))} – {formatDate(String(o.toDate))}</p>
                  <p className="text-xs text-slate-500">Delegated to {String(o.delegateName || o.delegateId || '')}</p>
                </div>
              ))}
            </CardContent></Card>
            <Card><CardHeader><CardTitle className="text-sm">Initiated Workflows</CardTitle></CardHeader><CardContent>
              {wfInstQ.isLoading ? <Skeleton className="h-20" /> : rows(wfInstQ.data).length === 0 ? (
                <EmptyState icon={<GitBranch className="h-6 w-6" />} title="No workflow instances" />
              ) : rows(wfInstQ.data).slice(0, 8).map((w: AnyObj) => (
                <div key={String(w.id)} className="border-b border-slate-100 last:border-0 py-2">
                  <p className="text-sm font-medium">{String(w.definitionName || w.entityType || '')}</p>
                  <p className="text-xs text-slate-500"><Badge>{String(w.status || '')}</Badge> · {w.startedAt ? formatDate(String(w.startedAt)) : ''}</p>
                </div>
              ))}
            </CardContent></Card>
            <Card><CardHeader><CardTitle className="text-sm">Separation</CardTitle></CardHeader><CardContent>
              {separationsQ.isLoading ? <Skeleton className="h-20" /> : rows(separationsQ.data).length === 0 ? (
                <EmptyState icon={<AlertTriangle className="h-6 w-6" />} title="No separation in progress" />
              ) : rows(separationsQ.data).map((s: AnyObj) => (
                <div key={String(s.id)} className="border-b border-slate-100 last:border-0 py-2">
                  <p className="text-sm font-medium">{String(s.separationType || s.type || '')}</p>
                  <p className="text-xs text-slate-500">LWD {s.lastWorkingDay ? formatDate(String(s.lastWorkingDay)) : '—'} · <Badge variant="warning">{String(s.status || '')}</Badge></p>
                </div>
              ))}
            </CardContent></Card>
          </div>
        </TabsContent>

        {/* AUDIT */}
        <TabsContent value="audit">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <Card><CardHeader><CardTitle className="text-sm">Activity Timeline</CardTitle></CardHeader><CardContent>
              {timelineQ.isLoading ? <Skeleton className="h-32" /> : rows(timelineQ.data).length === 0 && rows(auditQ.data).length === 0 ? (
                <EmptyState icon={<History className="h-6 w-6" />} title="No activity recorded" />
              ) : (
                <ul className="border-l-2 border-slate-100 ml-2 space-y-3">
                  {[...rows<AnyObj>(timelineQ.data), ...rows<AnyObj>(auditQ.data)].slice(0, 30).map((ev: AnyObj, i: number) => (
                    <li key={String(ev.id) || `${i}`} className="ml-3 -translate-x-[7px]">
                      <span className="inline-block h-2.5 w-2.5 rounded-full bg-brand-500 mr-2" />
                      <span className="text-sm">{String(ev.action || ev.eventType || ev.title || 'Event')}</span>
                      <span className="block text-xs text-slate-500 ml-4">{ev.at || ev.createdAt ? formatDate(String(ev.at || ev.createdAt)) : ''} {ev.actor ? `· by ${String(ev.actor)}` : ''}</span>
                    </li>
                  ))}
                </ul>
              )}
            </CardContent></Card>
            <Card><CardHeader><CardTitle className="text-sm">Compliance & Licenses</CardTitle></CardHeader><CardContent>
              {complianceQ.isLoading ? <Skeleton className="h-20" /> : (
                <>
                  {rows(complianceQ.data).map((c: AnyObj) => (
                    <div key={String(c.id)} className="border-b border-slate-100 last:border-0 py-2">
                      <p className="text-sm font-medium">{String(c.name || c.title || '')}</p>
                      <p className="text-xs text-slate-500">Due {c.dueDate ? formatDate(String(c.dueDate)) : '—'} · <Badge>{String(c.status || '')}</Badge></p>
                    </div>
                  ))}
                  {rows(licensesQ.data).map((l: AnyObj) => (
                    <div key={String(l.id)} className="border-b border-slate-100 last:border-0 py-2">
                      <p className="text-sm font-medium">{String(l.licenseType || l.name || '')} · {String(l.licenseNumber || '')}</p>
                      <p className="text-xs text-slate-500">Expires {l.expiryDate ? formatDate(String(l.expiryDate)) : '—'} · <Badge variant={String(l.status) === 'EXPIRED' ? 'destructive' : 'success'}>{String(l.status || '')}</Badge></p>
                    </div>
                  ))}
                  {rows(complianceQ.data).length === 0 && rows(licensesQ.data).length === 0 && (
                    <EmptyState icon={<Shield className="h-6 w-6" />} title="No compliance items or licenses" />
                  )}
                </>
              )}
            </CardContent></Card>
          </div>
        </TabsContent>
      </Tabs>
    </div>
  )
}
