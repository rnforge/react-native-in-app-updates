package com.rnforge.inappupdates

import android.content.Context
import com.google.android.gms.common.ConnectionResult
import com.margelo.nitro.core.Promise
import com.margelo.nitro.rnforge_inappupdates.GetUpdateStatusOptionsNative
import com.margelo.nitro.rnforge_inappupdates.UpdateStatusNative

/**
 * Handles Play Core status snapshot for getUpdateStatus().
 * Requests fresh AppUpdateInfo for every call.
 *
 * Testability: inject [AppUpdateManagerProvider] and [EnvironmentChecker]
 * to control Play Core responses and environment state.
 */
class PlayCoreStatusService(
    private val managerProvider: AppUpdateManagerProvider = PlayCoreAppUpdateManagerProvider,
    private val envChecker: EnvironmentChecker = DefaultEnvironmentChecker
) {

    fun getUpdateStatus(options: GetUpdateStatusOptionsNative?): Promise<UpdateStatusNative> {
        val promise = Promise<UpdateStatusNative>()
        val allowAssetPackDeletion = options?.android?.allowAssetPackDeletion
        val context = InAppUpdatesActivityProvider.applicationContext

        val earlyStatus = checkEarlyEnvironment(context, envChecker)
        if (earlyStatus != null) {
            promise.resolve(earlyStatus)
            return promise
        }

        val appUpdateManager = managerProvider.getManager(context!!)
        appUpdateManager.appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->
                promise.resolve(
                    mapAppUpdateInfoToStatus(appUpdateInfo, context, allowAssetPackDeletion)
                )
            }
            .addOnFailureListener { error ->
                promise.reject(encodeTaskFailure(error))
            }

        return promise
    }
}

/**
 * Shared early-return guard used by status and flow services.
 * Returns a typed status when context is null, install source is wrong,
 * or Google Play Services are unavailable.
 */
internal fun checkEarlyEnvironment(
    context: Context?,
    envChecker: EnvironmentChecker
): UpdateStatusNative? {
    if (context == null) {
        return createStatus(
            supported = true,
            updateAvailable = null,
            reason = "update-not-allowed"
        )
    }

    val installSource = envChecker.getInstallSource(context)
    if (installSource != "com.android.vending") {
        return createUnsupportedStatus("unsupported-install-source")
    }

    val playServicesResult = envChecker.isGooglePlayServicesAvailable(context)
    if (playServicesResult != ConnectionResult.SUCCESS) {
        return createUnsupportedStatus("play-core-unavailable")
    }

    return null
}
