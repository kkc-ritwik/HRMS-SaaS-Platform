import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { GitBranch, Check, X, Ban } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { DataList } from '@/components/ui/data-list'
import { ConfirmDialog } from '@/components/ui/confirm-dialog'
import { Button } from '@/components/ui/button'
import { workflowInstanceService } from '@/services/extendedServices'
import { Catalog } from '@/services/catalog'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { formatDate } from '@/lib/utils'

type AnyObj = Record<string, unknown>
function rows<T = AnyObj>(d: unknown): T[] {
  if (Array.isArray(d)) return d as T[]
  const o = d as { content?: T[] } | undefined
  return Array.isArray(o?.content) ? o!.content : []
}

export function WorkflowInstancesPage() {
  const qc = useQueryClient()
  const [confirm, setConfirm] = useState<{ id: string; kind: 'approve' | 'reject' | 'cancel' } | null>(null)
  const { data, isLoading } = useQuery({ queryKey: ['workflow-instances'], queryFn: () => workflowInstanceService.list() })

  const act = useMutation({
    mutationFn: ({ id, kind }: { id: string; kind: 'approve' | 'reject' | 'cancel' }) =>
      kind === 'approve' ? Catalog.workflows.instances.approve(id, {})
        : kind === 'reject' ? Catalog.workflows.instances.reject(id, { reason: 'Rejected' })
        : Catalog.workflows.instances.cancel(id, { reason: 'Cancelled' }),
    onSuccess: () => { toast.success('Done'); qc.invalidateQueries({ queryKey: ['workflow-instances'] }); setConfirm(null) },
    onError: () => { toast.error('Action failed'); setConfirm(null) },
  })

  return (
    <>
      <DataList<AnyObj>
        title="Workflow Instances"
        description="Live approval flows in progress"
        data={rows(data)} isLoading={isLoading}
        emptyIcon={<GitBranch className="h-10 w-10" />} emptyTitle="No active workflow instances"
        filters={{ status: ['PENDING', 'IN_PROGRESS', 'APPROVED', 'REJECTED', 'CANCELLED'] }}
        columns={[
          { key: 'entityType', label: 'Entity' },
          { key: 'initiatorName', label: 'Initiator', render: i => String(i.initiatorName ?? i.initiatorId ?? '—') },
          { key: 'currentStep', label: 'Step' },
          { key: 'status', label: 'Status', render: i => <Badge>{String(i.status)}</Badge> },
          { key: 'createdAt', label: 'Started', render: i => i.createdAt ? formatDate(String(i.createdAt)) : '—' },
          { key: 'id', label: '', align: 'right', sortable: false, render: i => {
            const status = String(i.status)
            return (
              <div className="flex justify-end gap-1" onClick={e => e.stopPropagation()}>
                {(status === 'PENDING' || status === 'IN_PROGRESS') && <>
                  <Button size="sm" variant="ghost" className="h-7 w-7 p-0 text-green-600" onClick={() => setConfirm({ id: String(i.id), kind: 'approve' })}><Check className="h-4 w-4" /></Button>
                  <Button size="sm" variant="ghost" className="h-7 w-7 p-0 text-red-500" onClick={() => setConfirm({ id: String(i.id), kind: 'reject' })}><X className="h-4 w-4" /></Button>
                  <Button size="sm" variant="ghost" className="h-7 w-7 p-0 text-slate-400" onClick={() => setConfirm({ id: String(i.id), kind: 'cancel' })}><Ban className="h-4 w-4" /></Button>
                </>}
              </div>
            )
          } },
        ]}
      />
      <ConfirmDialog
        open={!!confirm} onOpenChange={o => !o && setConfirm(null)}
        title={confirm ? `${confirm.kind[0].toUpperCase()}${confirm.kind.slice(1)} this workflow?` : ''}
        confirmLabel={confirm?.kind ?? 'Confirm'}
        variant={confirm?.kind === 'approve' ? 'default' : 'destructive'}
        loading={act.isPending}
        onConfirm={() => confirm && act.mutate(confirm)}
      />
    </>
  )
}
