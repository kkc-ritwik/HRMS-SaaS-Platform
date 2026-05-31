import { Navigate, Route, Routes } from 'react-router-dom'
import { useAuthStore } from '@/store/authStore'
import { AppLayout } from '@/components/layout/AppLayout'
import { AuthLayout } from '@/components/layout/AuthLayout'

// Existing pages
import { LoginPage } from '@/pages/auth/LoginPage'
import { ForgotPasswordPage } from '@/pages/auth/ForgotPasswordPage'
import { MfaChallengePage } from '@/pages/auth/MfaChallengePage'
import { JobDetailPage } from '@/pages/recruitment/JobDetailPage'
import { CandidateDetailPage } from '@/pages/recruitment/CandidateDetailPage'
import { AssetDetailPage } from '@/pages/assets/AssetDetailPage'
import { TicketDetailPage } from '@/pages/helpdesk/TicketDetailPage'
import { PipelineKanbanPage } from '@/pages/recruitment/PipelineKanbanPage'
import { OnboardingDetailPage } from '@/pages/onboarding/OnboardingDetailPage'
import { LeaveCalendarPage } from '@/pages/leave/LeaveCalendarPage'
import { WorkflowDesignerPage } from '@/pages/workflows/WorkflowDesignerPage'
import { FormBuilderPage } from '@/pages/forms/FormBuilderPage'
import { GoalCascadePage } from '@/pages/performance/GoalCascadePage'
import { NineBoxPage } from '@/pages/performance/NineBoxPage'
import { FloorPlanPage } from '@/pages/workplace/FloorPlanPage'
import { CoursePlayerPage } from '@/pages/lms/CoursePlayerPage'
import { TemplateEditorPage } from '@/pages/settings/TemplateEditorPage'

// Admin (Users, Roles, ApiKeys, LegalEntities, CustomFields, Biometric)
import { UsersPage, RolesPage, ApiKeysPage, LegalEntitiesPage, CustomFieldsPage, BiometricDevicesPage } from '@/pages/admin/AdminPages'

// Lifecycle depth
import {
  ProbationReviewsPage, BuddyAssignmentsPage, PreOnboardingPage, OnboardingTemplatesPage,
  OnboardingTasksAdminPage, ExitInterviewsPage, ExitChecklistsPage, KnowledgeTransferPage,
} from '@/pages/lifecycle/LifecyclePages'

// Bulk depth pages (40+ remaining controllers)
import {
  StatutoryReturnsPage, ComplianceTasksPage, CourseModulesPage, AssessmentsPage,
  ExpensePoliciesPage, ReceiptOcrPage, PerformanceAnalyticsPage, ContinuousFeedbackPage,
  RecruitmentAgenciesPage, RecruitmentAnalyticsPage, BgvPage, GroupsPage, EventsPage,
  DashboardsPage, SavedReportsPage, AnnouncementsAdminPage, EmailTemplatesPage,
  NotificationPreferencesPage, SalaryComponentsPage, EmployeeSalaryPage, TaxConfigPage,
  TicketCategoriesPage, DocumentTemplatesAdminPage, DocumentTypesPage, FileVaultPage,
  WorkflowInstancesPage, DelegationRulesPage, WebhooksPage, JobCostReportsPage,
} from '@/pages/extras/BulkPages'

// Final gap pages (20)
import {
  CompensationPlansPage, EmployeeBenefitsPage, MarketBenchmarkPage, ExpenseCategoriesPage,
  LeavePoliciesPage, GstReturnsPage, DashboardWidgetsPage, WorkflowStepsPage,
  ReferenceChecksPage, PsychometricPage, InternalMobilityPage, OnboardingDocumentsPage,
  DocumentVersionsPage, CsatDashboardPage, EmployeeBulkImportPage, FilesPage,
  NotificationDispatchPage, ScormPlayerPage, CareerSitePage,
} from '@/pages/extras/GapPages'
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

// Self-service
import { ProfilePage } from '@/pages/self/ProfilePage'
import { MyLeavePage } from '@/pages/self/MyLeavePage'
import { MyPayslipsPage } from '@/pages/self/MyPayslipsPage'
import { DocumentsPage } from '@/pages/documents/DocumentsPage'
import { TimesheetPage } from '@/pages/timesheet/TimesheetPage'
import { TaxDeclarationPage } from '@/pages/tax/TaxDeclarationPage'
import { InvestmentProofsPage } from '@/pages/tax/InvestmentProofsPage'

