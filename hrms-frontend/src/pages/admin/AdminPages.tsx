/**
 * Admin pages bundle — Users, Roles, ApiKeys, LegalEntities, CustomFields, Biometric.
 * Each is a focused list+create page using the shared DataList + FormDialog pattern.
 */
import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Plus, KeyRound, Users, Shield, Building2, Tag, Fingerprint } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { DataList } from '@/components/ui/data-list'
import { FormDialog } from '@/components/ui/form-dialog'
import { userService, roleService, apiKeyService, legalEntityService, customFieldService, biometricService } from '@/services/adminServices'

interface User { id: string; email: string; fullName: string; roles: string[]; active: boolean }
interface Role { id: string; name: string; permissions: string[] }
interface ApiKey { id: string; name: string; prefix: string; scope: string; lastUsedAt?: string; expiresAt?: string }
interface LegalEntity { id: string; name: string; country: string; taxNumber?: string; active: boolean }
interface CustomField { id: string; entity: string; name: string; label: string; type: string; required: boolean }
interface BiometricDevice { id: string; name: string; type: string; location?: string; serialNumber?: string; lastSyncAt?: string; status: string }

export function UsersPage() {
  const qc = useQueryClient()
  const [creating, setCreating] = useState(false)
  const { data, isLoading } = useQuery({ queryKey: ['users'], queryFn: userService.list })
  const items: User[] = (data as { content?: User[] } | undefined)?.content || (Array.isArray(data) ? data as User[] : [])
  const create = useMutation({ mutationFn: userService.create, onSuccess: () => qc.invalidateQueries({ queryKey: ['users'] }) })
  const setStatus = useMutation({
    mutationFn: ({ id, status }: { id: string; status: string }) => userService.setStatus(id, status),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['users'] }),
  })
  return (
    <>
      <DataList<User>
        title="Users" description="Platform login accounts"
        action={<Button onClick={() => setCreating(true)}><Plus className="h-4 w-4 mr-1" /> New User</Button>}
        data={items} isLoading={isLoading}
        emptyIcon={<Users className="h-10 w-10" />} emptyTitle="No users"
        columns={[
          { key: 'fullName', label: 'Name' }, { key: 'email', label: 'Email' },
          { key: 'roles', label: 'Roles', render: u => u.roles?.map(r => <Badge key={r} className="mr-1">{r}</Badge>) },
          { key: 'active', label: 'Status', render: u => <Badge>{u.active ? 'Active' : 'Inactive'}</Badge> },
          { key: 'id', label: '', align: 'right', sortable: false, render: u => (
            <Button size="sm" variant={u.active ? 'outline' : 'default'} className="h-7"
              onClick={() => setStatus.mutate({ id: u.id, status: u.active ? 'INACTIVE' : 'ACTIVE' })}>
              {u.active ? 'Deactivate' : 'Activate'}
            </Button>
          ) },
        ]}
      />
      <FormDialog open={creating} onOpenChange={setCreating} title="Add user"
        onSubmit={v => create.mutateAsync(v)}
        fields={[
          { name: 'fullName', label: 'Full name', type: 'text', required: true },
          { name: 'email', label: 'Email', type: 'email', required: true },
          { name: 'password', label: 'Temporary password', type: 'text', required: true },
        ]}
      />
    </>
  )
}

export function RolesPage() {
  const qc = useQueryClient()
  const [creating, setCreating] = useState(false)
  const { data, isLoading } = useQuery({ queryKey: ['roles'], queryFn: roleService.list })
  const items: Role[] = (data as { content?: Role[] } | undefined)?.content || (Array.isArray(data) ? data as Role[] : [])
  const create = useMutation({ mutationFn: roleService.create, onSuccess: () => qc.invalidateQueries({ queryKey: ['roles'] }) })
  return (
    <>
      <DataList<Role>
        title="Roles" description="RBAC role catalogue"
        action={<Button onClick={() => setCreating(true)}><Plus className="h-4 w-4 mr-1" /> New Role</Button>}
        data={items} isLoading={isLoading}
        emptyIcon={<Shield className="h-10 w-10" />} emptyTitle="No roles"
        columns={[
          { key: 'name', label: 'Name' },
          { key: 'permissions', label: 'Permissions', render: r => <Badge>{r.permissions?.length || 0} perms</Badge> },
        ]}
      />
      <FormDialog open={creating} onOpenChange={setCreating} title="Create role"
        onSubmit={v => create.mutateAsync(v)}
        fields={[
          { name: 'name', label: 'Role name', type: 'text', required: true },
          { name: 'description', label: 'Description', type: 'textarea', span: 2 },
        ]}
      />
    </>
  )
}

