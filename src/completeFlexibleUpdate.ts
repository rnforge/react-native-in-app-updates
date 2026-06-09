import { InAppUpdates } from './native'
import { mapNativeStatus } from './internal/mapNativeStatus'
import { normalizeNativeError } from './internal/normalizeNativeError'
import type { UpdateStatus } from './types'

/**
 * Complete a flexible update after the download has finished.
 *
 * Call this when `UpdateStatus.installStatus` is `'downloaded'`, typically
 * after receiving a `'flexible-update-downloaded'` event from
 * {@link addInstallStateListener}. On Android, this triggers the Play Core
 * install of the downloaded update.
 *
 * @returns The update status after the install is triggered.
 * @throws InAppUpdatesError on invalid input, native failures, bridge failures, or unexpected errors.
 * @public
 */
export async function completeFlexibleUpdate(): Promise<UpdateStatus> {
  try {
    const result = await InAppUpdates.completeFlexibleUpdate()
    return mapNativeStatus(result)
  } catch (error) {
    throw normalizeNativeError(error)
  }
}
