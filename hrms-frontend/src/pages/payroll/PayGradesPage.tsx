import { useQuery } from '@tanstack/react-query'
import { Award } from 'lucide-react'
import { DataList } from '@/components/ui/data-list'
import { compensationService, type PayGrade } from '@/services/compensationService'

export function PayGradesPage() {
  const { data, isLoading } = useQuery({ queryKey: ['paygrades'], queryFn: compensationService.listGrades })
  return (
    <DataList<PayGrade>
      title="Pay Grades" description="Salary grade matrix"
      data={data || []} isLoading={isLoading}
      emptyIcon={<Award className="h-10 w-10" />} emptyTitle="No pay grades"
      columns={[
        { key: 'code', label: 'Code' },
        { key: 'name', label: 'Name' },
        { key: 'level', label: 'Level' },
        { key: 'minSalary', label: 'Min', align: 'right' },
        { key: 'midSalary', label: 'Mid', align: 'right' },
        { key: 'maxSalary', label: 'Max', align: 'right' },
      ]}
    />
  )
}
