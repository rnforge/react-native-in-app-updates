import type { UpdateStatus } from './types'

/**
 * Returns true if a newer version is available in the store.
 * @public
 */
export function isUpdateAvailable(status: UpdateStatus): boolean {
  return status.updateAvailable === true
}

/**
 * Returns true if an immediate update flow can be started right now.
 * @public
 */
export function canStartImmediateUpdate(status: UpdateStatus): boolean {
  return (
    status.supported === true &&
    status.capabilities.immediate === true &&
    status.updateAvailable === true &&
    status.allowed.immediate === true
  )
}

/**
 * Returns true if a flexible update flow can be started right now.
 * @public
 */
export function canStartFlexibleUpdate(status: UpdateStatus): boolean {
  return (
    status.supported === true &&
    status.capabilities.flexible === true &&
    status.updateAvailable === true &&
    status.allowed.flexible === true
  )
}

/**
 * Returns true if a downloaded flexible update can be completed.
 *
 * This checks the downloaded install-state, not update availability.
 * @public
 */
export function canCompleteFlexibleUpdate(status: UpdateStatus): boolean {
  return (
    status.supported === true &&
    status.capabilities.flexible === true &&
    status.installStatus === 'downloaded'
  )
}

/**
 * Returns true if the store page can be opened for this app.
 *
 * This may be true even when in-app updates are unsupported (e.g. iOS).
 * @public
 */
export function canOpenStorePage(status: UpdateStatus): boolean {
  return status.capabilities.storePage === true
}

/**
 * Returns true if the platform supports install-state listeners.
 * @public
 */
export function supportsInstallStateListener(status: UpdateStatus): boolean {
  return status.capabilities.installStateListener === true
}
