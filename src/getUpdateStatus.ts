import { InAppUpdates } from './native'
import type { GetUpdateStatusOptions, UpdateStatus } from './types'

export async function getUpdateStatus(
  options?: GetUpdateStatusOptions
): Promise<UpdateStatus> {
  const nativeOptions = options?.ios?.appStoreId
    ? { ios: { appStoreId: options.ios.appStoreId } }
    : undefined
  const result = await InAppUpdates.getUpdateStatus(nativeOptions)
  return mapNativeStatus(result)
}

function mapNativeStatus(native: any): UpdateStatus {
  return {
    platform: native.platform as 'android' | 'ios',
    supported: native.supported,
    updateAvailable: native.updateAvailable,
    capabilities: native.capabilities,
    allowed: native.allowed,
    reason: native.reason as UpdateStatus['reason'],
    currentVersion: native.currentVersion,
    currentBuild: native.currentBuild,
    latestStoreVersion: native.latestStoreVersion,
    latestStoreBuild: native.latestStoreBuild,
    installStatus: native.installStatus as UpdateStatus['installStatus'],
    android: native.android,
    ios: native.ios,
  }
}
