import { InAppUpdates } from './native'
import { mapNativeStatus } from './internal/mapNativeStatus'
import { normalizeNativeError } from './internal/normalizeNativeError'
import { buildStartImmediateUpdateNativeOptions } from './internal/buildNativeUpdateOptions'
import type { StartImmediateUpdateOptions, UpdateStatus } from './types'

/**
 * Start an immediate update flow.
 *
 * On Android, this triggers the Play Core immediate update UI.
 * On iOS, returns an unsupported status (immediate updates are not available).
 *
 * @param options - Platform-specific options.
 * @returns The update status after the flow completes or is rejected.
 * @throws InAppUpdatesError on invalid input, native bridge, or unexpected errors.
 */
export async function startImmediateUpdate(
  options?: StartImmediateUpdateOptions
): Promise<UpdateStatus> {
  try {
    const nativeOptions = buildStartImmediateUpdateNativeOptions(options)
    const result = await InAppUpdates.startImmediateUpdate(nativeOptions)
    return mapNativeStatus(result)
  } catch (error) {
    throw normalizeNativeError(error)
  }
}
