import { Hash, LogIn } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { ResourcePage } from '@/components/ui/resource-page'
import { groupService } from '@/services/adminServices'
import { groupsCatalog } from '@/services/catalog'

interface Group extends Record<string, unknown> { id: string; name: string; description?: string; privacy?: string; memberCount?: number }

export function GroupsPage() {
  return (
    <ResourcePage<Group>
      title="Groups"
      description="Interest groups & Employee Resource Groups (ERGs)"
      icon={<Hash className="h-10 w-10" />}
      queryKey={['groups']}
      fetcher={() => groupService.list() as Promise<unknown>}
      rowHref={g => `/groups/${g.id}`}
      filters={{ privacy: ['PUBLIC', 'PRIVATE'] }}
      columns={[
        { key: 'name', label: 'Group' },
        { key: 'description', label: 'About' },
        { key: 'privacy', label: 'Privacy', render: g => <Badge>{String(g.privacy ?? 'PUBLIC')}</Badge> },
        { key: 'memberCount', label: 'Members', align: 'right' },
      ]}
      formFields={[
        { name: 'name', label: 'Group name', type: 'text', required: true, span: 2 },
        { name: 'description', label: 'Description', type: 'textarea', span: 2 },
        { name: 'privacy', label: 'Privacy', type: 'select', required: true, options: [
          { value: 'PUBLIC', label: 'Public' }, { value: 'PRIVATE', label: 'Private' },
        ] },
      ]}
      createTitle="Create group"
      onCreate={v => groupService.create(v)}
      onUpdate={(id, v) => groupsCatalog.update(id, v)}
      onDelete={id => groupsCatalog.delete(id)}
      rowActions={g => [
        { label: 'Join', icon: <LogIn className="h-3.5 w-3.5" />, run: () => groupService.join(g.id) },
      ]}
    />
  )
}
