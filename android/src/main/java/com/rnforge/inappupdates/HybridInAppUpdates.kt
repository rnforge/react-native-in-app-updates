package com.rnforge.inappupdates

import com.margelo.nitro.rnforge_inappupdates.HybridInAppUpdatesSpec
import com.margelo.nitro.core.Promise
import com.margelo.nitro.rnforge_inappupdates.InstallStateEventNative
import com.margelo.nitro.rnforge_inappupdates.UpdateStatusNative

class HybridInAppUpdates: HybridInAppUpdatesSpec() {
    private val immediateUpdateService = PlayCoreImmediateUpdateService()
    private val flexibleUpdateService = PlayCoreFlexibleUpdateService()
    private val installStateListenerService = PlayCoreInstallStateListenerService()

    override fun getUpdateStatus(): Promise<UpdateStatusNative> {
        val promise = Promise<UpdateStatusNative>()
        promise.reject(Exception("getUpdateStatus not yet implemented"))
        return promise
    }

    override fun startImmediateUpdate(): Promise<UpdateStatusNative> {
        return immediateUpdateService.startImmediateUpdate()
    }

    override fun startFlexibleUpdate(): Promise<UpdateStatusNative> {
        return flexibleUpdateService.startFlexibleUpdate()
    }

    override fun completeFlexibleUpdate(): Promise<UpdateStatusNative> {
        return flexibleUpdateService.completeFlexibleUpdate()
    }

    override fun addInstallStateListener(listener: (event: InstallStateEventNative) -> Unit): String {
        return installStateListenerService.addInstallStateListener(listener)
    }

    override fun removeInstallStateListener(listenerId: String) {
        installStateListenerService.removeInstallStateListener(listenerId)
    }
}
