import { InAppUpdates } from './native'
import type { InstallStateEvent } from './types'

export type InstallStateListener = (event: InstallStateEvent) => void

export type InstallStateSubscription = {
  remove: () => void
}

export function addInstallStateListener(
  listener: InstallStateListener
): InstallStateSubscription {
  const listenerId = InAppUpdates.addInstallStateListener((nativeEvent: any) => {
    const bytesDownloaded = nativeEvent.bytesDownloaded as number | undefined
    const totalBytesToDownload = nativeEvent.totalBytesToDownload as number | undefined
    const progress =
      typeof bytesDownloaded === 'number' &&
      typeof totalBytesToDownload === 'number' &&
      totalBytesToDownload > 0
        ? bytesDownloaded / totalBytesToDownload
        : undefined

    const event: InstallStateEvent = {
      platform: nativeEvent.platform as 'android' | 'ios',
      supported: nativeEvent.supported,
      installStatus: nativeEvent.installStatus as InstallStateEvent['installStatus'],
      reason: nativeEvent.reason as InstallStateEvent['reason'],
      bytesDownloaded,
      totalBytesToDownload,
      progress,
      errorCode: nativeEvent.errorCode,
      message: nativeEvent.message,
      android: nativeEvent.android,
    }
    listener(event)
  })

  return {
    remove: () => {
      InAppUpdates.removeInstallStateListener(listenerId)
    },
  }
}
