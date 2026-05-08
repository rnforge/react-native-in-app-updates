import { Platform } from 'react-native'
import { InAppUpdates } from './native'
import { InAppUpdatesError } from './types'
import type { OpenStorePageOptions } from './types'

function isDigitsOnly(value: string): boolean {
  return value.length > 0 && /^\d+$/.test(value)
}

function isValidCountry(value: string): boolean {
  return value.length === 2 && /^[a-zA-Z]+$/.test(value)
}

function normalizeCountry(value: string | undefined): string | undefined {
  if (value == null) return undefined
  const trimmed = value.trim()
  if (trimmed.length === 0) return undefined
  return trimmed.toLowerCase()
}

export async function openStorePage(options?: OpenStorePageOptions): Promise<void> {
  if (Platform.OS === 'ios') {
    const appStoreId = options?.ios?.appStoreId
    const rawCountry = options?.ios?.country
    const country = normalizeCountry(rawCountry)

    if (appStoreId == null || appStoreId.length === 0) {
      throw new InAppUpdatesError(
        'Missing ios.appStoreId for openStorePage()',
        'invalid-input'
      )
    }

    if (!isDigitsOnly(appStoreId)) {
      throw new InAppUpdatesError(
        'Invalid ios.appStoreId: must be digits-only',
        'invalid-input'
      )
    }

    if (country != null && !isValidCountry(country)) {
      throw new InAppUpdatesError(
        'Invalid ios.country: must be two-letter code',
        'invalid-input'
      )
    }

    await InAppUpdates.openStorePage({
      ios: { appStoreId, country },
    })
    return
  }

  // Android: allow no options, pass undefined to native
  await InAppUpdates.openStorePage(
    options?.ios?.appStoreId
      ? { ios: { appStoreId: options.ios.appStoreId, country: normalizeCountry(options.ios.country) } }
      : undefined
  )
}
