import { InAppUpdates } from './native'
import { mapNativeStatus } from './internal/mapNativeStatus'
import { normalizeNativeError } from './internal/normalizeNativeError'
import { buildStartImmediateUpdateNativeOptions } from './internal/buildNativeUpdateOptions'
import type { StartImmediateUpdateOptions, UpdateStatus } from './types'

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
