import { useQuery } from '@tanstack/react-query'
import { BadgeCheck } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { DataList } from '@/components/ui/data-list'
import { licenseService } from '@/services/extendedServices'

interface License { id: string; name: string; licenseNumber: string; issuingAuthority?: string; issueDate?: string; expiryDate: string; status: string }

export function LicensesPage() {
  const { data, isLoading } = useQuery({ queryKey: ['licenses'], queryFn: () => licenseService.list() })
  const items: License[] = (data as { content?: License[] } | undefined)?.content
    || (Array.isArray(data) ? data as License[] : [])
  return (
    <DataList<License>
      title="Licenses & Registrations" description="Corporate licenses with renewal alerts"
      data={items} isLoading={isLoading}
      emptyIcon={<BadgeCheck className="h-10 w-10" />} emptyTitle="No licenses on record"
      columns={[
        { key: 'name', label: 'License' },
        { key: 'licenseNumber', label: 'Number' },
        { key: 'issuingAuthority', label: 'Authority' },
        { key: 'issueDate', label: 'Issued' },
        { key: 'expiryDate', label: 'Expires' },
        { key: 'status', label: 'Status', render: l => <Badge>{l.status}</Badge> },
      ]}
    />
  )
}
