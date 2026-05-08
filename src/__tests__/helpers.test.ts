import type { UpdateStatus } from '../types'
import {
  isUpdateAvailable,
  canStartImmediateUpdate,
  canStartFlexibleUpdate,
  canCompleteFlexibleUpdate,
  canOpenStorePage,
  supportsInstallStateListener,
} from '../helpers'

function makeStatus(partial: Partial<UpdateStatus> & Pick<UpdateStatus, 'platform'>): UpdateStatus {
  return {
    platform: partial.platform,
    supported: partial.supported ?? true,
    updateAvailable: partial.updateAvailable ?? null,
    capabilities: {
      immediate: partial.capabilities?.immediate ?? false,
      flexible: partial.capabilities?.flexible ?? false,
      storePage: partial.capabilities?.storePage ?? true,
      latestVersionLookup: partial.capabilities?.latestVersionLookup ?? false,
      installStateListener: partial.capabilities?.installStateListener ?? false,
    },
    allowed: {
      immediate: partial.allowed?.immediate ?? false,
      flexible: partial.allowed?.flexible ?? false,
    },
    reason: partial.reason ?? 'unknown',
    currentVersion: partial.currentVersion,
    currentBuild: partial.currentBuild,
    latestStoreVersion: partial.latestStoreVersion,
    latestStoreBuild: partial.latestStoreBuild,
    installStatus: partial.installStatus,
    android: partial.android,
    ios: partial.ios,
  }
}

describe('isUpdateAvailable', () => {
  it('returns true when updateAvailable is true', () => {
    const status = makeStatus({ platform: 'android', updateAvailable: true })
    expect(isUpdateAvailable(status)).toBe(true)
  })

  it('returns false when updateAvailable is false', () => {
    const status = makeStatus({ platform: 'android', updateAvailable: false })
    expect(isUpdateAvailable(status)).toBe(false)
  })

  it('returns false when updateAvailable is null', () => {
    const status = makeStatus({ platform: 'ios', updateAvailable: null })
    expect(isUpdateAvailable(status)).toBe(false)
  })
})

describe('canStartImmediateUpdate', () => {
  it('returns true for Android Play with update available and allowed', () => {
    const status = makeStatus({
      platform: 'android',
      supported: true,
      updateAvailable: true,
      capabilities: { immediate: true, flexible: true, storePage: true, latestVersionLookup: false, installStateListener: true },
      allowed: { immediate: true, flexible: true },
      reason: 'update-available',
    })
    expect(canStartImmediateUpdate(status)).toBe(true)
  })

  it('returns false when immediate not allowed', () => {
    const status = makeStatus({
      platform: 'android',
      supported: true,
      updateAvailable: true,
      capabilities: { immediate: true, flexible: true, storePage: true, latestVersionLookup: false, installStateListener: true },
      allowed: { immediate: false, flexible: true },
      reason: 'update-not-allowed',
    })
    expect(canStartImmediateUpdate(status)).toBe(false)
  })

  it('returns false when no update available', () => {
    const status = makeStatus({
      platform: 'android',
      supported: true,
      updateAvailable: false,
      capabilities: { immediate: true, flexible: true, storePage: true, latestVersionLookup: false, installStateListener: true },
      allowed: { immediate: true, flexible: true },
      reason: 'no-update-available',
    })
    expect(canStartImmediateUpdate(status)).toBe(false)
  })

  it('returns false when unsupported', () => {
    const status = makeStatus({
      platform: 'ios',
      supported: false,
      updateAvailable: null,
      capabilities: { immediate: false, flexible: false, storePage: true, latestVersionLookup: false, installStateListener: false },
      allowed: { immediate: false, flexible: false },
      reason: 'unsupported-platform',
    })
    expect(canStartImmediateUpdate(status)).toBe(false)
  })

  it('returns false when immediate capability false', () => {
    const status = makeStatus({
      platform: 'android',
      supported: true,
      updateAvailable: true,
      capabilities: { immediate: false, flexible: true, storePage: true, latestVersionLookup: false, installStateListener: true },
      allowed: { immediate: false, flexible: true },
      reason: 'update-available',
    })
    expect(canStartImmediateUpdate(status)).toBe(false)
  })
})

