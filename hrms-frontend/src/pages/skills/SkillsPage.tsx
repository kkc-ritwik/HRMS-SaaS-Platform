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
import { useAuthStore } from '@/store/authStore'
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
  const [tab, setTab] = useState<'library' | 'gap' | 'experts'>('library')
  const [creating, setCreating] = useState(false)
  const [rating, setRating] = useState(false)
  const [roleReq, setRoleReq] = useState(false)
  const [expertSkillId, setExpertSkillId] = useState('')
  const [gapDesignationId, setGapDesignationId] = useState('')
  const user = useAuthStore(s => s.user)
  const gapEmployeeId = user?.employeeId || user?.id || ''

  const libQ = useQuery({ queryKey: ['skills'], queryFn: async () => (await api.get('/api/v1/skills')).data, enabled: tab === 'library' || tab === 'experts' })
  const desigQ = useQuery({ queryKey: ['designations'], queryFn: async () => (await api.get('/api/v1/designations')).data, enabled: tab === 'gap' })
  const gapQ = useQuery({ queryKey: ['skills-gap', gapEmployeeId, gapDesignationId], queryFn: () => skillsService.gap(gapEmployeeId, gapDesignationId), enabled: tab === 'gap' && !!gapEmployeeId && !!gapDesignationId })
  const expertsQ = useQuery({ queryKey: ['skill-experts', expertSkillId], queryFn: () => skillsService.experts(expertSkillId), enabled: tab === 'experts' && !!expertSkillId })

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

      <Tabs value={tab} onValueChange={v => setTab(v as 'library' | 'gap' | 'experts')}>
        <TabsList>
          <TabsTrigger value="library">Skill Library</TabsTrigger>
          <TabsTrigger value="gap">Gap Analysis</TabsTrigger>
          <TabsTrigger value="experts">Find Experts</TabsTrigger>
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
      ) : tab === 'experts' ? (
        <Card><CardContent className="p-4 space-y-3">
          <div className="flex items-center gap-2">
            <span className="text-sm text-slate-500">Skill:</span>
            <select className="h-9 rounded-md border border-slate-300 bg-white px-2 text-sm min-w-[220px]"
              value={expertSkillId} onChange={e => setExpertSkillId(e.target.value)}>
              <option value="">Select a skill…</option>
              {rows<Skill>(libQ.data).map(s => <option key={s.id} value={s.id}>{s.name}</option>)}
            </select>
          </div>
          {!expertSkillId ? (
            <EmptyState icon={<Users className="h-8 w-8" />} title="Pick a skill" description="Choose a skill to find employees rated proficient in it." />
          ) : expertsQ.isLoading ? <Skeleton className="h-40" /> : rows(expertsQ.data).length === 0 ? (
            <EmptyState icon={<Users className="h-8 w-8" />} title="No experts found" description="No employee meets the proficiency threshold for this skill." />
          ) : (
            <table className="w-full text-sm">
              <thead className="text-xs uppercase text-slate-500 border-b"><tr><th className="text-left p-2">Employee</th><th className="text-left p-2">Proficiency</th><th className="text-left p-2">Experience</th></tr></thead>
              <tbody>{rows<AnyObj>(expertsQ.data).map((x, i) => (
                <tr key={i} className="border-b last:border-0">
                  <td className="p-2">{String(x.employeeName ?? x.employeeId ?? '—')}</td>
                  <td className="p-2"><Badge>{String(x.level ?? x.proficiency ?? '—')}/5</Badge></td>
                  <td className="p-2">{x.yearsExperience != null ? `${x.yearsExperience} yr` : '—'}</td>
                </tr>
              ))}</tbody>
            </table>
          )}
        </CardContent></Card>
      ) : (
        <Card><CardContent className="p-4 space-y-3">
          <div className="flex items-center gap-2">
            <span className="text-sm text-slate-500">Target role:</span>
            <select className="h-9 rounded-md border border-slate-300 bg-white px-2 text-sm min-w-[220px]"
              value={gapDesignationId} onChange={e => setGapDesignationId(e.target.value)}>
              <option value="">Select a designation…</option>
              {rows<{ id: string; name?: string; title?: string }>(desigQ.data).map(d => <option key={d.id} value={d.id}>{d.name ?? d.title ?? d.id.slice(0, 8)}</option>)}
            </select>
          </div>
          {!gapDesignationId ? (
            <EmptyState icon={<Target className="h-8 w-8" />} title="Pick a target designation" description="Choose a role to compare your current skills against its requirements." />
          ) : gapQ.isLoading ? <Skeleton className="h-40" /> : rows(gapQ.data).length === 0 ? (
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
