import { InAppUpdates } from './native'
import { mapNativeStatus } from './internal/mapNativeStatus'
import { normalizeNativeError } from './internal/normalizeNativeError'
import type { UpdateStatus } from './types'

export async function completeFlexibleUpdate(): Promise<UpdateStatus> {
  try {
    const result = await InAppUpdates.completeFlexibleUpdate()
    return mapNativeStatus(result)
  } catch (error) {
    throw normalizeNativeError(error)
  }
}
