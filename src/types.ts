/** The host platform. */
export type Platform = 'android' | 'ios'

/**
 * Reasons for unsupported, unavailable, or store-lookup states.
 *
 * Returned in `UpdateStatus.reason` when the platform is not supported,
 * or when store lookups fail.
 */
export type UnsupportedReason =
  | 'unsupported-platform'
  | 'unsupported-os-version'
  | 'unsupported-install-source'
  | 'apk-expansion-files-unsupported'
  | 'missing-app-store-id'
  | 'play-core-unavailable'
  | 'store-lookup-unavailable'
  | 'store-lookup-timeout'
  | 'store-lookup-network-error'
  | 'store-lookup-http-error'
  | 'store-lookup-not-found'
  | 'store-lookup-invalid-response'

/**
 * Reasons describing the current update availability state.
 *
 * Returned in `UpdateStatus.reason` when the platform is supported.
 */
export type AvailabilityReason =
  | 'update-available'
  | 'no-update-available'
  | 'developer-triggered-update-in-progress'
  | 'flexible-update-downloaded'
  | 'update-not-allowed'
  | 'context-unavailable'
  | 'activity-unavailable'
  | 'user-canceled'
  | 'unknown'

/**
 * Lifecycle status of an in-app update installation.
 *
 * Present in `UpdateStatus.installStatus` and `InstallStateEvent.installStatus`.
 */
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

/**
 * Reason for an install-state listener event.
 *
 * Includes all {@link UnsupportedReason} values plus listener-specific reasons.
 */
export type InstallStateEventReason =
  | UnsupportedReason
  | 'download-progress'
  | 'install-state-changed'
  | 'flexible-update-downloaded'
  | 'unknown'

/** Platform capabilities reported by the current device and install. */
export type Capabilities = {
  /** Whether immediate update flows are supported. */
  immediate: boolean
  /** Whether flexible update flows are supported. */
  flexible: boolean
  /** Whether opening the store page is supported. */
  storePage: boolean
  /** Whether the latest store version can be looked up. */
  latestVersionLookup: boolean
  /** Whether install-state listeners are available. */
  installStateListener: boolean
}

/** Which update flows are currently allowed to start. */
export type AllowedFlows = {
  /** Whether an immediate update can be started now. */
  immediate: boolean
  /** Whether a flexible update can be started now. */
  flexible: boolean
}

/**
 * Raw details from the Google Play Core in-app updates API.
 *
 * Only present on Android when Play Core is available.
 */
export type PlayCoreDetails = {
  /** Precondition failures for immediate updates. */
  immediateFailedPreconditions?: string[]
  /** Precondition failures for flexible updates. */
  flexibleFailedPreconditions?: string[]
  /** Error code from the last install attempt. */
  installErrorCode?: number
  /** Error code from the last Play Core task. */
  taskErrorCode?: number
  /** Update availability status from Play Core. */
  updateAvailability?: string
  /** Current install status from Play Core. */
  installStatus?: string
  /** Update priority set by the developer in the Play Console (0-5). */
  updatePriority?: number
  /** Days the installed version has been stale according to Play Core. */
  clientVersionStalenessDays?: number
  /** Version code of the available update. */
  availableVersionCode?: number
  /** Bytes downloaded so far for a flexible update. */
  bytesDownloaded?: number
  /** Total bytes to download for the update. */
  totalBytesToDownload?: number
  /** Whether immediate update is allowed by Play Core. */
  immediateAllowed?: boolean
  /** Whether flexible update is allowed by Play Core. */
  flexibleAllowed?: boolean
}

/** Android-specific details included in update status and events. */
export type AndroidDetails = {
  /** The Android package name. */
  packageName?: string
  /** Raw Play Core details, if available. */
  playCore?: PlayCoreDetails
}

/**
 * Details from the Apple App Store lookup.
 *
 * Only present on iOS when a store lookup has been performed.
 */
export type IosAppStoreDetails = {
  /** Version string returned by the App Store lookup. */
  version?: string
  /** App Store URL for the app. */
  trackViewUrl?: string
  /** App name from the store. */
  trackName?: string
  /** Release notes from the store. */
  releaseNotes?: string
  /** App description from the store. */
  description?: string
  /** Minimum iOS version required. */
  minimumOsVersion?: string
  /** Average user rating on the store. */
  averageUserRating?: number
  /** Total number of user ratings. */
  userRatingCount?: number
  /** Small app icon URL (60x60). */
  artworkUrl60?: string
  /** Medium app icon URL (100x100). */
  artworkUrl100?: string
  /** Large app icon URL (512x512). */
  artworkUrl512?: string
}

