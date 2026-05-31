import { useQuery } from '@tanstack/react-query'
import { Award } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { DataList } from '@/components/ui/data-list'
import { lmsService, type Certification } from '@/services/lmsService'

export function CertificationsPage() {
  const { data, isLoading } = useQuery({ queryKey: ['certs', 'me'], queryFn: lmsService.myCertifications })
  return (
    <DataList<Certification>
      title="My Certifications" description="External + internal certifications"
      data={data || []} isLoading={isLoading}
      emptyIcon={<Award className="h-10 w-10" />} emptyTitle="No certifications"
      columns={[
        { key: 'certificateName', label: 'Certificate' },
        { key: 'issuedBy', label: 'Issued by' },
        { key: 'issueDate', label: 'Issued' },
        { key: 'expiryDate', label: 'Expires' },
        { key: 'status', label: 'Status', render: c => <Badge>{c.status}</Badge> },
      ]}
    />
  )
}
