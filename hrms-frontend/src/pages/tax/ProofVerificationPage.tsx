import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { ShieldCheck, Check, X } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { DataList } from '@/components/ui/data-list'
import { FormDialog } from '@/components/ui/form-dialog'
import { taxService } from '@/services/taxService'
import { useAuthStore } from '@/store/authStore'
import { toast } from 'sonner'

type AnyObj = Record<string, unknown>
function rows<T = AnyObj>(d: unknown): T[] {
  if (Array.isArray(d)) return d as T[]
  const o = d as { content?: T[] } | undefined
  return Array.isArray(o?.content) ? o!.content : []
}

export function ProofVerificationPage() {
  const qc = useQueryClient()
  const user = useAuthStore(s => s.user)
  const verifier = user?.employeeId || user?.id || ''
  const [verifyFor, setVerifyFor] = useState<AnyObj | null>(null)

  const { data, isLoading } = useQuery({ queryKey: ['proof-queue'], queryFn: () => taxService.proofQueue() })

  const verify = useMutation({
    mutationFn: (v: Record<string, unknown>) => taxService.verifyProof(String(verifyFor?.id), Number(v.verifiedAmount), verifier, String(v.comment ?? '')),
    onSuccess: () => { toast.success('Verified'); qc.invalidateQueries({ queryKey: ['proof-queue'] }); setVerifyFor(null) },
  })
  const reject = useMutation({
    mutationFn: (id: string) => taxService.rejectProof(id, verifier, 'Insufficient evidence'),
    onSuccess: () => { toast.success('Rejected'); qc.invalidateQueries({ queryKey: ['proof-queue'] }) },
  })

  return (
    <>
      <DataList<AnyObj>
        title="Investment Proof Verification"
        description="Review and verify employee tax-saving investment proofs"
        data={rows(data)} isLoading={isLoading}
        emptyIcon={<ShieldCheck className="h-10 w-10" />} emptyTitle="Queue is empty"
        filters={{ status: ['SUBMITTED', 'VERIFIED', 'REJECTED', 'PARTIAL'] }}
        columns={[
          { key: 'employeeName', label: 'Employee', render: p => String(p.employeeName ?? p.employeeId ?? '—') },
          { key: 'section', label: 'Section' },
          { key: 'claimedAmount', label: 'Claimed', align: 'right', render: p => p.claimedAmount ? `₹${Number(p.claimedAmount).toLocaleString()}` : '—' },
          { key: 'verifiedAmount', label: 'Verified', align: 'right', render: p => p.verifiedAmount ? `₹${Number(p.verifiedAmount).toLocaleString()}` : '—' },
          { key: 'status', label: 'Status', render: p => <Badge variant={p.status === 'REJECTED' ? 'destructive' : p.status === 'VERIFIED' ? 'success' : 'warning'}>{String(p.status)}</Badge> },
          { key: 'id', label: '', align: 'right', sortable: false, render: p => String(p.status) === 'SUBMITTED' ? (
            <div className="flex justify-end gap-1">
              <Button size="sm" variant="ghost" className="h-7 w-7 p-0 text-green-600" onClick={() => setVerifyFor(p)}><Check className="h-4 w-4" /></Button>
              <Button size="sm" variant="ghost" className="h-7 w-7 p-0 text-red-500" onClick={() => reject.mutate(String(p.id))}><X className="h-4 w-4" /></Button>
            </div>
          ) : null },
        ]}
      />
      <FormDialog
        open={!!verifyFor} onOpenChange={o => !o && setVerifyFor(null)}
        title="Verify proof" submitLabel="Verify"
        fields={[
          { name: 'verifiedAmount', label: 'Verified amount (₹)', type: 'currency', required: true, defaultValue: Number(verifyFor?.claimedAmount ?? 0) },
          { name: 'comment', label: 'Comment', type: 'textarea', span: 2 },
        ]}
        onSubmit={v => verify.mutateAsync(v)}
      />
    </>
  )
}
