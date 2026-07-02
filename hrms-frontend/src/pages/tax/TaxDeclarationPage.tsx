import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { Save, Check, X, ShieldCheck, Plus } from 'lucide-react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Skeleton } from '@/components/ui/skeleton'
import { PageHeader } from '@/components/ui/page-header'
import { FormDialog } from '@/components/ui/form-dialog'
import { taxService, type TaxDeclaration, type TaxRegime, type DeclarationProof } from '@/services/taxService'
import { useAuthStore } from '@/store/authStore'
import { getErrorMessage } from '@/lib/api'
import { toast } from 'sonner'

const fy = `${new Date().getFullYear()}-${String(new Date().getFullYear() + 1).slice(2)}`

export function TaxDeclarationPage() {
  const qc = useQueryClient()
  const user = useAuthStore(s => s.user)
  const empId = user?.employeeId || user?.id || ''
  const { data, isLoading } = useQuery({
    queryKey: ['tax', 'me', fy],
    queryFn: () => taxService.myDeclaration(fy),
  })
  const [form, setForm] = useState<Partial<TaxDeclaration>>({})
  const [addingProof, setAddingProof] = useState(false)
  const declarationId = data?.id

  const save = useMutation({
    mutationFn: () => taxService.saveDeclaration({ employeeId: empId, financialYear: fy, ...(data || {}), ...form }),
    onSuccess: () => { toast.success('Saved'); qc.invalidateQueries({ queryKey: ['tax', 'me', fy] }) },
  })

  const proofs = useQuery({
    queryKey: ['tax', 'proofs', declarationId],
    queryFn: () => taxService.declarationProofs(declarationId!) as Promise<DeclarationProof[]>,
    enabled: !!declarationId,
  })
  const onErr = (e: unknown) => toast.error(getErrorMessage(e))
  const invProofs = () => qc.invalidateQueries({ queryKey: ['tax', 'proofs', declarationId] })
  const addProof = useMutation({
    mutationFn: (v: Record<string, unknown>) => taxService.addDeclarationProof(declarationId!, {
      section: v.section, description: v.description,
      declaredAmount: Number(v.declaredAmount ?? 0),
      proofAmount: v.proofAmount ? Number(v.proofAmount) : undefined,
      proofUrl: v.proofUrl || undefined,
    }),
    onSuccess: () => { toast.success('Proof added'); setAddingProof(false); invProofs() }, onError: onErr,
  })
  const approveProof = useMutation({
    mutationFn: (proofId: string) => taxService.approveDeclarationProof(declarationId!, proofId),
    onSuccess: () => { toast.success('Proof approved'); invProofs() }, onError: onErr,
  })
  const rejectProof = useMutation({
    mutationFn: (proofId: string) => taxService.rejectDeclarationProof(declarationId!, proofId),
    onSuccess: () => { toast.success('Proof rejected'); invProofs() }, onError: onErr,
  })
  const verifyDeclaration = useMutation({
    mutationFn: () => taxService.verifyDeclaration(declarationId!),
    onSuccess: () => { toast.success('Declaration verified'); qc.invalidateQueries({ queryKey: ['tax', 'me', fy] }) }, onError: onErr,
  })

  if (isLoading) return <Skeleton className="h-64" />
  const d = { ...data, ...form } as TaxDeclaration
  const proofList = (proofs.data as DeclarationProof[] | undefined) ?? []

  const setField = <K extends keyof TaxDeclaration>(k: K, v: TaxDeclaration[K]) => setForm(prev => ({ ...prev, [k]: v }))

  return (
    <div className="space-y-6">
      <PageHeader title={`Tax Declaration · FY ${fy}`} description="Declare investments and exemptions to optimise TDS" />

      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <CardTitle>Regime</CardTitle>
            <div className="flex gap-2">
              {(['OLD', 'NEW'] as TaxRegime[]).map(r => (
                <Button key={r} size="sm" variant={d.regime === r ? 'default' : 'outline'} onClick={() => setField('regime', r)}>{r}</Button>
              ))}
            </div>
          </div>
        </CardHeader>
        <CardContent className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {(['section80c','section80d','section80e','section80g','section24b','hraExemptionClaimed','ltaClaimed','otherIncome','previousEmployerIncome','previousEmployerTds'] as const).map(field => (
            <div key={field}>
              <Label className="text-xs uppercase">{field.replace(/([A-Z])/g, ' $1')}</Label>
              <Input
                type="number"
                value={d[field] ?? 0}
                onChange={e => setField(field as keyof TaxDeclaration, Number(e.target.value) as never)}
              />
            </div>
          ))}
        </CardContent>
      </Card>

      <div className="flex gap-2">
        <Button onClick={() => save.mutate()}><Save className="h-4 w-4 mr-1" /> Save declaration</Button>
      </div>

      {declarationId && (
        <Card>
          <CardHeader>
            <div className="flex items-center justify-between">
              <CardTitle className="flex items-center gap-2"><ShieldCheck className="h-5 w-5" /> Supporting proofs</CardTitle>
              <div className="flex gap-2">
                <Button size="sm" variant="outline" onClick={() => setAddingProof(true)}><Plus className="h-4 w-4 mr-1" /> Add proof</Button>
                <Button size="sm" onClick={() => verifyDeclaration.mutate()} loading={verifyDeclaration.isPending}>Verify declaration</Button>
              </div>
            </div>
          </CardHeader>
          <CardContent className="space-y-2">
            {proofs.isLoading ? <Skeleton className="h-20" /> : proofList.length === 0 ? (
              <p className="text-sm text-slate-500">No proofs uploaded for this declaration yet.</p>
            ) : proofList.map(p => (
              <div key={p.id} className="flex items-center justify-between rounded border p-3">
                <div>
                  <p className="text-sm font-medium">{p.section}{p.description ? ` · ${p.description}` : ''}</p>
                  <p className="text-xs text-slate-500">
                    Declared ₹{Number(p.declaredAmount ?? 0).toLocaleString()}
                    {p.proofAmount != null ? ` · Proof ₹${Number(p.proofAmount).toLocaleString()}` : ''}
                    {p.proofUrl ? <> · <a className="text-brand-600 hover:underline" href={p.proofUrl} target="_blank" rel="noreferrer">document</a></> : ''}
                  </p>
                </div>
                <div className="flex items-center gap-2">
                  <Badge variant={p.status === 'APPROVED' ? 'success' : p.status === 'REJECTED' ? 'destructive' : 'warning'}>{p.status}</Badge>
                  {p.status === 'PENDING' && (
                    <>
                      <Button size="sm" variant="ghost" className="h-7 w-7 p-0 text-green-600" onClick={() => approveProof.mutate(p.id)}><Check className="h-4 w-4" /></Button>
                      <Button size="sm" variant="ghost" className="h-7 w-7 p-0 text-red-500" onClick={() => rejectProof.mutate(p.id)}><X className="h-4 w-4" /></Button>
                    </>
                  )}
                </div>
              </div>
            ))}
          </CardContent>
        </Card>
      )}

      <FormDialog
        open={addingProof} onOpenChange={setAddingProof} title="Add supporting proof" submitLabel="Add"
        fields={[
          { name: 'section', label: 'Section', type: 'text', required: true },
          { name: 'description', label: 'Description', type: 'text', span: 2 },
          { name: 'declaredAmount', label: 'Declared amount (₹)', type: 'currency', required: true },
          { name: 'proofAmount', label: 'Proof amount (₹)', type: 'currency' },
          { name: 'proofUrl', label: 'Proof document URL', type: 'text', span: 2 },
        ]}
        onSubmit={v => addProof.mutateAsync(v)}
      />
    </div>
  )
}
