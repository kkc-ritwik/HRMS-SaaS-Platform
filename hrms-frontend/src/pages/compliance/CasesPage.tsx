import { useQuery } from '@tanstack/react-query'
import { AlertTriangle } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { DataList } from '@/components/ui/data-list'
import { casesService } from '@/services/extendedServices'

interface HrCase { id: string; caseNumber: string; type: string; title: string; status: string; severity?: string; isAnonymous?: boolean }

export function CasesPage() {
  const { data, isLoading } = useQuery({ queryKey: ['cases'], queryFn: () => casesService.list() })
  const items: HrCase[] = (data as { content?: HrCase[] } | undefined)?.content
    || (Array.isArray(data) ? data as HrCase[] : [])
  return (
    <DataList<HrCase>
      title="HR Cases" description="Grievance, POSH, ethics, whistleblower"
      data={items} isLoading={isLoading}
      emptyIcon={<AlertTriangle className="h-10 w-10" />} emptyTitle="No active cases"
      columns={[
        { key: 'caseNumber', label: '#' },
        { key: 'type', label: 'Type', render: c => <Badge>{c.type}</Badge> },
        { key: 'title', label: 'Title' },
        { key: 'severity', label: 'Severity', render: c => c.severity ? <Badge>{c.severity}</Badge> : '—' },
        { key: 'status', label: 'Status', render: c => <Badge>{c.status}</Badge> },
        { key: 'isAnonymous', label: 'Anon', render: c => c.isAnonymous ? '✓' : '—' },
      ]}
    />
  )
}
