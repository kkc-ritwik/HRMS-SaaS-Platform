import { useMemo, useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Gift, CalendarCheck } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Checkbox } from '@/components/ui/checkbox'
import { Skeleton } from '@/components/ui/skeleton'
import { ResourcePage } from '@/components/ui/resource-page'
import { holidayService, type Holiday } from '@/services/holidayService'
import { useAuthStore } from '@/store/authStore'
import { getErrorMessage } from '@/lib/api'
import { formatDate } from '@/lib/utils'
import { toast } from 'sonner'

interface OptionalQuota {
  year: number
  quota: number
  used: number
  remaining: number
  selectedHolidayIds: string[]
  availableHolidays: Holiday[]
}

function OptionalHolidaysCard({ year }: { year: number }) {
  const qc = useQueryClient()
  const user = useAuthStore(s => s.user)
  const employeeId = user?.employeeId || user?.id
  const [picked, setPicked] = useState<string[] | null>(null)

  const quota = useQuery({
    queryKey: ['holidays', 'optional', employeeId, year],
    queryFn: () => holidayService.optionalQuota(employeeId!, year) as Promise<OptionalQuota>,
    enabled: !!employeeId,
  })

  const selected = picked ?? quota.data?.selectedHolidayIds ?? []
  const max = quota.data?.quota ?? 0
  const available = useMemo(() => quota.data?.availableHolidays ?? [], [quota.data])

  const save = useMutation({
    mutationFn: () => holidayService.selectOptional(employeeId!, selected),
    onSuccess: () => {
      toast.success('Optional holidays saved')
      setPicked(null)
      qc.invalidateQueries({ queryKey: ['holidays', 'optional', employeeId, year] })
    },
    onError: (e) => toast.error(getErrorMessage(e)),
  })

  if (!employeeId) return null

  const toggle = (id: string) => {
    setPicked(prev => {
      const cur = prev ?? quota.data?.selectedHolidayIds ?? []
      if (cur.includes(id)) return cur.filter(x => x !== id)
      if (cur.length >= max) return cur
      return [...cur, id]
    })
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <CalendarCheck className="h-5 w-5" /> My optional holidays {year}
          {quota.data && <Badge variant="outline">{selected.length}/{max} selected</Badge>}
        </CardTitle>
      </CardHeader>
      <CardContent className="space-y-3">
        {quota.isLoading ? (
          <Skeleton className="h-24" />
        ) : available.length === 0 ? (
          <p className="text-sm text-slate-500">No optional holidays have been published for {year}.</p>
        ) : (
          <>
            <p className="text-sm text-slate-500">
              Choose up to {max} optional holiday{max === 1 ? '' : 's'} for the year.
            </p>
            <div className="grid gap-2 sm:grid-cols-2">
              {available.map(h => {
                const checked = selected.includes(h.id)
                const disabled = !checked && selected.length >= max
                return (
                  <label
                    key={h.id}
                    className={`flex items-center gap-3 rounded border p-2 ${disabled ? 'opacity-50' : 'cursor-pointer hover:bg-slate-50'}`}
                  >
                    <Checkbox checked={checked} disabled={disabled} onCheckedChange={() => toggle(h.id)} />
                    <div>
                      <p className="text-sm font-medium">{h.name}</p>
                      <p className="text-xs text-slate-500">
                        {h.date ? formatDate(String(h.date)) : '—'}{h.region ? ` · ${h.region}` : ''}
                      </p>
                    </div>
                  </label>
                )
              })}
            </div>
            <Button
              onClick={() => save.mutate()}
              loading={save.isPending}
              disabled={picked === null || selected.length > max}
            >
              Save selection
            </Button>
          </>
        )}
      </CardContent>
    </Card>
  )
}

export function HolidaysPage() {
  const year = new Date().getFullYear()
  return (
    <div className="space-y-6">
      <OptionalHolidaysCard year={year} />
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
    </div>
  )
}
