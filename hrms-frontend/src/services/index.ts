/**
 * Barrel re-export — the master endpoint Catalog covering every backend REST
 * endpoint across all 28 microservices.
 *
 *   import { Catalog } from '@/services'
 *   await Catalog.assets.assignments.return(id)
 *   await Catalog.payrollRuns.process(runId)
 *
 * Legacy `*Service` exports continue to live in their dedicated files
 * (`@/services/employeeService`, `@/services/leaveService`, …). They are not
 * star-re-exported here because several legacy files have name collisions on
 * shared sub-services (e.g. customFieldService is defined in both adminServices
 * and gapServices). Import those directly from their source.
 */
export { Catalog as default, Catalog } from './catalog'
export * from './catalog'
