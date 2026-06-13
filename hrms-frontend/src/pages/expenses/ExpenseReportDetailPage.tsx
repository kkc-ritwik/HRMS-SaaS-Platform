import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Receipt, Send, Check, X, ArrowLeft } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { PageHeader } from '@/components/ui/page-header'
import { Skeleton } from '@/components/ui/skeleton'
import { CrudSection } from '@/components/ui/crud-section'
import { expenseService } from '@/services/expenseService'
import { toast } from 'sonner'
import { formatDate } from '@/lib/utils'

type AnyObj = Record<string, unknown>
function rows<T = AnyObj>(d: unknown): T[] {
  if (Array.isArray(d)) return d as T[]
  const o = d as { content?: T[] } | undefined
  return Array.isArray(o?.content) ? o!.content : []
}

export function ExpenseReportDetailPage() {
  const { id = '' } = useParams()
  const navigate = useNavigate()
  const qc = useQueryClient()

  const claimQ = useQuery({ queryKey: ['expense-claim', id], queryFn: () => expenseService.getClaim(id), enabled: !!id })
  const itemsQ = useQuery({ queryKey: ['expense-items', id], queryFn: () => expenseService.itemsForReport(id), enabled: !!id })
  const catsQ = useQuery({ queryKey: ['expense-cats'], queryFn: () => expenseService.categoriesAll() })

  const claim = (claimQ.data ?? {}) as AnyObj
  const invalidate = () => { qc.invalidateQueries({ queryKey: ['expense-claim', id] }); qc.invalidateQueries({ queryKey: ['expenses'] }) }

  const act = useMutation({
    mutationFn: ({ kind }: { kind: 'submit' | 'approve' | 'reject' }) =>
      kind === 'submit' ? expenseService.submit(id)
        : kind === 'approve' ? expenseService.approve(id)
        : expenseService.reject(id, 'Rejected'),
    onSuccess: () => { toast.success('Done'); invalidate() },
    onError: () => toast.error('Action failed'),
  })

  const status = String(claim.status ?? '')
  const catOptions = rows<AnyObj>(catsQ.data).map(c => ({ value: String(c.id), label: String(c.name ?? c.code ?? c.id) }))

  return (
    <div className="space-y-5 animate-fade-in">
      <Button variant="ghost" size="sm" onClick={() => navigate('/expenses')}><ArrowLeft className="h-4 w-4 mr-1" /> Back to claims</Button>
      <PageHeader
        title={claimQ.isLoading ? 'Loading…' : `${String(claim.claimNumber ?? 'Claim')} — ${String(claim.title ?? '')}`}
        description="Expense claim detail"
        action={
          <div className="flex gap-2">
            {status === 'DRAFT' && <Button size="sm" onClick={() => act.mutate({ kind: 'submit' })}><Send className="h-4 w-4 mr-1" /> Submit</Button>}
            {status === 'SUBMITTED' && <>
              <Button size="sm" onClick={() => act.mutate({ kind: 'approve' })}><Check className="h-4 w-4 mr-1" /> Approve</Button>
              <Button size="sm" variant="outline" onClick={() => act.mutate({ kind: 'reject' })}><X className="h-4 w-4 mr-1" /> Reject</Button>
            </>}
          </div>
        }
      />

      <div className="grid grid-cols-1 md:grid-cols-4 gap-3">
        {claimQ.isLoading ? Array.from({ length: 4 }).map((_, i) => <Skeleton key={i} className="h-20 rounded-xl" />) : <>
          <Card><CardContent className="p-4"><p className="text-xs text-slate-500">Status</p><Badge>{status}</Badge></CardContent></Card>
          <Card><CardContent className="p-4"><p className="text-xs text-slate-500">Total</p><p className="text-lg font-bold">{String(claim.currency ?? '₹')} {Number(claim.totalAmount ?? 0).toLocaleString()}</p></CardContent></Card>
          <Card><CardContent className="p-4"><p className="text-xs text-slate-500">Items</p><p className="text-lg font-bold">{rows(itemsQ.data).length}</p></CardContent></Card>
          <Card><CardContent className="p-4"><p className="text-xs text-slate-500">Submitted</p><p className="text-sm">{claim.submittedAt ? formatDate(String(claim.submittedAt)) : '—'}</p></CardContent></Card>
        </>}
      </div>

      <CrudSection
        title="Line Items" icon={<Receipt className="h-6 w-6" />}
        items={rows(itemsQ.data)} loading={itemsQ.isLoading}
        emptyText="No line items yet" queryKey={['expense-items', id]}
        fields={[
          { name: 'description', label: 'Description', type: 'text', required: true, span: 2 },
          { name: 'categoryId', label: 'Category', type: 'select', options: catOptions },
          { name: 'amount', label: 'Amount', type: 'currency', required: true },
          { name: 'spentOn', label: 'Spent on', type: 'date', required: true },
          { name: 'merchant', label: 'Merchant', type: 'text' },
        ]}
        onCreate={v => expenseService.addItem({ ...v, reportId: id })}
        onUpdate={(itemId, v) => expenseService.updateItem(itemId, v)}
        onDelete={itemId => expenseService.deleteItem(itemId)}
        renderItem={(it: AnyObj) => (<>
          <p className="text-sm font-medium">{String(it.description ?? '')}</p>
          <p className="text-xs text-slate-500">
            {String(claim.currency ?? '₹')} {Number(it.amount ?? 0).toLocaleString()}
            {it.spentOn ? ` · ${formatDate(String(it.spentOn))}` : ''}{it.merchant ? ` · ${String(it.merchant)}` : ''}
          </p>
        </>)}
      />

      <Card>
        <CardHeader><CardTitle className="text-sm">Receipt OCR</CardTitle></CardHeader>
        <CardContent>
          <p className="text-xs text-slate-500 mb-2">Upload a receipt image to auto-extract amount, date and merchant.</p>
          <input id="ocr" type="file" accept="image/*,.pdf" hidden onChange={async e => {
            const f = e.target.files?.[0]; if (!f) return
            try { const r = await expenseService.parseReceipt(f); toast.success('Parsed'); console.log('OCR', r) }
            catch { toast.error('OCR failed') }
          }} />
          <Button size="sm" variant="outline" onClick={() => document.getElementById('ocr')?.click()}>Scan a receipt</Button>
        </CardContent>
      </Card>
    </div>
  )
}
