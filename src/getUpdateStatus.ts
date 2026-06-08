import { InAppUpdates } from './native'
import { mapNativeStatus } from './internal/mapNativeStatus'
import { normalizeNativeError } from './internal/normalizeNativeError'
import { buildGetUpdateStatusNativeOptions } from './internal/buildNativeUpdateOptions'
import type { GetUpdateStatusOptions, UpdateStatus } from './types'

/**
 * Check the current in-app update status.
 *
 * Returns the platform capabilities, update availability, install status,
 * and platform-specific details.
 *
 * @param options - Platform-specific options (e.g. iOS App Store ID).
 * @returns The current update status.
 * @throws InAppUpdatesError on native bridge or unexpected errors.
 */
export async function getUpdateStatus(
  options?: GetUpdateStatusOptions
): Promise<UpdateStatus> {
  try {
    const nativeOptions = buildGetUpdateStatusNativeOptions(options)
    const result = await InAppUpdates.getUpdateStatus(nativeOptions)
    return mapNativeStatus(result)
  } catch (error) {
    throw normalizeNativeError(error)
  }
}
