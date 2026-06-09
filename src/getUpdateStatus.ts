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
 * Unsupported platforms and unavailable states are returned as typed
 * results, not thrown errors. Only invalid input, bridge failures, and
 * unexpected failures throw {@link InAppUpdatesError}.
 *
 * @param options - Platform-specific options (e.g. iOS App Store ID).
 * @returns The current update status.
 * @throws InAppUpdatesError on invalid input, bridge failures, or unexpected errors.
 * @public
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
