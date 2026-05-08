import { startImmediateUpdate } from '../startImmediateUpdate'
import { mockObject, resetMockObject } from '../__mocks__/react-native-nitro-modules'

jest.mock('react-native-nitro-modules')

describe('startImmediateUpdate', () => {
  beforeEach(() => {
    resetMockObject()
  })

  it('returns unsupported-platform typed result on iOS', async () => {
    mockObject.startImmediateUpdate.mockResolvedValue({
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
    })

    const result = await startImmediateUpdate()

    expect(result.supported).toBe(false)
    expect(result.platform).toBe('ios')
    expect(result.reason).toBe('unsupported-platform')
    expect(result.updateAvailable).toBeNull()
  })

  it('returns success status when Android immediate update starts', async () => {
    mockObject.startImmediateUpdate.mockResolvedValue({
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
      latestStoreVersion: '2.0.0',
      installStatus: 'pending',
      android: {
        packageName: 'com.example.app',
        playCore: {
          updateAvailability: 'UPDATE_AVAILABLE',
          immediateAllowed: true,
        },
      },
    })

    const result = await startImmediateUpdate()

    expect(result.supported).toBe(true)
    expect(result.platform).toBe('android')
    expect(result.reason).toBe('update-available')
    expect(result.capabilities.immediate).toBe(true)
    expect(result.capabilities.storePage).toBe(true)
    expect(result.allowed.immediate).toBe(true)
    expect(result.android?.packageName).toBe('com.example.app')
  })

  it('returns unsupported-install-source for non-Play Android installs', async () => {
    mockObject.startImmediateUpdate.mockResolvedValue({
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
    })

    const result = await startImmediateUpdate()

    expect(result.supported).toBe(false)
    expect(result.reason).toBe('unsupported-install-source')
    expect(result.updateAvailable).toBeNull()
  })

  it('returns update-not-allowed when Play Core disallows immediate', async () => {
    mockObject.startImmediateUpdate.mockResolvedValue({
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
        flexible: true,
      },
      reason: 'update-not-allowed',
    })

    const result = await startImmediateUpdate()

    expect(result.supported).toBe(true)
    expect(result.reason).toBe('update-not-allowed')
    expect(result.capabilities.storePage).toBe(true)
    expect(result.allowed.immediate).toBe(false)
  })

  it('passes android allowAssetPackDeletion option to native layer', async () => {
    mockObject.startImmediateUpdate.mockResolvedValue({
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

    await startImmediateUpdate({ android: { allowAssetPackDeletion: true } })

    expect(mockObject.startImmediateUpdate).toHaveBeenCalledWith({
      android: { allowAssetPackDeletion: true },
    })
  })

  it('does not pass options when omitted', async () => {
    mockObject.startImmediateUpdate.mockResolvedValue({
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

    await startImmediateUpdate()

    expect(mockObject.startImmediateUpdate).toHaveBeenCalledWith(undefined)
  })

  it('throws InAppUpdatesError for native bridge failures', async () => {
    mockObject.startImmediateUpdate.mockRejectedValue(new Error('Native bridge failure'))

    await expect(startImmediateUpdate()).rejects.toBeInstanceOf(Error)
  })
})
