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
        return Promise { resolve, _ in
            let appStoreId = options?.ios?.appStoreId

            if let appStoreId {
                resolve(AppStoreLookupSupport.makeLookupUnavailableStatus(appStoreId: appStoreId))
            } else {
                resolve(AppStoreLookupSupport.makeMissingAppStoreIdStatus())
            }
        }
    }

    func startImmediateUpdate() throws -> Promise<UpdateStatusNative> {
        return Promise { resolve, _ in
            resolve(makeStatus(reason: "unsupported-platform", storePage: false))
        }
    }

    func startFlexibleUpdate() throws -> Promise<UpdateStatusNative> {
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
            guard let appStoreId = options?.ios?.appStoreId else {
                reject(NSError(domain: "InAppUpdates", code: 2, userInfo: [NSLocalizedDescriptionKey: "Missing ios.appStoreId for openStorePage()"]))
                return
            }

            guard let url = AppStoreLookupSupport.storePageURL(appStoreId: appStoreId) else {
                reject(NSError(domain: "InAppUpdates", code: 3, userInfo: [NSLocalizedDescriptionKey: "Invalid appStoreId"]))
                return
            }

            DispatchQueue.main.async {
                UIApplication.shared.open(url, options: [:]) { success in
                    if success {
                        resolve()
                    } else {
                        reject(NSError(domain: "InAppUpdates", code: 4, userInfo: [NSLocalizedDescriptionKey: "Failed to open App Store"]))
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
