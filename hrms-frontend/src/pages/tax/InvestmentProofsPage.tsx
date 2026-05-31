import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useRef, useState } from 'react'
import { FileText, Upload } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Skeleton } from '@/components/ui/skeleton'
import { PageHeader } from '@/components/ui/page-header'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { taxService, type InvestmentProof } from '@/services/taxService'
import { useAuthStore } from '@/store/authStore'
import { toast } from 'sonner'

const SECTIONS = [
  'SEC_80C','SEC_80CCD_1B','SEC_80D','SEC_80E','SEC_80EE','SEC_80G',
  'HRA_RENT','LTA','SEC_24B_HOME_LOAN_INT','PRINCIPAL_REPAYMENT','OTHER',
]

const fy = `${new Date().getFullYear()}-${String(new Date().getFullYear() + 1).slice(2)}`

export function InvestmentProofsPage() {
  const qc = useQueryClient()
  const user = useAuthStore(s => s.user)
  const empId = user?.employeeId || user?.id || ''
  const fileRef = useRef<HTMLInputElement>(null)
  const [section, setSection] = useState('SEC_80C')
  const [amount, setAmount] = useState('')

  const { data, isLoading } = useQuery({
    queryKey: ['proofs', empId, fy],
    queryFn: () => taxService.myProofs(empId, fy),
    enabled: !!empId,
  })

  const upload = useMutation({
    mutationFn: (file: File) => taxService.uploadProof(file, {
      employeeId: empId, financialYear: fy, section, claimedAmount: Number(amount || 0),
    }),
    onSuccess: () => { toast.success('Proof uploaded'); qc.invalidateQueries({ queryKey: ['proofs'] }); setAmount('') },
  })

  return (
    <div className="space-y-6">
      <PageHeader title="Investment Proofs" description={`Submit evidence for FY ${fy}`} />

      <Card>
        <CardHeader><CardTitle>Upload new proof</CardTitle></CardHeader>
        <CardContent className="grid grid-cols-1 md:grid-cols-3 gap-3">
          <div>
            <Label>Section</Label>
            <Select value={section} onValueChange={setSection}>
              <SelectTrigger><SelectValue /></SelectTrigger>
              <SelectContent>
                {SECTIONS.map(s => <SelectItem key={s} value={s}>{s}</SelectItem>)}
              </SelectContent>
            </Select>
          </div>
          <div>
            <Label>Claimed amount (₹)</Label>
            <Input type="number" value={amount} onChange={e => setAmount(e.target.value)} />
          </div>
          <div className="flex items-end gap-2">
            <input ref={fileRef} type="file" hidden onChange={e => {
              const f = e.target.files?.[0]; if (f && amount) upload.mutate(f)
            }} />
            <Button onClick={() => fileRef.current?.click()} disabled={!amount}>
              <Upload className="h-4 w-4 mr-1" /> Upload
            </Button>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader><CardTitle>Submitted proofs</CardTitle></CardHeader>
        <CardContent>
          {isLoading ? <Skeleton className="h-32" /> : (data || []).length === 0 ? (
            <p className="text-sm text-slate-500 flex items-center gap-2"><FileText className="h-4 w-4" /> No proofs uploaded yet</p>
          ) : (
            <table className="w-full text-sm">
              <thead className="text-xs uppercase text-slate-500">
                <tr><th className="p-2 text-left">Section</th><th className="p-2 text-right">Claimed</th><th className="p-2 text-right">Verified</th><th className="p-2 text-left">Status</th></tr>
              </thead>
              <tbody>
                {(data as InvestmentProof[] || []).map(p => (
                  <tr key={p.id} className="border-t">
                    <td className="p-2">{p.section} {p.subSection && <span className="text-xs text-slate-400">/ {p.subSection}</span>}</td>
                    <td className="p-2 text-right">₹{p.claimedAmount?.toLocaleString()}</td>
                    <td className="p-2 text-right">{p.verifiedAmount ? `₹${p.verifiedAmount.toLocaleString()}` : '—'}</td>
                    <td className="p-2"><Badge>{p.status}</Badge></td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </CardContent>
      </Card>
    </div>
  )
}
