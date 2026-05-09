package com.rnforge.inappupdates

import android.app.Activity
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.margelo.nitro.core.Promise
import com.margelo.nitro.rnforge_inappupdates.StartFlexibleUpdateOptionsNative
import com.margelo.nitro.rnforge_inappupdates.UpdateStatusNative

/**
 * Handles Play Core flexible update flow.
 * Requests fresh AppUpdateInfo for every start and complete attempt.
 *
 * Testability: inject [AppUpdateManagerProvider] and [EnvironmentChecker]
 * to control Play Core responses and environment state.
 */
class PlayCoreFlexibleUpdateService(
    private val managerProvider: AppUpdateManagerProvider = PlayCoreAppUpdateManagerProvider,
    private val envChecker: EnvironmentChecker = DefaultEnvironmentChecker
) {

    fun startFlexibleUpdate(options: StartFlexibleUpdateOptionsNative?): Promise<UpdateStatusNative> {
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
                when (appUpdateInfo.updateAvailability()) {
                    UpdateAvailability.UPDATE_NOT_AVAILABLE -> {
                        promise.resolve(createStatus(
                            supported = true,
                            updateAvailable = false,
                            reason = "no-update-available"
                        ))
                    }
                    UpdateAvailability.UPDATE_AVAILABLE -> {
                        val flexibleAllowed = appUpdateInfo.isUpdateTypeAllowed(
                            buildAppUpdateOptions(AppUpdateType.FLEXIBLE, allowAssetPackDeletion)
                        )
                        val immediateAllowed = appUpdateInfo.isUpdateTypeAllowed(
                            buildAppUpdateOptions(AppUpdateType.IMMEDIATE, allowAssetPackDeletion)
                        )
                        if (flexibleAllowed) {
                            val activity = InAppUpdatesActivityProvider.currentActivity
                            if (activity != null) {
                                startUpdateFlow(
                                    appUpdateManager, appUpdateInfo, activity, promise,
                                    immediateAllowed, allowAssetPackDeletion
                                )
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

        val earlyStatus = checkEarlyEnvironment(context, envChecker)
        if (earlyStatus != null) {
            promise.resolve(earlyStatus)
            return promise
        }

        val appUpdateManager = managerProvider.getManager(context!!)
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
        immediateAllowed: Boolean,
        allowAssetPackDeletion: Boolean? = null
    ) {
        appUpdateManager.startUpdateFlow(
            appUpdateInfo,
            activity,
            buildAppUpdateOptions(AppUpdateType.FLEXIBLE, allowAssetPackDeletion)
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
