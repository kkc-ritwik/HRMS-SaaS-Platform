import { Lightbulb, ThumbsUp, ThumbsDown, Eye } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { ResourcePage } from '@/components/ui/resource-page'
import { engagementService, type Suggestion } from '@/services/engagementService'
import { Catalog } from '@/services/catalog'
import { useAuth } from '@/hooks/useAuth'

export function SuggestionsPage() {
  const { user } = useAuth()
  return (
    <ResourcePage<Suggestion & Record<string, unknown>>
      title="Suggestion Box"
      description="Ideas, complaints and improvements from the team — submit, vote, de-anonymise"
      icon={<Lightbulb className="h-10 w-10" />}
      queryKey={['suggestions']}
      fetcher={() => engagementService.listSuggestions({})}
      filters={{ category: ['IDEA', 'COMPLAINT', 'IMPROVEMENT', 'APPRECIATION'], status: ['NEW', 'UNDER_REVIEW', 'ACCEPTED', 'IMPLEMENTED', 'DECLINED'] }}
      columns={[
        { key: 'title', label: 'Title' },
        { key: 'category', label: 'Category', render: s => <Badge>{String(s.category)}</Badge> },
        { key: 'votesUp', label: '👍', align: 'right', render: s => Number(s.votesUp ?? 0) },
        { key: 'votesDown', label: '👎', align: 'right', render: s => Number(s.votesDown ?? 0) },
        { key: 'status', label: 'Status', render: s => <Badge>{String(s.status)}</Badge> },
      ]}
      formFields={[
        { name: 'title', label: 'Title', type: 'text', required: true, span: 2 },
        { name: 'category', label: 'Category', type: 'select', required: true, options: [
          { value: 'IDEA', label: 'Idea' }, { value: 'COMPLAINT', label: 'Complaint' },
          { value: 'IMPROVEMENT', label: 'Improvement' }, { value: 'APPRECIATION', label: 'Appreciation' },
        ] },
        { name: 'includeIdentity', label: 'Submit with my name', type: 'switch' },
        { name: 'description', label: 'Description', type: 'textarea', required: true, span: 2 },
      ]}
      createTitle="Submit a suggestion"
      onCreate={v => engagementService.submitSuggestion({ ...v, employeeId: user?.id } as { title: string; description: string; category: string; includeIdentity?: boolean; employeeId?: string })}
      rowActions={s => [
        { label: 'Upvote', icon: <ThumbsUp className="h-3.5 w-3.5" />, run: () => engagementService.voteSuggestion(s.id, user?.id ?? '', 'UP') },
        { label: 'Downvote', icon: <ThumbsDown className="h-3.5 w-3.5" />, run: () => engagementService.voteSuggestion(s.id, user?.id ?? '', 'DOWN') },
        { label: 'De-anonymise', icon: <Eye className="h-3.5 w-3.5" />, confirm: 'Reveal the identity of the submitter? This is audited.', run: () => Catalog.engagement.suggestions.deAnonymise(s.id) },
      ]}
    />
  )
}
