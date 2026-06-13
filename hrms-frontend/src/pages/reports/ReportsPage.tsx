import { BarChart3, Play } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import { Badge } from '@/components/ui/badge'
import { ResourcePage } from '@/components/ui/resource-page'
import { Catalog } from '@/services/catalog'
import type { ReportDefinition } from '@/services/reportsService'

export function ReportsPage() {
  const navigate = useNavigate()
  return (
    <ResourcePage<ReportDefinition & Record<string, unknown>>
      title="Reports & Analytics"
      description="Define, run, export and schedule reports across the platform"
      icon={<BarChart3 className="h-10 w-10" />}
      queryKey={['report-definitions']}
      fetcher={() => Catalog.reports.definitions.list()}
      rowHref={r => `/reports/${r.id}`}
      filters={{ category: ['HR', 'PAYROLL', 'ATTENDANCE', 'RECRUITMENT', 'PERFORMANCE', 'FINANCE', 'COMPLIANCE', 'CUSTOM'] }}
      columns={[
        { key: 'name', label: 'Report' },
        { key: 'category', label: 'Category', render: r => r.category ? <Badge>{String(r.category)}</Badge> : '—' },
        { key: 'code', label: 'Code' },
        { key: 'active', label: 'Active', render: r => <Badge variant={r.active === false ? 'secondary' : 'success'}>{r.active === false ? 'Inactive' : 'Active'}</Badge> },
      ]}
      formFields={[
        { name: 'name', label: 'Report name', type: 'text', required: true, span: 2 },
        { name: 'code', label: 'Code', type: 'text', required: true },
        { name: 'category', label: 'Category', type: 'select', options: [
          { value: 'HR', label: 'HR' }, { value: 'PAYROLL', label: 'Payroll' }, { value: 'ATTENDANCE', label: 'Attendance' },
          { value: 'RECRUITMENT', label: 'Recruitment' }, { value: 'PERFORMANCE', label: 'Performance' },
          { value: 'FINANCE', label: 'Finance' }, { value: 'COMPLIANCE', label: 'Compliance' }, { value: 'CUSTOM', label: 'Custom' },
        ] },
        { name: 'description', label: 'Description', type: 'textarea', span: 2 },
        { name: 'queryConfig', label: 'SQL query', type: 'textarea', span: 2, helper: 'The SELECT statement this report runs' },
      ]}
      createTitle="New report definition"
      onCreate={v => Catalog.reports.definitions.create({ ...v, queryConfig: v.queryConfig ? [String(v.queryConfig)] : [] })}
      onUpdate={(id, v) => Catalog.reports.definitions.update(id, { ...v, queryConfig: v.queryConfig ? [String(v.queryConfig)] : undefined })}
      onDelete={id => Catalog.reports.definitions.delete(id)}
      rowActions={r => [
        { label: 'Open & run', icon: <Play className="h-3.5 w-3.5" />, run: () => { navigate(`/reports/${r.id}`); return Promise.resolve() } },
      ]}
    />
  )
}
