import { Boxes } from 'lucide-react'
import { ResourcePage } from '@/components/ui/resource-page'
import { assetCategoryService } from '@/services/extendedServices'
import { Catalog } from '@/services/catalog'

interface AssetCategory extends Record<string, unknown> { id: string; name: string; description?: string; defaultLifespanYears?: number }

export function AssetCategoriesPage() {
  return (
    <ResourcePage<AssetCategory>
      title="Asset Categories"
      description="Laptop, monitor, phone, furniture, vehicle, etc."
      icon={<Boxes className="h-10 w-10" />}
      queryKey={['asset-categories']}
      fetcher={() => assetCategoryService.list()}
      columns={[
        { key: 'name', label: 'Name' },
        { key: 'description', label: 'Description' },
        { key: 'defaultLifespanYears', label: 'Lifespan (yrs)', align: 'right' },
      ]}
      formFields={[
        { name: 'name', label: 'Name', type: 'text', required: true },
        { name: 'description', label: 'Description', type: 'textarea', span: 2 },
        { name: 'defaultLifespanYears', label: 'Default lifespan (years)', type: 'number' },
        { name: 'depreciationRate', label: 'Depreciation rate (%)', type: 'number' },
      ]}
      onCreate={v => assetCategoryService.create(v)}
      onUpdate={(id, v) => Catalog.assets.categories.update(id, v)}
      onDelete={id => Catalog.assets.categories.delete(id)}
    />
  )
}
