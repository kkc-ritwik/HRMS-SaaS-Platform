import { FileSignature } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { ResourcePage } from '@/components/ui/resource-page'
import { amcContractService } from '@/services/extendedServices'
import { formatDate } from '@/lib/utils'

interface Amc extends Record<string, unknown> { id: string; contractNumber?: string; vendorName?: string; contractType?: string; startDate?: string; endDate?: string; status: string; contractValue?: number }

export function AmcContractsPage() {
  return (
    <ResourcePage<Amc>
      title="AMC Contracts"
      description="Annual maintenance contracts, warranties, software licenses"
      icon={<FileSignature className="h-10 w-10" />}
      queryKey={['amc']}
      fetcher={() => amcContractService.list()}
      filters={{ status: ['ACTIVE', 'EXPIRING', 'EXPIRED', 'RENEWED'] }}
      columns={[
        { key: 'contractNumber', label: 'Contract #' },
        { key: 'vendorName', label: 'Vendor' },
        { key: 'contractType', label: 'Type' },
        { key: 'startDate', label: 'Start', render: a => a.startDate ? formatDate(String(a.startDate)) : '—' },
        { key: 'endDate', label: 'End', render: a => a.endDate ? formatDate(String(a.endDate)) : '—' },
        { key: 'contractValue', label: 'Value', align: 'right', render: a => a.contractValue ? `₹${Number(a.contractValue).toLocaleString()}` : '—' },
        { key: 'status', label: 'Status', render: a => <Badge variant={a.status === 'EXPIRED' ? 'destructive' : a.status === 'EXPIRING' ? 'warning' : 'success'}>{String(a.status)}</Badge> },
      ]}
      formFields={[
        { name: 'contractNumber', label: 'Contract number', type: 'text', required: true },
        { name: 'vendorName', label: 'Vendor', type: 'text', required: true },
        { name: 'contractType', label: 'Type', type: 'select', options: [
          { value: 'AMC', label: 'AMC' }, { value: 'WARRANTY', label: 'Warranty' },
          { value: 'SOFTWARE_LICENSE', label: 'Software license' }, { value: 'LEASE', label: 'Lease' },
        ] },
        { name: 'startDate', label: 'Start date', type: 'date', required: true },
        { name: 'endDate', label: 'End date', type: 'date', required: true },
        { name: 'contractValue', label: 'Contract value', type: 'currency' },
        { name: 'notes', label: 'Notes', type: 'textarea', span: 2 },
      ]}
      onCreate={v => amcContractService.create(v)}
      onUpdate={(id, v) => amcContractService.update(id, v)}
      onDelete={id => amcContractService.remove(id)}
    />
  )
}
