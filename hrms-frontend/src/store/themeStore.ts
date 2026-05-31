import { create } from 'zustand'
import { persist } from 'zustand/middleware'

export type Theme = 'light' | 'dark' | 'system'

interface ThemeState {
  theme: Theme
  setTheme: (t: Theme) => void
  resolved: 'light' | 'dark'
  applyResolved: () => void
}

function resolve(t: Theme): 'light' | 'dark' {
  if (t === 'system') {
    return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
  }
  return t
}

function apply(t: 'light' | 'dark') {
  const root = document.documentElement
  root.classList.toggle('dark', t === 'dark')
  root.style.colorScheme = t
}

export const useThemeStore = create<ThemeState>()(
  persist(
    (set, get) => ({
      theme: 'light',
      resolved: 'light',
      setTheme: (t: Theme) => {
        const r = resolve(t)
        apply(r)
        set({ theme: t, resolved: r })
      },
      applyResolved: () => {
        const r = resolve(get().theme)
        apply(r)
        set({ resolved: r })
      },
    }),
    {
      name: 'theme-storage',
      onRehydrateStorage: () => state => {
        if (state) {
          const r = resolve(state.theme)
          apply(r)
          state.resolved = r
        }
      },
    }
  )
)

// Listen for system theme change
if (typeof window !== 'undefined') {
  window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', () => {
    const s = useThemeStore.getState()
    if (s.theme === 'system') s.applyResolved()
  })
}
