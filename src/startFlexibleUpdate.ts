import { InAppUpdates } from './native'
import { mapNativeStatus } from './internal/mapNativeStatus'
import { normalizeNativeError } from './internal/normalizeNativeError'
import { buildStartFlexibleUpdateNativeOptions } from './internal/buildNativeUpdateOptions'
import type { StartFlexibleUpdateOptions, UpdateStatus } from './types'

/**
 * Start a flexible update flow.
 *
 * On Android, this triggers the Play Core flexible update download.
 * Use {@link addInstallStateListener} to track download progress.
 * After download completes, call {@link completeFlexibleUpdate} to install.
 *
 * On iOS, returns an unsupported status (flexible updates are not available).
 *
 * Precondition failures and other expected outcomes are returned as typed
 * results, not thrown errors. Only invalid input, bridge failures, and
 * unexpected failures throw {@link InAppUpdatesError}.
 *
 * @param options - Platform-specific options.
 * @returns The update status after the flow starts or is rejected.
 * @throws InAppUpdatesError on invalid input, bridge failures, or unexpected errors.
 * @public
 */
export async function startFlexibleUpdate(
  options?: StartFlexibleUpdateOptions
): Promise<UpdateStatus> {
  try {
    const nativeOptions = buildStartFlexibleUpdateNativeOptions(options)
    const result = await InAppUpdates.startFlexibleUpdate(nativeOptions)
    return mapNativeStatus(result)
  } catch (error) {
    throw normalizeNativeError(error)
  }
}
