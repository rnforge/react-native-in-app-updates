import Foundation
import NitroModules

enum AppStoreLookupSupport {
    static func makeStatus(
        reason: String,
        appStoreId: String? = nil,
        storePage: Bool
    ) -> UpdateStatusNative {
        let iosDetails = appStoreId.map {
            IosDetailsNative(
                bundleIdentifier: Bundle.main.bundleIdentifier,
                appStoreId: $0,
                storeUrl: nil
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

    static func lookupURL(appStoreId: String, country: String? = nil) -> URL? {
        AppStoreLookupCore.lookupURL(appStoreId: appStoreId, country: country)
    }

    static func storePageURL(appStoreId: String) -> URL? {
        AppStoreLookupCore.storePageURL(appStoreId: appStoreId)
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