// Recruitment
import { JobsPage } from '@/pages/recruitment/JobsPage'
import { CandidatesPage } from '@/pages/recruitment/CandidatesPage'
import { ApplicationsPage } from '@/pages/recruitment/ApplicationsPage'
import { InterviewsPage } from '@/pages/recruitment/InterviewsPage'
import { OffersPage } from '@/pages/recruitment/OffersPage'
import { HiringLoopsPage } from '@/pages/recruitment/HiringLoopsPage'

// Lifecycle
import { OnboardingPage } from '@/pages/onboarding/OnboardingPage'
import { OffboardingPage } from '@/pages/offboarding/OffboardingPage'

// Performance
import { GoalsPage } from '@/pages/performance/GoalsPage'
import { ReviewsPage } from '@/pages/performance/ReviewsPage'
import { OneOnOnePage } from '@/pages/performance/OneOnOnePage'
import { CompetenciesPage } from '@/pages/performance/CompetenciesPage'
import { ReviewCyclesPage } from '@/pages/performance/ReviewCyclesPage'
import { PipPage } from '@/pages/performance/PipPage'

// LMS
import { CoursesPage } from '@/pages/lms/CoursesPage'
import { EnrollmentsPage } from '@/pages/lms/EnrollmentsPage'
import { CertificationsPage } from '@/pages/lms/CertificationsPage'

// Engagement
import { SocialFeedPage } from '@/pages/engagement/SocialFeedPage'
import { KudosPage } from '@/pages/engagement/KudosPage'
import { SuggestionsPage } from '@/pages/engagement/SuggestionsPage'
import { PulsePage } from '@/pages/engagement/PulsePage'
import { AwardsPage } from '@/pages/engagement/AwardsPage'
import { RewardsCatalogPage } from '@/pages/engagement/RewardsCatalogPage'
import { WellnessPage } from '@/pages/engagement/WellnessPage'
import { StayInterviewsPage } from '@/pages/engagement/StayInterviewsPage'
import { PollsPage } from '@/pages/engagement/PollsPage'
import { HeatmapPage } from '@/pages/engagement/HeatmapPage'

// Operational
import { AssetsPage } from '@/pages/assets/AssetsPage'
import { AssetCategoriesPage } from '@/pages/assets/AssetCategoriesPage'
import { AssetRequestsPage } from '@/pages/assets/AssetRequestsPage'
import { AssetMaintenancePage } from '@/pages/assets/AssetMaintenancePage'
import { AmcContractsPage } from '@/pages/assets/AmcContractsPage'
import { VendorsPage } from '@/pages/vendors/VendorsPage'
import { ExpensesPage } from '@/pages/expenses/ExpensesPage'
import { AdvancesPage } from '@/pages/expenses/AdvancesPage'
import { TicketsPage } from '@/pages/helpdesk/TicketsPage'
import { KbPage } from '@/pages/helpdesk/KbPage'

// Workplace
import { DeskBookingPage } from '@/pages/workplace/DeskBookingPage'
import { VisitorsPage } from '@/pages/workplace/VisitorsPage'

// Workflow
import { ApprovalsPage } from '@/pages/workflows/ApprovalsPage'
import { WorkflowsPage } from '@/pages/workflows/WorkflowsPage'
import { OutOfOfficePage } from '@/pages/workflows/OutOfOfficePage'

// Compliance
import { AuditLogPage } from '@/pages/compliance/AuditLogPage'
import { GdprPage } from '@/pages/compliance/GdprPage'
import { CasesPage } from '@/pages/compliance/CasesPage'
import { ComplianceItemsPage } from '@/pages/compliance/ComplianceItemsPage'
import { LicensesPage } from '@/pages/compliance/LicensesPage'

// Payroll depth
import { SalaryStructuresPage } from '@/pages/payroll/SalaryStructuresPage'
import { PayGradesPage } from '@/pages/payroll/PayGradesPage'
import { BenefitsPage } from '@/pages/payroll/BenefitsPage'
import { LoansPage } from '@/pages/payroll/LoansPage'

// Documents depth
import { LettersPage } from '@/pages/documents/LettersPage'
import { PoliciesPage } from '@/pages/documents/PoliciesPage'

// Org / Skills / Time / Travel / Forms
import { OrgChartPage } from '@/pages/organization/OrgChartPage'
import { CostCentersPage } from '@/pages/organization/CostCentersPage'
import { SkillsPage } from '@/pages/skills/SkillsPage'
import { TravelPage } from '@/pages/travel/TravelPage'
import { FormsPage } from '@/pages/forms/FormsPage'
import { ShiftsPage } from '@/pages/time/ShiftsPage'

