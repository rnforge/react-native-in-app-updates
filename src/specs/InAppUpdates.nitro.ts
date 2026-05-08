import type { HybridObject } from 'react-native-nitro-modules'

export interface CapabilitiesNative {
  immediate: boolean
  flexible: boolean
  storePage: boolean
  latestVersionLookup: boolean
  installStateListener: boolean
}

export interface AllowedFlowsNative {
  immediate: boolean
  flexible: boolean
}

export interface PlayCoreDetailsNative {
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

export interface AndroidDetailsNative {
  packageName?: string
  playCore?: PlayCoreDetailsNative
}

export interface IosAppStoreDetailsNative {
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

export interface IosDetailsNative {
  bundleIdentifier?: string
  appStoreId?: string
  storeUrl?: string
  appStore?: IosAppStoreDetailsNative
}

export interface IosGetUpdateStatusOptionsNative {
  appStoreId?: string
  country?: string
}

export interface GetUpdateStatusOptionsNative {
  ios?: IosGetUpdateStatusOptionsNative
}

export interface IosOpenStorePageOptionsNative {
  appStoreId: string
  country?: string
}

export interface OpenStorePageOptionsNative {
  ios?: IosOpenStorePageOptionsNative
}

export interface UpdateStatusNative {
  platform: string
  supported: boolean
  updateAvailable: boolean | null
  capabilities: CapabilitiesNative
  allowed: AllowedFlowsNative
  reason: string
  currentVersion?: string
  currentBuild?: string | number
  latestStoreVersion?: string
  latestStoreBuild?: string | number
  installStatus?: string
  android?: AndroidDetailsNative
  ios?: IosDetailsNative
}

export interface InstallStateEventNative {
  platform: string
  supported: boolean
  installStatus: string
  reason: string
  bytesDownloaded?: number
  totalBytesToDownload?: number
  progress?: number
  errorCode?: string
  message?: string
  android?: AndroidDetailsNative
}

export interface InAppUpdates extends HybridObject<{ ios: 'swift', android: 'kotlin' }> {
  getUpdateStatus(options?: GetUpdateStatusOptionsNative): Promise<UpdateStatusNative>
  startImmediateUpdate(): Promise<UpdateStatusNative>
  startFlexibleUpdate(): Promise<UpdateStatusNative>
  completeFlexibleUpdate(): Promise<UpdateStatusNative>
  openStorePage(options?: OpenStorePageOptionsNative): Promise<void>
  addInstallStateListener(listener: (event: InstallStateEventNative) => void): string
  removeInstallStateListener(listenerId: string): void
}