export function ApiKeysPage() {
  const qc = useQueryClient()
  const [creating, setCreating] = useState(false)
  const { data, isLoading } = useQuery({ queryKey: ['api-keys'], queryFn: apiKeyService.list })
  const items: ApiKey[] = (data as { content?: ApiKey[] } | undefined)?.content || (Array.isArray(data) ? data as ApiKey[] : [])
  const create = useMutation({ mutationFn: apiKeyService.create, onSuccess: () => qc.invalidateQueries({ queryKey: ['api-keys'] }) })
  return (
    <>
      <DataList<ApiKey>
        title="API Keys" description="Machine-to-machine access tokens"
        action={<Button onClick={() => setCreating(true)}><Plus className="h-4 w-4 mr-1" /> New Key</Button>}
        data={items} isLoading={isLoading}
        emptyIcon={<KeyRound className="h-10 w-10" />} emptyTitle="No API keys"
        columns={[
          { key: 'name', label: 'Name' },
          { key: 'prefix', label: 'Prefix', render: k => <code className="text-xs">{k.prefix}...</code> },
          { key: 'scope', label: 'Scope' },
          { key: 'lastUsedAt', label: 'Last used' },
          { key: 'expiresAt', label: 'Expires' },
        ]}
      />
      <FormDialog open={creating} onOpenChange={setCreating} title="Generate API key"
        onSubmit={v => create.mutateAsync(v)}
        fields={[
          { name: 'name', label: 'Name', type: 'text', required: true, span: 2 },
          { name: 'scope', label: 'Scope', type: 'select', required: true, options: [
            { value: 'READ', label: 'Read-only' }, { value: 'WRITE', label: 'Read + Write' }, { value: 'ADMIN', label: 'Admin' },
          ] },
          { name: 'expiryDays', label: 'Expires in (days)', type: 'number', defaultValue: 365 },
        ]}
      />
    </>
  )
}

export function LegalEntitiesPage() {
  const qc = useQueryClient()
  const [creating, setCreating] = useState(false)
  const { data, isLoading } = useQuery({ queryKey: ['legal-entities'], queryFn: legalEntityService.list })
  const items: LegalEntity[] = (data as { content?: LegalEntity[] } | undefined)?.content || (Array.isArray(data) ? data as LegalEntity[] : [])
  const create = useMutation({ mutationFn: legalEntityService.create, onSuccess: () => qc.invalidateQueries({ queryKey: ['legal-entities'] }) })
  return (
    <>
      <DataList<LegalEntity>
        title="Legal Entities" description="Subsidiaries, branches, joint ventures"
        action={<Button onClick={() => setCreating(true)}><Plus className="h-4 w-4 mr-1" /> New Entity</Button>}
        data={items} isLoading={isLoading}
        emptyIcon={<Building2 className="h-10 w-10" />} emptyTitle="No legal entities"
        columns={[
          { key: 'name', label: 'Name' }, { key: 'country', label: 'Country' },
          { key: 'taxNumber', label: 'Tax #' },
          { key: 'active', label: 'Active', render: e => <Badge>{e.active ? 'Active' : 'Inactive'}</Badge> },
        ]}
      />
      <FormDialog open={creating} onOpenChange={setCreating} title="Create legal entity"
        onSubmit={v => create.mutateAsync(v)}
        fields={[
          { name: 'name', label: 'Name', type: 'text', required: true, span: 2 },
          { name: 'country', label: 'Country', type: 'text', required: true },
          { name: 'taxNumber', label: 'Tax number', type: 'text' },
          { name: 'registrationNumber', label: 'Registration #', type: 'text' },
          { name: 'currency', label: 'Currency', type: 'text', defaultValue: 'INR' },
        ]}
      />
    </>
  )
}

