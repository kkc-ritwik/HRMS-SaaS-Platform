import React, { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Eye, EyeOff, Sparkles, Lock, Mail } from 'lucide-react'
import { useAuth } from '@/hooks/useAuth'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { FormField } from '@/components/ui/form-field'
import { cn } from '@/lib/utils'

const loginSchema = z.object({
  email: z.string().email('Please enter a valid email'),
  password: z.string().min(1, 'Password is required'),
})

type LoginFormData = z.infer<typeof loginSchema>

export function LoginPage() {
  const { login, isLoading } = useAuth()
  const [showPassword, setShowPassword] = useState(false)

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      email: '',
      password: '',
    },
  })

  const onSubmit = (data: LoginFormData) => {
    login({ ...data, tenantId: 'demo' })
  }

  return (
    <div className="w-full">
      {/* Card */}
      <div className="bg-white/95 backdrop-blur-sm rounded-2xl shadow-2xl border border-white/20 overflow-hidden">
        {/* Header */}
        <div className="px-8 pt-8 pb-6 text-center">
          {/* Logo */}
          <div className="flex justify-center mb-5">
            <div className="relative">
              <div className="flex h-14 w-14 items-center justify-center rounded-2xl bg-gradient-to-br from-brand-600 to-violet-600 shadow-lg shadow-brand-600/30">
                <span className="text-white font-bold text-2xl">H</span>
              </div>
              <div className="absolute -top-1 -right-1 flex h-5 w-5 items-center justify-center rounded-full bg-amber-400">
                <Sparkles className="h-3 w-3 text-white" />
              </div>
            </div>
          </div>
          <h1 className="text-2xl font-bold text-slate-900">Welcome back</h1>
          <p className="text-sm text-slate-500 mt-1.5">Sign in to your HRMS workspace</p>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit(onSubmit)} className="px-8 pb-8 space-y-4">
          <FormField label="Email address" error={errors.email?.message} required>
            <div className="relative">
              <Mail className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400 pointer-events-none" />
              <Input
                {...register('email')}
                type="email"
                placeholder="you@company.com"
                className="pl-9"
                error={!!errors.email}
                autoComplete="email"
              />
            </div>
          </FormField>

          <FormField label="Password" error={errors.password?.message} required>
            <div className="relative">
              <Lock className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400 pointer-events-none" />
              <Input
                {...register('password')}
                type={showPassword ? 'text' : 'password'}
                placeholder="••••••••"
                className="pl-9 pr-10"
                error={!!errors.password}
                autoComplete="current-password"
              />
              <button
                type="button"
                onClick={() => setShowPassword(prev => !prev)}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 transition-colors"
              >
                {showPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
              </button>
            </div>
          </FormField>

          <div className="flex items-center justify-between pt-1">
            <label className="flex items-center gap-2 cursor-pointer">
              <input
                type="checkbox"
                className="h-4 w-4 rounded border-slate-300 text-brand-600 focus:ring-brand-500 cursor-pointer"
              />
              <span className="text-sm text-slate-600">Remember me</span>
            </label>
            <button type="button" className="text-sm text-brand-600 hover:text-brand-700 font-medium transition-colors">
              Forgot password?
            </button>
          </div>

          <Button
            type="submit"
            className="w-full h-10 bg-gradient-to-r from-brand-600 to-violet-600 hover:from-brand-700 hover:to-violet-700 shadow-md shadow-brand-500/25"
            loading={isLoading}
          >
            {isLoading ? 'Signing in...' : 'Sign in'}
          </Button>

          {/* Demo credentials hint */}
          <div className="mt-4 rounded-xl bg-slate-50 border border-slate-200 p-4">
            <p className="text-xs font-semibold text-slate-600 mb-2 flex items-center gap-1">
              <span className="h-1.5 w-1.5 rounded-full bg-green-400 inline-block" />
              Demo credentials
            </p>
            <div className="space-y-1 text-xs text-slate-500">
              <div className="flex justify-between">
                <span>Email:</span>
                <code className="bg-white border border-slate-200 px-1.5 py-0.5 rounded text-slate-700 font-mono">
                  admin@demo.com
                </code>
              </div>
              <div className="flex justify-between">
                <span>Password:</span>
                <code className="bg-white border border-slate-200 px-1.5 py-0.5 rounded text-slate-700 font-mono">
                  Admin@123
                </code>
              </div>
            </div>
          </div>
        </form>
      </div>

      {/* Features row */}
      <div className="mt-6 flex items-center justify-center gap-6">
        {['SSO Ready', 'SOC 2 Certified', '99.9% Uptime'].map(feat => (
          <div key={feat} className="flex items-center gap-1.5 text-xs text-white/60">
            <svg className="h-3 w-3 text-green-400" fill="currentColor" viewBox="0 0 20 20">
              <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
            </svg>
            {feat}
          </div>
        ))}
      </div>
    </div>
  )
}
