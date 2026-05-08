import Foundation
import UIKit
import NitroModules

enum AppStoreLookupSupport {
    static func makeStatus(
        reason: String,
        appStoreId: String? = nil,
        storeUrl: String? = nil,
        storePage: Bool
    ) -> UpdateStatusNative {
        let iosDetails = appStoreId.map {
            IosDetailsNative(
                bundleIdentifier: Bundle.main.bundleIdentifier,
                appStoreId: $0,
                storeUrl: storeUrl,
                appStore: nil
            )
        }

        return UpdateStatusNative(
            platform: "ios",
            supported: false,
            updateAvailable: .first(NullType.null),
            capabilities: CapabilitiesNative(
                immediate: false,
                flexible: false,
                storePage: storePage,
                latestVersionLookup: false,
                installStateListener: false
            ),
            allowed: AllowedFlowsNative(
                immediate: false,
                flexible: false
            ),
            reason: reason,
            ios: iosDetails
        )
    }

    static func makeMissingAppStoreIdStatus() -> UpdateStatusNative {
        makeStatus(reason: "missing-app-store-id", storePage: false)
    }

    static func makeLookupUnavailableStatus(appStoreId: String) -> UpdateStatusNative {
        makeStatus(reason: "store-lookup-unavailable", appStoreId: appStoreId, storePage: true)
    }

    static func makeLookupFailedStatus(appStoreId: String, country: String? = nil) -> UpdateStatusNative {
        let storeUrl = storePageURL(appStoreId: appStoreId, country: country)?.absoluteString
        return makeStatus(reason: "store-lookup-unavailable", appStoreId: appStoreId, storeUrl: storeUrl, storePage: true)
    }

    static func makeSuccessStatus(metadata: AppStoreLookupMetadata, currentVersion: String?, appStoreId: String) -> UpdateStatusNative {
        let (updateAvailable, reason) = determineUpdateAvailability(
            currentVersion: currentVersion,
            latestVersion: metadata.version,
            minimumOsVersion: metadata.minimumOsVersion
        )

        let appStoreDetails = IosAppStoreDetailsNative(
            version: metadata.version,
            trackViewUrl: metadata.trackViewUrl,
            trackName: metadata.trackName,
            releaseNotes: metadata.releaseNotes,
            description: metadata.description,
            minimumOsVersion: metadata.minimumOsVersion,
            averageUserRating: metadata.averageUserRating,
            userRatingCount: metadata.userRatingCount.map { Double($0) },
            artworkUrl60: metadata.artworkUrl60,
            artworkUrl100: metadata.artworkUrl100,
            artworkUrl512: metadata.artworkUrl512
        )

        let iosDetails = IosDetailsNative(
            bundleIdentifier: Bundle.main.bundleIdentifier,
            appStoreId: appStoreId,
            storeUrl: metadata.trackViewUrl,
            appStore: appStoreDetails
        )

        return UpdateStatusNative(
            platform: "ios",
            supported: true,
            updateAvailable: updateAvailable,
            capabilities: CapabilitiesNative(
                immediate: false,
                flexible: false,
                storePage: true,
                latestVersionLookup: true,
                installStateListener: false
            ),
            allowed: AllowedFlowsNative(
                immediate: false,
                flexible: false
            ),
            reason: reason,
            currentVersion: currentVersion,
            latestStoreVersion: metadata.version,
            ios: iosDetails
        )
    }

    private static func determineUpdateAvailability(
        currentVersion: String?,
        latestVersion: String?,
        minimumOsVersion: String?
    ) -> (Variant_NullType_Bool, String) {
        // Check minimum OS first, before version comparison
        if let minimumOsVersion = minimumOsVersion {
            let currentOS = UIDevice.current.systemVersion
            if let osComparison = AppStoreLookupCore.compareDottedNumericVersions(currentOS, minimumOsVersion) {
                if osComparison == .orderedAscending {
                    return (.first(NullType.null), "update-not-allowed")
                }
            } else {
                // Ambiguous OS comparison → conservative
                return (.first(NullType.null), "update-not-allowed")
            }
        }

        if let currentVersion = currentVersion, let latestVersion = latestVersion {
            if let comparison = AppStoreLookupCore.compareDottedNumericVersions(currentVersion, latestVersion) {
                switch comparison {
                case .orderedAscending:
                    return (.second(true), "update-available")
                case .orderedSame, .orderedDescending:
                    return (.second(false), "no-update-available")
                @unknown default:
                    return (.first(NullType.null), "update-not-allowed")
                }
            } else {
                return (.first(NullType.null), "update-not-allowed")
            }
        } else {
            return (.first(NullType.null), "update-not-allowed")
        }
    }

    static func lookupURL(appStoreId: String, country: String? = nil) -> URL? {
        AppStoreLookupCore.lookupURL(appStoreId: appStoreId, country: country)
    }

    static func storePageURL(appStoreId: String, country: String? = nil) -> URL? {
        AppStoreLookupCore.storePageURL(appStoreId: appStoreId, country: country)
    }

    static func parseLookupMetadata(data: Data) -> AppStoreLookupMetadata? {
        switch AppStoreLookupCore.parseLookupResult(data: data) {
        case .metadata(let metadata):
            return metadata
        case .noResult, .malformedJSON:
            return nil
        }
    }

    static func compareDottedNumericVersions(_ lhs: String, _ rhs: String) -> ComparisonResult? {
        AppStoreLookupCore.compareDottedNumericVersions(lhs, rhs)
    }

    static func validateLookupInput(appStoreId: String, country: String?) -> AppStoreLookupValidation {
        AppStoreLookupCore.validateLookupInput(appStoreId: appStoreId, country: country)
    }
}
