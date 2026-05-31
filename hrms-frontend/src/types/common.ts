/** Shared types used across services and pages. */

export type UUID = string

export interface AuditedEntity {
  id: UUID
  tenantId?: string
  createdAt: string
  updatedAt?: string
  createdBy?: string
  updatedBy?: string
}

export interface PagedResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
  empty?: boolean
}

export interface PageParams {
  page?: number
  size?: number
  sort?: string
  search?: string
}

export interface MoneyAmount {
  amount: number
  currency: string
}

export type ApprovalStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED' | 'WITHDRAWN'
