import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { MapPin } from 'lucide-react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Skeleton } from '@/components/ui/skeleton'
import { Badge } from '@/components/ui/badge'
import { PageHeader } from '@/components/ui/page-header'
import { assetService } from '@/services/assetService'
import { useAuthStore } from '@/store/authStore'
import { toast } from 'sonner'

export function DeskBookingPage() {
  const qc = useQueryClient()
  const user = useAuthStore(s => s.user)
  const [date, setDate] = useState(new Date().toISOString().slice(0, 10))

  const desks = useQuery({ queryKey: ['desks'], queryFn: () => assetService.desks() })
  const avail = useQuery({ queryKey: ['desks', 'avail', date], queryFn: () => assetService.deskAvailability(date) })

  const book = useMutation({
    mutationFn: (deskId: string) => assetService.bookDesk({
      deskId, employeeId: user?.employeeId || user?.id || '', date,
    }),
    onSuccess: () => { toast.success('Desk booked'); qc.invalidateQueries({ queryKey: ['desks'] }) },
  })

  const desksList = (desks.data as Array<{ id: string; code: string; type: string; zone?: string }> | undefined) || []
  const availableIds = new Set(((avail.data as { availableDeskIds?: string[] } | undefined)?.availableDeskIds) || [])

  return (
    <div className="space-y-6">
      <PageHeader title="Book a Desk" description="Reserve a hot desk or check seating map" />

      <div className="flex items-center gap-3">
        <Input type="date" value={date} onChange={e => setDate(e.target.value)} className="max-w-xs" />
        <Badge>{(avail.data as { availableHotDesks?: number } | undefined)?.availableHotDesks ?? 0} available</Badge>
      </div>

      {desks.isLoading ? (
        <Skeleton className="h-64" />
      ) : (
        <div className="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-6 gap-3">
          {desksList.map(d => {
            const free = availableIds.has(d.id)
            return (
              <Card key={d.id} className={`cursor-pointer ${!free ? 'opacity-50' : ''}`}>
                <CardContent className="p-3 text-center">
                  <MapPin className={`h-6 w-6 mx-auto ${free ? 'text-green-500' : 'text-slate-300'}`} />
                  <p className="text-xs font-medium mt-1">{d.code}</p>
                  <p className="text-xs text-slate-500">{d.zone || d.type}</p>
                  <Button size="sm" className="mt-2 w-full" disabled={!free} onClick={() => book.mutate(d.id)}>
                    {free ? 'Book' : 'Taken'}
                  </Button>
                </CardContent>
              </Card>
            )
          })}
        </div>
      )}
    </div>
  )
}
