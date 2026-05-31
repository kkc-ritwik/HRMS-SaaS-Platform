import * as React from 'react'
import { cn } from '@/lib/utils'
import { Button } from './button'

interface EmptyStateProps {
  icon?: React.ReactNode
  title: string
  description?: string
  action?: { label: string; onClick: () => void } | React.ReactNode
  className?: string
}

function isLabelledAction(a: unknown): a is { label: string; onClick: () => void } {
  return !!a && typeof a === 'object' && 'label' in (a as Record<string, unknown>) && 'onClick' in (a as Record<string, unknown>)
}

export function EmptyState({ icon, title, description, action, className }: EmptyStateProps) {
  return (
    <div className={cn('flex flex-col items-center justify-center py-16 px-4 text-center', className)}>
      {icon && (
        <div className="mb-4 flex h-16 w-16 items-center justify-center rounded-2xl bg-slate-100 text-slate-400">
          {icon}
        </div>
      )}
      <h3 className="text-base font-semibold text-slate-800 mb-1">{title}</h3>
      {description && (
        <p className="text-sm text-slate-500 max-w-xs mb-6">{description}</p>
      )}
      {action && (
        isLabelledAction(action)
          ? <Button onClick={action.onClick} size="sm">{action.label}</Button>
          : <>{action}</>
      )}
    </div>
  )
}
