import { useState, useEffect } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Edit3, Save, X, User, Mail, Phone, MapPin, Briefcase, Calendar } from 'lucide-react'
import { useAuthStore } from '@/store/authStore'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Avatar } from '@/components/ui/avatar'
import { Skeleton } from '@/components/ui/skeleton'
import { PageHeader } from '@/components/ui/page-header'
import { Tabs, TabsList, TabsTrigger, TabsContent } from '@/components/ui/tabs'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Badge } from '@/components/ui/badge'
import { authService } from '@/services/authService'
import { employeeService } from '@/services/employeeService'
import { toast } from 'sonner'

export function ProfilePage() {
  const qc = useQueryClient()
  const user = useAuthStore(s => s.user)
  const [editing, setEditing] = useState(false)
  const [form, setForm] = useState<Record<string, string>>({})

  const profile = useQuery({ queryKey: ['profile', 'me'], queryFn: authService.me })
  const me = (profile.data as { data?: Record<string, unknown> } | undefined)?.data || (user as unknown as Record<string, unknown>) || {}

  useEffect(() => {
    if (me) {
      setForm({
        phone: String(me.phone || ''),
        address: String(me.address || ''),
        city: String(me.city || ''),
        country: String(me.country || ''),
        emergencyName: String((me.emergencyContact as { name?: string })?.name || ''),
        emergencyPhone: String((me.emergencyContact as { phone?: string })?.phone || ''),
        emergencyRelation: String((me.emergencyContact as { relation?: string })?.relation || ''),
      })
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [profile.data])

  const save = useMutation({
    mutationFn: async () => {
      const empId = (user?.employeeId || me.id || '') as string
      const payload: Record<string, unknown> = {
        phone: form.phone, address: form.address, city: form.city, country: form.country,
        emergencyContact: { name: form.emergencyName, phone: form.emergencyPhone, relation: form.emergencyRelation },
      }
      return employeeService.update(empId, payload)
    },
    onSuccess: () => { toast.success('Profile updated'); setEditing(false); qc.invalidateQueries({ queryKey: ['profile'] }) },
  })

  if (profile.isLoading) return <Skeleton className="h-96" />

  return (
    <div className="space-y-6">
      <PageHeader
        title="My Profile"
        action={editing
          ? <div className="flex gap-2"><Button size="sm" variant="outline" onClick={() => setEditing(false)}><X className="h-4 w-4 mr-1" />Cancel</Button><Button size="sm" onClick={() => save.mutate()}><Save className="h-4 w-4 mr-1" />Save</Button></div>
          : <Button size="sm" onClick={() => setEditing(true)}><Edit3 className="h-4 w-4 mr-1" />Edit</Button>}
      />

      <Card>
        <CardHeader className="flex flex-row items-center gap-4">
          <Avatar name={user?.fullName} size="lg" />
          <div>
            <CardTitle>{user?.fullName}</CardTitle>
            <p className="text-sm text-slate-500">{user?.email}</p>
            <div className="flex gap-1 mt-1">{user?.roles?.map(r => <Badge key={r}>{r.replace(/_/g, ' ')}</Badge>)}</div>
          </div>
        </CardHeader>
      </Card>

      <Tabs defaultValue="personal">
        <TabsList>
          <TabsTrigger value="personal"><User className="h-3.5 w-3.5 mr-1" />Personal</TabsTrigger>
          <TabsTrigger value="employment"><Briefcase className="h-3.5 w-3.5 mr-1" />Employment</TabsTrigger>
          <TabsTrigger value="emergency">Emergency</TabsTrigger>
        </TabsList>

        <TabsContent value="personal">
          <Card>
            <CardContent className="p-6 grid grid-cols-1 md:grid-cols-2 gap-4">
              {[
                ['firstName', 'First name', false],
                ['lastName', 'Last name', false],
                ['email', 'Email', false],
                ['phone', 'Phone', true],
                ['dateOfBirth', 'Date of birth', false],
                ['gender', 'Gender', false],
                ['address', 'Address', true],
                ['city', 'City', true],
                ['country', 'Country', true],
                ['panNumber', 'PAN', false],
              ].map(([k, label, editable]) => (
                <div key={k as string}>
                  <Label className="text-xs uppercase text-slate-500">{label as string}</Label>
                  {editing && editable
                    ? <Input value={form[k as string] || ''} onChange={e => setForm({ ...form, [k as string]: e.target.value })} />
                    : <p className="text-sm font-medium text-slate-800 py-1.5">{String((me as Record<string, unknown>)[k as string] ?? form[k as string] ?? '—')}</p>}
                </div>
              ))}
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="employment">
          <Card>
            <CardContent className="p-6 grid grid-cols-1 md:grid-cols-2 gap-4">
              {['employeeId', 'departmentName', 'designationName', 'managerName', 'locationName', 'employmentType', 'status', 'joinDate'].map(k => (
                <div key={k}>
                  <Label className="text-xs uppercase text-slate-500">{k.replace(/([A-Z])/g, ' $1').replace(/Id$/, ' ID')}</Label>
                  <p className="text-sm font-medium text-slate-800 py-1.5">{String((me as Record<string, unknown>)[k] ?? '—')}</p>
                </div>
              ))}
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="emergency">
          <Card>
            <CardContent className="p-6 grid grid-cols-1 md:grid-cols-3 gap-4">
              <div><Label>Name</Label>{editing ? <Input value={form.emergencyName} onChange={e => setForm({ ...form, emergencyName: e.target.value })} /> : <p className="text-sm py-1.5">{form.emergencyName || '—'}</p>}</div>
              <div><Label>Phone</Label>{editing ? <Input value={form.emergencyPhone} onChange={e => setForm({ ...form, emergencyPhone: e.target.value })} /> : <p className="text-sm py-1.5">{form.emergencyPhone || '—'}</p>}</div>
              <div><Label>Relation</Label>{editing ? <Input value={form.emergencyRelation} onChange={e => setForm({ ...form, emergencyRelation: e.target.value })} /> : <p className="text-sm py-1.5">{form.emergencyRelation || '—'}</p>}</div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  )
}
