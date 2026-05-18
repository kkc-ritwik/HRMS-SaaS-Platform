import React, { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { toast } from 'sonner'
import { Plus, Edit, Trash2, MapPin, MoreVertical, Users, Phone, Globe } from 'lucide-react'
import { PageHeader } from '@/components/ui/page-header'
import { Button } from '@/components/ui/button'
import { SearchInput } from '@/components/ui/search-input'
import { Card, CardContent } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Input } from '@/components/ui/input'
import { FormField } from '@/components/ui/form-field'
import { ConfirmDialog } from '@/components/ui/confirm-dialog'
import { EmptyState } from '@/components/ui/empty-state'
import { SkeletonCard } from '@/components/ui/skeleton'
import {
  Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter, DialogBody,
} from '@/components/ui/dialog'
import {
  DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuSeparator, DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { locationService, type Location } from '@/services/departmentService'
import { useDebounce } from '@/hooks/useDebounce'
import { getErrorMessage } from '@/lib/api'

const schema = z.object({
  name: z.string().min(1, 'Location name is required'),
  code: z.string().optional(),
  address: z.string().optional(),
  city: z.string().optional(),
  state: z.string().optional(),
  country: z.string().optional(),
  pincode: z.string().optional(),
  phone: z.string().optional(),
})

type FormData = z.infer<typeof schema>

const DEMO_LOCATIONS = [
  { id: '1', name: 'Bangalore HQ', code: 'BLR', city: 'Bangalore', state: 'Karnataka', country: 'India', address: 'Koramangala, Bangalore', employeeCount: 78, pincode: '560034', phone: '+91 80 1234 5678', createdAt: '', updatedAt: '' },
  { id: '2', name: 'Mumbai Office', code: 'MUM', city: 'Mumbai', state: 'Maharashtra', country: 'India', address: 'Bandra Kurla Complex', employeeCount: 34, pincode: '400051', phone: '+91 22 1234 5678', createdAt: '', updatedAt: '' },
  { id: '3', name: 'Delhi Office', code: 'DEL', city: 'New Delhi', state: 'Delhi', country: 'India', address: 'Connaught Place', employeeCount: 22, pincode: '110001', phone: '+91 11 1234 5678', createdAt: '', updatedAt: '' },
  { id: '4', name: 'Remote', code: 'REM', city: 'Virtual', state: '', country: 'India', address: 'Work from Home', employeeCount: 10, pincode: '', phone: '', createdAt: '', updatedAt: '' },
]

export function LocationsPage() {
  const queryClient = useQueryClient()
  const [search, setSearch] = useState('')
  const debouncedSearch = useDebounce(search)
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [editingItem, setEditingItem] = useState<Location | null>(null)
  const [deleteId, setDeleteId] = useState<string | null>(null)

  const { data, isLoading } = useQuery({
    queryKey: ['locations', debouncedSearch],
    queryFn: async () => {
      try {
        return await locationService.list({ search: debouncedSearch || undefined })
      } catch {
        return { data: { locations: DEMO_LOCATIONS, total: DEMO_LOCATIONS.length } }
      }
    },
  })

  const createMutation = useMutation({
    mutationFn: locationService.create,
    onSuccess: () => { toast.success('Location created'); queryClient.invalidateQueries({ queryKey: ['locations'] }); setIsModalOpen(false); reset() },
    onError: (e: unknown) => toast.error(getErrorMessage(e)),
  })

  const updateMutation = useMutation({
    mutationFn: ({ id, ...d }: { id: string } & FormData) => locationService.update(id, d),
    onSuccess: () => { toast.success('Location updated'); queryClient.invalidateQueries({ queryKey: ['locations'] }); setIsModalOpen(false); setEditingItem(null); reset() },
    onError: (e: unknown) => toast.error(getErrorMessage(e)),
  })

  const deleteMutation = useMutation({
    mutationFn: locationService.delete,
    onSuccess: () => { toast.success('Location deleted'); queryClient.invalidateQueries({ queryKey: ['locations'] }); setDeleteId(null) },
    onError: (e: unknown) => toast.error(getErrorMessage(e)),
  })

  const { register, handleSubmit, reset, setValue, formState: { errors } } = useForm<FormData>({ resolver: zodResolver(schema) })

  const openEdit = (item: Location) => {
    setEditingItem(item)
    setValue('name', item.name); setValue('code', item.code || ''); setValue('address', item.address || '')
    setValue('city', item.city || ''); setValue('state', item.state || ''); setValue('country', item.country || '')
    setValue('pincode', item.pincode || ''); setValue('phone', item.phone || '')
    setIsModalOpen(true)
  }

  const locations = data?.data?.locations || DEMO_LOCATIONS

  const filtered = locations.filter((l: Location) =>
    !debouncedSearch || l.name.toLowerCase().includes(debouncedSearch.toLowerCase()) ||
    l.city?.toLowerCase().includes(debouncedSearch.toLowerCase())
  )

  return (
    <div className="space-y-5 animate-fade-in">
      <PageHeader title="Locations" description={`${locations.length} locations`} breadcrumbs={[{ label: 'People' }, { label: 'Locations' }]}>
        <Button size="sm" onClick={() => { setEditingItem(null); reset(); setIsModalOpen(true) }}>
          <Plus className="h-4 w-4" /> Add Location
        </Button>
      </PageHeader>

      <div className="flex items-center gap-3">
        <SearchInput value={search} onChange={e => setSearch(e.target.value)} onClear={() => setSearch('')} placeholder="Search locations..." containerClassName="w-64" />
      </div>

      {isLoading ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {Array.from({ length: 4 }).map((_, i) => <SkeletonCard key={i} />)}
        </div>
      ) : filtered.length === 0 ? (
        <EmptyState icon={<MapPin className="h-8 w-8" />} title="No locations found" action={{ label: 'Add Location', onClick: () => setIsModalOpen(true) }} />
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {filtered.map((loc: Location) => (
            <Card key={loc.id} className="hover:shadow-card-hover transition-shadow">
              <CardContent className="pt-5">
                <div className="flex items-start justify-between mb-4">
                  <div className="flex h-11 w-11 items-center justify-center rounded-xl bg-brand-100">
                    <MapPin className="h-5 w-5 text-brand-600" />
                  </div>
                  <DropdownMenu>
                    <DropdownMenuTrigger asChild>
                      <Button variant="ghost" size="icon-sm"><MoreVertical className="h-4 w-4" /></Button>
                    </DropdownMenuTrigger>
                    <DropdownMenuContent align="end">
                      <DropdownMenuItem onClick={() => openEdit(loc)}><Edit className="h-4 w-4" /> Edit</DropdownMenuItem>
                      <DropdownMenuSeparator />
                      <DropdownMenuItem destructive onClick={() => setDeleteId(loc.id)}><Trash2 className="h-4 w-4" /> Delete</DropdownMenuItem>
                    </DropdownMenuContent>
                  </DropdownMenu>
                </div>
                <h3 className="font-semibold text-slate-900 mb-1">{loc.name}</h3>
                {loc.code && <Badge variant="secondary" className="mb-2 text-[10px]">{loc.code}</Badge>}
                <div className="space-y-1.5 mt-2">
                  {loc.city && (
                    <div className="flex items-center gap-1.5 text-xs text-slate-500">
                      <Globe className="h-3 w-3" />
                      {loc.city}, {loc.state || ''} {loc.country ? `· ${loc.country}` : ''}
                    </div>
                  )}
                  {loc.phone && (
                    <div className="flex items-center gap-1.5 text-xs text-slate-500">
                      <Phone className="h-3 w-3" />
                      {loc.phone}
                    </div>
                  )}
                </div>
                <div className="flex items-center gap-1.5 text-sm text-slate-600 border-t border-slate-50 pt-3 mt-3">
                  <Users className="h-3.5 w-3.5 text-slate-400" />
                  {loc.employeeCount || 0} employees
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      <Dialog open={isModalOpen} onOpenChange={setIsModalOpen}>
        <DialogContent>
          <DialogHeader><DialogTitle>{editingItem ? 'Edit Location' : 'Add Location'}</DialogTitle></DialogHeader>
          <form onSubmit={handleSubmit(d => editingItem ? updateMutation.mutate({ id: editingItem.id, ...d }) : createMutation.mutate(d))}>
            <DialogBody className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <FormField label="Location Name" error={errors.name?.message} required>
                  <Input {...register('name')} placeholder="Bangalore HQ" />
                </FormField>
                <FormField label="Code">
                  <Input {...register('code')} placeholder="BLR" />
                </FormField>
              </div>
              <FormField label="Address">
                <Input {...register('address')} placeholder="Street address" />
              </FormField>
              <div className="grid grid-cols-2 gap-4">
                <FormField label="City">
                  <Input {...register('city')} placeholder="Bangalore" />
                </FormField>
                <FormField label="State">
                  <Input {...register('state')} placeholder="Karnataka" />
                </FormField>
              </div>
              <div className="grid grid-cols-3 gap-4">
                <FormField label="Country">
                  <Input {...register('country')} placeholder="India" />
                </FormField>
                <FormField label="Pincode">
                  <Input {...register('pincode')} placeholder="560034" />
                </FormField>
                <FormField label="Phone">
                  <Input {...register('phone')} placeholder="+91 80..." />
                </FormField>
              </div>
            </DialogBody>
            <DialogFooter>
              <Button type="button" variant="outline" onClick={() => setIsModalOpen(false)}>Cancel</Button>
              <Button type="submit" loading={createMutation.isPending || updateMutation.isPending}>
                {editingItem ? 'Update' : 'Create'} Location
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

      <ConfirmDialog open={!!deleteId} onOpenChange={open => !open && setDeleteId(null)} title="Delete Location" confirmLabel="Delete" onConfirm={() => deleteId && deleteMutation.mutate(deleteId)} loading={deleteMutation.isPending} />
    </div>
  )
}
