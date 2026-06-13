import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Plus, Sparkles, Target, Users } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent } from '@/components/ui/card'
import { DataList } from '@/components/ui/data-list'
import { EmptyState } from '@/components/ui/empty-state'
import { Skeleton } from '@/components/ui/skeleton'
import { PageHeader } from '@/components/ui/page-header'
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { FormDialog } from '@/components/ui/form-dialog'
import { skillsService } from '@/services/extendedServices'
import { api } from '@/lib/api'
import { toast } from 'sonner'

type AnyObj = Record<string, unknown>
function rows<T = AnyObj>(d: unknown): T[] {
  if (Array.isArray(d)) return d as T[]
  const o = d as { content?: T[] } | undefined
  return Array.isArray(o?.content) ? o!.content : []
}
interface Skill extends Record<string, unknown> { id: string; name: string; category?: string; description?: string }

export function SkillsPage() {
  const qc = useQueryClient()
  const [tab, setTab] = useState<'library' | 'gap' | 'role'>('library')
  const [creating, setCreating] = useState(false)
  const [rating, setRating] = useState(false)
  const [roleReq, setRoleReq] = useState(false)

  const libQ = useQuery({ queryKey: ['skills'], queryFn: async () => (await api.get('/api/v1/skills')).data, enabled: tab === 'library' })
  const gapQ = useQuery({ queryKey: ['skills-gap'], queryFn: () => skillsService.gap(), enabled: tab === 'gap' })

  const create = useMutation({ mutationFn: (v: Record<string, unknown>) => api.post('/api/v1/skills', v).then(r => r.data), onSuccess: () => { toast.success('Skill added'); qc.invalidateQueries({ queryKey: ['skills'] }); setCreating(false) } })
  const rate = useMutation({ mutationFn: (v: Record<string, unknown>) => skillsService.rate(v), onSuccess: () => { toast.success('Employee skill recorded'); setRating(false) } })
  const setReq = useMutation({ mutationFn: (v: Record<string, unknown>) => skillsService.setRoleRequirement(v), onSuccess: () => { toast.success('Role requirement set'); setRoleReq(false) } })

  return (
    <div className="space-y-4">
      <PageHeader title="Skills" description="Skill library, gap analysis and role requirements"
        action={<div className="flex gap-2">
          <Button variant="outline" size="sm" onClick={() => setRating(true)}><Users className="h-4 w-4 mr-1" /> Rate Employee</Button>
          <Button variant="outline" size="sm" onClick={() => setRoleReq(true)}><Target className="h-4 w-4 mr-1" /> Role Requirement</Button>
          <Button size="sm" onClick={() => setCreating(true)}><Plus className="h-4 w-4 mr-1" /> New Skill</Button>
        </div>} />

      <Tabs value={tab} onValueChange={v => setTab(v as 'library' | 'gap' | 'role')}>
        <TabsList>
          <TabsTrigger value="library">Skill Library</TabsTrigger>
          <TabsTrigger value="gap">Gap Analysis</TabsTrigger>
        </TabsList>
      </Tabs>

      {tab === 'library' ? (
        <DataList<Skill>
          title="" data={rows<Skill>(libQ.data)} isLoading={libQ.isLoading}
          emptyIcon={<Sparkles className="h-10 w-10" />} emptyTitle="No skills"
          columns={[
            { key: 'name', label: 'Skill' },
            { key: 'category', label: 'Category', render: s => s.category ? <Badge>{String(s.category)}</Badge> : '—' },
            { key: 'description', label: 'Description' },
          ]}
        />
      ) : (
        <Card><CardContent className="p-4">
          {gapQ.isLoading ? <Skeleton className="h-40" /> : rows(gapQ.data).length === 0 ? (
            <EmptyState icon={<Target className="h-8 w-8" />} title="No skill gaps detected" />
          ) : (
            <table className="w-full text-sm">
              <thead className="text-xs uppercase text-slate-500 border-b"><tr><th className="text-left p-2">Role / Employee</th><th className="text-left p-2">Skill</th><th className="text-left p-2">Required</th><th className="text-left p-2">Current</th><th className="text-left p-2">Gap</th></tr></thead>
              <tbody>{rows<AnyObj>(gapQ.data).map((g, i) => (
                <tr key={i} className="border-b last:border-0">
                  <td className="p-2">{String(g.roleName ?? g.employeeName ?? '')}</td>
                  <td className="p-2">{String(g.skillName ?? '')}</td>
                  <td className="p-2">{String(g.requiredLevel ?? '')}</td>
                  <td className="p-2">{String(g.currentLevel ?? 0)}</td>
                  <td className="p-2">{Number(g.gap ?? 0) > 0 ? <Badge variant="destructive">−{String(g.gap)}</Badge> : <Badge variant="success">Met</Badge>}</td>
                </tr>
              ))}</tbody>
            </table>
          )}
        </CardContent></Card>
      )}

      <FormDialog open={creating} onOpenChange={setCreating} title="Add skill"
        fields={[
          { name: 'name', label: 'Skill name', type: 'text', required: true },
          { name: 'category', label: 'Category', type: 'text' },
          { name: 'description', label: 'Description', type: 'textarea', span: 2 },
        ]}
        onSubmit={v => create.mutateAsync(v)} />
      <FormDialog open={rating} onOpenChange={setRating} title="Rate employee skill" submitLabel="Save"
        fields={[
          { name: 'employeeId', label: 'Employee ID', type: 'text', required: true },
          { name: 'skillId', label: 'Skill ID', type: 'text', required: true },
          { name: 'level', label: 'Proficiency (1-5)', type: 'number', required: true },
          { name: 'yearsExperience', label: 'Years experience', type: 'number' },
        ]}
        onSubmit={v => rate.mutateAsync(v)} />
      <FormDialog open={roleReq} onOpenChange={setRoleReq} title="Set role skill requirement" submitLabel="Save"
        fields={[
          { name: 'roleId', label: 'Role ID', type: 'text', required: true },
          { name: 'skillId', label: 'Skill ID', type: 'text', required: true },
          { name: 'requiredLevel', label: 'Required level (1-5)', type: 'number', required: true },
          { name: 'mandatory', label: 'Mandatory', type: 'switch' },
        ]}
        onSubmit={v => setReq.mutateAsync(v)} />
    </div>
  )
}