describe('canStartFlexibleUpdate', () => {
  it('returns true for Android Play with update available and allowed', () => {
    const status = makeStatus({
      platform: 'android',
      supported: true,
      updateAvailable: true,
      capabilities: { immediate: true, flexible: true, storePage: true, latestVersionLookup: false, installStateListener: true },
      allowed: { immediate: true, flexible: true },
      reason: 'update-available',
    })
    expect(canStartFlexibleUpdate(status)).toBe(true)
  })

  it('returns false when flexible not allowed', () => {
    const status = makeStatus({
      platform: 'android',
      supported: true,
      updateAvailable: true,
      capabilities: { immediate: true, flexible: true, storePage: true, latestVersionLookup: false, installStateListener: true },
      allowed: { immediate: true, flexible: false },
      reason: 'update-not-allowed',
    })
    expect(canStartFlexibleUpdate(status)).toBe(false)
  })

  it('returns false when no update available', () => {
    const status = makeStatus({
      platform: 'android',
      supported: true,
      updateAvailable: false,
      capabilities: { immediate: true, flexible: true, storePage: true, latestVersionLookup: false, installStateListener: true },
      allowed: { immediate: true, flexible: true },
      reason: 'no-update-available',
    })
    expect(canStartFlexibleUpdate(status)).toBe(false)
  })

  it('returns false when unsupported', () => {
    const status = makeStatus({
      platform: 'ios',
      supported: false,
      updateAvailable: null,
      capabilities: { immediate: false, flexible: false, storePage: true, latestVersionLookup: false, installStateListener: false },
      allowed: { immediate: false, flexible: false },
      reason: 'unsupported-platform',
    })
    expect(canStartFlexibleUpdate(status)).toBe(false)
  })
})

describe('canCompleteFlexibleUpdate', () => {
  it('returns true when downloaded and supported', () => {
    const status = makeStatus({
      platform: 'android',
      supported: true,
      updateAvailable: true,
      capabilities: { immediate: true, flexible: true, storePage: true, latestVersionLookup: false, installStateListener: true },
      allowed: { immediate: false, flexible: false },
      reason: 'flexible-update-downloaded',
      installStatus: 'downloaded',
    })
    expect(canCompleteFlexibleUpdate(status)).toBe(true)
  })

  it('returns false when not downloaded', () => {
    const status = makeStatus({
      platform: 'android',
      supported: true,
      updateAvailable: true,
      capabilities: { immediate: true, flexible: true, storePage: true, latestVersionLookup: false, installStateListener: true },
      allowed: { immediate: true, flexible: true },
      reason: 'update-available',
      installStatus: 'pending',
    })
    expect(canCompleteFlexibleUpdate(status)).toBe(false)
  })

  it('returns false when flexible capability false', () => {
    const status = makeStatus({
      platform: 'android',
      supported: true,
      installStatus: 'downloaded',
      capabilities: { immediate: true, flexible: false, storePage: true, latestVersionLookup: false, installStateListener: true },
      allowed: { immediate: false, flexible: false },
      reason: 'update-not-allowed',
    })
    expect(canCompleteFlexibleUpdate(status)).toBe(false)
  })

  it('returns false when unsupported', () => {
    const status = makeStatus({
      platform: 'ios',
      supported: false,
      installStatus: 'downloaded',
      capabilities: { immediate: false, flexible: false, storePage: true, latestVersionLookup: false, installStateListener: false },
      allowed: { immediate: false, flexible: false },
      reason: 'unsupported-platform',
    })
    expect(canCompleteFlexibleUpdate(status)).toBe(false)
  })
})

describe('canOpenStorePage', () => {
  it('returns true when storePage capability is true', () => {
    const status = makeStatus({
      platform: 'android',
      supported: true,
      capabilities: { immediate: true, flexible: true, storePage: true, latestVersionLookup: false, installStateListener: true },
      allowed: { immediate: true, flexible: true },
      reason: 'update-available',
    })
    expect(canOpenStorePage(status)).toBe(true)
  })

  it('returns true even when supported is false (e.g. iOS lookup failure)', () => {
    const status = makeStatus({
      platform: 'ios',
      supported: false,
      capabilities: { immediate: false, flexible: false, storePage: true, latestVersionLookup: false, installStateListener: false },
      allowed: { immediate: false, flexible: false },
      reason: 'store-lookup-unavailable',
    })
    expect(canOpenStorePage(status)).toBe(true)
  })

  it('returns false when storePage capability is false', () => {
    const status = makeStatus({
      platform: 'android',
      supported: false,
      capabilities: { immediate: false, flexible: false, storePage: false, latestVersionLookup: false, installStateListener: false },
      allowed: { immediate: false, flexible: false },
      reason: 'play-core-unavailable',
    })
    expect(canOpenStorePage(status)).toBe(false)
  })
})

describe('supportsInstallStateListener', () => {
  it('returns true when installStateListener capability is true', () => {
    const status = makeStatus({
      platform: 'android',
      supported: true,
      capabilities: { immediate: true, flexible: true, storePage: true, latestVersionLookup: false, installStateListener: true },
      allowed: { immediate: true, flexible: true },
      reason: 'update-available',
    })
    expect(supportsInstallStateListener(status)).toBe(true)
  })

  it('returns false when installStateListener capability is false', () => {
    const status = makeStatus({
      platform: 'ios',
      supported: false,
      capabilities: { immediate: false, flexible: false, storePage: true, latestVersionLookup: false, installStateListener: false },
      allowed: { immediate: false, flexible: false },
      reason: 'unsupported-platform',
    })
    expect(supportsInstallStateListener(status)).toBe(false)
  })
})
