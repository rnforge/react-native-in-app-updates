import XCTest
@testable import AppStoreLookupCore

final class AppStoreLookupStatusCoreTests: XCTestCase {
    private let installed = AppStoreInstalledAppMetadata(
        currentVersion: "1.2.3",
        currentBuild: "45"
    )

    func testInstalledMetadataReadsBundleVersionFields() {
        let metadata = AppStoreInstalledAppMetadata.from(infoDictionary: [
            "CFBundleShortVersionString": "2.3.4",
            "CFBundleVersion": "56"
        ])

        XCTAssertEqual(metadata.currentVersion, "2.3.4")
        XCTAssertEqual(metadata.currentBuild, "56")
    }

    func testMissingAppStoreIdFallbackIncludesInstalledVersionAndBuild() {
        let status = AppStoreLookupCore.makeFallbackStatusFields(
            reason: "missing-app-store-id",
            installed: installed,
            storePage: false
        )

        XCTAssertFalse(status.supported)
        XCTAssertNil(status.updateAvailable)
        XCTAssertEqual(status.reason, "missing-app-store-id")
        XCTAssertEqual(status.currentVersion, "1.2.3")
        XCTAssertEqual(status.currentBuild, "45")
        XCTAssertNil(status.latestStoreVersion)
        XCTAssertNil(status.latestStoreBuild)
        XCTAssertFalse(status.storePage)
        XCTAssertFalse(status.latestVersionLookup)
        XCTAssertNil(status.appStoreId)
        XCTAssertNil(status.storeUrl)
    }

    func testLookupFailureFallbackIncludesInstalledVersionBuildAndStorePageUrl() {
        let status = AppStoreLookupCore.makeFallbackStatusFields(
            reason: "store-lookup-network-error",
            installed: installed,
            appStoreId: "1234567890",
            storeUrl: "https://apps.apple.com/app/id1234567890",
            storePage: true
        )

        XCTAssertFalse(status.supported)
        XCTAssertNil(status.updateAvailable)
        XCTAssertEqual(status.reason, "store-lookup-network-error")
        XCTAssertEqual(status.currentVersion, "1.2.3")
        XCTAssertEqual(status.currentBuild, "45")
        XCTAssertNil(status.latestStoreVersion)
        XCTAssertNil(status.latestStoreBuild)
        XCTAssertTrue(status.storePage)
        XCTAssertFalse(status.latestVersionLookup)
        XCTAssertEqual(status.appStoreId, "1234567890")
        XCTAssertEqual(status.storeUrl, "https://apps.apple.com/app/id1234567890")
    }

    func testUnsupportedPlatformFallbackIncludesInstalledVersionAndBuild() {
        let status = AppStoreLookupCore.makeFallbackStatusFields(
            reason: "unsupported-platform",
            installed: installed,
            storePage: false
        )

        XCTAssertFalse(status.supported)
        XCTAssertNil(status.updateAvailable)
        XCTAssertEqual(status.reason, "unsupported-platform")
        XCTAssertEqual(status.currentVersion, "1.2.3")
        XCTAssertEqual(status.currentBuild, "45")
        XCTAssertNil(status.latestStoreVersion)
        XCTAssertNil(status.latestStoreBuild)
        XCTAssertFalse(status.storePage)
        XCTAssertFalse(status.latestVersionLookup)
    }

    func testSuccessStatusIncludesInstalledAndStoreVersionButNoStoreBuild() {
        let metadata = makeMetadata(version: "2.0.0", minimumOsVersion: "15.0")

        let status = AppStoreLookupCore.makeSuccessStatusFields(
            metadata: metadata,
            installed: installed,
            appStoreId: "1234567890",
            currentOSVersion: "17.0"
        )

        XCTAssertTrue(status.supported)
        XCTAssertEqual(status.updateAvailable, true)
        XCTAssertEqual(status.reason, "update-available")
        XCTAssertEqual(status.currentVersion, "1.2.3")
        XCTAssertEqual(status.currentBuild, "45")
        XCTAssertEqual(status.latestStoreVersion, "2.0.0")
        XCTAssertNil(status.latestStoreBuild)
        XCTAssertTrue(status.storePage)
        XCTAssertTrue(status.latestVersionLookup)
        XCTAssertEqual(status.appStoreId, "1234567890")
        XCTAssertEqual(status.storeUrl, "https://apps.apple.com/app/id1234567890")
    }

    func testSuccessStatusMapsMinimumOsVersionToUnsupportedOsVersion() {
        let metadata = makeMetadata(version: "2.0.0", minimumOsVersion: "18.0")

        let status = AppStoreLookupCore.makeSuccessStatusFields(
            metadata: metadata,
            installed: installed,
            appStoreId: "1234567890",
            currentOSVersion: "17.0"
        )

        XCTAssertFalse(status.supported)
        XCTAssertNil(status.updateAvailable)
        XCTAssertEqual(status.reason, "unsupported-os-version")
        XCTAssertEqual(status.currentVersion, "1.2.3")
        XCTAssertEqual(status.currentBuild, "45")
        XCTAssertEqual(status.latestStoreVersion, "2.0.0")
        XCTAssertNil(status.latestStoreBuild)
    }

    func testSuccessStatusWithoutComparableVersionKeepsInstalledFieldsAndUnknownAvailability() {
        let metadata = makeMetadata(version: "2.0.0", minimumOsVersion: nil)
        let installed = AppStoreInstalledAppMetadata(
            currentVersion: "1.2-beta",
            currentBuild: "45"
        )

        let status = AppStoreLookupCore.makeSuccessStatusFields(
            metadata: metadata,
            installed: installed,
            appStoreId: "1234567890",
            currentOSVersion: "17.0"
        )

        XCTAssertTrue(status.supported)
        XCTAssertNil(status.updateAvailable)
        XCTAssertEqual(status.reason, "update-not-allowed")
        XCTAssertEqual(status.currentVersion, "1.2-beta")
        XCTAssertEqual(status.currentBuild, "45")
        XCTAssertEqual(status.latestStoreVersion, "2.0.0")
        XCTAssertNil(status.latestStoreBuild)
    }

    private func makeMetadata(
        version: String?,
        minimumOsVersion: String?
    ) -> AppStoreLookupMetadata {
        AppStoreLookupMetadata(
            wrapperType: "software",
            kind: "software",
            trackId: 1234567890,
            bundleId: "dev.rnforge.example",
            version: version,
            trackViewUrl: "https://apps.apple.com/app/id1234567890",
            trackName: "RNForge Example",
            artistName: nil,
            sellerName: nil,
            primaryGenreName: nil,
            releaseNotes: nil,
            description: nil,
            minimumOsVersion: minimumOsVersion,
            fileSizeBytes: nil,
            price: nil,
            formattedPrice: nil,
            currency: nil,
            averageUserRating: nil,
            userRatingCount: nil,
            artworkUrl60: nil,
            artworkUrl100: nil,
            artworkUrl512: nil
        )
    }
}
