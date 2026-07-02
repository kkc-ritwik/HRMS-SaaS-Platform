import { useState } from 'react'
import { CheckSquare, CheckCircle2 } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { ResourcePage } from '@/components/ui/resource-page'
import { complianceTaskService } from '@/services/adminServices'
import { Catalog } from '@/services/catalog'
import { useAuthStore } from '@/store/authStore'
import { formatDate } from '@/lib/utils'

interface Task extends Record<string, unknown> { id: string; title: string; category?: string; dueDate?: string; assignee?: string; assigneeName?: string; status: string }
type View = 'all' | 'mine'

export function ComplianceTasksPage() {
  const [view, setView] = useState<View>('all')
  const user = useAuthStore(s => s.user)
  const employeeId = user?.employeeId || user?.id || ''
  const fetcher = () => view === 'mine' ? Catalog.compliance.tasks.forAssignee(employeeId) : complianceTaskService.list()

  return (
    <>
      <Tabs value={view} onValueChange={v => setView(v as View)} className="mb-2">
        <TabsList>
          <TabsTrigger value="all">All tasks</TabsTrigger>
          <TabsTrigger value="mine">Assigned to me</TabsTrigger>
        </TabsList>
      </Tabs>
      <ResourcePage<Task>
        key={view}
        title="Compliance Tasks"
        description="Statutory + audit tasks with deadlines and owners"
        icon={<CheckSquare className="h-10 w-10" />}
        queryKey={['compliance-tasks', view]}
        fetcher={fetcher as () => Promise<unknown>}
        filters={{ status: ['PENDING', 'IN_PROGRESS', 'COMPLETED', 'OVERDUE'] }}
        columns={[
          { key: 'title', label: 'Task' },
          { key: 'category', label: 'Category' },
          { key: 'dueDate', label: 'Due', render: t => t.dueDate ? formatDate(String(t.dueDate)) : '—' },
          { key: 'assignee', label: 'Assignee', render: t => String(t.assigneeName ?? t.assignee ?? '—') },
          { key: 'status', label: 'Status', render: t => <Badge variant={t.status === 'OVERDUE' ? 'destructive' : t.status === 'COMPLETED' ? 'success' : 'warning'}>{String(t.status)}</Badge> },
        ]}
        formFields={[
          { name: 'title', label: 'Task title', type: 'text', required: true, span: 2 },
          { name: 'complianceItemId', label: 'Compliance item ID', type: 'text' },
          { name: 'assigneeId', label: 'Assignee employee ID', type: 'text' },
          { name: 'dueDate', label: 'Due date', type: 'date', required: true },
          { name: 'description', label: 'Description', type: 'textarea', span: 2 },
        ]}
        onCreate={v => Catalog.compliance.tasks.create(v)}
        onUpdate={(id, v) => Catalog.compliance.tasks.update(id, v)}
        onDelete={id => Catalog.compliance.tasks.delete(id)}
        rowActions={t => [
          { label: 'Mark complete', icon: <CheckCircle2 className="h-3.5 w-3.5" />, show: t.status !== 'COMPLETED', run: () => complianceTaskService.complete(t.id) },
        ]}
      />
    </>
  )
}
