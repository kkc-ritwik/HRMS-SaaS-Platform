import { create } from 'zustand'
import { persist } from 'zustand/middleware'

export type UserRole = 'ADMIN' | 'HR_MANAGER' | 'MANAGER' | 'EMPLOYEE'

export interface User {
  id: string
  email: string
  fullName: string
  roles: UserRole[]
  permissions: string[]
  avatar?: string
  employeeId?: string
  departmentId?: string
  tenantId?: string
}

interface AuthState {
  user: User | null
  accessToken: string | null
  refreshToken: string | null
  isAuthenticated: boolean
  isLoading: boolean

  login: (user: User, accessToken: string, refreshToken: string) => void
  logout: () => void
  setUser: (user: User) => void
  setTokens: (accessToken: string, refreshToken: string) => void
  setLoading: (loading: boolean) => void
  hasRole: (role: UserRole) => boolean
  hasPermission: (permission: string) => boolean
  isAdmin: () => boolean
  isHRManager: () => boolean
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      user: null,
      accessToken: null,
      refreshToken: null,
      isAuthenticated: false,
      isLoading: false,

      login: (user, accessToken, refreshToken) => {
        localStorage.setItem('accessToken', accessToken)
        localStorage.setItem('refreshToken', refreshToken)
        set({
          user,
          accessToken,
          refreshToken,
          isAuthenticated: true,
          isLoading: false,
        })
      },

      logout: () => {
        localStorage.removeItem('accessToken')
        localStorage.removeItem('refreshToken')
        set({
          user: null,
          accessToken: null,
          refreshToken: null,
          isAuthenticated: false,
        })
      },

      setUser: user => set({ user }),

      setTokens: (accessToken, refreshToken) => {
        localStorage.setItem('accessToken', accessToken)
        localStorage.setItem('refreshToken', refreshToken)
        set({ accessToken, refreshToken })
      },

      setLoading: isLoading => set({ isLoading }),

      hasRole: role => {
        const { user } = get()
        return user?.roles?.includes(role) ?? false
      },

      hasPermission: permission => {
        const { user } = get()
        return user?.permissions?.includes(permission) ?? false
      },

      isAdmin: () => {
        const { user } = get()
        return user?.roles?.includes('ADMIN') ?? false
      },

      isHRManager: () => {
        const { user } = get()
        return (
          (user?.roles?.includes('ADMIN') ||
            user?.roles?.includes('HR_MANAGER')) ??
          false
        )
      },
    }),
    {
      name: 'auth-storage',
      partialize: state => ({
        user: state.user,
        accessToken: state.accessToken,
        refreshToken: state.refreshToken,
        isAuthenticated: state.isAuthenticated,
      }),
    }
  )
)
