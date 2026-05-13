export * from './types'
export { getUpdateStatus } from './getUpdateStatus'
export { startImmediateUpdate } from './startImmediateUpdate'
export { startFlexibleUpdate } from './startFlexibleUpdate'
export { completeFlexibleUpdate } from './completeFlexibleUpdate'
export { openStorePage } from './openStorePage'
export {
  addInstallStateListener,
  type InstallStateListener,
  type InstallStateSubscription,
} from './addInstallStateListener'
export {
  isUpdateAvailable,
  canStartImmediateUpdate,
  canStartFlexibleUpdate,
  canCompleteFlexibleUpdate,
  canOpenStorePage,
  supportsInstallStateListener,
} from './helpers'
