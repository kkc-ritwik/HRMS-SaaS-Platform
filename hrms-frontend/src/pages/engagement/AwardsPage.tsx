import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Plus, Trophy } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { DataList } from '@/components/ui/data-list'
import { FormDialog } from '@/components/ui/form-dialog'
import { awardService } from '@/services/extendedServices'

interface Award {
  id: string; awardType: string; recipientEmployeeId: string; title: string;
  status: string; awardedOn?: string; monetaryValue?: number; currency?: string;
}

export function AwardsPage() {
  const qc = useQueryClient()
  const [creating, setCreating] = useState(false)
  const { data, isLoading } = useQuery({ queryKey: ['awards'], queryFn: () => awardService.list() })
  const items: Award[] = (data as { content?: Award[] } | undefined)?.content
    || (Array.isArray(data) ? data as Award[] : [])

  const create = useMutation({
    mutationFn: (v: Record<string, unknown>) => awardService.nominate(v),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['awards'] }),
  })

  return (
    <>
      <DataList<Award>
        title="Awards" description="Spot, long-service, and peer-nominated awards"
        action={<Button onClick={() => setCreating(true)}><Plus className="h-4 w-4 mr-1" /> Nominate</Button>}
        data={items} isLoading={isLoading}
        emptyIcon={<Trophy className="h-10 w-10" />} emptyTitle="No awards yet"
        filters={{
          awardType: ['LONG_SERVICE', 'SPOT', 'PEER_NOMINATED', 'MANAGER_DISCRETIONARY', 'ANNUAL', 'INNOVATION'],
          status: ['NOMINATED', 'APPROVED', 'REJECTED', 'AWARDED', 'REDEEMED'],
        }}
        columns={[
          { key: 'title', label: 'Title' },
          { key: 'awardType', label: 'Type', render: a => <Badge>{a.awardType}</Badge> },
          { key: 'recipientEmployeeId', label: 'Recipient' },
          { key: 'monetaryValue', label: 'Value', render: a => a.monetaryValue ? `${a.currency || ''} ${a.monetaryValue}` : '—' },
          { key: 'status', label: 'Status', render: a => <Badge>{a.status}</Badge> },
          { key: 'awardedOn', label: 'Date' },
        ]}
      />
      <FormDialog
        open={creating} onOpenChange={setCreating} title="Nominate for award"
        onSubmit={v => create.mutateAsync(v)}
        fields={[
          { name: 'title', label: 'Award title', type: 'text', required: true, span: 2 },
          { name: 'awardType', label: 'Award type', type: 'select', required: true, options: [
            { value: 'SPOT', label: 'Spot award' }, { value: 'PEER_NOMINATED', label: 'Peer nominated' },
            { value: 'MANAGER_DISCRETIONARY', label: 'Manager discretionary' }, { value: 'INNOVATION', label: 'Innovation' },
            { value: 'CUSTOMER_HERO', label: 'Customer hero' }, { value: 'LONG_SERVICE', label: 'Long service' },
          ] },
          { name: 'recipientEmployeeId', label: 'Recipient (employee ID)', type: 'text', required: true },
          { name: 'monetaryValue', label: 'Monetary value', type: 'currency' },
          { name: 'currency', label: 'Currency', type: 'text', defaultValue: 'INR' },
          { name: 'points', label: 'Points', type: 'number' },
          { name: 'citation', label: 'Citation', type: 'textarea', span: 2 },
        ]}
      />
    </>
  )
}
