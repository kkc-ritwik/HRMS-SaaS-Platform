import { useMemo, useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { FileText, FilePlus, TrendingUp, LogOut } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { DataList } from '@/components/ui/data-list'
import { FormDialog } from '@/components/ui/form-dialog'
import { lettersService } from '@/services/extendedServices'
import { getErrorMessage } from '@/lib/api'
import { formatDate } from '@/lib/utils'
import { toast } from 'sonner'

interface Letter { id: string; type: string; employeeId: string; storageUri?: string; generatedAt?: string; status?: string }
type Mode = null | 'employment' | 'salary' | 'exit'

export function LettersPage() {
  const qc = useQueryClient()
  const [type, setType] = useState<string>('ALL')
  const [mode, setMode] = useState<Mode>(null)

  const { data, isLoading } = useQuery({
    queryKey: ['letters'],
    queryFn: () => lettersService.list(),
  })
  const all: Letter[] = useMemo(() => (
    (data as { content?: Letter[] } | undefined)?.content || (Array.isArray(data) ? data as Letter[] : [])
  ), [data])
  const items = type === 'ALL' ? all : all.filter(l => l.type === type)

  const done = (msg: string) => () => { toast.success(msg); qc.invalidateQueries({ queryKey: ['letters'] }); setMode(null) }
  const onErr = (e: unknown) => toast.error(getErrorMessage(e))

  const employment = useMutation({ mutationFn: (v: Record<string, unknown>) => lettersService.employment(v), onSuccess: done('Employment letter generated'), onError: onErr })
  const salary = useMutation({ mutationFn: (v: Record<string, unknown>) => lettersService.salaryRevision(v), onSuccess: done('Salary revision letter generated'), onError: onErr })
  const exit = useMutation({
    mutationFn: (v: Record<string, unknown>) => lettersService.exit(String(v.exitType) as 'RELIEVING' | 'EXPERIENCE' | 'SERVICE' | 'REFERENCE', v),
    onSuccess: done('Exit letter generated'), onError: onErr,
  })

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-center justify-between gap-2">
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
        <div className="flex gap-2">
          <Button variant="outline" onClick={() => setMode('employment')}><FilePlus className="h-4 w-4 mr-1" /> Employment</Button>
          <Button variant="outline" onClick={() => setMode('salary')}><TrendingUp className="h-4 w-4 mr-1" /> Salary revision</Button>
          <Button variant="outline" onClick={() => setMode('exit')}><LogOut className="h-4 w-4 mr-1" /> Exit</Button>
        </div>
      </div>

      <DataList<Letter>
        title="Generated Letters" description="All generated employment, salary and exit letters"
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
        open={mode === 'employment'} onOpenChange={o => !o && setMode(null)} title="Generate employment letter" submitLabel="Generate"
        fields={[
          { name: 'employeeId', label: 'Employee ID', type: 'text', required: true, span: 2 },
          { name: 'letterType', label: 'Letter type', type: 'select', required: true, options: [
            { value: 'APPOINTMENT', label: 'Appointment' }, { value: 'CONFIRMATION', label: 'Confirmation' },
            { value: 'EMPLOYMENT', label: 'Employment verification' }, { value: 'ADDRESS_PROOF', label: 'Address proof' },
          ] },
          { name: 'effectiveDate', label: 'Effective date', type: 'date' },
        ]}
        onSubmit={v => employment.mutateAsync(v)}
      />

      <FormDialog
        open={mode === 'salary'} onOpenChange={o => !o && setMode(null)} title="Generate salary revision letter" submitLabel="Generate"
        fields={[
          { name: 'employeeId', label: 'Employee ID', type: 'text', span: 2 },
          { name: 'employeeName', label: 'Employee name', type: 'text', required: true },
          { name: 'employeeCode', label: 'Employee code', type: 'text', required: true },
          { name: 'effectiveDate', label: 'Effective date', type: 'date', required: true },
          { name: 'currency', label: 'Currency', type: 'text' },
          { name: 'currentCtc', label: 'Current CTC', type: 'currency', required: true },
          { name: 'newCtc', label: 'New CTC', type: 'currency', required: true },
          { name: 'reason', label: 'Reason', type: 'textarea', span: 2 },
          { name: 'signatoryName', label: 'Signatory name', type: 'text' },
          { name: 'signatoryTitle', label: 'Signatory title', type: 'text' },
        ]}
        onSubmit={v => salary.mutateAsync(v)}
      />

      <FormDialog
        open={mode === 'exit'} onOpenChange={o => !o && setMode(null)} title="Generate exit letter" submitLabel="Generate"
        fields={[
          { name: 'exitType', label: 'Letter type', type: 'select', required: true, options: [
            { value: 'RELIEVING', label: 'Relieving' }, { value: 'EXPERIENCE', label: 'Experience' },
            { value: 'SERVICE', label: 'Service certificate' }, { value: 'REFERENCE', label: 'Reference' },
          ] },
          { name: 'employeeName', label: 'Employee name', type: 'text', required: true },
          { name: 'employeeCode', label: 'Employee code', type: 'text', required: true },
          { name: 'designation', label: 'Designation', type: 'text' },
          { name: 'department', label: 'Department', type: 'text' },
          { name: 'joinDate', label: 'Join date', type: 'date' },
          { name: 'lastWorkingDay', label: 'Last working day', type: 'date', required: true },
          { name: 'signatoryName', label: 'Signatory name', type: 'text' },
          { name: 'signatoryTitle', label: 'Signatory title', type: 'text' },
        ]}
        onSubmit={v => exit.mutateAsync(v)}
      />
    </div>
  )
}
