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
        let fields = AppStoreLookupCore.makeFallbackStatusFields(
            reason: reason,
            installed: currentInstalledAppMetadata(),
            appStoreId: appStoreId,
            storeUrl: storeUrl,
            storePage: storePage
        )

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
            reason: fields.reason,
            currentVersion: fields.currentVersion,
            currentBuild: fields.currentBuild.map { .first($0) },
            latestStoreVersion: fields.latestStoreVersion,
            latestStoreBuild: fields.latestStoreBuild.map { .first($0) },
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
        let installed = AppStoreInstalledAppMetadata(
            currentVersion: currentVersion,
            currentBuild: currentAppBuild()
        )
        let fields = AppStoreLookupCore.makeSuccessStatusFields(
            metadata: metadata,
            installed: installed,
            appStoreId: appStoreId,
            currentOSVersion: UIDevice.current.systemVersion
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
            supported: fields.supported,
            updateAvailable: fields.updateAvailable.map { .second($0) } ?? .first(NullType.null),
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
            reason: fields.reason,
            currentVersion: fields.currentVersion,
            currentBuild: fields.currentBuild.map { .first($0) },
            latestStoreVersion: fields.latestStoreVersion,
            latestStoreBuild: fields.latestStoreBuild.map { .first($0) },
            installStatus: nil,
            android: nil,
            ios: iosDetails
        )
    }

    static func lookupURL(appStoreId: String, country: String? = nil) -> URL? {
        AppStoreLookupCore.lookupURL(appStoreId: appStoreId, country: country)
    }

    static func currentAppVersion() -> String? {
        Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String
    }

    static func currentAppBuild() -> String? {
        Bundle.main.infoDictionary?["CFBundleVersion"] as? String
    }

    static func currentInstalledAppMetadata() -> AppStoreInstalledAppMetadata {
        AppStoreInstalledAppMetadata.from(infoDictionary: Bundle.main.infoDictionary)
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
