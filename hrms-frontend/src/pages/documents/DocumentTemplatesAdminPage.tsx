import { FileSignature } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { ResourcePage } from '@/components/ui/resource-page'
import { documentTemplateService } from '@/services/extendedServices'

interface Template extends Record<string, unknown> { id: string; name: string; type?: string; active?: boolean }

export function DocumentTemplatesAdminPage() {
  return (
    <ResourcePage<Template>
      title="Document Templates"
      description="Reusable letter & document templates with merge variables"
      icon={<FileSignature className="h-10 w-10" />}
      queryKey={['document-templates']}
      fetcher={() => documentTemplateService.list() as Promise<unknown>}
      columns={[
        { key: 'name', label: 'Template' },
        { key: 'type', label: 'Type', render: t => t.type ? <Badge>{String(t.type)}</Badge> : '—' },
        { key: 'active', label: 'Active', render: t => <Badge variant={t.active === false ? 'secondary' : 'success'}>{t.active === false ? 'Inactive' : 'Active'}</Badge> },
      ]}
      formFields={[
        { name: 'name', label: 'Template name', type: 'text', required: true, span: 2 },
        { name: 'type', label: 'Type', type: 'select', options: [
          { value: 'OFFER', label: 'Offer letter' }, { value: 'APPOINTMENT', label: 'Appointment' },
          { value: 'CONFIRMATION', label: 'Confirmation' }, { value: 'RELIEVING', label: 'Relieving' },
          { value: 'EXPERIENCE', label: 'Experience' }, { value: 'SALARY_REVISION', label: 'Salary revision' }, { value: 'CUSTOM', label: 'Custom' },
        ] },
        { name: 'body', label: 'Body (supports {{variables}})', type: 'textarea', span: 2 },
      ]}
      onCreate={v => documentTemplateService.create(v)}
      onUpdate={(id, v) => documentTemplateService.update(id, v)}
      onDelete={id => documentTemplateService.remove(id)}
    />
  )
}
