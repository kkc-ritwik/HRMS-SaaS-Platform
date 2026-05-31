import { useQuery } from '@tanstack/react-query'
import { GitBranch } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { DataList } from '@/components/ui/data-list'
import { workflowDefinitionService } from '@/services/extendedServices'

interface WorkflowDef { id: string; name: string; module: string; version: number; status: string; createdAt: string }

export function WorkflowsPage() {
  const { data, isLoading } = useQuery({ queryKey: ['workflow-defs'], queryFn: () => workflowDefinitionService.list() })
  const items: WorkflowDef[] = (data as { content?: WorkflowDef[] } | undefined)?.content
    || (Array.isArray(data) ? data as WorkflowDef[] : [])
  return (
    <DataList<WorkflowDef>
      title="Workflow Definitions" description="Approval flows + automations"
      data={items} isLoading={isLoading}
      emptyIcon={<GitBranch className="h-10 w-10" />} emptyTitle="No workflows"
      columns={[
        { key: 'name', label: 'Name' },
        { key: 'module', label: 'Module' },
        { key: 'version', label: 'v' },
        { key: 'status', label: 'Status', render: w => <Badge>{w.status}</Badge> },
        { key: 'createdAt', label: 'Created' },
      ]}
    />
  )
}
