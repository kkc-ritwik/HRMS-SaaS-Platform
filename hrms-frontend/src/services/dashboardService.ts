import { api, unwrap } from '@/lib/api'

export interface DashboardStats {
  totalEmployees: number
  presentToday: number
  onLeave: number
  absentToday: number
  lateToday: number
  openRequisitions: number
}

export interface HeadcountByDept { department: string; count: number }
export interface AttendanceBreakdownItem { name: string; value: number }

interface DirectoryItem { id: string; department?: string }
interface AttendanceDashboard {
  totalExpected?: number; presentCount?: number; absentCount?: number
  lateCount?: number; onLeaveCount?: number; halfDayCount?: number
}

async function safe<T>(p: Promise<T>, fallback: T): Promise<T> {
  try { return await p } catch { return fallback }
}

async function directory(): Promise<DirectoryItem[]> {
  const res = unwrap<{ content?: DirectoryItem[] } | DirectoryItem[]>(
    await api.get('/api/v1/employees/directory', { params: { size: 1000 } }))
  return Array.isArray(res) ? res : (res?.content ?? [])
}

async function attendanceDashboard(): Promise<AttendanceDashboard> {
  return unwrap<AttendanceDashboard>(await api.get('/api/v1/attendance/dashboard'))
}

/** Dashboard data is composed from real endpoints — there is no single backend
 *  aggregate. Stats blend employee directory + attendance dashboard + open jobs. */
export const dashboardService = {
  stats: async (): Promise<DashboardStats> => {
    const [dir, att, jobs] = await Promise.all([
      safe(directory(), []),
      safe(attendanceDashboard(), {} as AttendanceDashboard),
      safe(
        (async () => {
          const r = unwrap<{ content?: unknown[] } | unknown[]>(await api.get('/api/v1/jobs', { params: { status: 'OPEN', size: 200 } }))
          return Array.isArray(r) ? r : (r?.content ?? [])
        })(),
        [] as unknown[],
      ),
    ])
    return {
      totalEmployees: dir.length,
      presentToday: att.presentCount ?? 0,
      onLeave: att.onLeaveCount ?? 0,
      absentToday: att.absentCount ?? 0,
      lateToday: att.lateCount ?? 0,
      openRequisitions: jobs.length,
    }
  },

  headcountByDept: async (): Promise<HeadcountByDept[]> => {
    const dir = await safe(directory(), [])
    const counts = new Map<string, number>()
    for (const e of dir) {
      const dept = e.department || 'Unassigned'
      counts.set(dept, (counts.get(dept) ?? 0) + 1)
    }
    return [...counts.entries()]
      .map(([department, count]) => ({ department, count }))
      .sort((a, b) => b.count - a.count)
  },

  todayAttendanceBreakdown: async (): Promise<AttendanceBreakdownItem[]> => {
    const att = await safe(attendanceDashboard(), {} as AttendanceDashboard)
    return [
      { name: 'Present', value: att.presentCount ?? 0 },
      { name: 'Late', value: att.lateCount ?? 0 },
      { name: 'Half-day', value: att.halfDayCount ?? 0 },
      { name: 'On Leave', value: att.onLeaveCount ?? 0 },
      { name: 'Absent', value: att.absentCount ?? 0 },
    ]
  },
}
