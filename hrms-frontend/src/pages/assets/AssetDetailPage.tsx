/**
 * Rich asset detail page — 5 tabs covering hardware lifecycle:
 *   Overview · Assignments · Maintenance · Audit · AMC
 */
import { useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import {
  ArrowLeft, Package, Wrench, History, Users, FileSignature, ArrowRightLeft,
} from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Tabs, TabsList, TabsTrigger, TabsContent } from '@/components/ui/tabs'
import { Skeleton } from '@/components/ui/skeleton'
import { EmptyState } from '@/components/ui/empty-state'
import { assetService, type Asset } from '@/services/assetService'
import { assetMaintenanceService } from '@/services/extendedServices'
import { CrudSection } from '@/components/ui/crud-section'
import { Catalog } from '@/services/catalog'
import { toast } from 'sonner'
import { formatDate } from '@/lib/utils'

type AnyObj = Record<string, unknown>
function rows<T = AnyObj>(d: unknown): T[] {
  if (Array.isArray(d)) return d as T[]
  const obj = d as { content?: T[]; data?: { content?: T[] } | T[] } | undefined
  if (!obj) return []
  if (Array.isArray(obj.content)) return obj.content
  if (Array.isArray(obj.data)) return obj.data as T[]
  return []
}

export function AssetDetailPage() {
  const { id = '' } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const qc = useQueryClient()
  const [tab, setTab] = useState('overview')

  const assetQ = useQuery({ queryKey: ['asset', id], queryFn: () => assetService.get(id), enabled: !!id })
  const assignmentsQ = useQuery({ queryKey: ['asset-asgn', id], queryFn: () => Catalog.assets.assignments.forAsset(id), enabled: !!id && tab === 'assignments' })
  const maintenanceQ = useQuery({ queryKey: ['asset-maint', id], queryFn: () => Catalog.assets.maintenance.forAsset(id), enabled: !!id && tab === 'maintenance' })
  const auditQ = useQuery({ queryKey: ['asset-audit', id], queryFn: () => Catalog.audit.byEntity('Asset', id), enabled: !!id && tab === 'audit' })

  const retire = useMutation({ mutationFn: () => assetService.retire(id, 'End of life'), onSuccess: () => { toast.success('Retired'); qc.invalidateQueries({ queryKey: ['asset', id] }) } })

  if (assetQ.isLoading) return <Skeleton className="h-96" />
  const a = assetQ.data as Asset
  if (!a) return <p>Asset not found</p>

  const currentAssignment = rows<AnyObj>(assignmentsQ.data).find(x => !x.returnedAt)

  return (
    <div className="space-y-6">
      <Button variant="ghost" size="sm" onClick={() => navigate('/assets')}>
        <ArrowLeft className="h-4 w-4 mr-1" /> Back to assets
      </Button>

      <Card>
        <CardContent className="p-6 flex items-start justify-between">
          <div>
            <div className="flex items-center gap-2 mb-2">
              <Package className="h-5 w-5 text-violet-600" />
              <h1 className="text-2xl font-bold">{a.name}</h1>
              <Badge>{a.status}</Badge>
            </div>
            <p className="text-sm text-slate-500">Tag: {a.assetTag} · {a.categoryName}</p>
            <div className="grid grid-cols-2 md:grid-cols-3 gap-4 mt-4 text-sm">
              <div><span className="text-slate-500">Serial:</span> {a.serialNumber || '—'}</div>
              <div><span className="text-slate-500">Model:</span> {a.model || '—'}</div>
              <div><span className="text-slate-500">Manufacturer:</span> {a.manufacturer || '—'}</div>
              <div><span className="text-slate-500">Purchase date:</span> {a.purchaseDate ? formatDate(a.purchaseDate) : '—'}</div>
              <div><span className="text-slate-500">Cost:</span> {a.purchaseCost ? `₹${a.purchaseCost.toLocaleString()}` : '—'}</div>
              <div><span className="text-slate-500">Warranty:</span> {a.warrantyExpiry ? formatDate(a.warrantyExpiry) : '—'}</div>
              <div className="col-span-3"><span className="text-slate-500">Assigned to:</span> {a.assignedToEmployeeName || '—'}</div>
            </div>
          </div>
          <div className="flex flex-col gap-2">
            {a.status !== 'RETIRED' && <Button size="sm" variant="outline" onClick={() => retire.mutate()} disabled={retire.isPending}>Retire</Button>}
          </div>
        </CardContent>
      </Card>

      <Tabs value={tab} onValueChange={setTab}>
        <TabsList className="flex flex-wrap h-auto">
          <TabsTrigger value="overview">Overview</TabsTrigger>
          <TabsTrigger value="assignments"><Users className="h-3.5 w-3.5" /> Assignments</TabsTrigger>
          <TabsTrigger value="maintenance"><Wrench className="h-3.5 w-3.5" /> Maintenance</TabsTrigger>
          <TabsTrigger value="amc"><FileSignature className="h-3.5 w-3.5" /> AMC</TabsTrigger>
          <TabsTrigger value="audit"><History className="h-3.5 w-3.5" /> Audit</TabsTrigger>
        </TabsList>

        <TabsContent value="overview">
          <Card>
            <CardHeader><CardTitle className="text-sm">Current Assignment</CardTitle></CardHeader>
            <CardContent>
              {currentAssignment ? (
                <div>
                  <p className="text-sm font-medium">{String(currentAssignment.employeeName || currentAssignment.employeeId || '')}</p>
                  <p className="text-xs text-slate-500">Assigned {currentAssignment.assignedAt ? formatDate(String(currentAssignment.assignedAt)) : '—'}</p>
                  <Button size="sm" variant="outline" className="mt-2" onClick={() => Catalog.assets.assignments.return(String(currentAssignment.id)).then(() => { toast.success('Return initiated'); qc.invalidateQueries({ queryKey: ['asset-asgn', id] }) })}>
                    <ArrowRightLeft className="h-3.5 w-3.5 mr-1" /> Initiate return
                  </Button>
                </div>
              ) : <EmptyState icon={<Users className="h-6 w-6" />} title="Asset is currently unassigned" />}
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="assignments">
          <CrudSection
            title="Assignments" icon={<Users className="h-6 w-6" />}
            items={rows(assignmentsQ.data)} loading={assignmentsQ.isLoading}
            emptyText="No assignment history" queryKey={['asset-asgn', id]}
            fields={[
              { name: 'employeeId', label: 'Assign to employee ID', type: 'text', required: true, span: 2 },
              { name: 'conditionOnAssign', label: 'Condition', type: 'text' },
              { name: 'handoverNotes', label: 'Handover notes', type: 'textarea', span: 2 },
            ]}
            onCreate={v => Catalog.assets.assignments.create({ ...v, assetId: id })}
            onDelete={aid => Catalog.assets.assignments.delete(aid)}
            renderItem={(a: AnyObj) => (<>
              <p className="text-sm font-medium">{String(a.employeeName || a.employeeId || '')} <Badge variant="secondary">{String(a.status || (a.returnedAt ? 'RETURNED' : 'ACTIVE'))}</Badge></p>
              <p className="text-xs text-slate-500">
                {a.assignedAt ? formatDate(String(a.assignedAt)) : '—'} → {a.returnedAt ? formatDate(String(a.returnedAt)) : 'current'}
                {!a.returnedAt && <button className="ml-2 text-brand-600 hover:underline" onClick={() => Catalog.assets.assignments.return(String(a.id)).then(() => { toast.success('Returned'); qc.invalidateQueries({ queryKey: ['asset-asgn', id] }) })}>Return</button>}
              </p>
            </>)}
          />
        </TabsContent>

        <TabsContent value="maintenance">
          <CrudSection
            title="Maintenance" icon={<Wrench className="h-6 w-6" />}
            items={rows(maintenanceQ.data)} loading={maintenanceQ.isLoading}
            emptyText="No maintenance records" queryKey={['asset-maint', id]}
            fields={[
              { name: 'maintenanceType', label: 'Type', type: 'select', required: true, options: [
                { value: 'PREVENTIVE', label: 'Preventive' }, { value: 'CORRECTIVE', label: 'Corrective' },
                { value: 'INSPECTION', label: 'Inspection' }, { value: 'REPAIR', label: 'Repair' },
              ] },
              { name: 'scheduledAt', label: 'Scheduled date', type: 'date', required: true },
              { name: 'vendor', label: 'Vendor', type: 'text' },
              { name: 'cost', label: 'Cost', type: 'currency' },
              { name: 'notes', label: 'Notes', type: 'textarea', span: 2 },
            ]}
            onCreate={v => assetMaintenanceService.schedule({ ...v, assetId: id })}
            onUpdate={(mid, v) => Catalog.assets.maintenance.update(mid, v)}
            onDelete={mid => Catalog.assets.maintenance.delete(mid)}
            renderItem={(m: AnyObj) => (<>
              <p className="text-sm font-medium">{String(m.title || m.maintenanceType || 'Service')} <Badge variant="secondary">{String(m.status || '')}</Badge></p>
              <p className="text-xs text-slate-500">{m.scheduledAt ? formatDate(String(m.scheduledAt)) : ''} {m.cost ? `· ₹${Number(m.cost).toLocaleString()}` : ''}
                {m.status !== 'COMPLETED' && <button className="ml-2 text-brand-600 hover:underline" onClick={() => assetMaintenanceService.complete(String(m.id), 'Completed').then(() => { toast.success('Completed'); qc.invalidateQueries({ queryKey: ['asset-maint', id] }) })}>Mark complete</button>}
              </p>
            </>)}
          />
        </TabsContent>

        <TabsContent value="amc">
          <Card><CardContent>
            <EmptyState icon={<FileSignature className="h-6 w-6" />} title="AMC contracts" description="View annual maintenance contracts for this asset." action={{ label: 'Open AMC Contracts', onClick: () => navigate('/amc-contracts') }} />
          </CardContent></Card>
        </TabsContent>

        <TabsContent value="audit">
          <Card><CardContent>
            {auditQ.isLoading ? <Skeleton className="h-20" /> : rows(auditQ.data).length === 0 ? (
              <EmptyState icon={<History className="h-6 w-6" />} title="No audit events" />
            ) : (
              <ul className="border-l-2 border-slate-100 ml-2 space-y-3">
                {rows(auditQ.data).map((e: AnyObj, i: number) => (
                  <li key={String(e.id) || `${i}`} className="ml-3 -translate-x-[7px]">
                    <span className="inline-block h-2.5 w-2.5 rounded-full bg-brand-500 mr-2" />
                    <span className="text-sm">{String(e.action || e.eventType || '')}</span>
                    <span className="block text-xs text-slate-500 ml-4">{e.at ? formatDate(String(e.at)) : ''} {e.actor ? `· by ${String(e.actor)}` : ''}</span>
                  </li>
                ))}
              </ul>
            )}
          </CardContent></Card>
        </TabsContent>
      </Tabs>
    </div>
  )
}
