import type { UpdateStatus } from '../types'

export function mapNativeStatus(native: any): UpdateStatus {
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
