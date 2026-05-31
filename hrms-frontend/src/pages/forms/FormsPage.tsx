import { useQuery } from '@tanstack/react-query'
import { ClipboardEdit } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { DataList } from '@/components/ui/data-list'
import { formService } from '@/services/extendedServices'

interface Form { id: string; name: string; description?: string; category?: string; active: boolean; submissionsCount?: number }

export function FormsPage() {
  const { data, isLoading } = useQuery({ queryKey: ['forms'], queryFn: () => formService.list() })
  const items: Form[] = (data as { content?: Form[] } | undefined)?.content
    || (Array.isArray(data) ? data as Form[] : [])
  return (
    <DataList<Form>
      title="Forms Library" description="Custom HR forms"
      data={items} isLoading={isLoading}
      emptyIcon={<ClipboardEdit className="h-10 w-10" />} emptyTitle="No forms"
      columns={[
        { key: 'name', label: 'Name' },
        { key: 'category', label: 'Category' },
        { key: 'submissionsCount', label: 'Submissions' },
        { key: 'active', label: 'Active', render: f => <Badge>{f.active ? 'Active' : 'Draft'}</Badge> },
      ]}
    />
  )
}
