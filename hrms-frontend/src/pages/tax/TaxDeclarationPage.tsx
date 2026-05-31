import { useQuery, useMutation } from '@tanstack/react-query'
import { useState } from 'react'
import { Save } from 'lucide-react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Skeleton } from '@/components/ui/skeleton'
import { PageHeader } from '@/components/ui/page-header'
import { taxService, type TaxDeclaration, type TaxRegime } from '@/services/taxService'
import { useAuthStore } from '@/store/authStore'
import { toast } from 'sonner'

const fy = `${new Date().getFullYear()}-${String(new Date().getFullYear() + 1).slice(2)}`

export function TaxDeclarationPage() {
  const user = useAuthStore(s => s.user)
  const empId = user?.employeeId || user?.id || ''
  const { data, isLoading } = useQuery({
    queryKey: ['tax', 'me', fy],
    queryFn: () => taxService.myDeclaration(fy),
  })
  const [form, setForm] = useState<Partial<TaxDeclaration>>({})

  const save = useMutation({
    mutationFn: () => taxService.saveDeclaration({ employeeId: empId, financialYear: fy, ...(data || {}), ...form }),
    onSuccess: () => toast.success('Saved'),
  })

  if (isLoading) return <Skeleton className="h-64" />
  const d = { ...data, ...form } as TaxDeclaration

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
    </div>
  )
}
