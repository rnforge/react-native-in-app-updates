//
//  HybridInAppUpdates.swift
//  Pods
//
//  Created by RNForge on 5/5/2026.
//

import Foundation
import NitroModules

class HybridInAppUpdates: HybridInAppUpdatesSpec {
    func getUpdateStatus(options: GetUpdateStatusOptionsNative?) throws -> Promise<UpdateStatusNative> {
        return Promise { resolve, reject in
            let appStoreId = options?.ios?.appStoreId
            
            if appStoreId == nil {
                let status = UpdateStatusNative(
                    platform: "ios",
                    supported: false,
                    updateAvailable: .first(NullType.null),
                    capabilities: CapabilitiesNative(
                        immediate: false,
                        flexible: false,
                        storePage: false,
                        latestVersionLookup: false,
                        installStateListener: false
                    ),
                    allowed: AllowedFlowsNative(
                        immediate: false,
                        flexible: false
                    ),
                    reason: "missing-app-store-id"
                )
                resolve(status)
            } else {
                let status = UpdateStatusNative(
                    platform: "ios",
                    supported: false,
                    updateAvailable: .first(NullType.null),
                    capabilities: CapabilitiesNative(
                        immediate: false,
                        flexible: false,
                        storePage: false,
                        latestVersionLookup: false,
                        installStateListener: false
                    ),
                    allowed: AllowedFlowsNative(
                        immediate: false,
                        flexible: false
                    ),
                    reason: "store-lookup-unavailable",
                    ios: IosDetailsNative(
                        bundleIdentifier: Bundle.main.bundleIdentifier,
                        appStoreId: appStoreId,
                        storeUrl: nil
                    )
                )
                resolve(status)
            }
        }
    }

    func startImmediateUpdate() throws -> Promise<UpdateStatusNative> {
        return Promise { resolve, reject in
            let status = UpdateStatusNative(
                platform: "ios",
                supported: false,
                updateAvailable: .first(NullType.null),
                capabilities: CapabilitiesNative(
                    immediate: false,
                    flexible: false,
                    storePage: false,
                    latestVersionLookup: false,
                    installStateListener: false
                ),
                allowed: AllowedFlowsNative(
                    immediate: false,
                    flexible: false
                ),
                reason: "unsupported-platform"
            )
            resolve(status)
        }
    }

    func startFlexibleUpdate() throws -> Promise<UpdateStatusNative> {
        return Promise { resolve, reject in
            let status = UpdateStatusNative(
                platform: "ios",
                supported: false,
                updateAvailable: .first(NullType.null),
                capabilities: CapabilitiesNative(
                    immediate: false,
                    flexible: false,
                    storePage: false,
                    latestVersionLookup: false,
                    installStateListener: false
                ),
                allowed: AllowedFlowsNative(
                    immediate: false,
                    flexible: false
                ),
                reason: "unsupported-platform"
            )
            resolve(status)
        }
    }

    func completeFlexibleUpdate() throws -> Promise<UpdateStatusNative> {
        return Promise { resolve, reject in
            let status = UpdateStatusNative(
                platform: "ios",
                supported: false,
                updateAvailable: .first(NullType.null),
                capabilities: CapabilitiesNative(
                    immediate: false,
                    flexible: false,
                    storePage: false,
                    latestVersionLookup: false,
                    installStateListener: false
                ),
                allowed: AllowedFlowsNative(
                    immediate: false,
                    flexible: false
                ),
                reason: "unsupported-platform"
            )
            resolve(status)
        }
    }

    func openStorePage(options: OpenStorePageOptionsNative?) throws -> Promise<Void> {
        return Promise { resolve, reject in
            guard let appStoreId = options?.ios?.appStoreId else {
                reject(NSError(domain: "InAppUpdates", code: 2, userInfo: [NSLocalizedDescriptionKey: "Missing ios.appStoreId for openStorePage()"]))
                return
            }

            let urlString = "https://apps.apple.com/app/id\(appStoreId)"
            guard let url = URL(string: urlString) else {
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
