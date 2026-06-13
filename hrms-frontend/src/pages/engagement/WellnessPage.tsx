import { Heart } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { ResourcePage } from '@/components/ui/resource-page'
import { wellnessService } from '@/services/extendedServices'

interface WellnessProgram extends Record<string, unknown> { id: string; code?: string; name: string; category?: string; goalMetric?: string; goalTarget?: number; active?: boolean }

export function WellnessPage() {
  return (
    <ResourcePage<WellnessProgram>
      title="Wellness Programs"
      description="Step challenges, meditation streaks, fitness goals"
      icon={<Heart className="h-10 w-10" />}
      queryKey={['wellness']}
      fetcher={() => wellnessService.programs()}
      filters={{ category: ['FITNESS', 'MENTAL_HEALTH', 'NUTRITION', 'FINANCIAL', 'SOCIAL'] }}
      columns={[
        { key: 'name', label: 'Name' },
        { key: 'category', label: 'Category', render: p => p.category ? <Badge>{String(p.category)}</Badge> : '—' },
        { key: 'goalMetric', label: 'Metric' },
        { key: 'goalTarget', label: 'Target', align: 'right' },
        { key: 'active', label: 'Active', render: p => <Badge variant={p.active === false ? 'secondary' : 'success'}>{p.active === false ? 'Inactive' : 'Active'}</Badge> },
      ]}
      formFields={[
        { name: 'name', label: 'Program name', type: 'text', required: true, span: 2 },
        { name: 'category', label: 'Category', type: 'select', required: true, options: [
          { value: 'FITNESS', label: 'Fitness' }, { value: 'MENTAL_HEALTH', label: 'Mental health' },
          { value: 'NUTRITION', label: 'Nutrition' }, { value: 'FINANCIAL', label: 'Financial' }, { value: 'SOCIAL', label: 'Social' },
        ] },
        { name: 'goalMetric', label: 'Goal metric', type: 'text', helper: 'e.g. steps, minutes' },
        { name: 'goalTarget', label: 'Goal target', type: 'number' },
        { name: 'startDate', label: 'Start date', type: 'date' },
        { name: 'endDate', label: 'End date', type: 'date' },
        { name: 'description', label: 'Description', type: 'textarea', span: 2 },
      ]}
      onCreate={v => wellnessService.createProgram(v)}
    />
  )
}
