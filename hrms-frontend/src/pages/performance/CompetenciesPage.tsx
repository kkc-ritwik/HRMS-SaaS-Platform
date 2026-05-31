import { useQuery } from '@tanstack/react-query'
import { Target } from 'lucide-react'
import { DataList } from '@/components/ui/data-list'
import { competencyService } from '@/services/extendedServices'

interface Competency { id: string; name: string; description?: string; category?: string; level?: string }

export function CompetenciesPage() {
  const { data, isLoading } = useQuery({ queryKey: ['competencies'], queryFn: () => competencyService.list() })
  const items: Competency[] = (data as { content?: Competency[] } | undefined)?.content
    || (Array.isArray(data) ? data as Competency[] : [])
  return (
    <DataList<Competency>
      title="Competencies" description="Skills + behavioural competencies used in reviews"
      data={items} isLoading={isLoading}
      emptyIcon={<Target className="h-10 w-10" />} emptyTitle="No competencies defined"
      columns={[
        { key: 'name', label: 'Name' },
        { key: 'category', label: 'Category' },
        { key: 'level', label: 'Level' },
        { key: 'description', label: 'Description' },
      ]}
    />
  )
}
