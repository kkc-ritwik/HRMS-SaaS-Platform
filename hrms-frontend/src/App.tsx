import { Navigate, Route, Routes } from 'react-router-dom'
import { useAuthStore } from '@/store/authStore'
import { AppLayout } from '@/components/layout/AppLayout'
import { AuthLayout } from '@/components/layout/AuthLayout'
import { LoginPage } from '@/pages/auth/LoginPage'
import { DashboardPage } from '@/pages/dashboard/DashboardPage'
import { EmployeeListPage } from '@/pages/employees/EmployeeListPage'
import { EmployeeDetailPage } from '@/pages/employees/EmployeeDetailPage'
import { EmployeeFormPage } from '@/pages/employees/EmployeeFormPage'
import { DepartmentsPage } from '@/pages/organization/DepartmentsPage'
import { DesignationsPage } from '@/pages/organization/DesignationsPage'
import { LocationsPage } from '@/pages/organization/LocationsPage'
import { AttendancePage } from '@/pages/attendance/AttendancePage'
import { LeaveApplicationPage } from '@/pages/leave/LeaveApplicationPage'
import { LeaveApprovalsPage } from '@/pages/leave/LeaveApprovalsPage'
import { LeaveBalancePage } from '@/pages/leave/LeaveBalancePage'
import { LeaveTypesPage } from '@/pages/leave/LeaveTypesPage'
import { PayrollRunPage } from '@/pages/payroll/PayrollRunPage'

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const isAuthenticated = useAuthStore(s => s.isAuthenticated)
  if (!isAuthenticated) return <Navigate to="/login" replace />
  return <>{children}</>
}

function PublicRoute({ children }: { children: React.ReactNode }) {
  const isAuthenticated = useAuthStore(s => s.isAuthenticated)
  if (isAuthenticated) return <Navigate to="/dashboard" replace />
  return <>{children}</>
}

export default function App() {
  return (
    <Routes>
      {/* Public routes */}
      <Route element={<AuthLayout />}>
        <Route path="/login" element={
          <PublicRoute>
            <LoginPage />
          </PublicRoute>
        } />
      </Route>

      {/* Protected routes */}
      <Route element={
        <ProtectedRoute>
          <AppLayout />
        </ProtectedRoute>
      }>
        <Route index element={<Navigate to="/dashboard" replace />} />
        <Route path="/dashboard" element={<DashboardPage />} />

        {/* People */}
        <Route path="/employees" element={<EmployeeListPage />} />
        <Route path="/employees/new" element={<EmployeeFormPage />} />
        <Route path="/employees/:id" element={<EmployeeDetailPage />} />
        <Route path="/employees/:id/edit" element={<EmployeeFormPage />} />

        {/* Organization */}
        <Route path="/departments" element={<DepartmentsPage />} />
        <Route path="/designations" element={<DesignationsPage />} />
        <Route path="/locations" element={<LocationsPage />} />

        {/* Attendance */}
        <Route path="/attendance" element={<AttendancePage />} />

        {/* Leave */}
        <Route path="/leave/apply" element={<LeaveApplicationPage />} />
        <Route path="/leave/approvals" element={<LeaveApprovalsPage />} />
        <Route path="/leave/balances" element={<LeaveBalancePage />} />
        <Route path="/leave/types" element={<LeaveTypesPage />} />

        {/* Payroll */}
        <Route path="/payroll/runs" element={<PayrollRunPage />} />
      </Route>

      {/* Catch-all */}
      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  )
}
