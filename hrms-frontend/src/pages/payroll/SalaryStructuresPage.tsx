import { DollarSign } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { ResourcePage } from '@/components/ui/resource-page'
import { salaryStructureService } from '@/services/salaryService'
import type { SalaryStructure } from '@/services/payrollService'

export function SalaryStructuresPage() {
  return (
    <ResourcePage<SalaryStructure & Record<string, unknown>>
      title="Salary Structures"
      description="Templates of earnings + deductions assigned to employees"
      icon={<DollarSign className="h-10 w-10" />}
      queryKey={['salary-structures']}
      fetcher={() => salaryStructureService.list()}
      columns={[
        { key: 'name', label: 'Name' },
        { key: 'description', label: 'Description' },
        { key: 'components', label: 'Components', render: s => <Badge>{(s.components as unknown[] | undefined)?.length ?? 0}</Badge> },
        { key: 'active', label: 'Active', render: s => <Badge variant={s.active === false ? 'secondary' : 'success'}>{s.active === false ? 'Inactive' : 'Active'}</Badge> },
      ]}
      formFields={[
        { name: 'name', label: 'Name', type: 'text', required: true, span: 2 },
        { name: 'description', label: 'Description', type: 'textarea', span: 2 },
        { name: 'effectiveFrom', label: 'Effective from', type: 'date' },
      ]}
      onCreate={v => salaryStructureService.create(v)}
      onUpdate={(id, v) => salaryStructureService.update(id, v)}
    />
  )
}