/** iOS-specific details included in update status. */
export type IosDetails = {
  /** The iOS bundle identifier. */
  bundleIdentifier?: string
  /** The App Store ID used for lookups, if provided. */
  appStoreId?: string
  /** The App Store URL, if resolved. */
  storeUrl?: string
  /** App Store lookup result, if available. */
  appStore?: IosAppStoreDetails
}

/**
 * The result of checking update status.
 *
 * Returned by `getUpdateStatus()`, `startImmediateUpdate()`,
 * `startFlexibleUpdate()`, and `completeFlexibleUpdate()`.
 */
export type UpdateStatus = {
  /** The host platform. */
  platform: Platform
  /** Whether in-app updates are supported on this device and install. */
  supported: boolean
  /** Whether a newer version is available in the store. `null` if unknown. */
  updateAvailable: boolean | null
  /** Capabilities reported by the current platform. */
  capabilities: Capabilities
  /** Which update flows are currently allowed to start. */
  allowed: AllowedFlows
  /** The reason describing the current availability or unsupported state. */
  reason: AvailabilityReason | UnsupportedReason
  /** The currently installed version string, if known. */
  currentVersion?: string
  /** The currently installed build number or version code, if known. */
  currentBuild?: string | number
  /** The latest version available in the store, if known. */
  latestStoreVersion?: string
  /** The latest build number or version code in the store, if known. */
  latestStoreBuild?: string | number
  /** The current install lifecycle status, if reported by the platform. */
  installStatus?: InstallStatus
  /** Android-specific details. Only present on Android. */
  android?: AndroidDetails
  /** iOS-specific details. Only present on iOS. */
  ios?: IosDetails
}

/** Event emitted by an install-state listener. */
export type InstallStateEvent = {
  /** The host platform. */
  platform: Platform
  /** Whether in-app updates are supported on this device and install. */
  supported: boolean
  /** The current install lifecycle status. */
  installStatus: InstallStatus
  /** The reason for this event. */
  reason: InstallStateEventReason
  /** Bytes downloaded so far, if reported. */
  bytesDownloaded?: number
  /** Total bytes to download, if reported. */
  totalBytesToDownload?: number
  /** Download progress as a fraction from 0 to 1, if computable. */
  progress?: number
  /** Error code string, if the event reports an error. */
  errorCode?: string
  /** Human-readable message, if provided by the platform. */
  message?: string
  /** Android-specific details. Only present on Android. */
  android?: AndroidDetails
}

/** Android-specific options for update flows. */
export type AndroidUpdateOptions = {
  /** Allow Play Core to delete asset packs to make room for the update. */
  allowAssetPackDeletion?: boolean
}

/** Options for `getUpdateStatus()`. */
export type GetUpdateStatusOptions = {
  ios?: {
    /** The App Store ID for iOS store lookups. */
    appStoreId?: string
    /** Two-letter country code for the store lookup. */
    country?: string
  }
  /** Android-specific options. */
  android?: AndroidUpdateOptions
}

/** Options for `startImmediateUpdate()`. */
export type StartImmediateUpdateOptions = {
  /** Android-specific options. */
  android?: AndroidUpdateOptions
}

/** Options for `startFlexibleUpdate()`. */
export type StartFlexibleUpdateOptions = {
  /** Android-specific options. */
  android?: AndroidUpdateOptions
}

/** Options for `openStorePage()`. */
export type OpenStorePageOptions = {
  ios?: {
    /** The App Store ID (digits-only). Required on iOS. */
    appStoreId: string
    /** Two-letter country code for the store URL. */
    country?: string
  }
}

/**
 * Error codes for {@link InAppUpdatesError}.
 *
 * - `invalid-input` — invalid arguments passed to an API function
 * - `bridge-error` — React Native bridge communication failure
 * - `native-error` — unexpected error from the native layer
 * - `unexpected` — any other unexpected error
 */
export type InAppUpdatesErrorCode =
  | 'invalid-input'
  | 'bridge-error'
  | 'native-error'
  | 'unexpected'

/**
 * Error thrown by in-app update API functions.
 *
 * Invalid input, bridge failures, native failures, and unexpected failures
 * are normalized into this class.
 */
export class InAppUpdatesError extends Error {
  /** The error code categorizing the failure. */
  readonly code: InAppUpdatesErrorCode
  /** Android-specific details, if the error originated on Android. */
  readonly android?: AndroidDetails

  /**
   * @param message - Human-readable error description.
   * @param code - The error code categorizing the failure.
   * @param android - Android-specific details, if available.
   */
  constructor(message: string, code: InAppUpdatesErrorCode, android?: AndroidDetails) {
    super(message)
    this.code = code
    this.android = android
    this.name = 'InAppUpdatesError'
  }
}
