import { useQuery } from '@tanstack/react-query'
import { Lightbulb } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { DataList } from '@/components/ui/data-list'
import { engagementService, type Suggestion } from '@/services/engagementService'

export function SuggestionsPage() {
  const { data, isLoading } = useQuery({
    queryKey: ['suggestions'],
    queryFn: () => engagementService.listSuggestions({}),
  })
  const items: Suggestion[] = (data as { content?: Suggestion[] } | undefined)?.content
    || (Array.isArray(data) ? data : [])

  return (
    <DataList<Suggestion>
      title="Suggestion Box"
      description="Ideas, complaints and improvements from the team"
      data={items}
      isLoading={isLoading}
      emptyIcon={<Lightbulb className="h-10 w-10" />}
      emptyTitle="No suggestions yet"
      columns={[
        { key: 'title', label: 'Title' },
        { key: 'category', label: 'Category', render: s => <Badge>{s.category}</Badge> },
        { key: 'votesUp', label: '👍', render: s => s.votesUp },
        { key: 'votesDown', label: '👎', render: s => s.votesDown },
        { key: 'status', label: 'Status', render: s => <Badge>{s.status}</Badge> },
      ]}
    />
  )
}
