import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Plus, Sparkles } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { DataList } from '@/components/ui/data-list'
import { FormDialog } from '@/components/ui/form-dialog'
import { api } from '@/lib/api'

interface Skill { id: string; name: string; category?: string; description?: string }

export function SkillsPage() {
  const qc = useQueryClient()
  const [creating, setCreating] = useState(false)
  const { data, isLoading } = useQuery({
    queryKey: ['skills'],
    queryFn: async () => (await api.get('/api/v1/skills')).data,
  })
  const items: Skill[] = (data as { content?: Skill[] } | undefined)?.content
    || (Array.isArray(data) ? data as Skill[] : [])

  const create = useMutation({
    mutationFn: (v: Record<string, unknown>) => api.post('/api/v1/skills', v).then(r => r.data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['skills'] }),
  })

  return (
    <>
      <DataList<Skill>
        title="Skills Library" description="All skills + categories tracked in the platform"
        action={<Button onClick={() => setCreating(true)}><Plus className="h-4 w-4 mr-1" /> New Skill</Button>}
        data={items} isLoading={isLoading}
        emptyIcon={<Sparkles className="h-10 w-10" />} emptyTitle="No skills"
        columns={[
          { key: 'name', label: 'Skill' },
          { key: 'category', label: 'Category', render: s => s.category ? <Badge>{s.category}</Badge> : '—' },
          { key: 'description', label: 'Description' },
        ]}
      />
      <FormDialog
        open={creating} onOpenChange={setCreating} title="Add skill"
        onSubmit={v => create.mutateAsync(v)}
        fields={[
          { name: 'name', label: 'Skill name', type: 'text', required: true },
          { name: 'category', label: 'Category', type: 'text' },
          { name: 'description', label: 'Description', type: 'textarea', span: 2 },
        ]}
      />
    </>
  )
}
