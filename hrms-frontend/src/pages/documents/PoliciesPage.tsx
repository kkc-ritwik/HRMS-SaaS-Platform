import { ShieldCheck } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { ResourcePage } from '@/components/ui/resource-page'
import { companyPolicyService } from '@/services/extendedServices'
import { Catalog } from '@/services/catalog'
import { formatDate } from '@/lib/utils'

interface Policy extends Record<string, unknown> { id: string; name?: string; title?: string; category?: string; version?: string; effectiveFrom?: string; status?: string }

export function PoliciesPage() {
  return (
    <ResourcePage<Policy>
      title="Company Policies"
      description="Publish and manage company policy documents"
      icon={<ShieldCheck className="h-10 w-10" />}
      queryKey={['policies']}
      fetcher={() => companyPolicyService.list()}
      columns={[
        { key: 'name', label: 'Policy', render: p => String(p.name ?? p.title ?? '—') },
        { key: 'category', label: 'Category' },
        { key: 'version', label: 'Version' },
        { key: 'effectiveFrom', label: 'Effective', render: p => p.effectiveFrom ? formatDate(String(p.effectiveFrom)) : '—' },
        { key: 'status', label: 'Status', render: p => p.status ? <Badge>{String(p.status)}</Badge> : '—' },
      ]}
      formFields={[
        { name: 'title', label: 'Policy title', type: 'text', required: true, span: 2 },
        { name: 'category', label: 'Category', type: 'text' },
        { name: 'version', label: 'Version', type: 'text' },
        { name: 'effectiveFrom', label: 'Effective from', type: 'date' },
        { name: 'content', label: 'Content', type: 'textarea', span: 2 },
      ]}
      onCreate={v => companyPolicyService.create(v)}
      onUpdate={(id, v) => companyPolicyService.update(id, v)}
      onDelete={id => Catalog.documents.policies.delete(id)}
    />
  )
}
