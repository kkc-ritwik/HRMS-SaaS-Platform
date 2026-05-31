import { useQuery } from '@tanstack/react-query'
import { useState } from 'react'
import { Shield } from 'lucide-react'
import { Input } from '@/components/ui/input'
import { Badge } from '@/components/ui/badge'
import { DataList } from '@/components/ui/data-list'
import { complianceService, type AuditLog } from '@/services/complianceService'

export function AuditLogPage() {
  const [entity, setEntity] = useState('')
  const { data, isLoading } = useQuery({
    queryKey: ['audit', entity],
    queryFn: () => complianceService.searchAudit({ entityName: entity || undefined }),
  })
  const rows: AuditLog[] = (data as { content?: AuditLog[] } | undefined)?.content
    || (Array.isArray(data) ? data : [])

  return (
    <div className="space-y-4">
      <Input placeholder="Filter by entity name (e.g. Employee)" value={entity} onChange={e => setEntity(e.target.value)} />
      <DataList<AuditLog>
        title="Audit Log"
        description="Tamper-evident trail of all changes"
        data={rows}
        isLoading={isLoading}
        emptyIcon={<Shield className="h-10 w-10" />}
        emptyTitle="No audit events"
        columns={[
          { key: 'createdAt', label: 'When' },
          { key: 'entityName', label: 'Entity' },
          { key: 'entityId', label: 'ID' },
          { key: 'action', label: 'Action', render: a => <Badge>{a.action}</Badge> },
          { key: 'actorEmail', label: 'Actor' },
        ]}
      />
    </div>
  )
}
