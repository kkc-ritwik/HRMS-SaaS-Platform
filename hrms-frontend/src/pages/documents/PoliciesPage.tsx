import { useQuery, useMutation } from '@tanstack/react-query'
import { ShieldCheck } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { DataList } from '@/components/ui/data-list'
import { companyPolicyService } from '@/services/extendedServices'
import { toast } from 'sonner'

interface Policy { id: string; name: string; category?: string; version?: string; effectiveFrom?: string; acknowledgedByMe?: boolean }

export function PoliciesPage() {
  const { data, isLoading, refetch } = useQuery({ queryKey: ['policies'], queryFn: () => companyPolicyService.list() })
  const ack = useMutation({
    mutationFn: (id: string) => companyPolicyService.acknowledge(id),
    onSuccess: () => { toast.success('Acknowledged'); refetch() },
  })
  const items: Policy[] = (data as { content?: Policy[] } | undefined)?.content
    || (Array.isArray(data) ? data as Policy[] : [])
  return (
    <DataList<Policy>
      title="Company Policies" description="Active policies you must acknowledge"
      data={items} isLoading={isLoading}
      emptyIcon={<ShieldCheck className="h-10 w-10" />} emptyTitle="No policies"
      columns={[
        { key: 'name', label: 'Policy' },
        { key: 'category', label: 'Category' },
        { key: 'version', label: 'Version' },
        { key: 'effectiveFrom', label: 'Effective' },
        { key: 'acknowledgedByMe', label: 'Status', render: p => p.acknowledgedByMe
          ? <Badge>Acknowledged</Badge>
          : <Button size="sm" onClick={() => ack.mutate(p.id)}>Acknowledge</Button>
        },
      ]}
    />
  )
}
