import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Plus, Gift } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { DataList } from '@/components/ui/data-list'
import { FormDialog } from '@/components/ui/form-dialog'
import { holidayService, type Holiday } from '@/services/holidayService'

export function HolidaysPage() {
  const qc = useQueryClient()
  const [creating, setCreating] = useState(false)
  const year = new Date().getFullYear()
  const { data, isLoading } = useQuery({ queryKey: ['holidays', year], queryFn: () => holidayService.list(year) })

  const create = useMutation({
    mutationFn: (v: Record<string, unknown>) => holidayService.create(v as Partial<Holiday>),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['holidays'] }),
  })

  return (
    <>
      <DataList<Holiday>
        title={`Holidays ${year}`} description="Public, restricted, optional and company holidays"
        action={<Button onClick={() => setCreating(true)}><Plus className="h-4 w-4 mr-1" /> Add Holiday</Button>}
        data={data || []} isLoading={isLoading}
        emptyIcon={<Gift className="h-10 w-10" />} emptyTitle="No holidays configured"
        filters={{ type: ['PUBLIC', 'RESTRICTED', 'OPTIONAL', 'COMPANY'] }}
        columns={[
          { key: 'date', label: 'Date' },
          { key: 'name', label: 'Name' },
          { key: 'type', label: 'Type', render: h => <Badge>{h.type}</Badge> },
          { key: 'region', label: 'Region' },
        ]}
      />
      <FormDialog
        open={creating} onOpenChange={setCreating} title="Add holiday"
        onSubmit={v => create.mutateAsync(v)}
        fields={[
          { name: 'name', label: 'Name', type: 'text', required: true, span: 2 },
          { name: 'date', label: 'Date', type: 'date', required: true },
          { name: 'type', label: 'Type', type: 'select', required: true, options: [
            { value: 'PUBLIC', label: 'Public' }, { value: 'RESTRICTED', label: 'Restricted' },
            { value: 'OPTIONAL', label: 'Optional' }, { value: 'COMPANY', label: 'Company' },
          ] },
          { name: 'region', label: 'Region', type: 'text' },
          { name: 'religion', label: 'Religion', type: 'text' },
          { name: 'description', label: 'Description', type: 'textarea', span: 2 },
        ]}
      />
    </>
  )
}
