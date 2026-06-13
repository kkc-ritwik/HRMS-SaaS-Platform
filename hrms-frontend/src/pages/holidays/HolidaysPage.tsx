import { Gift } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { ResourcePage } from '@/components/ui/resource-page'
import { holidayService, type Holiday } from '@/services/holidayService'
import { formatDate } from '@/lib/utils'

export function HolidaysPage() {
  const year = new Date().getFullYear()
  return (
    <ResourcePage<Holiday & Record<string, unknown>>
      title={`Holidays ${year}`}
      description="Public, restricted, optional and company holidays — create, edit, delete"
      icon={<Gift className="h-10 w-10" />}
      queryKey={['holidays', year]}
      fetcher={() => holidayService.list(year)}
      filters={{ type: ['PUBLIC', 'RESTRICTED', 'OPTIONAL', 'COMPANY'] }}
      columns={[
        { key: 'date', label: 'Date', render: h => h.date ? formatDate(String(h.date)) : '—' },
        { key: 'name', label: 'Name' },
        { key: 'type', label: 'Type', render: h => <Badge>{String(h.type)}</Badge> },
        { key: 'region', label: 'Region' },
      ]}
      formFields={[
        { name: 'name', label: 'Name', type: 'text', required: true, span: 2 },
        { name: 'date', label: 'Date', type: 'date', required: true },
        { name: 'type', label: 'Type', type: 'select', required: true, options: [
          { value: 'PUBLIC', label: 'Public' }, { value: 'RESTRICTED', label: 'Restricted' },
          { value: 'OPTIONAL', label: 'Optional' }, { value: 'COMPANY', label: 'Company' },
        ] },
        { name: 'region', label: 'Region', type: 'text' },
        { name: 'religion', label: 'Religion', type: 'text' },
        { name: 'description', label: 'Description', type: 'textarea', span: 2 },
      ]}
      onCreate={v => holidayService.create(v as Partial<Holiday>)}
      onUpdate={(id, v) => holidayService.update(id, v as Partial<Holiday>)}
      onDelete={id => holidayService.delete(id)}
    />
  )
}