// Reports + Settings + Notifications + Holidays
import { ReportsPage } from '@/pages/reports/ReportsPage'
import { DeiAnalyticsPage } from '@/pages/reports/DeiAnalyticsPage'
import { SettingsPage } from '@/pages/settings/SettingsPage'
import { NotificationsPage } from '@/pages/notifications/NotificationsPage'
import { HolidaysPage } from '@/pages/holidays/HolidaysPage'

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
      <Route element={<AuthLayout />}>
        <Route path="/login" element={<PublicRoute><LoginPage /></PublicRoute>} />
        <Route path="/forgot-password" element={<PublicRoute><ForgotPasswordPage /></PublicRoute>} />
        <Route path="/mfa" element={<PublicRoute><MfaChallengePage /></PublicRoute>} />
      </Route>

      <Route element={<ProtectedRoute><AppLayout /></ProtectedRoute>}>
        <Route index element={<Navigate to="/dashboard" replace />} />
        <Route path="/dashboard" element={<DashboardPage />} />

        {/* Self-service */}
        <Route path="/profile" element={<ProfilePage />} />
        <Route path="/my-leave" element={<MyLeavePage />} />
        <Route path="/payslips" element={<MyPayslipsPage />} />
        <Route path="/documents" element={<DocumentsPage />} />
        <Route path="/timesheet" element={<TimesheetPage />} />
        <Route path="/tax/declaration" element={<TaxDeclarationPage />} />
        <Route path="/tax/proofs" element={<InvestmentProofsPage />} />
        <Route path="/policies" element={<PoliciesPage />} />

        {/* People + Org */}
        <Route path="/employees" element={<EmployeeListPage />} />
        <Route path="/employees/new" element={<EmployeeFormPage />} />
        <Route path="/employees/:id" element={<EmployeeDetailPage />} />
        <Route path="/employees/:id/edit" element={<EmployeeFormPage />} />
        <Route path="/departments" element={<DepartmentsPage />} />
        <Route path="/designations" element={<DesignationsPage />} />
        <Route path="/locations" element={<LocationsPage />} />
        <Route path="/org-chart" element={<OrgChartPage />} />
        <Route path="/cost-centers" element={<CostCentersPage />} />
        <Route path="/skills" element={<SkillsPage />} />

        {/* Attendance + Leave + Shifts */}
        <Route path="/attendance" element={<AttendancePage />} />
        <Route path="/leave/apply" element={<LeaveApplicationPage />} />
        <Route path="/leave-approvals" element={<LeaveApprovalsPage />} />
        <Route path="/leave-balances" element={<LeaveBalancePage />} />
        <Route path="/leave-types" element={<LeaveTypesPage />} />
        <Route path="/holidays" element={<HolidaysPage />} />
        <Route path="/calendar" element={<LeaveCalendarPage />} />
        <Route path="/shifts" element={<ShiftsPage />} />

        {/* Payroll */}
        <Route path="/pay-runs" element={<PayrollRunPage />} />
        <Route path="/salary-structures" element={<SalaryStructuresPage />} />
        <Route path="/pay-grades" element={<PayGradesPage />} />
        <Route path="/benefits" element={<BenefitsPage />} />
        <Route path="/loans" element={<LoansPage />} />

        {/* Recruitment */}
        <Route path="/jobs" element={<JobsPage />} />
        <Route path="/jobs/:id" element={<JobDetailPage />} />
        <Route path="/candidates" element={<CandidatesPage />} />
        <Route path="/candidates/:id" element={<CandidateDetailPage />} />
        <Route path="/applications" element={<ApplicationsPage />} />
        <Route path="/pipeline" element={<PipelineKanbanPage />} />
        <Route path="/interviews" element={<InterviewsPage />} />
        <Route path="/offers" element={<OffersPage />} />
        <Route path="/hiring-loops" element={<HiringLoopsPage />} />

        {/* Lifecycle */}
        <Route path="/onboarding" element={<OnboardingPage />} />
        <Route path="/onboarding/:id" element={<OnboardingDetailPage />} />
        <Route path="/offboarding" element={<OffboardingPage />} />

        {/* Performance */}
        <Route path="/goals" element={<GoalsPage />} />
        <Route path="/goal-cascade" element={<GoalCascadePage />} />
        <Route path="/nine-box" element={<NineBoxPage />} />
        <Route path="/reviews" element={<ReviewsPage />} />
        <Route path="/review-cycles" element={<ReviewCyclesPage />} />
        <Route path="/one-on-ones" element={<OneOnOnePage />} />
        <Route path="/competencies" element={<CompetenciesPage />} />
        <Route path="/pip" element={<PipPage />} />

        {/* LMS */}
        <Route path="/courses" element={<CoursesPage />} />
        <Route path="/courses/:id/play" element={<CoursePlayerPage />} />
        <Route path="/enrollments" element={<EnrollmentsPage />} />
        <Route path="/certifications" element={<CertificationsPage />} />

        {/* Engagement */}
        <Route path="/social" element={<SocialFeedPage />} />
        <Route path="/kudos" element={<KudosPage />} />
        <Route path="/suggestions" element={<SuggestionsPage />} />
        <Route path="/pulse" element={<PulsePage />} />
        <Route path="/awards" element={<AwardsPage />} />
        <Route path="/rewards-catalog" element={<RewardsCatalogPage />} />
        <Route path="/wellness" element={<WellnessPage />} />
        <Route path="/stay-interviews" element={<StayInterviewsPage />} />
        <Route path="/polls" element={<PollsPage />} />
        <Route path="/heatmap" element={<HeatmapPage />} />

        {/* Operational */}
        <Route path="/assets" element={<AssetsPage />} />
        <Route path="/assets/:id" element={<AssetDetailPage />} />
        <Route path="/asset-categories" element={<AssetCategoriesPage />} />
        <Route path="/asset-requests" element={<AssetRequestsPage />} />
        <Route path="/asset-maintenance" element={<AssetMaintenancePage />} />
        <Route path="/amc-contracts" element={<AmcContractsPage />} />
        <Route path="/vendors" element={<VendorsPage />} />
        <Route path="/expenses" element={<ExpensesPage />} />
        <Route path="/advances" element={<AdvancesPage />} />
        <Route path="/helpdesk" element={<TicketsPage />} />
        <Route path="/helpdesk/:id" element={<TicketDetailPage />} />
        <Route path="/kb" element={<KbPage />} />

        {/* Workplace + Travel */}
        <Route path="/desk-booking" element={<DeskBookingPage />} />
        <Route path="/floor-plan" element={<FloorPlanPage />} />
        <Route path="/visitors" element={<VisitorsPage />} />
        <Route path="/travel" element={<TravelPage />} />

        {/* Workflow */}
        <Route path="/approvals" element={<ApprovalsPage />} />
        <Route path="/workflows" element={<WorkflowsPage />} />
        <Route path="/workflows/designer" element={<WorkflowDesignerPage />} />
        <Route path="/out-of-office" element={<OutOfOfficePage />} />

        {/* Compliance + Cases */}
        <Route path="/compliance" element={<AuditLogPage />} />
        <Route path="/cases" element={<CasesPage />} />
        <Route path="/gdpr" element={<GdprPage />} />
        <Route path="/compliance-items" element={<ComplianceItemsPage />} />
        <Route path="/licenses" element={<LicensesPage />} />

        {/* Document depth */}
        <Route path="/letters" element={<LettersPage />} />

        {/* Forms */}
        <Route path="/forms" element={<FormsPage />} />
        <Route path="/forms/builder" element={<FormBuilderPage />} />

        {/* Template editor */}
        <Route path="/settings/templates" element={<TemplateEditorPage />} />

        {/* Reports + Notifications + Settings */}
        <Route path="/reports" element={<ReportsPage />} />
        <Route path="/dei" element={<DeiAnalyticsPage />} />
        <Route path="/dashboards" element={<DashboardsPage />} />
        <Route path="/saved-reports" element={<SavedReportsPage />} />
        <Route path="/job-cost-reports" element={<JobCostReportsPage />} />
        <Route path="/recruitment-analytics" element={<RecruitmentAnalyticsPage />} />
        <Route path="/performance-analytics" element={<PerformanceAnalyticsPage />} />
        <Route path="/notifications" element={<NotificationsPage />} />
        <Route path="/notification-preferences" element={<NotificationPreferencesPage />} />
        <Route path="/announcements" element={<AnnouncementsAdminPage />} />
        <Route path="/email-templates" element={<EmailTemplatesPage />} />
        <Route path="/settings" element={<SettingsPage />} />

        {/* Admin */}
        <Route path="/users" element={<UsersPage />} />
        <Route path="/roles" element={<RolesPage />} />
        <Route path="/api-keys" element={<ApiKeysPage />} />
        <Route path="/legal-entities" element={<LegalEntitiesPage />} />
        <Route path="/custom-fields" element={<CustomFieldsPage />} />
        <Route path="/biometric-devices" element={<BiometricDevicesPage />} />
        <Route path="/webhooks" element={<WebhooksPage />} />

        {/* Lifecycle depth */}
        <Route path="/probation-reviews" element={<ProbationReviewsPage />} />
        <Route path="/buddy-assignments" element={<BuddyAssignmentsPage />} />
        <Route path="/pre-onboarding" element={<PreOnboardingPage />} />
        <Route path="/onboarding-templates" element={<OnboardingTemplatesPage />} />
        <Route path="/onboarding-tasks-library" element={<OnboardingTasksAdminPage />} />
        <Route path="/exit-interviews" element={<ExitInterviewsPage />} />
        <Route path="/exit-checklists" element={<ExitChecklistsPage />} />
        <Route path="/knowledge-transfers" element={<KnowledgeTransferPage />} />

        {/* Compliance + Statutory */}
        <Route path="/statutory-returns" element={<StatutoryReturnsPage />} />
        <Route path="/compliance-tasks" element={<ComplianceTasksPage />} />

        {/* LMS depth */}
        <Route path="/course-modules" element={<CourseModulesPage />} />
        <Route path="/assessments" element={<AssessmentsPage />} />

        {/* Expense depth */}
        <Route path="/expense-policies" element={<ExpensePoliciesPage />} />
        <Route path="/receipt-ocr" element={<ReceiptOcrPage />} />

        {/* Performance depth */}
        <Route path="/continuous-feedback" element={<ContinuousFeedbackPage />} />

        {/* Recruitment depth */}
        <Route path="/recruitment-agencies" element={<RecruitmentAgenciesPage />} />
        <Route path="/bgv" element={<BgvPage />} />

        {/* Social depth */}
        <Route path="/groups" element={<GroupsPage />} />
        <Route path="/events" element={<EventsPage />} />

        {/* Payroll depth */}
        <Route path="/salary-components" element={<SalaryComponentsPage />} />
        <Route path="/employee-salaries" element={<EmployeeSalaryPage />} />
        <Route path="/tax-config" element={<TaxConfigPage />} />

        {/* Helpdesk depth */}
        <Route path="/ticket-categories" element={<TicketCategoriesPage />} />

        {/* Documents depth */}
        <Route path="/document-templates" element={<DocumentTemplatesAdminPage />} />
        <Route path="/document-types" element={<DocumentTypesPage />} />
        <Route path="/file-vault" element={<FileVaultPage />} />

        {/* Workflow depth */}
        <Route path="/workflow-instances" element={<WorkflowInstancesPage />} />
        <Route path="/delegation-rules" element={<DelegationRulesPage />} />

        {/* Final gap pages */}
        <Route path="/compensation-plans" element={<CompensationPlansPage />} />
        <Route path="/employee-benefits" element={<EmployeeBenefitsPage />} />
        <Route path="/market-benchmark" element={<MarketBenchmarkPage />} />
        <Route path="/expense-categories" element={<ExpenseCategoriesPage />} />
        <Route path="/leave-policies" element={<LeavePoliciesPage />} />
        <Route path="/gst-returns" element={<GstReturnsPage />} />
        <Route path="/dashboard-widgets" element={<DashboardWidgetsPage />} />
        <Route path="/workflow-steps" element={<WorkflowStepsPage />} />
        <Route path="/reference-checks" element={<ReferenceChecksPage />} />
        <Route path="/psychometric" element={<PsychometricPage />} />
        <Route path="/internal-mobility" element={<InternalMobilityPage />} />
        <Route path="/onboarding-documents" element={<OnboardingDocumentsPage />} />
        <Route path="/document-versions" element={<DocumentVersionsPage />} />
        <Route path="/csat" element={<CsatDashboardPage />} />
        <Route path="/employee-bulk-import" element={<EmployeeBulkImportPage />} />
        <Route path="/files" element={<FilesPage />} />
        <Route path="/notification-dispatch" element={<NotificationDispatchPage />} />
        <Route path="/scorm-player" element={<ScormPlayerPage />} />
        <Route path="/career-site" element={<CareerSitePage />} />
      </Route>

      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  )
}
