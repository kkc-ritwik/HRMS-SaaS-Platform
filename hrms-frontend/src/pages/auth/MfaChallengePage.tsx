import { useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { ShieldCheck } from 'lucide-react'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { authService } from '@/services/authService'
import { useAuthStore } from '@/store/authStore'
import { toast } from 'sonner'

export function MfaChallengePage() {
  const navigate = useNavigate()
  const [params] = useSearchParams()
  const challenge = params.get('challenge') || ''
  const login = useAuthStore(s => s.login)
  const [code, setCode] = useState('')

  const verify = useMutation({
    mutationFn: async () => {
      const r = await (authService as unknown as { verifyMfa: (c: string, token: string) => Promise<{ data: { accessToken: string; refreshToken: string; user: { id: string; email: string; fullName: string; roles: ['EMPLOYEE'] } } }> })
        .verifyMfa?.(challenge, code) ?? Promise.reject(new Error('MFA service unavailable'))
      return r
    },
    onSuccess: r => {
      const u = r.data.user
      login({ ...u, permissions: [] }, r.data.accessToken, r.data.refreshToken)
      navigate('/dashboard')
    },
    onError: e => toast.error(e instanceof Error ? e.message : 'Invalid code'),
  })

  return (
    <Card className="w-full max-w-sm">
      <CardHeader>
        <CardTitle className="flex items-center gap-2"><ShieldCheck className="h-5 w-5 text-violet-600" /> Two-factor verification</CardTitle>
        <CardDescription>Enter the 6-digit code from your authenticator app.</CardDescription>
      </CardHeader>
      <CardContent>
        <form onSubmit={e => { e.preventDefault(); verify.mutate() }} className="space-y-4">
          <div>
            <Label>Verification code</Label>
            <Input
              inputMode="numeric" pattern="[0-9]*" maxLength={6} autoFocus required
              value={code} onChange={e => setCode(e.target.value.replace(/\D/g, ''))}
              className="text-center text-2xl tracking-widest font-mono"
              placeholder="000000"
            />
          </div>
          <Button type="submit" className="w-full" disabled={verify.isPending || code.length !== 6}>
            {verify.isPending ? 'Verifying...' : 'Verify'}
          </Button>
        </form>
        <p className="text-xs text-slate-400 mt-4 text-center">
          Lost access? <a href="#" className="text-violet-600 hover:underline">Use a backup code</a>
        </p>
      </CardContent>
    </Card>
  )
}
