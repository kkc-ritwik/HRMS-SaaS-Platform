import { useQuery } from '@tanstack/react-query'
import { FileSignature } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { DataList } from '@/components/ui/data-list'
import { offerService } from '@/services/extendedServices'

interface Offer {
  id: string
  candidateName?: string
  designation: string
  ctc: number
  status: string
  joiningDate?: string
  issuedAt?: string
}

export function OffersPage() {
  const { data, isLoading } = useQuery({ queryKey: ['offers'], queryFn: () => offerService.list() })
  const items: Offer[] = (data as { content?: Offer[] } | undefined)?.content
    || (Array.isArray(data) ? data as Offer[] : [])
  return (
    <DataList<Offer>
      title="Offer Letters" description="Issued, accepted, withdrawn"
      data={items} isLoading={isLoading}
      emptyIcon={<FileSignature className="h-10 w-10" />} emptyTitle="No offers yet"
      columns={[
        { key: 'candidateName', label: 'Candidate' },
        { key: 'designation', label: 'Role' },
        { key: 'ctc', label: 'CTC', align: 'right', render: o => `₹${o.ctc?.toLocaleString()}` },
        { key: 'joiningDate', label: 'Joining' },
        { key: 'status', label: 'Status', render: o => <Badge>{o.status}</Badge> },
      ]}
    />
  )
}
