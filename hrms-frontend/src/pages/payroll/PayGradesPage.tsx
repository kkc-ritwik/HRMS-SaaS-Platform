import { Award } from 'lucide-react'
import { ResourcePage } from '@/components/ui/resource-page'
import { Catalog } from '@/services/catalog'
import type { PayGrade } from '@/services/compensationService'

export function PayGradesPage() {
  return (
    <ResourcePage<PayGrade & Record<string, unknown>>
      title="Pay Grades"
      description="Salary grade matrix with min/mid/max bands"
      icon={<Award className="h-10 w-10" />}
      queryKey={['paygrades']}
      fetcher={() => Catalog.compensation.payGrades.list()}
      columns={[
        { key: 'code', label: 'Code' },
        { key: 'name', label: 'Name' },
        { key: 'level', label: 'Level' },
        { key: 'minSalary', label: 'Min', align: 'right', render: g => g.minSalary ? `₹${Number(g.minSalary).toLocaleString()}` : '—' },
        { key: 'midSalary', label: 'Mid', align: 'right', render: g => g.midSalary ? `₹${Number(g.midSalary).toLocaleString()}` : '—' },
        { key: 'maxSalary', label: 'Max', align: 'right', render: g => g.maxSalary ? `₹${Number(g.maxSalary).toLocaleString()}` : '—' },
      ]}
      formFields={[
        { name: 'code', label: 'Code', type: 'text', required: true },
        { name: 'name', label: 'Name', type: 'text', required: true },
        { name: 'level', label: 'Level', type: 'number' },
        { name: 'minSalary', label: 'Min salary', type: 'currency', required: true },
        { name: 'midSalary', label: 'Mid salary', type: 'currency' },
        { name: 'maxSalary', label: 'Max salary', type: 'currency', required: true },
        { name: 'currency', label: 'Currency', type: 'text', defaultValue: 'INR' },
      ]}
      onCreate={v => Catalog.compensation.payGrades.create(v)}
      onUpdate={(id, v) => Catalog.compensation.payGrades.update(id, v)}
      onDelete={id => Catalog.compensation.payGrades.delete(id)}
    />
  )
}
