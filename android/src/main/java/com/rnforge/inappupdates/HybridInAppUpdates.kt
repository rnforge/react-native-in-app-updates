package com.rnforge.inappupdates

import com.margelo.nitro.rnforge_inappupdates.HybridInAppUpdatesSpec
import com.margelo.nitro.core.Promise
import com.margelo.nitro.rnforge_inappupdates.GetUpdateStatusOptionsNative
import com.margelo.nitro.rnforge_inappupdates.InstallStateEventNative
import com.margelo.nitro.rnforge_inappupdates.OpenStorePageOptionsNative
import com.margelo.nitro.rnforge_inappupdates.UpdateStatusNative

class HybridInAppUpdates: HybridInAppUpdatesSpec() {
    private val immediateUpdateService = PlayCoreImmediateUpdateService()
    private val flexibleUpdateService = PlayCoreFlexibleUpdateService()
    private val installStateListenerService = PlayCoreInstallStateListenerService()
    private val statusService = PlayCoreStatusService()
    private val storeService = PlayCoreStoreService()

    override fun getUpdateStatus(options: GetUpdateStatusOptionsNative?): Promise<UpdateStatusNative> {
        return statusService.getUpdateStatus(options)
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

    override fun openStorePage(options: OpenStorePageOptionsNative?): Promise<Unit> {
        return storeService.openStorePage(options)
    }

    override fun addInstallStateListener(listener: (event: InstallStateEventNative) -> Unit): String {
        return installStateListenerService.addInstallStateListener(listener)
    }

    override fun removeInstallStateListener(listenerId: String) {
        installStateListenerService.removeInstallStateListener(listenerId)
    }
}
