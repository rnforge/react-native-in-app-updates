import { InAppUpdates } from './native'
import type { InstallStateEvent } from './types'

/**
 * Callback for install-state events.
 * @public
 */
export type InstallStateListener = (event: InstallStateEvent) => void

/**
 * Subscription handle returned by {@link addInstallStateListener}.
 * @public
 */
export type InstallStateSubscription = {
  /** Remove the listener and stop receiving events. */
  remove: () => void
}

/**
 * Subscribe to install-state events (download progress, state changes, errors).
 *
 * On Android, events are emitted during flexible update downloads and
 * install-state transitions. On iOS, the listener receives a typed
 * unsupported event and can be removed safely.
 *
 * @param listener - Callback invoked for each install-state event.
 * @returns A subscription handle. Call `remove()` to unsubscribe.
 * @public
 */
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
