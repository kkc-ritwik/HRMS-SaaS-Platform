import { useState, useRef } from 'react'
import { useQuery } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { MapPin, ArrowLeft, ZoomIn, ZoomOut, Maximize2 } from 'lucide-react'
import { Card, CardContent } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Skeleton } from '@/components/ui/skeleton'
import { Input } from '@/components/ui/input'
import { PageHeader } from '@/components/ui/page-header'
import { assetService } from '@/services/assetService'

interface DeskNode { id: string; code: string; type: string; x?: number | null; y?: number | null; zone?: string | null; occupantEmployeeId?: string | null; status?: string }
interface FloorData { name: string; widthPx?: number; heightPx?: number; desks: DeskNode[] }

export function FloorPlanPage() {
  const navigate = useNavigate()
  const [floorId, setFloorId] = useState('')
  const [date, setDate] = useState(new Date().toISOString().slice(0, 10))
  const [zoom, setZoom] = useState(1)
  const [pan, setPan] = useState({ x: 0, y: 0 })
  const dragRef = useRef<{ x: number; y: number } | null>(null)

  const floors = useQuery({ queryKey: ['floors'], queryFn: () => assetService.floors() })
  const map = useQuery({
    queryKey: ['seating-map', floorId, date],
    queryFn: () => assetService.seatingMap(floorId, date),
    enabled: !!floorId,
  })

  const floorsList = (floors.data as Array<{ id: string; name: string }> | undefined) || []
  const seating = (map.data as FloorData | undefined)

  const startDrag = (e: React.MouseEvent) => {
    dragRef.current = { x: e.clientX - pan.x, y: e.clientY - pan.y }
  }
  const onDrag = (e: React.MouseEvent) => {
    if (dragRef.current) {
      setPan({ x: e.clientX - dragRef.current.x, y: e.clientY - dragRef.current.y })
    }
  }
  const endDrag = () => { dragRef.current = null }

  return (
    <div className="space-y-4">
      <Button variant="ghost" size="sm" onClick={() => navigate('/desk-booking')}>
        <ArrowLeft className="h-4 w-4 mr-1" /> Back
      </Button>

      <PageHeader title="Floor Plan" description="Interactive seating map — drag to pan, scroll to zoom"
        action={
          <div className="flex items-center gap-2">
            <select className="h-9 rounded-md border px-2 text-sm" value={floorId} onChange={e => setFloorId(e.target.value)}>
              <option value="">Choose floor...</option>
              {floorsList.map(f => <option key={f.id} value={f.id}>{f.name}</option>)}
            </select>
            <Input type="date" value={date} onChange={e => setDate(e.target.value)} className="w-40" />
            <Button size="icon" variant="outline" onClick={() => setZoom(z => Math.min(2, z + 0.2))}><ZoomIn className="h-4 w-4" /></Button>
            <Button size="icon" variant="outline" onClick={() => setZoom(z => Math.max(0.4, z - 0.2))}><ZoomOut className="h-4 w-4" /></Button>
            <Button size="icon" variant="outline" onClick={() => { setZoom(1); setPan({ x: 0, y: 0 }) }}><Maximize2 className="h-4 w-4" /></Button>
          </div>
        }
      />

      <Card>
        <CardContent className="p-0 relative" style={{ height: '600px', overflow: 'hidden', background: '#f8fafc' }}>
          {!floorId ? (
            <div className="flex items-center justify-center h-full text-sm text-slate-500">
              <MapPin className="h-10 w-10 text-slate-300 mr-2" /> Select a floor to see the layout
            </div>
          ) : map.isLoading ? (
            <Skeleton className="absolute inset-0" />
          ) : !seating ? (
            <p className="p-10 text-center text-slate-500">No data</p>
          ) : (
            <div
              className="absolute inset-0 cursor-grab active:cursor-grabbing"
              onMouseDown={startDrag} onMouseMove={onDrag} onMouseUp={endDrag} onMouseLeave={endDrag}
            >
              <div
                className="absolute origin-center"
                style={{
                  transform: `translate(${pan.x}px, ${pan.y}px) scale(${zoom})`,
                  width: seating.widthPx || 1200, height: seating.heightPx || 800,
                  background: 'repeating-linear-gradient(0deg, transparent, transparent 19px, #e2e8f0 20px), repeating-linear-gradient(90deg, transparent, transparent 19px, #e2e8f0 20px)',
                }}
              >
                {seating.desks.map(d => (
                  <div
                    key={d.id}
                    title={`${d.code}${d.occupantEmployeeId ? ' · OCCUPIED' : ' · Free'}`}
                    className={`absolute rounded-lg border-2 text-xs flex flex-col items-center justify-center font-medium transition hover:scale-110 cursor-pointer ${
                      d.occupantEmployeeId
                        ? 'bg-red-100 border-red-400 text-red-700'
                        : 'bg-green-100 border-green-400 text-green-700'
                    }`}
                    style={{ left: (d.x ?? 0) - 20, top: (d.y ?? 0) - 20, width: 40, height: 40 }}
                  >
                    {d.code}
                  </div>
                ))}
              </div>
            </div>
          )}
        </CardContent>
      </Card>

      <div className="flex items-center gap-4 text-xs text-slate-500">
        <span className="flex items-center gap-1"><span className="h-3 w-3 rounded bg-green-100 border border-green-400" /> Available</span>
        <span className="flex items-center gap-1"><span className="h-3 w-3 rounded bg-red-100 border border-red-400" /> Occupied</span>
        <Badge>Zoom: {Math.round(zoom * 100)}%</Badge>
      </div>
    </div>
  )
}
