import { useNavigate } from 'react-router-dom'
import { ClipboardEdit, Send } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { ResourcePage } from '@/components/ui/resource-page'
import { formService } from '@/services/extendedServices'
import { Catalog } from '@/services/catalog'

interface Form extends Record<string, unknown> { id: string; name: string; description?: string; category?: string; active?: boolean; status?: string; submissionsCount?: number }

export function FormsPage() {
  const navigate = useNavigate()
  return (
    <ResourcePage<Form>
      title="Forms Library"
      description="Custom HR forms — build, publish, collect submissions"
      icon={<ClipboardEdit className="h-10 w-10" />}
      queryKey={['forms']}
      fetcher={() => formService.list()}
      headerExtra={<Button variant="outline" size="sm" onClick={() => navigate('/forms/builder')}><ClipboardEdit className="h-4 w-4 mr-1" /> Form Builder</Button>}
      columns={[
        { key: 'name', label: 'Name' },
        { key: 'category', label: 'Category' },
        { key: 'submissionsCount', label: 'Submissions', align: 'right' },
        { key: 'active', label: 'State', render: f => <Badge variant={f.active || f.status === 'PUBLISHED' ? 'success' : 'secondary'}>{f.active || f.status === 'PUBLISHED' ? 'Published' : 'Draft'}</Badge> },
      ]}
      formFields={[
        { name: 'name', label: 'Form name', type: 'text', required: true, span: 2 },
        { name: 'code', label: 'Code', type: 'text', required: true },
        { name: 'category', label: 'Category', type: 'text' },
        { name: 'description', label: 'Description', type: 'textarea', span: 2 },
      ]}
      onCreate={v => Catalog.forms.create(v)}
      rowActions={f => [
        { label: 'Publish', icon: <Send className="h-3.5 w-3.5" />, show: !(f.active || f.status === 'PUBLISHED'), run: () => Catalog.forms.publish(f.id) },
      ]}
    />
  )
}
