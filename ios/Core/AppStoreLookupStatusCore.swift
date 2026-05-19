import Foundation

struct AppStoreInstalledAppMetadata: Equatable {
    let currentVersion: String?
    let currentBuild: String?

    static func from(infoDictionary: [String: Any]?) -> AppStoreInstalledAppMetadata {
        AppStoreInstalledAppMetadata(
            currentVersion: infoDictionary?["CFBundleShortVersionString"] as? String,
            currentBuild: infoDictionary?["CFBundleVersion"] as? String
        )
    }
}

struct AppStoreLookupStatusFields: Equatable {
    let supported: Bool
    let updateAvailable: Bool?
    let reason: String
    let currentVersion: String?
    let currentBuild: String?
    let latestStoreVersion: String?
    let latestStoreBuild: String?
    let storePage: Bool
    let latestVersionLookup: Bool
    let appStoreId: String?
    let storeUrl: String?
}

struct AppStoreLookupAvailabilityDecision: Equatable {
    let supported: Bool
    let updateAvailable: Bool?
    let reason: String
}

extension AppStoreLookupCore {
    static func makeFallbackStatusFields(
        reason: String,
        installed: AppStoreInstalledAppMetadata,
        appStoreId: String? = nil,
        storeUrl: String? = nil,
        storePage: Bool
    ) -> AppStoreLookupStatusFields {
        AppStoreLookupStatusFields(
            supported: false,
            updateAvailable: nil,
            reason: reason,
            currentVersion: installed.currentVersion,
            currentBuild: installed.currentBuild,
            latestStoreVersion: nil,
            latestStoreBuild: nil,
            storePage: storePage,
            latestVersionLookup: false,
            appStoreId: appStoreId,
            storeUrl: storeUrl
        )
    }

    static func makeSuccessStatusFields(
        metadata: AppStoreLookupMetadata,
        installed: AppStoreInstalledAppMetadata,
        appStoreId: String,
        currentOSVersion: String
    ) -> AppStoreLookupStatusFields {
        let decision = determineUpdateAvailability(
            currentVersion: installed.currentVersion,
            latestVersion: metadata.version,
            minimumOsVersion: metadata.minimumOsVersion,
            currentOSVersion: currentOSVersion
        )

        return AppStoreLookupStatusFields(
            supported: decision.supported,
            updateAvailable: decision.updateAvailable,
            reason: decision.reason,
            currentVersion: installed.currentVersion,
            currentBuild: installed.currentBuild,
            latestStoreVersion: metadata.version,
            latestStoreBuild: nil,
            storePage: true,
            latestVersionLookup: true,
            appStoreId: appStoreId,
            storeUrl: metadata.trackViewUrl
        )
    }

    static func determineUpdateAvailability(
        currentVersion: String?,
        latestVersion: String?,
        minimumOsVersion: String?,
        currentOSVersion: String
    ) -> AppStoreLookupAvailabilityDecision {
        if let minimumOsVersion {
            if let osComparison = compareDottedNumericVersions(currentOSVersion, minimumOsVersion) {
                if osComparison == .orderedAscending {
                    return AppStoreLookupAvailabilityDecision(
                        supported: false,
                        updateAvailable: nil,
                        reason: "unsupported-os-version"
                    )
                }
            } else {
                return AppStoreLookupAvailabilityDecision(
                    supported: true,
                    updateAvailable: nil,
                    reason: "update-not-allowed"
                )
            }
        }

        if let currentVersion, let latestVersion {
            if let comparison = compareDottedNumericVersions(currentVersion, latestVersion) {
                switch comparison {
                case .orderedAscending:
                    return AppStoreLookupAvailabilityDecision(
                        supported: true,
                        updateAvailable: true,
                        reason: "update-available"
                    )
                case .orderedSame, .orderedDescending:
                    return AppStoreLookupAvailabilityDecision(
                        supported: true,
                        updateAvailable: false,
                        reason: "no-update-available"
                    )
                @unknown default:
                    return AppStoreLookupAvailabilityDecision(
                        supported: true,
                        updateAvailable: nil,
                        reason: "update-not-allowed"
                    )
                }
            } else {
                return AppStoreLookupAvailabilityDecision(
                    supported: true,
                    updateAvailable: nil,
                    reason: "update-not-allowed"
                )
            }
        } else {
            return AppStoreLookupAvailabilityDecision(
                supported: true,
                updateAvailable: nil,
                reason: "update-not-allowed"
            )
        }
    }
}
