import { useQuery } from '@tanstack/react-query'
import { useState } from 'react'
import { FileText } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { DataList } from '@/components/ui/data-list'
import { lettersService } from '@/services/extendedServices'

interface Letter { id: string; type: string; employeeId: string; storageUri?: string; generatedAt?: string; status?: string }

export function LettersPage() {
  const [type, setType] = useState<string>('ALL')
  const { data, isLoading } = useQuery({
    queryKey: ['letters', type],
    queryFn: () => lettersService.list(type === 'ALL' ? undefined : type),
  })
  const items: Letter[] = (data as { content?: Letter[] } | undefined)?.content
    || (Array.isArray(data) ? data as Letter[] : [])

  return (
    <div className="space-y-4">
      <Tabs value={type} onValueChange={setType}>
        <TabsList>
          <TabsTrigger value="ALL">All</TabsTrigger>
          <TabsTrigger value="OFFER">Offer</TabsTrigger>
          <TabsTrigger value="APPOINTMENT">Appointment</TabsTrigger>
          <TabsTrigger value="CONFIRMATION">Confirmation</TabsTrigger>
          <TabsTrigger value="SALARY_REVISION">Salary revision</TabsTrigger>
          <TabsTrigger value="RELIEVING">Relieving</TabsTrigger>
          <TabsTrigger value="EXPERIENCE">Experience</TabsTrigger>
        </TabsList>
      </Tabs>
      <DataList<Letter>
        title="Generated Letters" description="All generated employment, salary, exit letters"
        data={items} isLoading={isLoading}
        emptyIcon={<FileText className="h-10 w-10" />} emptyTitle="No letters generated"
        columns={[
          { key: 'type', label: 'Type', render: l => <Badge>{l.type}</Badge> },
          { key: 'employeeId', label: 'Employee' },
          { key: 'generatedAt', label: 'Generated' },
          { key: 'status', label: 'Status', render: l => l.status ? <Badge>{l.status}</Badge> : '—' },
          { key: 'storageUri', label: 'Document', render: l => l.storageUri ? <a className="text-violet-600 hover:underline" href={l.storageUri} target="_blank" rel="noreferrer">Download</a> : '—' },
        ]}
      />
    </div>
  )
}
