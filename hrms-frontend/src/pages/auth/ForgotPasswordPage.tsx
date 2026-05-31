import { useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { Mail, ArrowLeft, CheckCircle } from 'lucide-react'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { authService } from '@/services/authService'
import { toast } from 'sonner'

export function ForgotPasswordPage() {
  const [email, setEmail] = useState('')
  const [done, setDone] = useState(false)

  const send = useMutation({
    mutationFn: () => authService.forgotPassword(email),
    onSuccess: () => { setDone(true); toast.success('Check your inbox') },
    onError: e => toast.error(e instanceof Error ? e.message : 'Send failed'),
  })

  return (
    <Card className="w-full max-w-sm">
      <CardHeader>
        <CardTitle>Reset your password</CardTitle>
        <CardDescription>
          {done
            ? 'If an account exists for this email, you will receive a reset link shortly.'
            : 'Enter your email and we will send you a link to reset your password.'}
        </CardDescription>
      </CardHeader>
      <CardContent>
        {done ? (
          <div className="flex flex-col items-center py-4">
            <CheckCircle className="h-12 w-12 text-green-500" />
            <p className="text-sm text-slate-600 mt-3 text-center">Email sent to <strong>{email}</strong></p>
          </div>
        ) : (
          <form onSubmit={e => { e.preventDefault(); send.mutate() }} className="space-y-4">
            <div>
              <Label>Email</Label>
              <div className="relative">
                <Mail className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400" />
                <Input
                  type="email" required value={email} onChange={e => setEmail(e.target.value)}
                  className="pl-10" placeholder="you@company.com"
                />
              </div>
            </div>
            <Button type="submit" className="w-full" disabled={send.isPending}>
              {send.isPending ? 'Sending...' : 'Send reset link'}
            </Button>
          </form>
        )}
        <Link to="/login" className="mt-4 inline-flex items-center gap-1 text-sm text-violet-600 hover:underline">
          <ArrowLeft className="h-3 w-3" /> Back to sign in
        </Link>
      </CardContent>
    </Card>
  )
}
