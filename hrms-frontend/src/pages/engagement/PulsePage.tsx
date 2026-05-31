import { useQuery, useMutation } from '@tanstack/react-query'
import { useState } from 'react'
import { Smile } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Textarea } from '@/components/ui/textarea'
import { PageHeader } from '@/components/ui/page-header'
import { ResponsiveContainer, LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip } from 'recharts'
import { engagementService } from '@/services/engagementService'
import { useAuthStore } from '@/store/authStore'
import { toast } from 'sonner'

export function PulsePage() {
  const user = useAuthStore(s => s.user)
  const [score, setScore] = useState(3)
  const [comment, setComment] = useState('')
  const trend = useQuery({ queryKey: ['pulse', 'trend'], queryFn: () => engagementService.pulseTrend(30) })
  const submit = useMutation({
    mutationFn: () => engagementService.submitPulse({
      employeeId: user?.employeeId || user?.id || '',
      score, comment, anonymous: false, category: 'DAILY',
    }),
    onSuccess: () => { toast.success('Thanks for sharing!'); setComment(''); setScore(3) },
  })

  return (
    <div className="space-y-6 max-w-2xl mx-auto">
      <PageHeader title="Pulse Check-in" description="Quick daily eNPS — under 10 seconds" />
      <Card>
        <CardHeader><CardTitle>How are you feeling today?</CardTitle></CardHeader>
        <CardContent className="space-y-4">
          <div className="flex justify-around text-3xl">
            {[1, 2, 3, 4, 5].map(s => (
              <button key={s} onClick={() => setScore(s)} className={`p-3 rounded-full transition ${score === s ? 'bg-violet-100 ring-2 ring-violet-500' : 'hover:bg-slate-50'}`}>
                {['😟','😕','😐','🙂','😄'][s - 1]}
              </button>
            ))}
          </div>
          <Textarea placeholder="Anything you'd like to share? (optional)" value={comment} onChange={e => setComment(e.target.value)} rows={3} />
          <Button onClick={() => submit.mutate()} disabled={submit.isPending}>Submit</Button>
        </CardContent>
      </Card>

      <Card>
        <CardHeader><CardTitle className="flex items-center gap-2"><Smile className="h-5 w-5" /> Team trend (30 days)</CardTitle></CardHeader>
        <CardContent>
          <ResponsiveContainer width="100%" height={200}>
            <LineChart data={(trend.data as { date: string; averageScore: number; count: number }[]) || []}>
              <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" />
              <XAxis dataKey="date" tick={{ fontSize: 11 }} />
              <YAxis domain={[1, 5]} tick={{ fontSize: 11 }} />
              <Tooltip />
              <Line type="monotone" dataKey="averageScore" stroke="#7c3aed" strokeWidth={2.5} />
            </LineChart>
          </ResponsiveContainer>
        </CardContent>
      </Card>
    </div>
  )
}
