import { useQuery, useMutation } from '@tanstack/react-query'
import { Gift } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { EmptyState } from '@/components/ui/empty-state'
import { PageHeader } from '@/components/ui/page-header'
import { Badge } from '@/components/ui/badge'
import { rewardsCatalogService } from '@/services/extendedServices'
import { useAuthStore } from '@/store/authStore'
import { toast } from 'sonner'

interface CatalogItem { id: string; title: string; description?: string; pointsCost: number; category: string; imageUrl?: string; stockQty?: number }

export function RewardsCatalogPage() {
  const user = useAuthStore(s => s.user)
  const { data, isLoading } = useQuery({ queryKey: ['rewards', 'catalog'], queryFn: rewardsCatalogService.catalog })
  const items: CatalogItem[] = (Array.isArray(data) ? data as CatalogItem[] : [])

  const redeem = useMutation({
    mutationFn: (item: CatalogItem) => rewardsCatalogService.redeem({
      catalogItemId: item.id, employeeId: user?.employeeId || user?.id || '', availablePoints: 9999,
    }),
    onSuccess: () => toast.success('Redeemed!'),
  })

  return (
    <div className="space-y-6">
      <PageHeader title="Rewards Catalogue" description="Redeem points for vouchers, swag, and experiences" />
      {isLoading ? <Skeleton className="h-64" /> : items.length === 0 ? (
        <EmptyState icon={<Gift className="h-10 w-10" />} title="Catalogue is empty" />
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {items.map(item => (
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
      )}
    </div>
  )
}
