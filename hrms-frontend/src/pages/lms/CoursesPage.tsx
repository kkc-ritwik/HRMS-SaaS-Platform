import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { BookOpen, Plus } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { Badge } from '@/components/ui/badge'
import { EmptyState } from '@/components/ui/empty-state'
import { PageHeader } from '@/components/ui/page-header'
import { FormDialog } from '@/components/ui/form-dialog'
import { lmsService, type Course } from '@/services/lmsService'

export function CoursesPage() {
  const qc = useQueryClient()
  const navigate = useNavigate()
  const [creating, setCreating] = useState(false)
  const { data, isLoading } = useQuery({ queryKey: ['courses'], queryFn: () => lmsService.listCourses() })
  const courses: Course[] = (data as { data?: Course[]; content?: Course[] } | undefined)?.data
    || (data as { content?: Course[] } | undefined)?.content
    || (Array.isArray(data) ? data : [])

  const create = useMutation({
    mutationFn: (v: Record<string, unknown>) => lmsService.createCourse(v as Partial<Course>),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['courses'] }),
  })

  return (
    <div className="space-y-6">
      <PageHeader title="Courses" description="Catalogue of learning content" action={<Button onClick={() => setCreating(true)}><Plus className="h-4 w-4 mr-1" /> New Course</Button>} />
      {isLoading ? (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">{Array.from({ length: 6 }).map((_, i) => <Skeleton key={i} className="h-44" />)}</div>
      ) : courses.length === 0 ? (
        <EmptyState icon={<BookOpen className="h-10 w-10" />} title="No courses yet" />
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {courses.map(c => (
            <Card key={c.id} className="hover:shadow-md transition cursor-pointer" onClick={() => navigate(`/courses/${c.id}`)}>
              <CardContent className="p-4 space-y-2">
                <div className="flex items-start justify-between">
                  <h3 className="font-semibold text-slate-800">{c.title}</h3>
                  {c.level && <Badge>{c.level}</Badge>}
                </div>
                {c.description && <p className="text-xs text-slate-500 line-clamp-2">{c.description}</p>}
                <div className="flex items-center justify-between text-xs text-slate-600 pt-2 border-t">
                  <span>{c.durationHours || 0}h</span>
                  <span>{c.mode}</span>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
      <FormDialog
        open={creating} onOpenChange={setCreating} title="Add course"
        onSubmit={v => create.mutateAsync(v)}
        fields={[
          { name: 'title', label: 'Title', type: 'text', required: true, span: 2 },
          { name: 'category', label: 'Category', type: 'text' },
          { name: 'level', label: 'Level', type: 'select', options: [
            { value: 'BEGINNER', label: 'Beginner' }, { value: 'INTERMEDIATE', label: 'Intermediate' }, { value: 'ADVANCED', label: 'Advanced' },
          ] },
          { name: 'durationHours', label: 'Duration (hours)', type: 'number' },
          { name: 'mode', label: 'Mode', type: 'select', options: [
            { value: 'SELF_PACED', label: 'Self-paced' }, { value: 'INSTRUCTOR_LED', label: 'Instructor-led' }, { value: 'BLENDED', label: 'Blended' },
          ] },
          { name: 'contentUri', label: 'Content URL', type: 'url' },
          { name: 'description', label: 'Description', type: 'textarea', span: 2 },
        ]}
      />
    </div>
  )
}
