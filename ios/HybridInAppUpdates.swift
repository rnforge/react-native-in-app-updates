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
        return Promise { resolve, reject in
            let appStoreId = options?.ios?.appStoreId
            let country = options?.ios?.country

            guard let appStoreId else {
                resolve(AppStoreLookupSupport.makeMissingAppStoreIdStatus())
                return
            }

            let validation = AppStoreLookupSupport.validateLookupInput(appStoreId: appStoreId, country: country)
            switch validation {
            case .invalidAppStoreId:
                let message = "Invalid appStoreId: must be digits-only"
                let encoded = message.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? message
                reject(NSError(domain: "InAppUpdates", code: 100, userInfo: [NSLocalizedDescriptionKey: "INVALID_INPUT|message=\(encoded)"]))
                return
            case .invalidCountry:
                let message = "Invalid country: must be two-letter code"
                let encoded = message.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? message
                reject(NSError(domain: "InAppUpdates", code: 101, userInfo: [NSLocalizedDescriptionKey: "INVALID_INPUT|message=\(encoded)"]))
                return
            case .valid:
                break
            }

            guard let url = AppStoreLookupSupport.lookupURL(appStoreId: appStoreId, country: country) else {
                resolve(AppStoreLookupSupport.makeLookupFailedStatus(appStoreId: appStoreId, country: country))
                return
            }

            let client = AppStoreLookupHTTPClient()
            client.performLookup(url: url) { result in
                DispatchQueue.main.async {
                    switch result {
                    case .success(let data):
                        guard let metadata = AppStoreLookupSupport.parseLookupMetadata(data: data) else {
                            resolve(AppStoreLookupSupport.makeLookupFailedStatus(appStoreId: appStoreId, country: country))
                            return
                        }

                        let currentVersion = Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String
                        resolve(AppStoreLookupSupport.makeSuccessStatus(metadata: metadata, currentVersion: currentVersion, appStoreId: appStoreId))

                    case .failure:
                        resolve(AppStoreLookupSupport.makeLookupFailedStatus(appStoreId: appStoreId, country: country))
                    }
                }
            }
        }
    }

    func startImmediateUpdate(options: StartImmediateUpdateOptionsNative?) throws -> Promise<UpdateStatusNative> {
        return Promise { resolve, _ in
            resolve(makeStatus(reason: "unsupported-platform", storePage: false))
        }
    }

    func startFlexibleUpdate(options: StartFlexibleUpdateOptionsNative?) throws -> Promise<UpdateStatusNative> {
        return Promise { resolve, _ in
            resolve(makeStatus(reason: "unsupported-platform", storePage: false))
        }
    }

    func completeFlexibleUpdate() throws -> Promise<UpdateStatusNative> {
        return Promise { resolve, _ in
            resolve(makeStatus(reason: "unsupported-platform", storePage: false))
        }
    }

    func openStorePage(options: OpenStorePageOptionsNative?) throws -> Promise<Void> {
        return Promise { resolve, reject in
            let appStoreId = options?.ios?.appStoreId
            let country = options?.ios?.country

            guard let appStoreId, !appStoreId.isEmpty else {
                let message = "Missing ios.appStoreId for openStorePage()"
                let encoded = message.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? message
                reject(NSError(domain: "InAppUpdates", code: 2, userInfo: [NSLocalizedDescriptionKey: "INVALID_INPUT|message=\(encoded)"]))
                return
            }

            let validation = AppStoreLookupSupport.validateLookupInput(appStoreId: appStoreId, country: country)
            switch validation {
            case .invalidAppStoreId:
                let message = "Invalid ios.appStoreId: must be digits-only"
                let encoded = message.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? message
                reject(NSError(domain: "InAppUpdates", code: 3, userInfo: [NSLocalizedDescriptionKey: "INVALID_INPUT|message=\(encoded)"]))
                return
            case .invalidCountry:
                let message = "Invalid ios.country: must be two-letter code"
                let encoded = message.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? message
                reject(NSError(domain: "InAppUpdates", code: 4, userInfo: [NSLocalizedDescriptionKey: "INVALID_INPUT|message=\(encoded)"]))
                return
            case .valid:
                break
            }

            guard let url = AppStoreLookupSupport.storePageURL(appStoreId: appStoreId, country: country) else {
                let message = "Failed to build App Store URL"
                let encoded = message.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? message
                reject(NSError(domain: "InAppUpdates", code: 5, userInfo: [NSLocalizedDescriptionKey: "INVALID_INPUT|message=\(encoded)"]))
                return
            }

            DispatchQueue.main.async {
                UIApplication.shared.open(url, options: [:]) { success in
                    if success {
                        resolve()
                    } else {
                        reject(NSError(domain: "InAppUpdates", code: 6, userInfo: [NSLocalizedDescriptionKey: "Failed to open App Store"]))
                    }
                }
            }
        }
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
