import { getUpdateStatus } from '../getUpdateStatus'
import { mockObject, resetMockObject } from '../__mocks__/react-native-nitro-modules'

jest.mock('react-native-nitro-modules')

describe('getUpdateStatus', () => {
  beforeEach(() => {
    resetMockObject()
  })

  it('returns Android update available snapshot', async () => {
    mockObject.getUpdateStatus.mockResolvedValue({
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
      installStatus: 'pending',
      currentVersion: '1.0.0',
      latestStoreVersion: '2.0.0',
      android: {
        packageName: 'com.example.app',
        playCore: {
          updateAvailability: 'UPDATE_AVAILABLE',
          installStatus: 'pending',
          updatePriority: 0,
          availableVersionCode: 2,
          bytesDownloaded: 0,
          totalBytesToDownload: 4096,
          immediateAllowed: true,
          flexibleAllowed: true,
        },
      },
    })

    const result = await getUpdateStatus()

    expect(result.supported).toBe(true)
    expect(result.platform).toBe('android')
    expect(result.reason).toBe('update-available')
    expect(result.updateAvailable).toBe(true)
    expect(result.capabilities.immediate).toBe(true)
    expect(result.capabilities.storePage).toBe(true)
    expect(result.allowed.immediate).toBe(true)
    expect(result.android?.packageName).toBe('com.example.app')
    expect(result.android?.playCore?.availableVersionCode).toBe(2)
  })

  it('returns Android no update available with playCore snapshot', async () => {
    mockObject.getUpdateStatus.mockResolvedValue({
      platform: 'android',
      supported: true,
      updateAvailable: false,
      capabilities: {
        immediate: true,
        flexible: true,
        storePage: true,
        latestVersionLookup: false,
        installStateListener: true,
      },
      allowed: {
        immediate: false,
        flexible: false,
      },
      reason: 'no-update-available',
      installStatus: 'unknown',
      android: {
        packageName: 'com.example.app',
        playCore: {
          updateAvailability: 'UPDATE_NOT_AVAILABLE',
          installStatus: 'unknown',
          immediateAllowed: false,
          flexibleAllowed: false,
        },
      },
    })

    const result = await getUpdateStatus()

    expect(result.supported).toBe(true)
    expect(result.updateAvailable).toBe(false)
    expect(result.reason).toBe('no-update-available')
    expect(result.capabilities.storePage).toBe(true)
    expect(result.android?.packageName).toBe('com.example.app')
    expect(result.android?.playCore?.updateAvailability).toBe('UPDATE_NOT_AVAILABLE')
  })

  it('returns unsupported-install-source for non-Play Android installs', async () => {
    mockObject.getUpdateStatus.mockResolvedValue({
      platform: 'android',
      supported: false,
      updateAvailable: null,
      capabilities: {
        immediate: false,
        flexible: false,
        storePage: true,
        latestVersionLookup: false,
        installStateListener: false,
      },
      allowed: {
        immediate: false,
        flexible: false,
      },
      reason: 'unsupported-install-source',
    })

    const result = await getUpdateStatus()

    expect(result.supported).toBe(false)
    expect(result.reason).toBe('unsupported-install-source')
    expect(result.updateAvailable).toBeNull()
  })

  it('returns play-core-unavailable when Play Services missing', async () => {
    mockObject.getUpdateStatus.mockResolvedValue({
      platform: 'android',
      supported: false,
      updateAvailable: null,
      capabilities: {
        immediate: false,
        flexible: false,
        storePage: true,
        latestVersionLookup: false,
        installStateListener: false,
      },
      allowed: {
        immediate: false,
        flexible: false,
      },
      reason: 'play-core-unavailable',
    })

    const result = await getUpdateStatus()

    expect(result.supported).toBe(false)
    expect(result.reason).toBe('play-core-unavailable')
  })

  it('Android ignores options safely', async () => {
    mockObject.getUpdateStatus.mockResolvedValue({
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
    })

    const result = await getUpdateStatus({ ios: { appStoreId: '1234567890' } })

    expect(result.supported).toBe(true)
    expect(result.platform).toBe('android')
    expect(mockObject.getUpdateStatus).toHaveBeenCalledWith({
      ios: { appStoreId: '1234567890', country: undefined },
    })
  })

  it('forwards empty appStoreId to native for validation', async () => {
    mockObject.getUpdateStatus.mockRejectedValue(
      new Error('INVALID_INPUT|message=Invalid%20appStoreId')
    )

    await expect(getUpdateStatus({ ios: { appStoreId: '' } })).rejects.toMatchObject({
      name: 'InAppUpdatesError',
      code: 'invalid-input',
    })

    expect(mockObject.getUpdateStatus).toHaveBeenCalledWith({
      ios: { appStoreId: '', country: undefined },
    })
  })

  it('passes country option to native layer', async () => {
    mockObject.getUpdateStatus.mockResolvedValue({
      platform: 'ios',
      supported: true,
      updateAvailable: false,
      capabilities: {
        immediate: false,
        flexible: false,
        storePage: true,
        latestVersionLookup: true,
        installStateListener: false,
      },
      allowed: {
        immediate: false,
        flexible: false,
      },
      reason: 'no-update-available',
    })

    await getUpdateStatus({ ios: { appStoreId: '1234567890', country: 'us' } })

    expect(mockObject.getUpdateStatus).toHaveBeenCalledWith({
      ios: { appStoreId: '1234567890', country: 'us' },
    })
  })

  it('iOS missing appStoreId returns missing-app-store-id', async () => {
    mockObject.getUpdateStatus.mockResolvedValue({
      platform: 'ios',
      supported: false,
      updateAvailable: null,
      capabilities: {
        immediate: false,
        flexible: false,
        storePage: true,
        latestVersionLookup: false,
        installStateListener: false,
      },
      allowed: {
        immediate: false,
        flexible: false,
      },
      reason: 'missing-app-store-id',
    })

    const result = await getUpdateStatus()

    expect(result.supported).toBe(false)
    expect(result.platform).toBe('ios')
    expect(result.reason).toBe('missing-app-store-id')
    expect(result.updateAvailable).toBeNull()
  })

  it('iOS lookup success with update available populates ios.appStore', async () => {
    mockObject.getUpdateStatus.mockResolvedValue({
      platform: 'ios',
      supported: true,
      updateAvailable: true,
      capabilities: {
        immediate: false,
        flexible: false,
        storePage: true,
        latestVersionLookup: true,
        installStateListener: false,
      },
      allowed: {
        immediate: false,
        flexible: false,
      },
      reason: 'update-available',
      currentVersion: '1.0.0',
      latestStoreVersion: '2.0.0',
      ios: {
        bundleIdentifier: 'com.example.app',
        appStoreId: '1234567890',
        storeUrl: 'https://apps.apple.com/app/id1234567890',
        appStore: {
          version: '2.0.0',
          trackName: 'Example App',
          releaseNotes: 'Bug fixes',
          averageUserRating: 4.5,
          userRatingCount: 100,
        },
      },
    })

    const result = await getUpdateStatus({ ios: { appStoreId: '1234567890' } })

    expect(result.supported).toBe(true)
    expect(result.updateAvailable).toBe(true)
    expect(result.reason).toBe('update-available')
    expect(result.capabilities.latestVersionLookup).toBe(true)
    expect(result.ios?.appStoreId).toBe('1234567890')
    expect(result.ios?.appStore?.version).toBe('2.0.0')
    expect(result.ios?.appStore?.trackName).toBe('Example App')
    expect(result.latestStoreVersion).toBe('2.0.0')
  })

  it('iOS lookup success with no update', async () => {
    mockObject.getUpdateStatus.mockResolvedValue({
      platform: 'ios',
      supported: true,
      updateAvailable: false,
      capabilities: {
        immediate: false,
        flexible: false,
        storePage: true,
        latestVersionLookup: true,
        installStateListener: false,
      },
      allowed: {
        immediate: false,
        flexible: false,
      },
      reason: 'no-update-available',
      currentVersion: '2.0.0',
      latestStoreVersion: '2.0.0',
      ios: {
        bundleIdentifier: 'com.example.app',
        appStoreId: '1234567890',
        appStore: {
          version: '2.0.0',
        },
      },
    })

    const result = await getUpdateStatus({ ios: { appStoreId: '1234567890' } })

    expect(result.supported).toBe(true)
    expect(result.updateAvailable).toBe(false)
    expect(result.reason).toBe('no-update-available')
  })

  it('iOS lookup failure returns store-lookup-unavailable', async () => {
    mockObject.getUpdateStatus.mockResolvedValue({
      platform: 'ios',
      supported: false,
      updateAvailable: null,
      capabilities: {
        immediate: false,
        flexible: false,
        storePage: true,
        latestVersionLookup: false,
        installStateListener: false,
      },
      allowed: {
        immediate: false,
        flexible: false,
      },
      reason: 'store-lookup-unavailable',
      ios: {
        bundleIdentifier: 'com.example.app',
        appStoreId: '1234567890',
      },
    })

    const result = await getUpdateStatus({ ios: { appStoreId: '1234567890' } })

    expect(result.supported).toBe(false)
    expect(result.reason).toBe('store-lookup-unavailable')
    expect(result.capabilities.storePage).toBe(true)
    expect(result.capabilities.latestVersionLookup).toBe(false)
    expect(result.ios?.appStoreId).toBe('1234567890')
  })

  it('iOS ambiguous version returns update-not-allowed', async () => {
    mockObject.getUpdateStatus.mockResolvedValue({
      platform: 'ios',
      supported: true,
      updateAvailable: null,
      capabilities: {
        immediate: false,
        flexible: false,
        storePage: true,
        latestVersionLookup: true,
        installStateListener: false,
      },
      allowed: {
        immediate: false,
        flexible: false,
      },
      reason: 'update-not-allowed',
      currentVersion: '1.0.0-beta',
      latestStoreVersion: '2.0.0',
      ios: {
        bundleIdentifier: 'com.example.app',
        appStoreId: '1234567890',
        appStore: {
          version: '2.0.0',
        },
      },
    })

    const result = await getUpdateStatus({ ios: { appStoreId: '1234567890' } })

    expect(result.supported).toBe(true)
    expect(result.updateAvailable).toBeNull()
    expect(result.reason).toBe('update-not-allowed')
    expect(result.capabilities.latestVersionLookup).toBe(true)
  })

  it('returns developer-triggered-update-in-progress as supported with updateAvailable true', async () => {
    mockObject.getUpdateStatus.mockResolvedValue({
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
        immediate: false,
        flexible: false,
      },
      reason: 'developer-triggered-update-in-progress',
      installStatus: 'installing',
      android: {
        packageName: 'com.example.app',
        playCore: {
          updateAvailability: 'DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS',
          installStatus: 'installing',
          immediateAllowed: false,
          flexibleAllowed: false,
        },
      },
    })

    const result = await getUpdateStatus()

    expect(result.supported).toBe(true)
    expect(result.updateAvailable).toBe(true)
    expect(result.reason).toBe('developer-triggered-update-in-progress')
    expect(result.installStatus).toBe('installing')
    expect(result.allowed.immediate).toBe(false)
    expect(result.allowed.flexible).toBe(false)
  })

  it('returns update-not-allowed when Play Core disallows both flows', async () => {
    mockObject.getUpdateStatus.mockResolvedValue({
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
        immediate: false,
        flexible: false,
      },
      reason: 'update-not-allowed',
      installStatus: 'unknown',
      android: {
        packageName: 'com.example.app',
        playCore: {
          updateAvailability: 'UPDATE_AVAILABLE',
          installStatus: 'unknown',
          immediateAllowed: false,
          flexibleAllowed: false,
        },
      },
    })

    const result = await getUpdateStatus()

    expect(result.supported).toBe(true)
    expect(result.updateAvailable).toBe(true)
    expect(result.reason).toBe('update-not-allowed')
    expect(result.allowed.immediate).toBe(false)
    expect(result.allowed.flexible).toBe(false)
  })

  it('throws InAppUpdatesError for native bridge failures', async () => {
    mockObject.getUpdateStatus.mockRejectedValue(new Error('Native bridge failure'))

    await expect(getUpdateStatus()).rejects.toBeInstanceOf(Error)
  })

  it('normalizes INVALID_INPUT prefix to invalid-input error', async () => {
    mockObject.getUpdateStatus.mockRejectedValue(
      new Error('INVALID_INPUT|message=Invalid%20appStoreId')
    )

    await expect(getUpdateStatus()).rejects.toMatchObject({
      name: 'InAppUpdatesError',
      code: 'invalid-input',
    })

    await expect(getUpdateStatus()).rejects.toHaveProperty('message', 'Invalid appStoreId')
  })

  it('normalizes structured Play Core task failures', async () => {
    mockObject.getUpdateStatus.mockRejectedValue(
      new Error(
        'PLAY_CORE_TASK_FAILURE|message=Task%20failed%20%7C%20bad%3Dinput|taskErrorCode=-1'
      )
    )

    await expect(getUpdateStatus()).rejects.toMatchObject({
      name: 'InAppUpdatesError',
      code: 'native-error',
      android: {
        playCore: {
          taskErrorCode: -1,
        },
      },
    })

    await expect(getUpdateStatus()).rejects.toHaveProperty('message', 'Task failed | bad=input')
  })
})
