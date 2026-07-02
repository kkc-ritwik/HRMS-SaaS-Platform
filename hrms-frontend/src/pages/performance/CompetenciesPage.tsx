import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Target, Plus, Trash2 } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { EmptyState } from '@/components/ui/empty-state'
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { FormDialog } from '@/components/ui/form-dialog'
import { ResourcePage } from '@/components/ui/resource-page'
import { competencyService } from '@/services/extendedServices'
import { roleService } from '@/services/adminServices'
import { Catalog } from '@/services/catalog'
import { getErrorMessage } from '@/lib/api'
import { toast } from 'sonner'

interface Competency extends Record<string, unknown> { id: string; name: string; description?: string; category?: string; level?: string }
type AnyObj = Record<string, unknown>
function rows<T = AnyObj>(d: unknown): T[] {
  if (Array.isArray(d)) return d as T[]
  const o = d as { content?: T[] } | undefined
  return Array.isArray(o?.content) ? o!.content : []
}
const selectCls = 'h-9 rounded-md border border-slate-300 bg-white px-2 text-sm min-w-[220px]'

function RoleRequirements() {
  const qc = useQueryClient()
  const [roleId, setRoleId] = useState('')
  const [adding, setAdding] = useState(false)
  const rolesQ = useQuery({ queryKey: ['roles'], queryFn: roleService.list })
  const compsQ = useQuery({ queryKey: ['competencies'], queryFn: () => competencyService.list() })
  const mapQ = useQuery({ queryKey: ['role-mappings', roleId], queryFn: () => Catalog.competencies.roleMappings.forRole(roleId), enabled: !!roleId })

  const add = useMutation({
    mutationFn: (v: Record<string, unknown>) => Catalog.competencies.roleMappings.create({ roleId, competencyId: v.competencyId, requiredLevel: Number(v.requiredLevel ?? 3) }),
    onSuccess: () => { toast.success('Requirement added'); setAdding(false); qc.invalidateQueries({ queryKey: ['role-mappings', roleId] }) },
    onError: e => toast.error(getErrorMessage(e)),
  })
  const remove = useMutation({
    mutationFn: (id: string) => Catalog.competencies.roleMappings.delete(id),
    onSuccess: () => { toast.success('Requirement removed'); qc.invalidateQueries({ queryKey: ['role-mappings', roleId] }) },
    onError: e => toast.error(getErrorMessage(e)),
  })

  return (
    <Card><CardContent className="p-4 space-y-3">
      <div className="flex items-center justify-between gap-2">
        <div className="flex items-center gap-2">
          <span className="text-sm text-slate-500">Role:</span>
          <select className={selectCls} value={roleId} onChange={e => setRoleId(e.target.value)}>
            <option value="">Select a role…</option>
            {rows<{ id: string; name: string }>(rolesQ.data).map(r => <option key={r.id} value={r.id}>{r.name}</option>)}
          </select>
        </div>
        {roleId && <Button size="sm" onClick={() => setAdding(true)}><Plus className="h-4 w-4 mr-1" /> Add requirement</Button>}
      </div>
      {!roleId ? (
        <EmptyState icon={<Target className="h-8 w-8" />} title="Pick a role" description="Choose a role to view and manage its required competencies." />
      ) : mapQ.isLoading ? <Skeleton className="h-32" /> : rows(mapQ.data).length === 0 ? (
        <EmptyState icon={<Target className="h-8 w-8" />} title="No competency requirements" description="Add the competencies this role requires." />
      ) : (
        <div className="space-y-2">
          {rows<AnyObj>(mapQ.data).map(m => (
            <div key={String(m.id)} className="flex items-center justify-between rounded border p-3">
              <div>
                <p className="text-sm font-medium">{String(m.competencyName ?? m.competencyId ?? 'Competency')}</p>
                <p className="text-xs text-slate-500">Required level {String(m.requiredLevel ?? '—')}/5</p>
              </div>
              <Button size="sm" variant="ghost" className="text-red-500 h-7 w-7 p-0" onClick={() => remove.mutate(String(m.id))}><Trash2 className="h-4 w-4" /></Button>
            </div>
          ))}
        </div>
      )}
      <FormDialog open={adding} onOpenChange={setAdding} title="Add competency requirement" submitLabel="Add"
        fields={[
          { name: 'competencyId', label: 'Competency', type: 'select', required: true, options: rows<Competency>(compsQ.data).map(c => ({ value: c.id, label: c.name })) },
          { name: 'requiredLevel', label: 'Required level (1-5)', type: 'number', required: true },
        ]}
        onSubmit={v => add.mutateAsync(v)} />
    </CardContent></Card>
  )
}

export function CompetenciesPage() {
  const [tab, setTab] = useState<'library' | 'roles'>('library')
  return (
    <div className="space-y-4">
      <Tabs value={tab} onValueChange={v => setTab(v as 'library' | 'roles')}>
        <TabsList>
          <TabsTrigger value="library">Library</TabsTrigger>
          <TabsTrigger value="roles">Role requirements</TabsTrigger>
        </TabsList>
      </Tabs>

      {tab === 'roles' ? <RoleRequirements /> : (
        <ResourcePage<Competency>
          title="Competencies"
          description="Skills + behavioural competencies used in reviews"
          icon={<Target className="h-10 w-10" />}
          queryKey={['competencies']}
          fetcher={() => competencyService.list()}
          filters={{ category: ['TECHNICAL', 'BEHAVIOURAL', 'LEADERSHIP', 'FUNCTIONAL'] }}
          columns={[
            { key: 'name', label: 'Name' },
            { key: 'category', label: 'Category', render: c => c.category ? <Badge>{String(c.category)}</Badge> : '—' },
            { key: 'level', label: 'Level' },
            { key: 'description', label: 'Description' },
          ]}
          formFields={[
            { name: 'name', label: 'Name', type: 'text', required: true, span: 2 },
            { name: 'category', label: 'Category', type: 'select', required: true, options: [
              { value: 'TECHNICAL', label: 'Technical' }, { value: 'BEHAVIOURAL', label: 'Behavioural' },
              { value: 'LEADERSHIP', label: 'Leadership' }, { value: 'FUNCTIONAL', label: 'Functional' },
            ] },
            { name: 'description', label: 'Description', type: 'textarea', span: 2 },
          ]}
          onCreate={v => competencyService.create(v)}
          onUpdate={(id, v) => Catalog.competencies.update(id, v)}
        />
      )}
    </div>
  )
}
