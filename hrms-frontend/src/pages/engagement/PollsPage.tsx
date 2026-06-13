import { BarChart3, Square } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { ResourcePage } from '@/components/ui/resource-page'
import { pollService } from '@/services/extendedServices'
import { Catalog } from '@/services/catalog'
import { formatDate } from '@/lib/utils'

interface Poll extends Record<string, unknown> { id: string; question: string; totalVotes?: number; isActive?: boolean; status?: string; endsAt?: string }

export function PollsPage() {
  return (
    <ResourcePage<Poll>
      title="Polls"
      description="Quick pulse polls — create, launch, tally"
      icon={<BarChart3 className="h-10 w-10" />}
      queryKey={['polls']}
      fetcher={() => pollService.list()}
      columns={[
        { key: 'question', label: 'Question' },
        { key: 'totalVotes', label: 'Votes', align: 'right' },
        { key: 'endsAt', label: 'Ends', render: p => p.endsAt ? formatDate(String(p.endsAt)) : '—' },
        { key: 'isActive', label: 'State', render: p => <Badge variant={p.isActive ? 'success' : 'secondary'}>{p.isActive ? 'Open' : 'Closed'}</Badge> },
      ]}
      formFields={[
        { name: 'question', label: 'Question', type: 'text', required: true, span: 2 },
        { name: 'options', label: 'Options (comma-separated)', type: 'text', required: true, span: 2, helper: 'e.g. Yes, No, Maybe' },
        { name: 'anonymous', label: 'Anonymous', type: 'switch', defaultValue: true },
        { name: 'endsAt', label: 'Ends at', type: 'date' },
      ]}
      onCreate={v => {
        const opts = String(v.options ?? '').split(',').map(s => s.trim()).filter(Boolean)
        return Catalog.engagement.polls.create({ ...v, options: opts })
      }}
      rowActions={p => [
        { label: 'Tally results', icon: <Square className="h-3.5 w-3.5" />, run: () => pollService.list() },
      ]}
    />
  )
}
