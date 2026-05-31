import { useQuery } from '@tanstack/react-query'
import { DollarSign } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { DataList } from '@/components/ui/data-list'
import { payrollService, type SalaryStructure } from '@/services/payrollService'

export function SalaryStructuresPage() {
  const { data, isLoading } = useQuery({ queryKey: ['salary-structures'], queryFn: () => payrollService.listSalaryStructures() })
  const items: SalaryStructure[] = (data as { content?: SalaryStructure[] } | undefined)?.content
    || (Array.isArray(data) ? data as SalaryStructure[] : (data as { data?: SalaryStructure[] } | undefined)?.data || [])
  return (
    <DataList<SalaryStructure>
      title="Salary Structures" description="Templates of earnings + deductions"
      data={items} isLoading={isLoading}
      emptyIcon={<DollarSign className="h-10 w-10" />} emptyTitle="No structures defined"
      columns={[
        { key: 'name', label: 'Name' },
        { key: 'description', label: 'Description' },
        { key: 'components', label: 'Components', render: s => <Badge>{s.components?.length ?? 0}</Badge> },
      ]}
    />
  )
}
