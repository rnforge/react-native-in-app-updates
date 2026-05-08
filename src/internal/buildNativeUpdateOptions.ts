import type {
  GetUpdateStatusOptionsNative,
  StartImmediateUpdateOptionsNative,
  StartFlexibleUpdateOptionsNative,
} from '../specs/InAppUpdates.nitro'
import type {
  AndroidUpdateOptions,
  GetUpdateStatusOptions,
  StartImmediateUpdateOptions,
  StartFlexibleUpdateOptions,
} from '../types'

function buildAndroidOptions(
  options?: AndroidUpdateOptions
): { android: { allowAssetPackDeletion?: boolean } } | undefined {
  if (!options) return undefined
  const nativeAndroid: { allowAssetPackDeletion?: boolean } = {}
  if (typeof options.allowAssetPackDeletion === 'boolean') {
    nativeAndroid.allowAssetPackDeletion = options.allowAssetPackDeletion
  }
  return Object.keys(nativeAndroid).length > 0
    ? { android: nativeAndroid }
    : undefined
}

export function buildGetUpdateStatusNativeOptions(
  options?: GetUpdateStatusOptions
): GetUpdateStatusOptionsNative | undefined {
  if (!options) return undefined

  const nativeOptions: GetUpdateStatusOptionsNative = {}

  if (options.ios) {
    nativeOptions.ios = {
      appStoreId: options.ios.appStoreId,
      country: options.ios.country,
    }
  }

  const androidNative = buildAndroidOptions(options.android)
  if (androidNative) {
    nativeOptions.android = androidNative.android
  }

  return Object.keys(nativeOptions).length > 0 ? nativeOptions : undefined
}

export function buildStartImmediateUpdateNativeOptions(
  options?: StartImmediateUpdateOptions
): StartImmediateUpdateOptionsNative | undefined {
  if (!options) return undefined
  const androidNative = buildAndroidOptions(options.android)
  return androidNative ? { android: androidNative.android } : undefined
}

export function buildStartFlexibleUpdateNativeOptions(
  options?: StartFlexibleUpdateOptions
): StartFlexibleUpdateOptionsNative | undefined {
  if (!options) return undefined
  const androidNative = buildAndroidOptions(options.android)
  return androidNative ? { android: androidNative.android } : undefined
}
