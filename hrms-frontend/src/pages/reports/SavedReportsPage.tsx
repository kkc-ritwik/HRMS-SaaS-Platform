import { FileBarChart, Share2 } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { ResourcePage } from '@/components/ui/resource-page'
import { reportsService } from '@/services/reportsService'
import { Catalog } from '@/services/catalog'
import { formatDate } from '@/lib/utils'

interface Saved extends Record<string, unknown> { id: string; name: string; status?: string; fileFormat?: string; generatedAt?: string }

export function SavedReportsPage() {
  return (
    <ResourcePage<Saved>
      title="Saved Reports"
      description="Previously generated report snapshots"
      icon={<FileBarChart className="h-10 w-10" />}
      queryKey={['saved-reports', 'me']}
      fetcher={() => Catalog.reports.saved.mine()}
      filters={{ status: ['PENDING', 'COMPLETED', 'FAILED'] }}
      columns={[
        { key: 'name', label: 'Report' },
        { key: 'fileFormat', label: 'Format', render: s => s.fileFormat ? <Badge>{String(s.fileFormat)}</Badge> : '—' },
        { key: 'status', label: 'Status', render: s => <Badge variant={s.status === 'FAILED' ? 'destructive' : s.status === 'COMPLETED' ? 'success' : 'warning'}>{String(s.status)}</Badge> },
        { key: 'generatedAt', label: 'Generated', render: s => s.generatedAt ? formatDate(String(s.generatedAt)) : '—' },
        { key: 'fileUrl', label: 'File', render: s => s.fileUrl ? <a className="text-brand-600 hover:underline" href={String(s.fileUrl)} target="_blank" rel="noreferrer" onClick={e => e.stopPropagation()}>Download</a> : '—' },
      ]}
      onDelete={id => Catalog.reports.saved.delete(id)}
      rowActions={s => [
        { label: 'Share', icon: <Share2 className="h-3.5 w-3.5" />, run: () => reportsService.shareReport(s.id, []) },
      ]}
    />
  )
}
