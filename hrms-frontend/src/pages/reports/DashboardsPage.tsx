import { LayoutDashboard, Star } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { ResourcePage } from '@/components/ui/resource-page'
import { reportsService } from '@/services/reportsService'

interface Dash extends Record<string, unknown> { id: string; name: string; description?: string; isDefault?: boolean; shared?: boolean }

export function DashboardsPage() {
  return (
    <ResourcePage<Dash>
      title="Dashboards"
      description="Build and manage analytics dashboards"
      icon={<LayoutDashboard className="h-10 w-10" />}
      queryKey={['dashboards', 'me']}
      fetcher={() => reportsService.listDashboards()}
      columns={[
        { key: 'name', label: 'Dashboard' },
        { key: 'description', label: 'Description' },
        { key: 'isDefault', label: 'Default', render: d => d.isDefault ? <Badge variant="success">Default</Badge> : '—' },
        { key: 'shared', label: 'Shared', render: d => d.shared ? <Badge>Shared</Badge> : '—' },
      ]}
      formFields={[
        { name: 'name', label: 'Name', type: 'text', required: true, span: 2 },
        { name: 'description', label: 'Description', type: 'textarea', span: 2 },
        { name: 'shared', label: 'Share with team', type: 'switch' },
      ]}
      onCreate={v => reportsService.createDashboard(v)}
      onUpdate={(id, v) => reportsService.updateDashboard(id, v)}
      onDelete={id => reportsService.deleteDashboard(id)}
      rowActions={d => [
        { label: 'Set as default', icon: <Star className="h-3.5 w-3.5" />, show: !d.isDefault, run: () => reportsService.setDefaultDashboard(d.id) },
      ]}
    />
  )
}
