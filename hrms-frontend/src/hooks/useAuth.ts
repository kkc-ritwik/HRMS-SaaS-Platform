import { useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import { useMutation } from '@tanstack/react-query'
import { toast } from 'sonner'
import { useAuthStore } from '@/store/authStore'
import { authService } from '@/services/authService'
import { getErrorMessage } from '@/lib/api'

export function useAuth() {
  const store = useAuthStore()
  const navigate = useNavigate()

  const loginMutation = useMutation({
    mutationFn: authService.login,
    onSuccess: response => {
      // Backend wraps the payload as { success, data: { accessToken, ... } }, but tolerate an
      // already-unwrapped shape too so a stale/alternate response never breaks login.
      const payload = response as unknown as Record<string, any>
      const tokens = payload?.data?.accessToken ? payload.data : payload
      const { accessToken, refreshToken, user } = tokens
      store.login(user, accessToken, refreshToken)
      toast.success(`Welcome back, ${user.fullName}!`)
      navigate('/dashboard')
    },
    onError: (error: unknown) => {
      toast.error(getErrorMessage(error))
    },
  })

  const logout = useCallback(() => {
    store.logout()
    navigate('/login')
    toast.success('Logged out successfully')
  }, [store, navigate])

  return {
    user: store.user,
    isAuthenticated: store.isAuthenticated,
    isLoading: loginMutation.isPending,
    login: loginMutation.mutate,
    logout,
    hasRole: store.hasRole,
    hasPermission: store.hasPermission,
    isAdmin: store.isAdmin,
    isHRManager: store.isHRManager,
  }
}
