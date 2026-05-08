import { InAppUpdates } from './native'
import { mapNativeStatus } from './internal/mapNativeStatus'
import { normalizeNativeError } from './internal/normalizeNativeError'
import { buildStartFlexibleUpdateNativeOptions } from './internal/buildNativeUpdateOptions'
import type { StartFlexibleUpdateOptions, UpdateStatus } from './types'

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
