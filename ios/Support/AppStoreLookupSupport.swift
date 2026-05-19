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
            currentVersion: nil,
            currentBuild: nil,
            latestStoreVersion: nil,
            latestStoreBuild: nil,
            installStatus: nil,
            android: nil,
            ios: iosDetails
        )
    }

    static func makeMissingAppStoreIdStatus() -> UpdateStatusNative {
        makeStatus(reason: "missing-app-store-id", storePage: false)
    }

    static func makeLookupUnavailableStatus(appStoreId: String) -> UpdateStatusNative {
        makeStatus(reason: "store-lookup-unavailable", appStoreId: appStoreId, storePage: true)
    }

    static func makeLookupFailedStatus(reason: String = "store-lookup-unavailable", appStoreId: String, country: String? = nil) -> UpdateStatusNative {
        let storeUrl = storePageURL(appStoreId: appStoreId, country: country)?.absoluteString
        return makeStatus(reason: reason, appStoreId: appStoreId, storeUrl: storeUrl, storePage: true)
    }

    static func makeLookupFailedStatus(error: LookupHTTPError, appStoreId: String, country: String? = nil) -> UpdateStatusNative {
        makeLookupFailedStatus(reason: reason(for: error), appStoreId: appStoreId, country: country)
    }

    static func makeLookupFailedStatus(parseResult: AppStoreLookupParseResult, appStoreId: String, country: String? = nil) -> UpdateStatusNative {
        makeLookupFailedStatus(reason: reason(for: parseResult), appStoreId: appStoreId, country: country)
    }

    static func makeSuccessStatus(metadata: AppStoreLookupMetadata, currentVersion: String?, appStoreId: String) -> UpdateStatusNative {
        let (supported, updateAvailable, reason) = determineUpdateAvailability(
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

        let currentBuild = Bundle.main.infoDictionary?["CFBundleVersion"] as? String

        return UpdateStatusNative(
            platform: "ios",
            supported: supported,
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
            currentBuild: currentBuild.map { .first($0) },
            latestStoreVersion: metadata.version,
            latestStoreBuild: nil,
            installStatus: nil,
            android: nil,
            ios: iosDetails
        )
    }

    private static func determineUpdateAvailability(
        currentVersion: String?,
        latestVersion: String?,
        minimumOsVersion: String?
    ) -> (Bool, Variant_NullType_Bool, String) {
        // Check minimum OS first, before version comparison
        if let minimumOsVersion = minimumOsVersion {
            let currentOS = UIDevice.current.systemVersion
            if let osComparison = AppStoreLookupCore.compareDottedNumericVersions(currentOS, minimumOsVersion) {
                if osComparison == .orderedAscending {
                    return (false, .first(NullType.null), "unsupported-os-version")
                }
            } else {
                // Ambiguous OS comparison → conservative
                return (true, .first(NullType.null), "update-not-allowed")
            }
        }

        if let currentVersion = currentVersion, let latestVersion = latestVersion {
            if let comparison = AppStoreLookupCore.compareDottedNumericVersions(currentVersion, latestVersion) {
                switch comparison {
                case .orderedAscending:
                    return (true, .second(true), "update-available")
                case .orderedSame, .orderedDescending:
                    return (true, .second(false), "no-update-available")
                @unknown default:
                    return (true, .first(NullType.null), "update-not-allowed")
                }
            } else {
                return (true, .first(NullType.null), "update-not-allowed")
            }
        } else {
            return (true, .first(NullType.null), "update-not-allowed")
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

    static func parseLookupResult(data: Data) -> AppStoreLookupParseResult {
        AppStoreLookupCore.parseLookupResult(data: data)
    }

    static func compareDottedNumericVersions(_ lhs: String, _ rhs: String) -> ComparisonResult? {
        AppStoreLookupCore.compareDottedNumericVersions(lhs, rhs)
    }

    static func validateLookupInput(appStoreId: String, country: String?) -> AppStoreLookupValidation {
        AppStoreLookupCore.validateLookupInput(appStoreId: appStoreId, country: country)
    }

    private static func reason(for error: LookupHTTPError) -> String {
        switch error {
        case .timeout:
            return "store-lookup-timeout"
        case .networkError:
            return "store-lookup-network-error"
        case .noData:
            return "store-lookup-invalid-response"
        case .httpError:
            return "store-lookup-http-error"
        }
    }

    private static func reason(for parseResult: AppStoreLookupParseResult) -> String {
        switch parseResult {
        case .metadata:
            return "store-lookup-unavailable"
        case .noResult:
            return "store-lookup-not-found"
        case .malformedJSON:
            return "store-lookup-invalid-response"
        }
    }
}
