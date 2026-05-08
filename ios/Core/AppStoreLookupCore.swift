import Foundation

struct AppStoreLookupMetadata: Equatable {
    let wrapperType: String?
    let kind: String?
    let trackId: Int?
    let bundleId: String?
    let version: String?
    let trackViewUrl: String?
    let trackName: String?
    let artistName: String?
    let sellerName: String?
    let primaryGenreName: String?
    let releaseNotes: String?
    let description: String?
    let minimumOsVersion: String?
    let fileSizeBytes: String?
    let price: Double?
    let formattedPrice: String?
    let currency: String?
    let averageUserRating: Double?
    let userRatingCount: Int?
    let artworkUrl60: String?
    let artworkUrl100: String?
    let artworkUrl512: String?
}

enum AppStoreLookupValidation: Equatable {
    case valid(appStoreId: String, country: String?)
    case invalidAppStoreId
    case invalidCountry
}

enum AppStoreLookupParseResult: Equatable {
    case metadata(AppStoreLookupMetadata)
    case noResult
    case malformedJSON
}

enum AppStoreLookupCore {
    static func validateLookupInput(appStoreId: String, country: String?) -> AppStoreLookupValidation {
        guard isDigitsOnly(appStoreId) else {
            return .invalidAppStoreId
        }

        guard let country else {
            return .valid(appStoreId: appStoreId, country: nil)
        }

        let normalized = country.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
        guard normalized.count == 2, normalized.unicodeScalars.allSatisfy({ CharacterSet.letters.contains($0) }) else {
            return .invalidCountry
        }

        return .valid(appStoreId: appStoreId, country: normalized)
    }

    static func lookupURL(appStoreId: String, country: String? = nil) -> URL? {
        switch validateLookupInput(appStoreId: appStoreId, country: country) {
        case .valid(let validAppStoreId, let validCountry):
            var components = URLComponents()
            components.scheme = "https"
            components.host = "itunes.apple.com"
            components.path = "/lookup"

            var queryItems = [
                URLQueryItem(name: "id", value: validAppStoreId),
                URLQueryItem(name: "entity", value: "software")
            ]
            if let validCountry {
                queryItems.append(URLQueryItem(name: "country", value: validCountry))
            }

            components.queryItems = queryItems
            return components.url
        case .invalidAppStoreId, .invalidCountry:
            return nil
        }
    }

    static func storePageURL(appStoreId: String, country: String? = nil) -> URL? {
        guard isDigitsOnly(appStoreId) else {
            return nil
        }

        if let country = country {
            let normalized = country.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
            guard normalized.count == 2, normalized.unicodeScalars.allSatisfy({ CharacterSet.letters.contains($0) }) else {
                return nil
            }
            return URL(string: "https://apps.apple.com/\(normalized)/app/id\(appStoreId)")
        }

        return URL(string: "https://apps.apple.com/app/id\(appStoreId)")
    }

    static func parseLookupResult(data: Data) -> AppStoreLookupParseResult {
        let decoder = JSONDecoder()

        guard let envelope = try? decoder.decode(AppStoreLookupEnvelope.self, from: data) else {
            return .malformedJSON
        }

        guard envelope.resultCount > 0, let entry = envelope.results.first else {
            return .noResult
        }

        return .metadata(AppStoreLookupMetadata(
            wrapperType: entry.wrapperType,
            kind: entry.kind,
            trackId: entry.trackId,
            bundleId: entry.bundleId,
            version: entry.version,
            trackViewUrl: entry.trackViewUrl,
            trackName: entry.trackName,
            artistName: entry.artistName,
            sellerName: entry.sellerName,
            primaryGenreName: entry.primaryGenreName,
            releaseNotes: entry.releaseNotes,
            description: entry.description,
            minimumOsVersion: entry.minimumOsVersion,
            fileSizeBytes: entry.fileSizeBytes,
            price: entry.price,
            formattedPrice: entry.formattedPrice,
            currency: entry.currency,
            averageUserRating: entry.averageUserRating,
            userRatingCount: entry.userRatingCount,
            artworkUrl60: entry.artworkUrl60,
            artworkUrl100: entry.artworkUrl100,
            artworkUrl512: entry.artworkUrl512
        ))
    }

    static func compareDottedNumericVersions(_ lhs: String, _ rhs: String) -> ComparisonResult? {
        guard let left = dottedNumericComponents(lhs), let right = dottedNumericComponents(rhs) else {
            return nil
        }

        let length = max(left.count, right.count)

        for index in 0..<length {
            let leftValue = index < left.count ? left[index] : 0
            let rightValue = index < right.count ? right[index] : 0

            if leftValue == rightValue {
                continue
            }

            return leftValue < rightValue ? .orderedAscending : .orderedDescending
        }

        return .orderedSame
    }

    private static func dottedNumericComponents(_ value: String) -> [Int]? {
        let rawComponents = value.split(separator: ".", omittingEmptySubsequences: false)
        guard !rawComponents.isEmpty else {
            return nil
        }

        var normalized: [Int] = []
        normalized.reserveCapacity(rawComponents.count)

        for component in rawComponents {
            guard !component.isEmpty, component.allSatisfy({ $0.isNumber }) else {
                return nil
            }

            let trimmed = String(component).drop(while: { $0 == "0" })
            let normalizedComponent = Int(trimmed.isEmpty ? "0" : String(trimmed))
            guard let normalizedComponent else {
                return nil
            }
            normalized.append(normalizedComponent)
        }

        return normalized
    }

    private static func isDigitsOnly(_ value: String) -> Bool {
        !value.isEmpty && value.allSatisfy { $0.isNumber }
    }
}

private struct AppStoreLookupEnvelope: Decodable {
    let resultCount: Int
    let results: [AppStoreLookupEntry]
}

private struct AppStoreLookupEntry: Decodable {
    let wrapperType: String?
    let kind: String?
    let trackId: Int?
    let bundleId: String?
    let version: String?
    let trackViewUrl: String?
    let trackName: String?
    let artistName: String?
    let sellerName: String?
    let primaryGenreName: String?
    let releaseNotes: String?
    let description: String?
    let minimumOsVersion: String?
    let fileSizeBytes: String?
    let price: Double?
    let formattedPrice: String?
    let currency: String?
    let averageUserRating: Double?
    let userRatingCount: Int?
    let artworkUrl60: String?
    let artworkUrl100: String?
    let artworkUrl512: String?
}
