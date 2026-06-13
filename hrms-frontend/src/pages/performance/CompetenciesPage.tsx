import { Target } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { ResourcePage } from '@/components/ui/resource-page'
import { competencyService } from '@/services/extendedServices'
import { Catalog } from '@/services/catalog'

interface Competency extends Record<string, unknown> { id: string; name: string; description?: string; category?: string; level?: string }

export function CompetenciesPage() {
  return (
    <ResourcePage<Competency>
      title="Competencies"
      description="Skills + behavioural competencies used in reviews"
      icon={<Target className="h-10 w-10" />}
      queryKey={['competencies']}
      fetcher={() => competencyService.list()}
      filters={{ category: ['TECHNICAL', 'BEHAVIOURAL', 'LEADERSHIP', 'FUNCTIONAL'] }}
      columns={[
        { key: 'name', label: 'Name' },
        { key: 'category', label: 'Category', render: c => c.category ? <Badge>{String(c.category)}</Badge> : '—' },
        { key: 'level', label: 'Level' },
        { key: 'description', label: 'Description' },
      ]}
      formFields={[
        { name: 'name', label: 'Name', type: 'text', required: true, span: 2 },
        { name: 'category', label: 'Category', type: 'select', required: true, options: [
          { value: 'TECHNICAL', label: 'Technical' }, { value: 'BEHAVIOURAL', label: 'Behavioural' },
          { value: 'LEADERSHIP', label: 'Leadership' }, { value: 'FUNCTIONAL', label: 'Functional' },
        ] },
        { name: 'description', label: 'Description', type: 'textarea', span: 2 },
      ]}
      onCreate={v => competencyService.create(v)}
      onUpdate={(id, v) => Catalog.competencies.update(id, v)}
    />
  )
}
