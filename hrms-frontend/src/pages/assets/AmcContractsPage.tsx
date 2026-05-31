import { useQuery } from '@tanstack/react-query'
import { FileSignature } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { DataList } from '@/components/ui/data-list'
import { amcContractService } from '@/services/extendedServices'

interface Amc { id: string; contractNumber: string; vendorName?: string; contractType: string; startDate: string; endDate: string; status: string; contractValue?: number }

export function AmcContractsPage() {
  const { data, isLoading } = useQuery({ queryKey: ['amc'], queryFn: () => amcContractService.list() })
  const items: Amc[] = (data as { content?: Amc[] } | undefined)?.content
    || (Array.isArray(data) ? data as Amc[] : [])
  return (
    <DataList<Amc>
      title="AMC Contracts" description="Annual maintenance contracts, warranties, software licenses"
      data={items} isLoading={isLoading}
      emptyIcon={<FileSignature className="h-10 w-10" />} emptyTitle="No active contracts"
      columns={[
        { key: 'contractNumber', label: 'Contract #' },
        { key: 'vendorName', label: 'Vendor' },
        { key: 'contractType', label: 'Type' },
        { key: 'startDate', label: 'Start' },
        { key: 'endDate', label: 'End' },
        { key: 'contractValue', label: 'Value', align: 'right' },
        { key: 'status', label: 'Status', render: a => <Badge>{a.status}</Badge> },
      ]}
    />
  )
}
