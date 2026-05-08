package com.rnforge.inappupdates

import android.app.Activity
import android.content.Context
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.ConnectionResult
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.margelo.nitro.core.Promise
import com.margelo.nitro.rnforge_inappupdates.StartImmediateUpdateOptionsNative
import com.margelo.nitro.rnforge_inappupdates.UpdateStatusNative

/**
 * Handles Play Core immediate update flow.
 * Requests fresh AppUpdateInfo for every start attempt.
 */
class PlayCoreImmediateUpdateService {

    fun startImmediateUpdate(options: StartImmediateUpdateOptionsNative?): Promise<UpdateStatusNative> {
        val promise = Promise<UpdateStatusNative>()
        val allowAssetPackDeletion = options?.android?.allowAssetPackDeletion
        val context = InAppUpdatesActivityProvider.applicationContext

        if (context == null) {
            promise.resolve(createStatus(
                supported = true,
                updateAvailable = null,
                reason = "update-not-allowed"
            ))
            return promise
        }

        val installSource = getInstallSource(context)
        if (installSource != "com.android.vending") {
            promise.resolve(createUnsupportedStatus("unsupported-install-source"))
            return promise
        }

        val playServicesResult = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
        if (playServicesResult != ConnectionResult.SUCCESS) {
            promise.resolve(createUnsupportedStatus("play-core-unavailable"))
            return promise
        }

        val appUpdateManager = PlayCoreAppUpdateManager.getInstance(context)
        appUpdateManager.appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->
                when (appUpdateInfo.updateAvailability()) {
                    UpdateAvailability.UPDATE_NOT_AVAILABLE -> {
                        promise.resolve(createStatus(
                            supported = true,
                            updateAvailable = false,
                            reason = "no-update-available"
                        ))
                    }
                    UpdateAvailability.UPDATE_AVAILABLE -> {
                        val immediateAllowed = appUpdateInfo.isUpdateTypeAllowed(buildAppUpdateOptions(AppUpdateType.IMMEDIATE, allowAssetPackDeletion))
                        val flexibleAllowed = appUpdateInfo.isUpdateTypeAllowed(buildAppUpdateOptions(AppUpdateType.FLEXIBLE, allowAssetPackDeletion))
                        if (immediateAllowed) {
                            val activity = InAppUpdatesActivityProvider.currentActivity
                            if (activity != null) {
                                startUpdateFlow(appUpdateManager, appUpdateInfo, activity, promise, flexibleAllowed, allowAssetPackDeletion)
                            } else {
                                promise.resolve(createStatus(
                                    supported = true,
                                    updateAvailable = true,
                                    reason = "update-not-allowed",
                                    immediateAllowed = immediateAllowed,
                                    flexibleAllowed = flexibleAllowed
                                ))
                            }
                        } else {
                            promise.resolve(createStatus(
                                supported = true,
                                updateAvailable = true,
                                reason = "update-not-allowed",
                                immediateAllowed = false,
                                flexibleAllowed = flexibleAllowed
                            ))
                        }
                    }
                    UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> {
                        promise.resolve(createStatus(
                            supported = true,
                            updateAvailable = true,
                            reason = "developer-triggered-update-in-progress"
                        ))
                    }
                    else -> {
                        promise.resolve(createUnsupportedStatus("unknown"))
                    }
                }
            }
            .addOnFailureListener { error ->
                promise.reject(encodeTaskFailure(error))
            }

        return promise
    }

    private fun startUpdateFlow(
        appUpdateManager: AppUpdateManager,
        appUpdateInfo: com.google.android.play.core.appupdate.AppUpdateInfo,
        activity: Activity,
        promise: Promise<UpdateStatusNative>,
        flexibleAllowed: Boolean,
        allowAssetPackDeletion: Boolean? = null
    ) {
        appUpdateManager.startUpdateFlow(
            appUpdateInfo,
            activity,
            buildAppUpdateOptions(AppUpdateType.IMMEDIATE, allowAssetPackDeletion)
        ).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                promise.resolve(createStatus(
                    supported = true,
                    updateAvailable = true,
                    reason = "update-available",
                    immediateAllowed = true,
                    flexibleAllowed = flexibleAllowed
                ))
            } else {
                promise.reject(encodeTaskFailure(task.exception ?: Exception("Immediate update flow failed")))
            }
        }
    }
}
