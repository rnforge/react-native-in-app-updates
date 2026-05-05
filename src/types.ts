export type Platform = 'android' | 'ios'

export type UnsupportedReason =
  | 'unsupported-platform'
  | 'unsupported-os-version'
  | 'unsupported-install-source'
  | 'apk-expansion-files-unsupported'
  | 'missing-app-store-id'
  | 'play-core-unavailable'
  | 'store-lookup-unavailable'

export type AvailabilityReason =
  | 'update-available'
  | 'no-update-available'
  | 'developer-triggered-update-in-progress'
  | 'flexible-update-downloaded'
  | 'update-not-allowed'
  | 'unknown'

export type InstallStatus =
  | 'unknown'
  | 'pending'
  | 'downloading'
  | 'downloaded'
  | 'installing'
  | 'installed'
  | 'failed'
  | 'canceled'
  | 'unsupported'

export type InstallStateEventReason =
  | UnsupportedReason
  | 'download-progress'
  | 'install-state-changed'
  | 'flexible-update-downloaded'
  | 'unknown'

export type Capabilities = {
  immediate: boolean
  flexible: boolean
  storePage: boolean
  latestVersionLookup: boolean
  installStateListener: boolean
}

export type AllowedFlows = {
  immediate: boolean
  flexible: boolean
}

export type PlayCoreDetails = {
  updateAvailability?: string
  installStatus?: string
  updatePriority?: number
  clientVersionStalenessDays?: number
  availableVersionCode?: number
  bytesDownloaded?: number
  totalBytesToDownload?: number
  immediateAllowed?: boolean
  flexibleAllowed?: boolean
}

export type AndroidDetails = {
  packageName?: string
  playCore?: PlayCoreDetails
}

export type IosDetails = {
  bundleIdentifier?: string
  appStoreId?: string
  storeUrl?: string
}

export type UpdateStatus = {
  platform: Platform
  supported: boolean
  updateAvailable: boolean | null
  capabilities: Capabilities
  allowed: AllowedFlows
  reason: AvailabilityReason | UnsupportedReason
  currentVersion?: string
  currentBuild?: string | number
  latestStoreVersion?: string
  latestStoreBuild?: string | number
  installStatus?: InstallStatus
  android?: AndroidDetails
  ios?: IosDetails
}

export type InstallStateEvent = {
  platform: Platform
  supported: boolean
  installStatus: InstallStatus
  reason: InstallStateEventReason
  bytesDownloaded?: number
  totalBytesToDownload?: number
  progress?: number
  errorCode?: string
  message?: string
  android?: AndroidDetails
}

export type GetUpdateStatusOptions = {
  ios?: {
    appStoreId?: string
  }
}

export type OpenStorePageOptions = {
  ios?: {
    appStoreId: string
  }
}

export type InAppUpdatesErrorCode =
  | 'invalid-input'
  | 'bridge-error'
  | 'native-error'
  | 'unexpected'

export class InAppUpdatesError extends Error {
  readonly code: InAppUpdatesErrorCode

  constructor(message: string, code: InAppUpdatesErrorCode) {
    super(message)
    this.code = code
    this.name = 'InAppUpdatesError'
  }
}
