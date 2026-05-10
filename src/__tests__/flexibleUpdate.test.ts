import { startFlexibleUpdate } from '../startFlexibleUpdate'
import { completeFlexibleUpdate } from '../completeFlexibleUpdate'
import { addInstallStateListener } from '../addInstallStateListener'
import { mockObject, resetMockObject } from '../__mocks__/react-native-nitro-modules'

jest.mock('react-native-nitro-modules')

describe('startFlexibleUpdate', () => {
  beforeEach(() => {
    resetMockObject()
  })

  it('returns success status when Android flexible update starts', async () => {
    mockObject.startFlexibleUpdate.mockResolvedValue({
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
          flexibleAllowed: true,
        },
      },
    })

    const result = await startFlexibleUpdate()

    expect(result.supported).toBe(true)
    expect(result.platform).toBe('android')
    expect(result.reason).toBe('update-available')
    expect(result.capabilities.flexible).toBe(true)
    expect(result.capabilities.storePage).toBe(true)
    expect(result.allowed.flexible).toBe(true)
    expect(result.android?.packageName).toBe('com.example.app')
  })

  it('returns unsupported-platform typed result on iOS', async () => {
    mockObject.startFlexibleUpdate.mockResolvedValue({
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

    const result = await startFlexibleUpdate()

    expect(result.supported).toBe(false)
    expect(result.platform).toBe('ios')
    expect(result.reason).toBe('unsupported-platform')
    expect(result.updateAvailable).toBeNull()
  })

  it('passes android allowAssetPackDeletion option to native layer', async () => {
    mockObject.startFlexibleUpdate.mockResolvedValue({
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

    await startFlexibleUpdate({ android: { allowAssetPackDeletion: true } })

    expect(mockObject.startFlexibleUpdate).toHaveBeenCalledWith({
      android: { allowAssetPackDeletion: true },
    })
  })

  it('does not pass options when omitted', async () => {
    mockObject.startFlexibleUpdate.mockResolvedValue({
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

    await startFlexibleUpdate()

    expect(mockObject.startFlexibleUpdate).toHaveBeenCalledWith(undefined)
  })

  it('normalizes native failure', async () => {
    mockObject.startFlexibleUpdate.mockRejectedValue(new Error('Flexible bridge failed'))

    await expect(startFlexibleUpdate()).rejects.toMatchObject({
      name: 'InAppUpdatesError',
      code: 'native-error',
      message: 'Flexible bridge failed',
    })
  })
})

describe('completeFlexibleUpdate', () => {
  beforeEach(() => {
    resetMockObject()
  })

  it('returns success when downloaded flexible update is completed', async () => {
    mockObject.completeFlexibleUpdate.mockResolvedValue({
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
      reason: 'flexible-update-downloaded',
      installStatus: 'downloaded',
    })

    const result = await completeFlexibleUpdate()

    expect(result.supported).toBe(true)
    expect(result.reason).toBe('flexible-update-downloaded')
    expect(result.installStatus).toBe('downloaded')
  })

  it('returns update-not-allowed when no downloaded flexible update is present', async () => {
    mockObject.completeFlexibleUpdate.mockResolvedValue({
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

    const result = await completeFlexibleUpdate()

    expect(result.supported).toBe(true)
    expect(result.reason).toBe('update-not-allowed')
  })

  it('normalizes native failure', async () => {
    mockObject.completeFlexibleUpdate.mockRejectedValue(new Error('Complete bridge failed'))

    await expect(completeFlexibleUpdate()).rejects.toMatchObject({
      name: 'InAppUpdatesError',
      code: 'native-error',
      message: 'Complete bridge failed',
    })
  })
})

describe('addInstallStateListener', () => {
  beforeEach(() => {
    resetMockObject()
  })

  it('on iOS emits unsupported event and returns a noop subscription', () => {
    let capturedEvent: any = null
    mockObject.addInstallStateListener.mockImplementation((callback: (event: any) => void) => {
      callback({
        platform: 'ios',
        supported: false,
        installStatus: 'unsupported',
        reason: 'unsupported-platform',
        bytesDownloaded: undefined,
        totalBytesToDownload: undefined,
        progress: undefined,
        errorCode: undefined,
        message: undefined,
        android: undefined,
      })
      return 'ios-listener-id-123'
    })

    const subscription = addInstallStateListener((event) => {
      capturedEvent = event
    })

    expect(capturedEvent).not.toBeNull()
    expect(capturedEvent.platform).toBe('ios')
    expect(capturedEvent.supported).toBe(false)
    expect(capturedEvent.installStatus).toBe('unsupported')
    expect(capturedEvent.reason).toBe('unsupported-platform')
    expect(subscription.remove).toBeDefined()
  })

  it('subscription remove calls native removeInstallStateListener', () => {
    mockObject.addInstallStateListener.mockReturnValue('listener-id-456')

    const subscription = addInstallStateListener(() => {})
    subscription.remove()

    expect(mockObject.removeInstallStateListener).toHaveBeenCalledWith('listener-id-456')
  })

  it('maps Android listener progress/status event correctly', () => {
    let capturedEvent: any = null
    mockObject.addInstallStateListener.mockImplementation((callback: (event: any) => void) => {
      callback({
        platform: 'android',
        supported: true,
        installStatus: 'downloading',
        reason: 'download-progress',
        bytesDownloaded: 1024,
        totalBytesToDownload: 4096,
        progress: 0.25,
        errorCode: undefined,
        message: undefined,
        android: {
          packageName: 'com.example.app',
          playCore: {
            installStatus: 'downloading',
            bytesDownloaded: 1024,
            totalBytesToDownload: 4096,
          },
        },
      })
      return 'android-listener-id-789'
    })

    const subscription = addInstallStateListener((event) => {
      capturedEvent = event
    })

    expect(capturedEvent).not.toBeNull()
    expect(capturedEvent.platform).toBe('android')
    expect(capturedEvent.supported).toBe(true)
    expect(capturedEvent.installStatus).toBe('downloading')
    expect(capturedEvent.reason).toBe('download-progress')
    expect(capturedEvent.bytesDownloaded).toBe(1024)
    expect(capturedEvent.totalBytesToDownload).toBe(4096)
    expect(capturedEvent.progress).toBe(0.25)
    expect(capturedEvent.android?.packageName).toBe('com.example.app')
    expect(capturedEvent.android?.playCore?.installStatus).toBe('downloading')
    expect(subscription.remove).toBeDefined()
  })
})
