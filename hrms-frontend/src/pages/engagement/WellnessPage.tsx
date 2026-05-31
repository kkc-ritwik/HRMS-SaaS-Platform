import { useQuery } from '@tanstack/react-query'
import { Heart } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { DataList } from '@/components/ui/data-list'
import { wellnessService } from '@/services/extendedServices'

interface WellnessProgram { id: string; code: string; name: string; category: string; goalMetric?: string; goalTarget?: number; active: boolean }

export function WellnessPage() {
  const { data, isLoading } = useQuery({ queryKey: ['wellness'], queryFn: wellnessService.programs })
  return (
    <DataList<WellnessProgram>
      title="Wellness Programs" description="Step challenges, meditation, fitness"
      data={(data as WellnessProgram[]) || []} isLoading={isLoading}
      emptyIcon={<Heart className="h-10 w-10" />} emptyTitle="No active programs"
      columns={[
        { key: 'name', label: 'Name' },
        { key: 'category', label: 'Category', render: p => <Badge>{p.category}</Badge> },
        { key: 'goalMetric', label: 'Metric' },
        { key: 'goalTarget', label: 'Target' },
        { key: 'active', label: 'Active', render: p => p.active ? '✓' : '—' },
      ]}
    />
  )
}
