import { api, unwrap } from '@/lib/api'

export interface OnboardingWorkflow {
  id: string
  employeeId: string
  templateId?: string
  status: 'NOT_STARTED' | 'IN_PROGRESS' | 'COMPLETED' | 'ON_HOLD'
  startDate: string
  expectedCompletion?: string
  completedAt?: string
  progressPercent: number
}

export interface OnboardingTask {
  id: string
  workflowId: string
  title: string
  description?: string
  category: string
  assigneeRole?: string
  dueDate?: string
  status: 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'SKIPPED'
  completedAt?: string
  documentUri?: string
}

export const onboardingService = {
  listWorkflows: async () => unwrap(await api.get<OnboardingWorkflow[]>('/api/v1/onboarding/workflows')),
  getWorkflow: async (id: string) => unwrap(await api.get<OnboardingWorkflow>(`/api/v1/onboarding/workflows/${id}`)),
  startOnboarding: async (employeeId: string, templateId?: string) =>
    unwrap(await api.post<OnboardingWorkflow>('/api/v1/onboarding/workflows', { employeeId, templateId })),
  listTasks: async (workflowId: string) =>
    unwrap(await api.get<OnboardingTask[]>(`/api/v1/onboarding/workflows/${workflowId}/tasks`)),
  completeTask: async (taskId: string, payload?: { notes?: string; documentUri?: string }) =>
    unwrap(await api.post(`/api/v1/onboarding/tasks/${taskId}/complete`, payload)),
}
