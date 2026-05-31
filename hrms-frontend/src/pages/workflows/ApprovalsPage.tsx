import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { CheckSquare, Check, X } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { DataList } from '@/components/ui/data-list'
import { workflowService, type ApprovalRequest } from '@/services/workflowService'
import { toast } from 'sonner'

export function ApprovalsPage() {
  const qc = useQueryClient()
  const { data, isLoading } = useQuery({ queryKey: ['approvals'], queryFn: workflowService.approvalsForMe })

  const approve = useMutation({
    mutationFn: (id: string) => workflowService.approve(id),
    onSuccess: () => { toast.success('Approved'); qc.invalidateQueries({ queryKey: ['approvals'] }) },
  })
  const reject = useMutation({
    mutationFn: (id: string) => workflowService.reject(id, 'Rejected'),
    onSuccess: () => { toast.success('Rejected'); qc.invalidateQueries({ queryKey: ['approvals'] }) },
  })

  return (
    <DataList<ApprovalRequest>
      title="Pending Approvals"
      description="Requests awaiting your action"
      data={data || []}
      isLoading={isLoading}
      emptyIcon={<CheckSquare className="h-10 w-10" />}
      emptyTitle="Inbox zero"
      emptyDescription="No approvals waiting for you"
      columns={[
        { key: 'type', label: 'Type' },
        { key: 'initiatorName', label: 'Requested by' },
        { key: 'state', label: 'State', render: a => <Badge>{a.state}</Badge> },
        { key: 'createdAt', label: 'Created' },
        {
          key: 'id', label: 'Action', align: 'right', render: a => (
            <div className="flex gap-1 justify-end">
              <Button size="sm" onClick={() => approve.mutate(a.id)}><Check className="h-3.5 w-3.5" /></Button>
              <Button size="sm" variant="outline" onClick={() => reject.mutate(a.id)}><X className="h-3.5 w-3.5" /></Button>
            </div>
          ),
        },
      ]}
    />
  )
}
