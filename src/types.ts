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
  immediateFailedPreconditions?: string[]
  flexibleFailedPreconditions?: string[]
  installErrorCode?: number
  taskErrorCode?: number
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

export type IosAppStoreDetails = {
  version?: string
  trackViewUrl?: string
  trackName?: string
  releaseNotes?: string
  description?: string
  minimumOsVersion?: string
  averageUserRating?: number
  userRatingCount?: number
  artworkUrl60?: string
  artworkUrl100?: string
  artworkUrl512?: string
}

export type IosDetails = {
  bundleIdentifier?: string
  appStoreId?: string
  storeUrl?: string
  appStore?: IosAppStoreDetails
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
    country?: string
  }
}

export type OpenStorePageOptions = {
  ios?: {
    appStoreId: string
    country?: string
  }
}

export type InAppUpdatesErrorCode =
  | 'invalid-input'
  | 'bridge-error'
  | 'native-error'
  | 'unexpected'

export class InAppUpdatesError extends Error {
  readonly code: InAppUpdatesErrorCode
  readonly android?: AndroidDetails

  constructor(message: string, code: InAppUpdatesErrorCode, android?: AndroidDetails) {
    super(message)
    this.code = code
    this.android = android
    this.name = 'InAppUpdatesError'
  }
}
