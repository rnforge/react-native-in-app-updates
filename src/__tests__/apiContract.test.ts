import * as api from '../index'

describe('v1 public API exports', () => {
  it('exports getUpdateStatus', () => {
    expect(typeof api.getUpdateStatus).toBe('function')
  })

  it('exports startImmediateUpdate', () => {
    expect(typeof api.startImmediateUpdate).toBe('function')
  })

  it('exports startFlexibleUpdate', () => {
    expect(typeof api.startFlexibleUpdate).toBe('function')
  })

  it('exports completeFlexibleUpdate', () => {
    expect(typeof api.completeFlexibleUpdate).toBe('function')
  })

  it('exports openStorePage', () => {
    expect(typeof api.openStorePage).toBe('function')
  })

  it('exports addInstallStateListener', () => {
    expect(typeof api.addInstallStateListener).toBe('function')
  })
})

describe('UpdateStatus contract', () => {
  it('has all required fields in the type shape', () => {
    const status: api.UpdateStatus = {
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

    expect(status.platform).toBe('android')
    expect(status.supported).toBe(true)
    expect(status.updateAvailable).toBe(true)
    expect(status.capabilities).toBeDefined()
    expect(status.allowed).toBeDefined()
    expect(status.reason).toBe('update-available')
  })

  it('allows optional fields to be omitted', () => {
    const status: api.UpdateStatus = {
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

    expect(status.currentVersion).toBeUndefined()
    expect(status.currentBuild).toBeUndefined()
    expect(status.latestStoreVersion).toBeUndefined()
    expect(status.latestStoreBuild).toBeUndefined()
    expect(status.installStatus).toBeUndefined()
    expect(status.android).toBeUndefined()
    expect(status.ios).toBeUndefined()
  })

  it('accepts all supported platforms', () => {
    const android: api.UpdateStatus = {
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
    const ios: api.UpdateStatus = {
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

    expect(android.platform).toBe('android')
    expect(ios.platform).toBe('ios')
  })
})

describe('InstallStateEvent contract', () => {
  it('has all required fields in the type shape', () => {
    const event: api.InstallStateEvent = {
      platform: 'android',
      supported: true,
      installStatus: 'downloading',
      reason: 'download-progress',
    }

    expect(event.platform).toBe('android')
    expect(event.supported).toBe(true)
    expect(event.installStatus).toBe('downloading')
    expect(event.reason).toBe('download-progress')
  })

  it('accepts optional progress and android fields', () => {
    const event: api.InstallStateEvent = {
      platform: 'android',
      supported: true,
      installStatus: 'downloading',
      reason: 'download-progress',
      bytesDownloaded: 1024,
      totalBytesToDownload: 4096,
      progress: 0.25,
      android: {
        packageName: 'com.example.app',
        playCore: {
          installStatus: 'downloading',
          bytesDownloaded: 1024,
          totalBytesToDownload: 4096,
        },
      },
    }

    expect(event.progress).toBe(0.25)
    expect(event.android?.packageName).toBe('com.example.app')
  })
})

describe('UnsupportedReason union', () => {
  it('includes all expected values', () => {
    const reasons: api.UnsupportedReason[] = [
      'unsupported-platform',
      'unsupported-os-version',
      'unsupported-install-source',
      'apk-expansion-files-unsupported',
      'missing-app-store-id',
      'play-core-unavailable',
      'store-lookup-unavailable',
    ]

    expect(reasons).toHaveLength(7)
    expect(new Set(reasons).size).toBe(7)
  })
})

describe('AvailabilityReason union', () => {
  it('includes all expected values', () => {
    const reasons: api.AvailabilityReason[] = [
      'update-available',
      'no-update-available',
      'developer-triggered-update-in-progress',
      'flexible-update-downloaded',
      'update-not-allowed',
      'unknown',
    ]

    expect(reasons).toHaveLength(6)
    expect(new Set(reasons).size).toBe(6)
  })
})

describe('InstallStatus union', () => {
  it('includes all expected values', () => {
    const statuses: api.InstallStatus[] = [
      'unknown',
      'pending',
      'downloading',
      'downloaded',
      'installing',
      'installed',
      'failed',
      'canceled',
      'unsupported',
    ]

    expect(statuses).toHaveLength(9)
    expect(new Set(statuses).size).toBe(9)
  })
})
