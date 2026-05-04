package com.rnforge.inappupdates

import com.margelo.nitro.rnforge_inappupdates.HybridInAppUpdatesSpec
import com.margelo.nitro.core.Promise
import com.margelo.nitro.rnforge_inappupdates.UpdateStatusNative

class HybridInAppUpdates: HybridInAppUpdatesSpec() {
    private val immediateUpdateService = PlayCoreImmediateUpdateService()

    override fun getUpdateStatus(): Promise<UpdateStatusNative> {
        val promise = Promise<UpdateStatusNative>()
        promise.reject(Exception("getUpdateStatus not yet implemented"))
        return promise
    }

    override fun startImmediateUpdate(): Promise<UpdateStatusNative> {
        return immediateUpdateService.startImmediateUpdate()
    }
}
