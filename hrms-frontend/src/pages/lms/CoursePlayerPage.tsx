import { useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation } from '@tanstack/react-query'
import { ArrowLeft, Play, CheckCircle2, BookOpen, FileQuestion, Clock } from 'lucide-react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import { Progress } from '@/components/ui/progress'
import { Badge } from '@/components/ui/badge'
import { lmsService } from '@/services/lmsService'
import { toast } from 'sonner'
import { cn } from '@/lib/utils'

interface Module { id: string; title: string; durationMinutes?: number; type: 'VIDEO' | 'READING' | 'QUIZ'; completed?: boolean; contentUrl?: string }

export function CoursePlayerPage() {
  const { id = '' } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const [activeModuleId, setActiveModuleId] = useState<string | null>(null)

  const course = useQuery({ queryKey: ['course', id], queryFn: () => lmsService.getCourse(id), enabled: !!id })

  // Mock modules — in production fetch from /api/v1/courses/{id}/modules
  const modules: Module[] = [
    { id: '1', title: 'Introduction', type: 'VIDEO', durationMinutes: 10, completed: true },
    { id: '2', title: 'Core concepts', type: 'READING', durationMinutes: 20, completed: true },
    { id: '3', title: 'Practice exercises', type: 'READING', durationMinutes: 30, completed: false },
    { id: '4', title: 'Final quiz', type: 'QUIZ', durationMinutes: 15, completed: false },
  ]

  const complete = useMutation({
    mutationFn: () => lmsService.complete(id, 100),
    onSuccess: () => { toast.success('Course completed! Certificate generated'); navigate('/enrollments') },
  })

  if (course.isLoading) return <Skeleton className="h-96" />
  const c = course.data
  if (!c) return <p>Course not found</p>

  const completed = modules.filter(m => m.completed).length
  const progress = Math.round((completed / modules.length) * 100)
  const active = modules.find(m => m.id === activeModuleId) || modules.find(m => !m.completed) || modules[0]

  return (
    <div className="space-y-4">
      <Button variant="ghost" size="sm" onClick={() => navigate('/courses')}>
        <ArrowLeft className="h-4 w-4 mr-1" /> Back to courses
      </Button>

      <div className="grid grid-cols-1 lg:grid-cols-4 gap-4">
        {/* Sidebar: module list */}
        <Card className="lg:col-span-1">
          <CardHeader>
            <CardTitle className="text-base">{c.title}</CardTitle>
            <Progress value={progress} />
            <p className="text-xs text-slate-500">{completed}/{modules.length} modules · {progress}%</p>
          </CardHeader>
          <CardContent className="p-2">
            {modules.map(m => {
              const Icon = m.type === 'VIDEO' ? Play : m.type === 'QUIZ' ? FileQuestion : BookOpen
              return (
                <button
                  key={m.id}
                  onClick={() => setActiveModuleId(m.id)}
                  className={cn(
                    'w-full flex items-center gap-2 p-2 rounded-lg text-left text-sm transition',
                    active.id === m.id ? 'bg-violet-100 text-violet-800' : 'hover:bg-slate-50',
                  )}
                >
                  {m.completed
                    ? <CheckCircle2 className="h-4 w-4 text-green-500" />
                    : <Icon className="h-4 w-4 text-slate-400" />}
                  <span className="flex-1 truncate">{m.title}</span>
                  <span className="text-xs text-slate-400 flex items-center gap-0.5">
                    <Clock className="h-3 w-3" />{m.durationMinutes}m
                  </span>
                </button>
              )
            })}
          </CardContent>
        </Card>

        {/* Main viewer */}
        <Card className="lg:col-span-3">
          <CardContent className="p-6">
            <div className="flex items-center gap-2 mb-3">
              <Badge>{active.type}</Badge>
              <h2 className="text-xl font-bold">{active.title}</h2>
            </div>

            {active.type === 'VIDEO' && (
              <div className="aspect-video bg-slate-900 rounded-lg flex items-center justify-center text-white">
                <div className="text-center">
                  <Play className="h-16 w-16 mx-auto opacity-50" />
                  <p className="text-sm opacity-70 mt-2">Video player would mount here</p>
                </div>
              </div>
            )}

            {active.type === 'READING' && (
              <div className="prose prose-sm max-w-none p-4 bg-slate-50 rounded-lg">
                <p>Lesson content goes here. In production this renders SCORM HTML, Markdown, or PDF.</p>
                <p>The backend's <code>CourseModuleController</code> returns the content URL.</p>
              </div>
            )}

            {active.type === 'QUIZ' && (
              <div className="space-y-3">
                <p className="text-sm text-slate-700">Quiz items load from <code>AssessmentController</code>. Sample:</p>
                <div className="space-y-2">
                  <div className="p-3 border rounded-lg">
                    <p className="font-medium mb-2">Q1: Which of these is a tax regime?</p>
                    {['Old', 'New', 'Both', 'Neither'].map(o => (
                      <label key={o} className="flex items-center gap-2 text-sm py-1"><input type="radio" name="q1" /> {o}</label>
                    ))}
                  </div>
                </div>
                <Button>Submit quiz</Button>
              </div>
            )}

            <div className="flex justify-between mt-6 pt-4 border-t">
              <Button variant="outline">Mark as complete</Button>
              {completed === modules.length - 1 && (
                <Button onClick={() => complete.mutate()}>Finish course →</Button>
              )}
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
