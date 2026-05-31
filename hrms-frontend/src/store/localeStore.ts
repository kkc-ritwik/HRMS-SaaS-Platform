import { create } from 'zustand'
import { persist } from 'zustand/middleware'

export type Locale = 'en' | 'hi' | 'es' | 'fr' | 'de' | 'pt' | 'ja' | 'zh' | 'ar'

export const LOCALES: Array<{ code: Locale; label: string; dir: 'ltr' | 'rtl' }> = [
  { code: 'en', label: 'English', dir: 'ltr' },
  { code: 'hi', label: 'हिन्दी', dir: 'ltr' },
  { code: 'es', label: 'Español', dir: 'ltr' },
  { code: 'fr', label: 'Français', dir: 'ltr' },
  { code: 'de', label: 'Deutsch', dir: 'ltr' },
  { code: 'pt', label: 'Português', dir: 'ltr' },
  { code: 'ja', label: '日本語', dir: 'ltr' },
  { code: 'zh', label: '中文', dir: 'ltr' },
  { code: 'ar', label: 'العربية', dir: 'rtl' },
]

interface LocaleState {
  locale: Locale
  setLocale: (l: Locale) => void
}

function apply(l: Locale) {
  const def = LOCALES.find(x => x.code === l)
  document.documentElement.lang = l
  document.documentElement.dir = def?.dir || 'ltr'
}

export const useLocaleStore = create<LocaleState>()(
  persist(
    (set) => ({
      locale: 'en',
      setLocale: (locale: Locale) => { apply(locale); set({ locale }) },
    }),
    {
      name: 'locale-storage',
      onRehydrateStorage: () => state => { if (state) apply(state.locale) },
    }
  )
)
