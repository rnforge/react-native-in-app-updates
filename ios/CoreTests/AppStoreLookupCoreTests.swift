import XCTest
@testable import AppStoreLookupCore

final class AppStoreLookupCoreTests: XCTestCase {
    func testValidLookupURL() {
        let url = AppStoreLookupCore.lookupURL(appStoreId: "1234567890", country: nil)
        XCTAssertEqual(url?.absoluteString, "https://itunes.apple.com/lookup?id=1234567890&entity=software")
    }

    func testValidCountryNormalization() {
        let validation = AppStoreLookupCore.validateLookupInput(appStoreId: "1234567890", country: "US")

        switch validation {
        case .valid(let appStoreId, let country):
            XCTAssertEqual(appStoreId, "1234567890")
            XCTAssertEqual(country, "us")
        default:
            XCTFail("Expected valid validation")
        }
    }

    func testInvalidCountryRejection() {
        let validation = AppStoreLookupCore.validateLookupInput(appStoreId: "1234567890", country: "usa")

        switch validation {
        case .invalidCountry:
            XCTAssertTrue(true)
        default:
            XCTFail("Expected invalid country")
        }
    }

    func testInvalidAppStoreIdRejection() {
        let validation = AppStoreLookupCore.validateLookupInput(appStoreId: "12ab", country: nil)

        switch validation {
        case .invalidAppStoreId:
            XCTAssertTrue(true)
        default:
            XCTFail("Expected invalid appStoreId")
        }
    }

    func testNoResultJSON() {
        let data = "{\"resultCount\":0,\"results\":[]}".data(using: .utf8)!
        XCTAssertEqual(AppStoreLookupCore.parseLookupResult(data: data), .noResult)
    }

    func testMalformedJSON() {
        let data = "not json".data(using: .utf8)!
        XCTAssertEqual(AppStoreLookupCore.parseLookupResult(data: data), .malformedJSON)
    }

    func testDottedNumericVersionComparison() {
        XCTAssertEqual(AppStoreLookupCore.compareDottedNumericVersions("1.2.0", "1.10.0"), .orderedAscending)
        XCTAssertEqual(AppStoreLookupCore.compareDottedNumericVersions("2.0", "1.9.9"), .orderedDescending)
        XCTAssertEqual(AppStoreLookupCore.compareDottedNumericVersions("1.0.0", "1.0"), .orderedSame)
    }

    func testAmbiguousVersionComparison() {
        XCTAssertNil(AppStoreLookupCore.compareDottedNumericVersions("1.2b", "1.2"))
    }
}
