//
//  HybridInAppUpdates.swift
//  Pods
//
//  Created by RNForge on 5/5/2026.
//

import Foundation
import NitroModules

class HybridInAppUpdates: HybridInAppUpdatesSpec {
    func getUpdateStatus() throws -> Promise<UpdateStatusNative> {
        // Placeholder: Issue 0002 full implementation deferred
        return Promise { _ in throw NSError(domain: "InAppUpdates", code: 1, userInfo: [NSLocalizedDescriptionKey: "getUpdateStatus not yet implemented"]) }
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
