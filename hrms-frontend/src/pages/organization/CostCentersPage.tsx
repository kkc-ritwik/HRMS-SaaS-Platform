import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Plus, Wallet } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { DataList } from '@/components/ui/data-list'
import { FormDialog } from '@/components/ui/form-dialog'
import { costCenterService } from '@/services/extendedServices'

interface CostCenter { id: string; code: string; name: string; currency?: string; annualBudget?: number; active: boolean }

export function CostCentersPage() {
  const qc = useQueryClient()
  const [creating, setCreating] = useState(false)
  const { data, isLoading } = useQuery({ queryKey: ['cost-centers'], queryFn: () => costCenterService.list() })
  const items: CostCenter[] = (data as { content?: CostCenter[] } | undefined)?.content
    || (Array.isArray(data) ? data as CostCenter[] : [])

  const create = useMutation({
    mutationFn: (v: Record<string, unknown>) => costCenterService.create(v),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['cost-centers'] }),
  })

  return (
    <>
      <DataList<CostCenter>
        title="Cost Centres" description="Departmental budget buckets"
        action={<Button onClick={() => setCreating(true)}><Plus className="h-4 w-4 mr-1" /> New</Button>}
        data={items} isLoading={isLoading}
        emptyIcon={<Wallet className="h-10 w-10" />} emptyTitle="No cost centres"
        columns={[
          { key: 'code', label: 'Code' },
          { key: 'name', label: 'Name' },
          { key: 'currency', label: 'Currency' },
          { key: 'annualBudget', label: 'Annual budget', align: 'right', render: c => c.annualBudget ? `${c.currency || ''} ${c.annualBudget.toLocaleString()}` : '—' },
          { key: 'active', label: 'Active', render: c => <Badge>{c.active ? 'Active' : 'Inactive'}</Badge> },
        ]}
      />
      <FormDialog
        open={creating} onOpenChange={setCreating} title="Create cost centre"
        onSubmit={v => create.mutateAsync(v)}
        fields={[
          { name: 'code', label: 'Code', type: 'text', required: true },
          { name: 'name', label: 'Name', type: 'text', required: true },
          { name: 'currency', label: 'Currency', type: 'text', defaultValue: 'INR' },
          { name: 'annualBudget', label: 'Annual budget', type: 'currency' },
        ]}
      />
    </>
  )
}
