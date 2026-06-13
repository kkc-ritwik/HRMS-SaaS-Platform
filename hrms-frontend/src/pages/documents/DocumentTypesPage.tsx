import { FileType } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { ResourcePage } from '@/components/ui/resource-page'
import { documentTypeService } from '@/services/adminServices'

interface DocType extends Record<string, unknown> { id: string; name: string; code?: string; mandatory?: boolean; category?: string }

export function DocumentTypesPage() {
  return (
    <ResourcePage<DocType>
      title="Document Types"
      description="Categories of HR documents (offer letter, ID proof, etc.)"
      icon={<FileType className="h-10 w-10" />}
      queryKey={['document-types']}
      fetcher={() => documentTypeService.list() as Promise<unknown>}
      columns={[
        { key: 'code', label: 'Code' },
        { key: 'name', label: 'Name' },
        { key: 'category', label: 'Category' },
        { key: 'mandatory', label: 'Mandatory', render: t => t.mandatory ? <Badge variant="warning">Required</Badge> : '—' },
      ]}
      formFields={[
        { name: 'code', label: 'Code', type: 'text', required: true },
        { name: 'name', label: 'Name', type: 'text', required: true },
        { name: 'category', label: 'Category', type: 'select', options: [
          { value: 'IDENTITY', label: 'Identity' }, { value: 'EDUCATION', label: 'Education' }, { value: 'EMPLOYMENT', label: 'Employment' },
          { value: 'FINANCIAL', label: 'Financial' }, { value: 'COMPLIANCE', label: 'Compliance' }, { value: 'OTHER', label: 'Other' },
        ] },
        { name: 'mandatory', label: 'Mandatory at onboarding', type: 'switch' },
        { name: 'description', label: 'Description', type: 'textarea', span: 2 },
      ]}
      onCreate={v => documentTypeService.create(v)}
      onUpdate={(id, v) => documentTypeService.update(id, v)}
      onDelete={id => documentTypeService.remove(id)}
    />
  )
}
