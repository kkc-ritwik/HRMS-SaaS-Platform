import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { FileText, Plus } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { DataList } from '@/components/ui/data-list'
import { FormDialog } from '@/components/ui/form-dialog'
import { lettersService } from '@/services/extendedServices'
import { formatDate } from '@/lib/utils'

interface Letter { id: string; type: string; employeeId: string; storageUri?: string; generatedAt?: string; status?: string }

export function LettersPage() {
  const qc = useQueryClient()
  const [type, setType] = useState<string>('ALL')
  const [generating, setGenerating] = useState(false)
  const { data, isLoading } = useQuery({
    queryKey: ['letters', type],
    queryFn: () => lettersService.list(type === 'ALL' ? undefined : type),
  })
  const items: Letter[] = (data as { content?: Letter[] } | undefined)?.content
    || (Array.isArray(data) ? data as Letter[] : [])

  const generate = useMutation({
    mutationFn: (v: Record<string, unknown>) => lettersService.employment(v),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['letters'] }); setGenerating(false) },
  })

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <Tabs value={type} onValueChange={setType}>
          <TabsList className="flex flex-wrap h-auto">
            <TabsTrigger value="ALL">All</TabsTrigger>
            <TabsTrigger value="OFFER">Offer</TabsTrigger>
            <TabsTrigger value="APPOINTMENT">Appointment</TabsTrigger>
            <TabsTrigger value="CONFIRMATION">Confirmation</TabsTrigger>
            <TabsTrigger value="SALARY_REVISION">Salary revision</TabsTrigger>
            <TabsTrigger value="RELIEVING">Relieving</TabsTrigger>
            <TabsTrigger value="EXPERIENCE">Experience</TabsTrigger>
          </TabsList>
        </Tabs>
        <Button onClick={() => setGenerating(true)}><Plus className="h-4 w-4 mr-1" /> Generate Letter</Button>
      </div>
      <DataList<Letter>
        title="Generated Letters" description="All generated employment, salary, exit letters"
        data={items} isLoading={isLoading}
        emptyIcon={<FileText className="h-10 w-10" />} emptyTitle="No letters generated"
        columns={[
          { key: 'type', label: 'Type', render: l => <Badge>{l.type}</Badge> },
          { key: 'employeeId', label: 'Employee' },
          { key: 'generatedAt', label: 'Generated', render: l => l.generatedAt ? formatDate(l.generatedAt) : '—' },
          { key: 'status', label: 'Status', render: l => l.status ? <Badge>{l.status}</Badge> : '—' },
          { key: 'storageUri', label: 'Document', render: l => l.storageUri ? <a className="text-brand-600 hover:underline" href={l.storageUri} target="_blank" rel="noreferrer">Download</a> : '—' },
        ]}
      />
      <FormDialog
        open={generating} onOpenChange={setGenerating} title="Generate employment letter" submitLabel="Generate"
        fields={[
          { name: 'employeeId', label: 'Employee ID', type: 'text', required: true, span: 2 },
          { name: 'letterType', label: 'Letter type', type: 'select', required: true, options: [
            { value: 'APPOINTMENT', label: 'Appointment' }, { value: 'CONFIRMATION', label: 'Confirmation' },
            { value: 'EMPLOYMENT', label: 'Employment verification' }, { value: 'ADDRESS_PROOF', label: 'Address proof' },
          ] },
          { name: 'effectiveDate', label: 'Effective date', type: 'date' },
        ]}
        onSubmit={v => generate.mutateAsync(v)}
      />
    </div>
  )
}
