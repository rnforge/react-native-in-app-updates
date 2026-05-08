package com.rnforge.inappupdates

import android.app.Activity
import android.content.Context
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.ConnectionResult
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.margelo.nitro.core.Promise
import com.margelo.nitro.rnforge_inappupdates.UpdateStatusNative

/**
 * Handles Play Core flexible update flow.
 * Requests fresh AppUpdateInfo for every start and complete attempt.
 */
class PlayCoreFlexibleUpdateService {

    fun startFlexibleUpdate(): Promise<UpdateStatusNative> {
        val promise = Promise<UpdateStatusNative>()
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
                        val flexibleAllowed = appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
                        val immediateAllowed = appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
                        if (flexibleAllowed) {
                            val activity = InAppUpdatesActivityProvider.currentActivity
                            if (activity != null) {
                                startUpdateFlow(appUpdateManager, appUpdateInfo, activity, promise, immediateAllowed)
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
                                immediateAllowed = immediateAllowed,
                                flexibleAllowed = false
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

    fun completeFlexibleUpdate(): Promise<UpdateStatusNative> {
        val promise = Promise<UpdateStatusNative>()
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
                val immediateAllowed = appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
                if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                    appUpdateManager.completeUpdate()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                promise.resolve(createStatus(
                                    supported = true,
                                    updateAvailable = true,
                                    reason = "flexible-update-downloaded",
                                    immediateAllowed = immediateAllowed,
                                    flexibleAllowed = true
                                ))
                            } else {
                                promise.reject(encodeTaskFailure(task.exception ?: Exception("completeUpdate failed")))
                            }
                        }
                } else {
                    promise.resolve(createStatus(
                        supported = true,
                        updateAvailable = true,
                        reason = "update-not-allowed",
                        immediateAllowed = immediateAllowed,
                        flexibleAllowed = appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
                    ))
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
        immediateAllowed: Boolean
    ) {
        appUpdateManager.startUpdateFlow(
            appUpdateInfo,
            activity,
            AppUpdateOptions.defaultOptions(AppUpdateType.FLEXIBLE)
        ).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                promise.resolve(createStatus(
                    supported = true,
                    updateAvailable = true,
                    reason = "update-available",
                    immediateAllowed = immediateAllowed,
                    flexibleAllowed = true
                ))
            } else {
                promise.reject(encodeTaskFailure(task.exception ?: Exception("Flexible update flow failed")))
            }
        }
    }
}
