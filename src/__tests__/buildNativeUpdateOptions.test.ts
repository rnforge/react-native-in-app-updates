import {
  buildGetUpdateStatusNativeOptions,
  buildStartImmediateUpdateNativeOptions,
  buildStartFlexibleUpdateNativeOptions,
} from '../internal/buildNativeUpdateOptions'

describe('buildNativeUpdateOptions', () => {
  describe('buildGetUpdateStatusNativeOptions', () => {
    it('returns undefined when no options provided', () => {
      expect(buildGetUpdateStatusNativeOptions()).toBeUndefined()
    })

    it('returns undefined for empty options object', () => {
      expect(buildGetUpdateStatusNativeOptions({})).toBeUndefined()
    })

    it('builds ios options only', () => {
      const result = buildGetUpdateStatusNativeOptions({
        ios: { appStoreId: '1234567890', country: 'us' },
      })
      expect(result).toEqual({
        ios: { appStoreId: '1234567890', country: 'us' },
      })
    })

    it('builds android options only', () => {
      const result = buildGetUpdateStatusNativeOptions({
        android: { allowAssetPackDeletion: true },
      })
      expect(result).toEqual({
        android: { allowAssetPackDeletion: true },
      })
    })

    it('builds both ios and android options', () => {
      const result = buildGetUpdateStatusNativeOptions({
        ios: { appStoreId: '1234567890' },
        android: { allowAssetPackDeletion: true },
      })
      expect(result).toEqual({
        ios: { appStoreId: '1234567890', country: undefined },
        android: { allowAssetPackDeletion: true },
      })
    })

    it('omits android key when allowAssetPackDeletion is omitted', () => {
      const result = buildGetUpdateStatusNativeOptions({
        ios: { appStoreId: '1234567890' },
        android: {},
      })
      expect(result).toEqual({
        ios: { appStoreId: '1234567890', country: undefined },
      })
    })

    it('passes allowAssetPackDeletion false explicitly', () => {
      const result = buildGetUpdateStatusNativeOptions({
        android: { allowAssetPackDeletion: false },
      })
      expect(result).toEqual({
        android: { allowAssetPackDeletion: false },
      })
    })
  })

  describe('buildStartImmediateUpdateNativeOptions', () => {
    it('returns undefined when no options provided', () => {
      expect(buildStartImmediateUpdateNativeOptions()).toBeUndefined()
    })

    it('returns undefined when android is omitted', () => {
      expect(buildStartImmediateUpdateNativeOptions({})).toBeUndefined()
    })

    it('builds android options when provided', () => {
      const result = buildStartImmediateUpdateNativeOptions({
        android: { allowAssetPackDeletion: true },
      })
      expect(result).toEqual({
        android: { allowAssetPackDeletion: true },
      })
    })

    it('omits android key when allowAssetPackDeletion is omitted', () => {
      const result = buildStartImmediateUpdateNativeOptions({
        android: {},
      })
      expect(result).toBeUndefined()
    })
  })

  describe('buildStartFlexibleUpdateNativeOptions', () => {
    it('returns undefined when no options provided', () => {
      expect(buildStartFlexibleUpdateNativeOptions()).toBeUndefined()
    })

    it('returns undefined when android is omitted', () => {
      expect(buildStartFlexibleUpdateNativeOptions({})).toBeUndefined()
    })

    it('builds android options when provided', () => {
      const result = buildStartFlexibleUpdateNativeOptions({
        android: { allowAssetPackDeletion: true },
      })
      expect(result).toEqual({
        android: { allowAssetPackDeletion: true },
      })
    })

    it('omits android key when allowAssetPackDeletion is omitted', () => {
      const result = buildStartFlexibleUpdateNativeOptions({
        android: {},
      })
      expect(result).toBeUndefined()
    })
  })
})
