import * as React from 'react'
import { cn } from '@/lib/utils'
import { TrendingUp, TrendingDown } from 'lucide-react'

interface StatCardProps {
  title: string
  value: string | number
  subtitle?: string
  icon?: React.ReactNode
  iconBg?: string
  trend?: number
  trendLabel?: string
  className?: string
  loading?: boolean
}

export function StatCard({
  title,
  value,
  subtitle,
  icon,
  iconBg = 'bg-brand-100',
  trend,
  trendLabel,
  className,
  loading,
}: StatCardProps) {
  const isPositive = trend !== undefined && trend >= 0

  if (loading) {
    return (
      <div className={cn('bg-white rounded-xl border border-slate-100 shadow-[0_1px_3px_rgba(0,0,0,0.08)] p-5', className)}>
        <div className="flex items-start justify-between">
          <div className="space-y-2 flex-1">
            <div className="h-3 bg-slate-200 rounded animate-pulse w-24" />
            <div className="h-8 bg-slate-200 rounded animate-pulse w-16" />
          </div>
          <div className="h-11 w-11 rounded-xl bg-slate-200 animate-pulse" />
        </div>
        <div className="mt-3 h-3 bg-slate-200 rounded animate-pulse w-32" />
      </div>
    )
  }

  return (
    <div className={cn('bg-white rounded-xl border border-slate-100 shadow-[0_1px_3px_rgba(0,0,0,0.08)] p-5 hover:shadow-md transition-shadow duration-200', className)}>
      <div className="flex items-start justify-between">
        <div className="flex-1 min-w-0">
          <p className="text-sm font-medium text-slate-500">{title}</p>
          <p className="text-2xl font-bold text-slate-900 mt-1 tabular-nums">{value}</p>
        </div>
        {icon && (
          <div className={cn('flex h-11 w-11 items-center justify-center rounded-xl flex-shrink-0 ml-3', iconBg)}>
            {icon}
          </div>
        )}
      </div>
      {(trend !== undefined || subtitle) && (
        <div className="mt-3 flex items-center gap-2">
          {trend !== undefined && (
            <span className={cn(
              'flex items-center gap-0.5 text-xs font-medium',
              isPositive ? 'text-green-600' : 'text-red-500'
            )}>
              {isPositive ? <TrendingUp className="h-3.5 w-3.5" /> : <TrendingDown className="h-3.5 w-3.5" />}
              {Math.abs(trend)}%
            </span>
          )}
          {trendLabel && (
            <span className="text-xs text-slate-400">{trendLabel}</span>
          )}
          {subtitle && (
            <span className="text-xs text-slate-500">{subtitle}</span>
          )}
        </div>
      )}
    </div>
  )
}
