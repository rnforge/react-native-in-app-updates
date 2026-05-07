import { mapNativeStatus } from '../internal/mapNativeStatus'

describe('mapNativeStatus', () => {
  it('maps a full native status with all fields', () => {
    const native = {
      platform: 'android',
      supported: true,
      updateAvailable: true,
      capabilities: {
        immediate: true,
        flexible: true,
        storePage: true,
        latestVersionLookup: false,
        installStateListener: true,
      },
      allowed: {
        immediate: true,
        flexible: true,
      },
      reason: 'update-available',
      currentVersion: '1.0.0',
      currentBuild: 1,
      latestStoreVersion: '2.0.0',
      latestStoreBuild: 2,
      installStatus: 'pending',
      android: {
        packageName: 'com.example.app',
        playCore: {
          updateAvailability: 'UPDATE_AVAILABLE',
          availableVersionCode: 2,
        },
      },
      ios: {
        bundleIdentifier: 'com.example.app',
        appStoreId: '1234567890',
      },
    }

    const result = mapNativeStatus(native)

    expect(result.platform).toBe('android')
    expect(result.supported).toBe(true)
    expect(result.updateAvailable).toBe(true)
    expect(result.capabilities.immediate).toBe(true)
    expect(result.allowed.immediate).toBe(true)
    expect(result.reason).toBe('update-available')
    expect(result.currentVersion).toBe('1.0.0')
    expect(result.currentBuild).toBe(1)
    expect(result.latestStoreVersion).toBe('2.0.0')
    expect(result.latestStoreBuild).toBe(2)
    expect(result.installStatus).toBe('pending')
    expect(result.android?.packageName).toBe('com.example.app')
    expect(result.android?.playCore?.availableVersionCode).toBe(2)
    expect(result.ios?.bundleIdentifier).toBe('com.example.app')
  })

  it('maps a minimal native status with only required fields', () => {
    const native = {
      platform: 'ios',
      supported: false,
      updateAvailable: null,
      capabilities: {
        immediate: false,
        flexible: false,
        storePage: false,
        latestVersionLookup: false,
        installStateListener: false,
      },
      allowed: {
        immediate: false,
        flexible: false,
      },
      reason: 'unsupported-platform',
    }

    const result = mapNativeStatus(native)

    expect(result.platform).toBe('ios')
    expect(result.supported).toBe(false)
    expect(result.updateAvailable).toBeNull()
    expect(result.reason).toBe('unsupported-platform')
    expect(result.currentVersion).toBeUndefined()
    expect(result.currentBuild).toBeUndefined()
    expect(result.latestStoreVersion).toBeUndefined()
    expect(result.latestStoreBuild).toBeUndefined()
    expect(result.installStatus).toBeUndefined()
    expect(result.android).toBeUndefined()
    expect(result.ios).toBeUndefined()
  })

  it('preserves null for updateAvailable when native is null', () => {
    const native = {
      platform: 'android',
      supported: false,
      updateAvailable: null,
      capabilities: {
        immediate: false,
        flexible: false,
        storePage: false,
        latestVersionLookup: false,
        installStateListener: false,
      },
      allowed: {
        immediate: false,
        flexible: false,
      },
      reason: 'unsupported-install-source',
    }

    const result = mapNativeStatus(native)
    expect(result.updateAvailable).toBeNull()
  })

  it('maps false for updateAvailable when native is false', () => {
    const native = {
      platform: 'android',
      supported: true,
      updateAvailable: false,
      capabilities: {
        immediate: false,
        flexible: false,
        storePage: false,
        latestVersionLookup: false,
        installStateListener: false,
      },
      allowed: {
        immediate: false,
        flexible: false,
      },
      reason: 'no-update-available',
    }

    const result = mapNativeStatus(native)
    expect(result.updateAvailable).toBe(false)
  })

  it('passes through undefined optional fields without adding them', () => {
    const native = {
      platform: 'android',
      supported: true,
      updateAvailable: true,
      capabilities: {
        immediate: true,
        flexible: true,
        storePage: false,
        latestVersionLookup: false,
        installStateListener: true,
      },
      allowed: {
        immediate: true,
        flexible: true,
      },
      reason: 'update-available',
    }

    const result = mapNativeStatus(native)

    expect(result.currentVersion).toBeUndefined()
    expect(result.currentBuild).toBeUndefined()
    expect(result.latestStoreVersion).toBeUndefined()
    expect(result.latestStoreBuild).toBeUndefined()
    expect(result.installStatus).toBeUndefined()
    expect(result.android).toBeUndefined()
    expect(result.ios).toBeUndefined()
  })
})
