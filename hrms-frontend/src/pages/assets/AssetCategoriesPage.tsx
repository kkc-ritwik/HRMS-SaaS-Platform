import { useQuery } from '@tanstack/react-query'
import { Boxes } from 'lucide-react'
import { DataList } from '@/components/ui/data-list'
import { assetCategoryService } from '@/services/extendedServices'

interface AssetCategory { id: string; name: string; description?: string; defaultLifespanYears?: number }

export function AssetCategoriesPage() {
  const { data, isLoading } = useQuery({ queryKey: ['asset-categories'], queryFn: () => assetCategoryService.list() })
  const items: AssetCategory[] = Array.isArray(data) ? data as AssetCategory[] : []
  return (
    <DataList<AssetCategory>
      title="Asset Categories"
      data={items} isLoading={isLoading}
      emptyIcon={<Boxes className="h-10 w-10" />} emptyTitle="No categories"
      columns={[{ key: 'name', label: 'Name' }, { key: 'description', label: 'Description' }, { key: 'defaultLifespanYears', label: 'Lifespan (yrs)' }]}
    />
  )
}
