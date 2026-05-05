import { InAppUpdates } from './native'
import type { UpdateStatus } from './types'

export async function startFlexibleUpdate(): Promise<UpdateStatus> {
  const result = await InAppUpdates.startFlexibleUpdate()
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
