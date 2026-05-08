import { InAppUpdates } from './native'
import { mapNativeStatus } from './internal/mapNativeStatus'
import { normalizeNativeError } from './internal/normalizeNativeError'
import type { GetUpdateStatusOptions, UpdateStatus } from './types'

export async function getUpdateStatus(
  options?: GetUpdateStatusOptions
): Promise<UpdateStatus> {
  try {
    const nativeOptions = options?.ios?.appStoreId
      ? { ios: { appStoreId: options.ios.appStoreId } }
      : undefined
    const result = await InAppUpdates.getUpdateStatus(nativeOptions)
    return mapNativeStatus(result)
  } catch (error) {
    throw normalizeNativeError(error)
  }
}
