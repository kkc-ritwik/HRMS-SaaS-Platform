import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { ArrowLeft, Package } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Tabs, TabsList, TabsTrigger, TabsContent } from '@/components/ui/tabs'
import { Skeleton } from '@/components/ui/skeleton'
import { assetService, type Asset } from '@/services/assetService'
import { toast } from 'sonner'

export function AssetDetailPage() {
  const { id = '' } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const qc = useQueryClient()
  const { data, isLoading } = useQuery({ queryKey: ['asset', id], queryFn: () => assetService.get(id), enabled: !!id })

  const retire = useMutation({
    mutationFn: () => assetService.retire(id, 'End of life'),
    onSuccess: () => { toast.success('Retired'); qc.invalidateQueries({ queryKey: ['asset', id] }) },
  })

  if (isLoading) return <Skeleton className="h-96" />
  const a = data as Asset
  if (!a) return <p>Asset not found</p>

  return (
    <div className="space-y-6">
      <Button variant="ghost" size="sm" onClick={() => navigate('/assets')}>
        <ArrowLeft className="h-4 w-4 mr-1" /> Back to assets
      </Button>

      <Card>
        <CardContent className="p-6 flex items-start justify-between">
          <div>
            <div className="flex items-center gap-2 mb-2">
              <Package className="h-5 w-5 text-violet-600" />
              <h1 className="text-2xl font-bold">{a.name}</h1>
              <Badge>{a.status}</Badge>
            </div>
            <p className="text-sm text-slate-500">Tag: {a.assetTag} · {a.categoryName}</p>
            <div className="grid grid-cols-2 md:grid-cols-3 gap-4 mt-4 text-sm">
              <div><span className="text-slate-500">Serial:</span> {a.serialNumber || '—'}</div>
              <div><span className="text-slate-500">Model:</span> {a.model || '—'}</div>
              <div><span className="text-slate-500">Manufacturer:</span> {a.manufacturer || '—'}</div>
              <div><span className="text-slate-500">Purchase date:</span> {a.purchaseDate || '—'}</div>
              <div><span className="text-slate-500">Cost:</span> {a.purchaseCost ? `₹${a.purchaseCost.toLocaleString()}` : '—'}</div>
              <div><span className="text-slate-500">Warranty:</span> {a.warrantyExpiry || '—'}</div>
              <div className="col-span-3"><span className="text-slate-500">Assigned to:</span> {a.assignedToEmployeeName || '—'}</div>
            </div>
          </div>
          {a.status !== 'RETIRED' && <Button size="sm" variant="outline" onClick={() => retire.mutate()}>Retire</Button>}
        </CardContent>
      </Card>

      <Tabs defaultValue="history">
        <TabsList>
          <TabsTrigger value="history">Assignment history</TabsTrigger>
          <TabsTrigger value="maintenance">Maintenance</TabsTrigger>
        </TabsList>
        <TabsContent value="history"><Card><CardContent className="p-6 text-sm text-slate-500">Detailed assignment log via service</CardContent></Card></TabsContent>
        <TabsContent value="maintenance"><Card><CardContent className="p-6 text-sm text-slate-500">Schedule + completed servicing</CardContent></Card></TabsContent>
      </Tabs>
    </div>
  )
}
