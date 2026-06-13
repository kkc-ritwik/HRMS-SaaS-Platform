import { BellRing } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { ResourcePage } from '@/components/ui/resource-page'
import { Catalog } from '@/services/catalog'

interface Pref extends Record<string, unknown> { id: string; eventType?: string; channel?: string; enabled?: boolean }

export function NotificationPreferencesPage() {
  return (
    <ResourcePage<Pref>
      title="Notification Preferences"
      description="Per-event channel routing (email / in-app / SMS / push)"
      icon={<BellRing className="h-10 w-10" />}
      queryKey={['notification-preferences']}
      fetcher={() => Catalog.notifications.preferences.list()}
      filters={{ channel: ['EMAIL', 'IN_APP', 'SMS', 'PUSH', 'SLACK'] }}
      columns={[
        { key: 'eventType', label: 'Event' },
        { key: 'channel', label: 'Channel', render: p => p.channel ? <Badge>{String(p.channel)}</Badge> : '—' },
        { key: 'enabled', label: 'Enabled', render: p => <Badge variant={p.enabled === false ? 'secondary' : 'success'}>{p.enabled === false ? 'Off' : 'On'}</Badge> },
      ]}
      formFields={[
        { name: 'eventType', label: 'Event type', type: 'text', required: true, span: 2, helper: 'e.g. LEAVE_APPROVED, PAYSLIP_READY' },
        { name: 'channel', label: 'Channel', type: 'select', required: true, options: [
          { value: 'EMAIL', label: 'Email' }, { value: 'IN_APP', label: 'In-app' }, { value: 'SMS', label: 'SMS' },
          { value: 'PUSH', label: 'Push' }, { value: 'SLACK', label: 'Slack' },
        ] },
        { name: 'employeeId', label: 'Employee ID (blank = tenant default)', type: 'text' },
        { name: 'enabled', label: 'Enabled', type: 'switch', defaultValue: true },
      ]}
      onCreate={v => Catalog.notifications.preferences.create(v)}
      onUpdate={(id, v) => Catalog.notifications.preferences.update(id, v)}
      onDelete={id => Catalog.notifications.preferences.delete(id)}
    />
  )
}
