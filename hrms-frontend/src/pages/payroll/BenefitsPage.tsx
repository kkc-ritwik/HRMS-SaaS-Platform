import { useQuery } from '@tanstack/react-query'
import { Heart } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { DataList } from '@/components/ui/data-list'
import { compensationService, type Benefit } from '@/services/compensationService'

export function BenefitsPage() {
  const { data, isLoading } = useQuery({ queryKey: ['benefits'], queryFn: compensationService.listBenefits })
  return (
    <DataList<Benefit>
      title="Benefits" description="Health, life, accident and other employee benefits"
      data={data || []} isLoading={isLoading}
      emptyIcon={<Heart className="h-10 w-10" />} emptyTitle="No benefits configured"
      columns={[
        { key: 'code', label: 'Code' },
        { key: 'name', label: 'Name' },
        { key: 'category', label: 'Category', render: b => <Badge>{b.category}</Badge> },
        { key: 'premiumAmount', label: 'Premium' },
        { key: 'premiumPaidBy', label: 'Paid by', render: b => <Badge>{b.premiumPaidBy}</Badge> },
      ]}
    />
  )
}
