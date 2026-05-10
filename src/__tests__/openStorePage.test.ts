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
      ios: { appStoreId: '1234567890', country: undefined },
    })
  })

  it('iOS country option passed through', async () => {
    mockPlatformOS = 'ios'
    mockObject.openStorePage.mockResolvedValue(undefined)

    await openStorePage({ ios: { appStoreId: '1234567890', country: 'us' } })

    expect(mockObject.openStorePage).toHaveBeenCalledWith({
      ios: { appStoreId: '1234567890', country: 'us' },
    })
  })

  it('iOS country "US" normalizes to "us" before passing to native', async () => {
    mockPlatformOS = 'ios'
    mockObject.openStorePage.mockResolvedValue(undefined)

    await openStorePage({ ios: { appStoreId: '1234567890', country: 'US' } })

    expect(mockObject.openStorePage).toHaveBeenCalledWith({
      ios: { appStoreId: '1234567890', country: 'us' },
    })
  })

  it('iOS country " us " normalizes to "us" before passing to native', async () => {
    mockPlatformOS = 'ios'
    mockObject.openStorePage.mockResolvedValue(undefined)

    await openStorePage({ ios: { appStoreId: '1234567890', country: ' us ' } })

    expect(mockObject.openStorePage).toHaveBeenCalledWith({
      ios: { appStoreId: '1234567890', country: 'us' },
    })
  })

  it('iOS missing appStoreId throws InAppUpdatesError', async () => {
    mockPlatformOS = 'ios'

    await expect(openStorePage()).rejects.toBeInstanceOf(InAppUpdatesError)
    await expect(openStorePage()).rejects.toMatchObject({ code: 'invalid-input' })
    expect(mockObject.openStorePage).not.toHaveBeenCalled()
  })

  it('iOS empty appStoreId throws invalid-input', async () => {
    mockPlatformOS = 'ios'

    await expect(openStorePage({ ios: { appStoreId: '' } })).rejects.toMatchObject({
      name: 'InAppUpdatesError',
      code: 'invalid-input',
    })
    expect(mockObject.openStorePage).not.toHaveBeenCalled()
  })

  it('iOS non-digit appStoreId throws invalid-input', async () => {
    mockPlatformOS = 'ios'

    await expect(openStorePage({ ios: { appStoreId: 'abc' } })).rejects.toMatchObject({
      name: 'InAppUpdatesError',
      code: 'invalid-input',
    })
    expect(mockObject.openStorePage).not.toHaveBeenCalled()
  })

  it('iOS invalid country throws invalid-input', async () => {
    mockPlatformOS = 'ios'

    await expect(openStorePage({ ios: { appStoreId: '1234567890', country: 'usa' } })).rejects.toMatchObject({
      name: 'InAppUpdatesError',
      code: 'invalid-input',
    })
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

  it('Android normalizes ios country when appStoreId is provided', async () => {
    mockPlatformOS = 'android'
    mockObject.openStorePage.mockResolvedValue(undefined)

    await openStorePage({ ios: { appStoreId: '1234567890', country: ' US ' } })

    expect(mockObject.openStorePage).toHaveBeenCalledWith({
      ios: { appStoreId: '1234567890', country: 'us' },
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
