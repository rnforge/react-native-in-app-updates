import { openStorePage } from '../openStorePage'
import { InAppUpdatesError } from '../types'
import { mockObject, resetMockObject } from '../__mocks__/react-native-nitro-modules'

jest.mock('react-native-nitro-modules')

let mockPlatformOS = 'ios'
jest.mock('react-native', () => ({
  Platform: {
    get OS() {
      return mockPlatformOS
    },
  },
}))

describe('openStorePage', () => {
  beforeEach(() => {
    resetMockObject()
    mockPlatformOS = 'ios'
  })

  it('iOS appStoreId passed through resolves', async () => {
    mockPlatformOS = 'ios'
    mockObject.openStorePage.mockResolvedValue(undefined)

    await openStorePage({ ios: { appStoreId: '1234567890' } })

    expect(mockObject.openStorePage).toHaveBeenCalledWith({
      ios: { appStoreId: '1234567890' },
    })
  })

  it('iOS missing appStoreId throws InAppUpdatesError', async () => {
    mockPlatformOS = 'ios'

    await expect(openStorePage()).rejects.toBeInstanceOf(InAppUpdatesError)
    await expect(openStorePage()).rejects.toThrow('Missing ios.appStoreId')
    expect(mockObject.openStorePage).not.toHaveBeenCalled()
  })

  it('Android call can be made without appStoreId', async () => {
    mockPlatformOS = 'android'
    mockObject.openStorePage.mockResolvedValue(undefined)

    await openStorePage()

    expect(mockObject.openStorePage).toHaveBeenCalledWith(undefined)
  })

  it('Android ignores ios appStoreId', async () => {
    mockPlatformOS = 'android'
    mockObject.openStorePage.mockResolvedValue(undefined)

    await openStorePage({ ios: { appStoreId: '1234567890' } })

    expect(mockObject.openStorePage).toHaveBeenCalledWith({
      ios: { appStoreId: '1234567890' },
    })
  })

  it('native failure rejects', async () => {
    mockPlatformOS = 'ios'
    mockObject.openStorePage.mockRejectedValue(new Error('Failed to open store'))

    await expect(openStorePage({ ios: { appStoreId: '1234567890' } })).rejects.toBeInstanceOf(
      Error
    )
  })
})
