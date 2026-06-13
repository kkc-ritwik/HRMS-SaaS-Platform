import { DollarSign } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { ResourcePage } from '@/components/ui/resource-page'
import { salaryComponentService, type SalaryComponent } from '@/services/salaryService'

export function SalaryComponentsPage() {
  return (
    <ResourcePage<SalaryComponent & Record<string, unknown>>
      title="Salary Components"
      description="Earnings and deductions used in salary structures"
      icon={<DollarSign className="h-10 w-10" />}
      queryKey={['salary-components']}
      fetcher={() => salaryComponentService.list()}
      filters={{ type: ['EARNING', 'DEDUCTION', 'REIMBURSEMENT', 'STATUTORY'] }}
      columns={[
        { key: 'code', label: 'Code' },
        { key: 'name', label: 'Name' },
        { key: 'type', label: 'Type', render: c => <Badge>{String(c.type)}</Badge> },
        { key: 'calculationType', label: 'Calculation' },
        { key: 'taxable', label: 'Taxable', render: c => c.taxable ? <Badge>Taxable</Badge> : '—' },
      ]}
      formFields={[
        { name: 'code', label: 'Code', type: 'text', required: true },
        { name: 'name', label: 'Name', type: 'text', required: true },
        { name: 'type', label: 'Type', type: 'select', required: true, options: [
          { value: 'EARNING', label: 'Earning' }, { value: 'DEDUCTION', label: 'Deduction' },
          { value: 'REIMBURSEMENT', label: 'Reimbursement' }, { value: 'STATUTORY', label: 'Statutory' },
        ] },
        { name: 'calculationType', label: 'Calculation', type: 'select', options: [
          { value: 'FIXED', label: 'Fixed amount' }, { value: 'PERCENT_OF_BASIC', label: '% of basic' },
          { value: 'PERCENT_OF_CTC', label: '% of CTC' }, { value: 'FORMULA', label: 'Formula' },
        ] },
        { name: 'defaultValue', label: 'Default value', type: 'number' },
        { name: 'taxable', label: 'Taxable', type: 'switch' },
      ]}
      onCreate={v => salaryComponentService.create(v)}
      onUpdate={(id, v) => salaryComponentService.update(id, v)}
      onDelete={id => salaryComponentService.remove(id)}
    />
  )
}
