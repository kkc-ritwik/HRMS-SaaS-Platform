import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Gift, Plus, Wallet } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { EmptyState } from '@/components/ui/empty-state'
import { PageHeader } from '@/components/ui/page-header'
import { Badge } from '@/components/ui/badge'
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { FormDialog } from '@/components/ui/form-dialog'
import { rewardsCatalogService } from '@/services/extendedServices'
import { engagementService } from '@/services/engagementService'
import { engagementCatalog } from '@/services/catalog'
import { useAuthStore } from '@/store/authStore'
import { getErrorMessage } from '@/lib/api'
import { formatDate } from '@/lib/utils'
import { toast } from 'sonner'

type AnyObj = Record<string, unknown>
function rows<T = AnyObj>(d: unknown): T[] {
  if (Array.isArray(d)) return d as T[]
  const o = d as { content?: T[] } | undefined
  return Array.isArray(o?.content) ? o!.content : []
}
interface CatalogItem extends Record<string, unknown> { id: string; title: string; description?: string; pointsCost: number; category: string; imageUrl?: string }

export function RewardsCatalogPage() {
  const qc = useQueryClient()
  const user = useAuthStore(s => s.user)
  const [tab, setTab] = useState<'catalog' | 'budgets' | 'redemptions'>('catalog')
  const [newItem, setNewItem] = useState(false)
  const [newBudget, setNewBudget] = useState(false)
  const employeeId = user?.employeeId || user?.id || ''

  const catQ = useQuery({ queryKey: ['rewards', 'catalog'], queryFn: rewardsCatalogService.catalog, enabled: tab === 'catalog' })
  const budQ = useQuery({ queryKey: ['rewards', 'budgets'], queryFn: rewardsCatalogService.budgets, enabled: tab === 'budgets' })
  const redQ = useQuery({ queryKey: ['rewards', 'redemptions', employeeId], queryFn: () => engagementService.myRedemptions(employeeId), enabled: tab === 'redemptions' && !!employeeId })
  const fulfill = useMutation({
    mutationFn: (id: string) => engagementCatalog.rewards.fulfillRedemption(id, {}),
    onSuccess: () => { toast.success('Redemption fulfilled'); qc.invalidateQueries({ queryKey: ['rewards', 'redemptions', employeeId] }) },
    onError: e => toast.error(getErrorMessage(e)),
  })

  const redeem = useMutation({
    mutationFn: (item: CatalogItem) => rewardsCatalogService.redeem({ catalogItemId: item.id, employeeId: user?.employeeId || user?.id || '', availablePoints: 9999 }),
    onSuccess: () => toast.success('Redeemed!'),
  })
  const addItem = useMutation({ mutationFn: (v: Record<string, unknown>) => engagementCatalog.rewards.createCatalogItem(v), onSuccess: () => { toast.success('Reward added'); qc.invalidateQueries({ queryKey: ['rewards', 'catalog'] }); setNewItem(false) } })
  const addBudget = useMutation({ mutationFn: (v: Record<string, unknown>) => engagementCatalog.rewards.createBudget(v), onSuccess: () => { toast.success('Budget created'); qc.invalidateQueries({ queryKey: ['rewards', 'budgets'] }); setNewBudget(false) } })

  return (
    <div className="space-y-5">
      <PageHeader title="Rewards" description="Redeem points and manage reward budgets"
        action={tab === 'catalog'
          ? <Button size="sm" onClick={() => setNewItem(true)}><Plus className="h-4 w-4 mr-1" /> Add Reward</Button>
          : <Button size="sm" onClick={() => setNewBudget(true)}><Plus className="h-4 w-4 mr-1" /> New Budget</Button>} />

      <Tabs value={tab} onValueChange={v => setTab(v as 'catalog' | 'budgets' | 'redemptions')}>
        <TabsList>
          <TabsTrigger value="catalog">Catalogue</TabsTrigger>
          <TabsTrigger value="budgets">Budgets</TabsTrigger>
          <TabsTrigger value="redemptions">My redemptions</TabsTrigger>
        </TabsList>
      </Tabs>

      {tab === 'catalog' ? (
        catQ.isLoading ? <Skeleton className="h-64" /> : rows<CatalogItem>(catQ.data).length === 0 ? (
          <EmptyState icon={<Gift className="h-10 w-10" />} title="Catalogue is empty" action={{ label: 'Add Reward', onClick: () => setNewItem(true) }} />
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            {rows<CatalogItem>(catQ.data).map(item => (
              <Card key={item.id}>
                <CardContent className="p-4">
                  {item.imageUrl && <img src={item.imageUrl} alt="" className="rounded-lg mb-3 aspect-video object-cover" />}
                  <div className="flex items-start justify-between mb-1">
                    <h3 className="font-semibold">{item.title}</h3>
                    <Badge>{item.category}</Badge>
                  </div>
                  {item.description && <p className="text-xs text-slate-500 mb-3 line-clamp-2">{item.description}</p>}
                  <div className="flex items-center justify-between">
                    <span className="text-violet-600 font-bold">{item.pointsCost} pts</span>
                    <Button size="sm" onClick={() => redeem.mutate(item)}>Redeem</Button>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        )
      ) : tab === 'budgets' ? (
        budQ.isLoading ? <Skeleton className="h-48" /> : rows<AnyObj>(budQ.data).length === 0 ? (
          <EmptyState icon={<Wallet className="h-10 w-10" />} title="No reward budgets" action={{ label: 'New Budget', onClick: () => setNewBudget(true) }} />
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            {rows<AnyObj>(budQ.data).map(b => (
              <Card key={String(b.id)}>
                <CardContent className="p-4">
                  <p className="text-sm font-medium">{String(b.name ?? b.department ?? 'Budget')}</p>
                  <p className="text-2xl font-bold mt-1">{String(b.remainingPoints ?? b.totalPoints ?? 0)} <span className="text-sm text-slate-400">pts left</span></p>
                  <p className="text-xs text-slate-500">of {String(b.totalPoints ?? 0)} allocated · {String(b.period ?? '')}</p>
                  <Button size="sm" variant="outline" className="mt-2" onClick={() => engagementCatalog.rewards.spendBudget(String(b.id), { points: 0 }).then(() => toast.success('Recorded'))}>Record spend</Button>
                </CardContent>
              </Card>
            ))}
          </div>
        )
      ) : (
        redQ.isLoading ? <Skeleton className="h-48" /> : rows<AnyObj>(redQ.data).length === 0 ? (
          <EmptyState icon={<Gift className="h-10 w-10" />} title="No redemptions yet" description="Redeem a reward from the catalogue to see it here." />
        ) : (
          <div className="space-y-2">
            {rows<AnyObj>(redQ.data).map(r => (
              <Card key={String(r.id)}>
                <CardContent className="p-4 flex items-center justify-between">
                  <div>
                    <p className="text-sm font-medium">{String(r.itemTitle ?? r.catalogItemTitle ?? r.catalogItemId ?? 'Reward')}</p>
                    <p className="text-xs text-slate-500">
                      {String(r.pointsSpent ?? r.pointsCost ?? 0)} pts · {r.redeemedAt ? formatDate(String(r.redeemedAt)) : r.createdAt ? formatDate(String(r.createdAt)) : ''}
                    </p>
                  </div>
                  <div className="flex items-center gap-2">
                    <Badge variant={String(r.status) === 'FULFILLED' ? 'success' : String(r.status) === 'REJECTED' ? 'destructive' : 'warning'}>{String(r.status ?? 'PENDING')}</Badge>
                    {String(r.status ?? 'PENDING') !== 'FULFILLED' && (
                      <Button size="sm" variant="outline" loading={fulfill.isPending} onClick={() => fulfill.mutate(String(r.id))}>Fulfill</Button>
                    )}
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        )
      )}

      <FormDialog open={newItem} onOpenChange={setNewItem} title="Add reward" submitLabel="Add"
        fields={[
          { name: 'title', label: 'Title', type: 'text', required: true, span: 2 },
          { name: 'category', label: 'Category', type: 'select', options: [
            { value: 'VOUCHER', label: 'Voucher' }, { value: 'SWAG', label: 'Swag' }, { value: 'EXPERIENCE', label: 'Experience' }, { value: 'TIME_OFF', label: 'Time off' },
          ] },
          { name: 'pointsCost', label: 'Points cost', type: 'number', required: true },
          { name: 'stockQty', label: 'Stock quantity', type: 'number' },
          { name: 'description', label: 'Description', type: 'textarea', span: 2 },
        ]}
        onSubmit={v => addItem.mutateAsync(v)} />
      <FormDialog open={newBudget} onOpenChange={setNewBudget} title="Create reward budget" submitLabel="Create"
        fields={[
          { name: 'name', label: 'Budget name', type: 'text', required: true, span: 2 },
          { name: 'department', label: 'Department', type: 'text' },
          { name: 'totalPoints', label: 'Total points', type: 'number', required: true },
          { name: 'period', label: 'Period', type: 'text', placeholder: 'e.g. 2026-Q2' },
        ]}
        onSubmit={v => addBudget.mutateAsync(v)} />
    </div>
  )
}
