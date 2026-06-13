import { LayoutGrid } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { ResourcePage } from '@/components/ui/resource-page'
import { Catalog } from '@/services/catalog'

interface Widget extends Record<string, unknown> { id: string; title: string; type?: string; dashboardId?: string }

export function DashboardWidgetsPage() {
  return (
    <ResourcePage<Widget>
      title="Dashboard Widgets"
      description="Charts, counters and tables placed on dashboards"
      icon={<LayoutGrid className="h-10 w-10" />}
      queryKey={['dashboard-widgets']}
      fetcher={() => Catalog.reports.widgets.list()}
      filters={{ type: ['COUNTER', 'BAR', 'LINE', 'PIE', 'TABLE', 'GAUGE'] }}
      columns={[
        { key: 'title', label: 'Widget' },
        { key: 'type', label: 'Type', render: w => w.type ? <Badge>{String(w.type)}</Badge> : '—' },
        { key: 'dashboardId', label: 'Dashboard' },
      ]}
      formFields={[
        { name: 'title', label: 'Widget title', type: 'text', required: true, span: 2 },
        { name: 'dashboardId', label: 'Dashboard ID', type: 'text', required: true },
        { name: 'type', label: 'Type', type: 'select', required: true, options: [
          { value: 'COUNTER', label: 'Counter' }, { value: 'BAR', label: 'Bar chart' }, { value: 'LINE', label: 'Line chart' },
          { value: 'PIE', label: 'Pie chart' }, { value: 'TABLE', label: 'Table' }, { value: 'GAUGE', label: 'Gauge' },
        ] },
        { name: 'reportDefinitionId', label: 'Report definition ID', type: 'text' },
        { name: 'sortOrder', label: 'Order', type: 'number' },
      ]}
      onCreate={v => Catalog.reports.widgets.create(v)}
      onUpdate={(id, v) => Catalog.reports.widgets.update(id, v)}
      onDelete={id => Catalog.reports.widgets.delete(id)}
    />
  )
}
