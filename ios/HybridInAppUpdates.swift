//
//  HybridInAppUpdates.swift
//  Pods
//
//  Created by RNForge on 5/5/2026.
//

import Foundation
import UIKit
import NitroModules

class HybridInAppUpdates: HybridInAppUpdatesSpec {
    func getUpdateStatus(options: GetUpdateStatusOptionsNative?) throws -> Promise<UpdateStatusNative> {
        let promise = Promise<UpdateStatusNative>()
        let appStoreId = options?.ios?.appStoreId
        let country = options?.ios?.country

        guard let appStoreId else {
            promise.resolve(withResult: AppStoreLookupSupport.makeMissingAppStoreIdStatus())
            return promise
        }

        let validation = AppStoreLookupSupport.validateLookupInput(appStoreId: appStoreId, country: country)
        switch validation {
        case .invalidAppStoreId:
            let message = "Invalid appStoreId: must be digits-only"
            let encoded = message.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? message
            promise.reject(withError: NSError(domain: "InAppUpdates", code: 100, userInfo: [NSLocalizedDescriptionKey: "INVALID_INPUT|message=\(encoded)"]))
            return promise
        case .invalidCountry:
            let message = "Invalid country: must be two-letter code"
            let encoded = message.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? message
            promise.reject(withError: NSError(domain: "InAppUpdates", code: 101, userInfo: [NSLocalizedDescriptionKey: "INVALID_INPUT|message=\(encoded)"]))
            return promise
        case .valid:
            break
        }

        guard let url = AppStoreLookupSupport.lookupURL(appStoreId: appStoreId, country: country) else {
            promise.resolve(withResult: AppStoreLookupSupport.makeLookupFailedStatus(appStoreId: appStoreId, country: country))
            return promise
        }

        let client = AppStoreLookupHTTPClient()
        client.performLookup(url: url) { result in
            DispatchQueue.main.async {
                switch result {
                case .success(let data):
                    guard let metadata = AppStoreLookupSupport.parseLookupMetadata(data: data) else {
                        promise.resolve(withResult: AppStoreLookupSupport.makeLookupFailedStatus(appStoreId: appStoreId, country: country))
                        return
                    }

                    let currentVersion = Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String
                    promise.resolve(withResult: AppStoreLookupSupport.makeSuccessStatus(metadata: metadata, currentVersion: currentVersion, appStoreId: appStoreId))

                case .failure:
                    promise.resolve(withResult: AppStoreLookupSupport.makeLookupFailedStatus(appStoreId: appStoreId, country: country))
                }
            }
        }
        return promise
    }

    func startImmediateUpdate(options: StartImmediateUpdateOptionsNative?) throws -> Promise<UpdateStatusNative> {
        return Promise.resolved(withResult: AppStoreLookupSupport.makeStatus(reason: "unsupported-platform", storePage: false))
    }

    func startFlexibleUpdate(options: StartFlexibleUpdateOptionsNative?) throws -> Promise<UpdateStatusNative> {
        return Promise.resolved(withResult: AppStoreLookupSupport.makeStatus(reason: "unsupported-platform", storePage: false))
    }

    func completeFlexibleUpdate() throws -> Promise<UpdateStatusNative> {
        return Promise.resolved(withResult: AppStoreLookupSupport.makeStatus(reason: "unsupported-platform", storePage: false))
    }

    func openStorePage(options: OpenStorePageOptionsNative?) throws -> Promise<Void> {
        let promise = Promise<Void>()
        let appStoreId = options?.ios?.appStoreId
        let country = options?.ios?.country

        guard let appStoreId, !appStoreId.isEmpty else {
            let message = "Missing ios.appStoreId for openStorePage()"
            let encoded = message.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? message
            promise.reject(withError: NSError(domain: "InAppUpdates", code: 2, userInfo: [NSLocalizedDescriptionKey: "INVALID_INPUT|message=\(encoded)"]))
            return promise
        }

        let validation = AppStoreLookupSupport.validateLookupInput(appStoreId: appStoreId, country: country)
        switch validation {
        case .invalidAppStoreId:
            let message = "Invalid ios.appStoreId: must be digits-only"
            let encoded = message.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? message
            promise.reject(withError: NSError(domain: "InAppUpdates", code: 3, userInfo: [NSLocalizedDescriptionKey: "INVALID_INPUT|message=\(encoded)"]))
            return promise
        case .invalidCountry:
            let message = "Invalid ios.country: must be two-letter code"
            let encoded = message.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? message
            promise.reject(withError: NSError(domain: "InAppUpdates", code: 4, userInfo: [NSLocalizedDescriptionKey: "INVALID_INPUT|message=\(encoded)"]))
            return promise
        case .valid:
            break
        }

        guard let url = AppStoreLookupSupport.storePageURL(appStoreId: appStoreId, country: country) else {
            let message = "Failed to build App Store URL"
            let encoded = message.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? message
            promise.reject(withError: NSError(domain: "InAppUpdates", code: 5, userInfo: [NSLocalizedDescriptionKey: "INVALID_INPUT|message=\(encoded)"]))
            return promise
        }

        DispatchQueue.main.async {
            UIApplication.shared.open(url, options: [:]) { success in
                if success {
                    promise.resolve()
                } else {
                    promise.reject(withError: NSError(domain: "InAppUpdates", code: 6, userInfo: [NSLocalizedDescriptionKey: "Failed to open App Store"]))
                }
            }
        }
        return promise
    }

    func addInstallStateListener(listener: @escaping (_ event: InstallStateEventNative) -> Void) throws -> String {
        let listenerId = UUID().uuidString
        let event = InstallStateEventNative(
            platform: "ios",
            supported: false,
            installStatus: "unsupported",
            reason: "unsupported-platform",
            bytesDownloaded: nil,
            totalBytesToDownload: nil,
            progress: nil,
            errorCode: nil,
            message: nil,
            android: nil
        )
        listener(event)
        return listenerId
    }

    func removeInstallStateListener(listenerId: String) throws {
        // No-op: iOS does not support install state listeners.
    }
}
