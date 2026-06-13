import { useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation } from '@tanstack/react-query'
import { ArrowLeft, Play, Download, CalendarClock, Share2 } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { PageHeader } from '@/components/ui/page-header'
import { Skeleton } from '@/components/ui/skeleton'
import { EmptyState } from '@/components/ui/empty-state'
import { FormDialog } from '@/components/ui/form-dialog'
import { reportsService } from '@/services/reportsService'
import { Catalog } from '@/services/catalog'
import { toast } from 'sonner'

type AnyObj = Record<string, unknown>

export function ReportDetailPage() {
  const { id = '' } = useParams()
  const navigate = useNavigate()
  const [result, setResult] = useState<{ columns: string[]; rows: AnyObj[] } | null>(null)
  const [scheduling, setScheduling] = useState(false)

  const defQ = useQuery({ queryKey: ['report-def', id], queryFn: () => Catalog.reports.definitions.get(id), enabled: !!id })
  const def = (defQ.data ?? {}) as AnyObj

  const run = useMutation({
    mutationFn: () => reportsService.runReport(id, {}),
    onSuccess: (d: unknown) => {
      const obj = d as { columns?: string[]; rows?: AnyObj[] }
      setResult({ columns: obj.columns ?? [], rows: obj.rows ?? [] })
      toast.success('Report ran')
    },
    onError: () => toast.error('Run failed'),
  })

  const exportAs = async (format: 'csv' | 'xlsx' | 'pdf') => {
    try { await reportsService.exportReport(id, {}, format); toast.success(`Exported ${format.toUpperCase()}`) }
    catch { toast.error('Export failed') }
  }

  const scheduleMut = useMutation({
    mutationFn: (v: Record<string, unknown>) =>
      reportsService.schedule(id, String(v.cronExpression), String(v.recipients ?? '').split(',').map(s => s.trim()).filter(Boolean)),
    onSuccess: () => { toast.success('Schedule created'); setScheduling(false) },
    onError: () => toast.error('Schedule failed'),
  })

  return (
    <div className="space-y-5 animate-fade-in">
      <Button variant="ghost" size="sm" onClick={() => navigate('/reports')}><ArrowLeft className="h-4 w-4 mr-1" /> Back to reports</Button>
      <PageHeader
        title={defQ.isLoading ? 'Loading…' : String(def.name ?? 'Report')}
        description={String(def.description ?? 'Report definition')}
        action={
          <div className="flex gap-2">
            <Button size="sm" onClick={() => run.mutate()} disabled={run.isPending}><Play className="h-4 w-4 mr-1" /> Run</Button>
            <Button size="sm" variant="outline" onClick={() => exportAs('csv')}><Download className="h-4 w-4 mr-1" /> CSV</Button>
            <Button size="sm" variant="outline" onClick={() => exportAs('xlsx')}>XLSX</Button>
            <Button size="sm" variant="outline" onClick={() => exportAs('pdf')}>PDF</Button>
            <Button size="sm" variant="outline" onClick={() => setScheduling(true)}><CalendarClock className="h-4 w-4 mr-1" /> Schedule</Button>
          </div>
        }
      />

      <Card>
        <CardHeader><CardTitle className="text-sm">Result</CardTitle></CardHeader>
        <CardContent>
          {run.isPending ? <Skeleton className="h-40" /> : !result ? (
            <EmptyState icon={<Play className="h-8 w-8" />} title="Run the report to see results" />
          ) : result.rows.length === 0 ? (
            <EmptyState icon={<Share2 className="h-8 w-8" />} title="No rows returned" />
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead className="bg-slate-50 text-xs uppercase text-slate-500">
                  <tr>{result.columns.map(c => <th key={c} className="p-2 text-left">{c}</th>)}</tr>
                </thead>
                <tbody>
                  {result.rows.slice(0, 200).map((row, i) => (
                    <tr key={i} className="border-t border-slate-100">
                      {result.columns.map(c => <td key={c} className="p-2">{String(row[c] ?? '')}</td>)}
                    </tr>
                  ))}
                </tbody>
              </table>
              {result.rows.length > 200 && <p className="text-xs text-slate-400 mt-2">Showing first 200 of {result.rows.length} rows — export for the full set.</p>}
            </div>
          )}
        </CardContent>
      </Card>

      <FormDialog
        open={scheduling} onOpenChange={setScheduling} title="Schedule report" submitLabel="Schedule"
        fields={[
          { name: 'cronExpression', label: 'Cron expression', type: 'text', required: true, span: 2, helper: 'e.g. 0 8 * * MON (every Monday 8am)' },
          { name: 'format', label: 'Format', type: 'select', options: [
            { value: 'CSV', label: 'CSV' }, { value: 'XLSX', label: 'Excel' }, { value: 'PDF', label: 'PDF' },
          ] },
          { name: 'recipients', label: 'Recipients (comma-separated emails)', type: 'text', span: 2 },
        ]}
        onSubmit={v => scheduleMut.mutateAsync(v)}
      />
    </div>
  )
}
