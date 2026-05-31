import { useQuery } from '@tanstack/react-query'
import { CheckCircle2 } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { DataList } from '@/components/ui/data-list'
import { complianceItemService } from '@/services/extendedServices'

interface ComplianceItem { id: string; title: string; category: string; dueDate?: string; status: string; assignee?: string }

export function ComplianceItemsPage() {
  const { data, isLoading } = useQuery({ queryKey: ['compliance-items'], queryFn: () => complianceItemService.list() })
  const items: ComplianceItem[] = (data as { content?: ComplianceItem[] } | undefined)?.content
    || (Array.isArray(data) ? data as ComplianceItem[] : [])
  return (
    <DataList<ComplianceItem>
      title="Compliance Checklist" description="Statutory + internal tasks with deadlines"
      data={items} isLoading={isLoading}
      emptyIcon={<CheckCircle2 className="h-10 w-10" />} emptyTitle="All clear"
      columns={[
        { key: 'title', label: 'Item' },
        { key: 'category', label: 'Category' },
        { key: 'dueDate', label: 'Due' },
        { key: 'assignee', label: 'Assignee' },
        { key: 'status', label: 'Status', render: c => <Badge>{c.status}</Badge> },
      ]}
    />
  )
}
