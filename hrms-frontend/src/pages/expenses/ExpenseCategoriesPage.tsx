import { Tag } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { ResourcePage } from '@/components/ui/resource-page'
import { expenseService } from '@/services/expenseService'

interface Cat extends Record<string, unknown> { id: string; name: string; code?: string; capAmount?: number; active?: boolean }

export function ExpenseCategoriesPage() {
  return (
    <ResourcePage<Cat>
      title="Expense Categories"
      description="Travel, meals, lodging, software, etc. — with per-claim caps"
      icon={<Tag className="h-10 w-10" />}
      queryKey={['expense-categories']}
      fetcher={() => expenseService.categoriesAll()}
      columns={[
        { key: 'code', label: 'Code' },
        { key: 'name', label: 'Name' },
        { key: 'capAmount', label: 'Cap', align: 'right', render: c => c.capAmount ? `₹${Number(c.capAmount).toLocaleString()}` : '—' },
        { key: 'active', label: 'Active', render: c => <Badge variant={c.active === false ? 'secondary' : 'success'}>{c.active === false ? 'Inactive' : 'Active'}</Badge> },
      ]}
      formFields={[
        { name: 'code', label: 'Code', type: 'text', required: true },
        { name: 'name', label: 'Name', type: 'text', required: true },
        { name: 'capAmount', label: 'Per-claim cap', type: 'currency' },
        { name: 'glCode', label: 'GL code', type: 'text' },
        { name: 'description', label: 'Description', type: 'textarea', span: 2 },
      ]}
      onCreate={v => expenseService.createCategory(v)}
      onUpdate={(id, v) => expenseService.updateCategory(id, v)}
      onDelete={id => expenseService.deleteCategory(id)}
    />
  )
}
