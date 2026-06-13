import { Tag } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { ResourcePage } from '@/components/ui/resource-page'
import { Catalog } from '@/services/catalog'

interface Cat extends Record<string, unknown> { id: string; name: string; slaHours?: number; defaultAssigneeId?: string; active?: boolean }

export function TicketCategoriesPage() {
  return (
    <ResourcePage<Cat>
      title="Ticket Categories"
      description="Helpdesk categories with SLA and default assignee"
      icon={<Tag className="h-10 w-10" />}
      queryKey={['ticket-categories']}
      fetcher={() => Catalog.tickets.categories.list()}
      columns={[
        { key: 'name', label: 'Category' },
        { key: 'slaHours', label: 'SLA (hrs)', align: 'right' },
        { key: 'defaultAssigneeId', label: 'Default assignee' },
        { key: 'active', label: 'Active', render: c => <Badge variant={c.active === false ? 'secondary' : 'success'}>{c.active === false ? 'Inactive' : 'Active'}</Badge> },
      ]}
      formFields={[
        { name: 'name', label: 'Name', type: 'text', required: true, span: 2 },
        { name: 'slaHours', label: 'SLA (hours)', type: 'number' },
        { name: 'defaultAssigneeId', label: 'Default assignee ID', type: 'text' },
        { name: 'description', label: 'Description', type: 'textarea', span: 2 },
      ]}
      onCreate={v => Catalog.tickets.categories.create(v)}
      onUpdate={(id, v) => Catalog.tickets.categories.update(id, v)}
      onDelete={id => Catalog.tickets.categories.delete(id)}
    />
  )
}
