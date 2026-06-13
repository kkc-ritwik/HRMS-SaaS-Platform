import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Receipt } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Skeleton } from '@/components/ui/skeleton'
import { PageHeader } from '@/components/ui/page-header'
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { taxConfigService } from '@/services/salaryService'
import { toast } from 'sonner'

type AnyObj = Record<string, unknown>

function ConfigForm({ value, onSave, rows: fieldRows }: { value: AnyObj; onSave: (v: AnyObj) => void; rows: { name: string; label: string }[] }) {
  const [form, setForm] = useState<AnyObj>(value)
  return (
    <div className="space-y-3 max-w-md">
      {fieldRows.map(f => (
        <div key={f.name}>
          <Label>{f.label}</Label>
          <Input type="number" value={String(form[f.name] ?? '')} onChange={e => setForm(s => ({ ...s, [f.name]: e.target.value === '' ? '' : Number(e.target.value) }))} />
        </div>
      ))}
      <Button size="sm" onClick={() => onSave(form)}>Save</Button>
    </div>
  )
}

export function TaxConfigPage() {
  const qc = useQueryClient()
  const [tab, setTab] = useState<'pf' | 'esi' | 'pt'>('pf')
  const [state, setState] = useState('KA')

  const pfQ = useQuery({ queryKey: ['tax-pf'], queryFn: () => taxConfigService.pf(), enabled: tab === 'pf' })
  const esiQ = useQuery({ queryKey: ['tax-esi'], queryFn: () => taxConfigService.esi(), enabled: tab === 'esi' })
  const ptQ = useQuery({ queryKey: ['tax-pt', state], queryFn: () => taxConfigService.pt(state), enabled: tab === 'pt' })

  const savePf = useMutation({ mutationFn: (v: AnyObj) => taxConfigService.savePf(v), onSuccess: () => { toast.success('PF config saved'); qc.invalidateQueries({ queryKey: ['tax-pf'] }) } })
  const saveEsi = useMutation({ mutationFn: (v: AnyObj) => taxConfigService.saveEsi(v), onSuccess: () => { toast.success('ESI config saved'); qc.invalidateQueries({ queryKey: ['tax-esi'] }) } })
  const savePt = useMutation({ mutationFn: (v: AnyObj) => taxConfigService.savePt({ ...v, state }), onSuccess: () => { toast.success('PT slab saved'); qc.invalidateQueries({ queryKey: ['tax-pt'] }) } })

  return (
    <div className="space-y-5 animate-fade-in">
      <PageHeader title="Statutory Tax Config" description="Provident Fund, ESI and Professional Tax rules" />
      <Tabs value={tab} onValueChange={v => setTab(v as 'pf' | 'esi' | 'pt')}>
        <TabsList>
          <TabsTrigger value="pf">Provident Fund</TabsTrigger>
          <TabsTrigger value="esi">ESI</TabsTrigger>
          <TabsTrigger value="pt">Professional Tax</TabsTrigger>
        </TabsList>
      </Tabs>

      <Card>
        <CardHeader><CardTitle className="text-sm flex items-center gap-2"><Receipt className="h-4 w-4" /> {tab.toUpperCase()} configuration</CardTitle></CardHeader>
        <CardContent>
          {tab === 'pf' && (pfQ.isLoading ? <Skeleton className="h-40" /> :
            <ConfigForm value={(pfQ.data as AnyObj) ?? {}} onSave={v => savePf.mutate(v)} rows={[
              { name: 'employeeRate', label: 'Employee contribution (%)' },
              { name: 'employerRate', label: 'Employer contribution (%)' },
              { name: 'wageCeiling', label: 'Wage ceiling (₹)' },
            ]} />)}
          {tab === 'esi' && (esiQ.isLoading ? <Skeleton className="h-40" /> :
            <ConfigForm value={(esiQ.data as AnyObj) ?? {}} onSave={v => saveEsi.mutate(v)} rows={[
              { name: 'employeeRate', label: 'Employee contribution (%)' },
              { name: 'employerRate', label: 'Employer contribution (%)' },
              { name: 'wageCeiling', label: 'Wage ceiling (₹)' },
            ]} />)}
          {tab === 'pt' && (
            <div className="space-y-4">
              <div className="max-w-xs">
                <Label>State</Label>
                <Input value={state} onChange={e => setState(e.target.value.toUpperCase())} placeholder="State code e.g. KA, MH" />
              </div>
              {ptQ.isLoading ? <Skeleton className="h-40" /> :
                <ConfigForm value={(ptQ.data as AnyObj) ?? {}} onSave={v => savePt.mutate(v)} rows={[
                  { name: 'minSalary', label: 'From salary (₹)' },
                  { name: 'maxSalary', label: 'To salary (₹)' },
                  { name: 'monthlyTax', label: 'Monthly PT (₹)' },
                ]} />}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  )
}