export function CustomFieldsPage() {
  const qc = useQueryClient()
  const [entity, setEntity] = useState('EMPLOYEE')
  const [creating, setCreating] = useState(false)
  const { data, isLoading } = useQuery({ queryKey: ['custom-fields', entity], queryFn: customFieldService.list(entity) })
  const items: CustomField[] = (data as { content?: CustomField[] } | undefined)?.content || (Array.isArray(data) ? data as CustomField[] : [])
  const create = useMutation({ mutationFn: customFieldService.create, onSuccess: () => qc.invalidateQueries({ queryKey: ['custom-fields'] }) })
  return (
    <>
      <div className="mb-4 flex items-center gap-3">
        <span className="text-sm text-slate-500">For entity:</span>
        <select className="h-9 rounded-md border px-2 text-sm" value={entity} onChange={e => setEntity(e.target.value)}>
          {['EMPLOYEE', 'CANDIDATE', 'JOB', 'LEAVE', 'EXPENSE', 'ASSET'].map(o => <option key={o} value={o}>{o}</option>)}
        </select>
      </div>
      <DataList<CustomField>
        title="Custom Fields" description="Per-tenant extensions to standard entities"
        action={<Button onClick={() => setCreating(true)}><Plus className="h-4 w-4 mr-1" /> New Field</Button>}
        data={items} isLoading={isLoading}
        emptyIcon={<Tag className="h-10 w-10" />} emptyTitle="No custom fields"
        columns={[
          { key: 'label', label: 'Label' }, { key: 'name', label: 'Field name' },
          { key: 'type', label: 'Type', render: f => <Badge>{f.type}</Badge> },
          { key: 'required', label: 'Required', render: f => f.required ? '✓' : '—' },
        ]}
      />
      <FormDialog open={creating} onOpenChange={setCreating} title="Add custom field"
        onSubmit={v => create.mutateAsync({ ...v, entity })}
        fields={[
          { name: 'label', label: 'Display label', type: 'text', required: true },
          { name: 'name', label: 'Field name (snake_case)', type: 'text', required: true },
          { name: 'type', label: 'Type', type: 'select', required: true, options: [
            { value: 'TEXT', label: 'Text' }, { value: 'NUMBER', label: 'Number' }, { value: 'DATE', label: 'Date' },
            { value: 'SELECT', label: 'Dropdown' }, { value: 'BOOLEAN', label: 'Toggle' },
          ] },
          { name: 'required', label: 'Required', type: 'switch' },
        ]}
      />
    </>
  )
}

export function BiometricDevicesPage() {
  const qc = useQueryClient()
  const [creating, setCreating] = useState(false)
  const { data, isLoading } = useQuery({ queryKey: ['biometric-devices'], queryFn: biometricService.devices })
  const items: BiometricDevice[] = (data as { content?: BiometricDevice[] } | undefined)?.content || (Array.isArray(data) ? data as BiometricDevice[] : [])
  const create = useMutation({ mutationFn: biometricService.registerDevice, onSuccess: () => qc.invalidateQueries({ queryKey: ['biometric-devices'] }) })
  const sync = useMutation({ mutationFn: (id: string) => biometricService.syncDevice(id), onSuccess: () => qc.invalidateQueries({ queryKey: ['biometric-devices'] }) })
  return (
    <>
      <DataList<BiometricDevice>
        title="Biometric Devices" description="Fingerprint / face / RFID terminals across locations"
        action={<Button onClick={() => setCreating(true)}><Plus className="h-4 w-4 mr-1" /> Register Device</Button>}
        data={items} isLoading={isLoading}
        emptyIcon={<Fingerprint className="h-10 w-10" />} emptyTitle="No devices registered"
        columns={[
          { key: 'name', label: 'Name' }, { key: 'type', label: 'Type', render: d => <Badge>{d.type}</Badge> },
          { key: 'location', label: 'Location' }, { key: 'serialNumber', label: 'Serial' },
          { key: 'lastSyncAt', label: 'Last sync' },
          { key: 'status', label: 'Status', render: d => <Badge>{d.status}</Badge> },
          { key: 'id', label: '', render: d => <Button size="sm" variant="outline" onClick={() => sync.mutate(d.id)}>Sync</Button> },
        ]}
      />
      <FormDialog open={creating} onOpenChange={setCreating} title="Register biometric device"
        onSubmit={v => create.mutateAsync(v)}
        fields={[
          { name: 'name', label: 'Device name', type: 'text', required: true },
          { name: 'type', label: 'Type', type: 'select', required: true, options: [
            { value: 'FINGERPRINT', label: 'Fingerprint' }, { value: 'FACE', label: 'Face' },
            { value: 'RFID', label: 'RFID/Card' }, { value: 'IRIS', label: 'Iris' },
          ] },
          { name: 'location', label: 'Location', type: 'text' },
          { name: 'serialNumber', label: 'Serial number', type: 'text' },
          { name: 'ipAddress', label: 'IP address', type: 'text' },
        ]}
      />
    </>
  )
}
