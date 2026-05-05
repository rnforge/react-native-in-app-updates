import { Platform } from 'react-native'
import { InAppUpdates } from './native'
import { InAppUpdatesError } from './types'
import type { OpenStorePageOptions } from './types'

export async function openStorePage(options?: OpenStorePageOptions): Promise<void> {
  if (Platform.OS === 'ios') {
    if (options?.ios?.appStoreId == null) {
      throw new InAppUpdatesError(
        'Missing ios.appStoreId for openStorePage()',
        'invalid-input'
      )
    }
    await InAppUpdates.openStorePage({
      ios: { appStoreId: options.ios.appStoreId },
    })
    return
  }

  // Android: allow no options, pass undefined to native
  await InAppUpdates.openStorePage(
    options?.ios?.appStoreId
      ? { ios: { appStoreId: options.ios.appStoreId } }
      : undefined
  )
}
