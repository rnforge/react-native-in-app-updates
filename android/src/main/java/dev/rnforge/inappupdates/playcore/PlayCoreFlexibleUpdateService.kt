package dev.rnforge.inappupdates.playcore

import dev.rnforge.inappupdates.ActivityProvider
import dev.rnforge.inappupdates.DefaultActivityProvider
import dev.rnforge.inappupdates.EnvironmentChecker
import dev.rnforge.inappupdates.DefaultEnvironmentChecker

import android.app.Activity
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.margelo.nitro.core.Promise
import com.margelo.nitro.rnforge.inappupdates.StartFlexibleUpdateOptionsNative
import com.margelo.nitro.rnforge.inappupdates.UpdateStatusNative

/**
 * Handles Play Core flexible update flow.
 * Requests fresh AppUpdateInfo for every start and complete attempt.
 *
 * Testability: inject [AppUpdateManagerProvider] and [EnvironmentChecker]
 * to control Play Core responses and environment state.
 */
class PlayCoreFlexibleUpdateService(
    private val managerProvider: AppUpdateManagerProvider = PlayCoreAppUpdateManagerProvider,
    private val envChecker: EnvironmentChecker = DefaultEnvironmentChecker,
    private val activityProvider: ActivityProvider = DefaultActivityProvider
) {

    fun startFlexibleUpdate(options: StartFlexibleUpdateOptionsNative?): Promise<UpdateStatusNative> {
        val promise = Promise<UpdateStatusNative>()
        startFlexibleUpdate(
            options = options,
            onSuccess = { promise.resolve(it) },
            onFailure = { promise.reject(it) }
        )
        return promise
    }

    internal fun startFlexibleUpdate(
        options: StartFlexibleUpdateOptionsNative?,
        onSuccess: (UpdateStatusNative) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val allowAssetPackDeletion = options?.android?.allowAssetPackDeletion
        val context = activityProvider.applicationContext

        val earlyStatus = checkEarlyEnvironment(context, envChecker)
        if (earlyStatus != null) {
            onSuccess(earlyStatus)
            return
        }

        val appUpdateManager = managerProvider.getManager(context!!)
        appUpdateManager.appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->
                when (appUpdateInfo.updateAvailability()) {
                    UpdateAvailability.UPDATE_NOT_AVAILABLE -> {
                        onSuccess(createStatus(
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
                            val activity = activityProvider.currentActivity
                            if (activity != null) {
                                startUpdateFlow(
                                    appUpdateManager = appUpdateManager,
                                    appUpdateInfo = appUpdateInfo,
                                    activity = activity,
                                    onSuccess = onSuccess,
                                    onFailure = onFailure,
                                    immediateAllowed = immediateAllowed,
                                    allowAssetPackDeletion = allowAssetPackDeletion
                                )
                            } else {
                                onSuccess(createStatus(
                                    supported = true,
                                    updateAvailable = true,
                                    reason = "update-not-allowed",
                                    immediateAllowed = immediateAllowed,
                                    flexibleAllowed = flexibleAllowed
                                ))
                            }
                        } else {
                            onSuccess(createStatus(
                                supported = true,
                                updateAvailable = true,
                                reason = "update-not-allowed",
                                immediateAllowed = immediateAllowed,
                                flexibleAllowed = false
                            ))
                        }
                    }
                    UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> {
                        onSuccess(createStatus(
                            supported = true,
                            updateAvailable = true,
                            reason = "developer-triggered-update-in-progress"
                        ))
                    }
                    else -> {
                        onSuccess(createUnsupportedStatus("unknown"))
                    }
                }
            }
            .addOnFailureListener { error ->
                onFailure(encodeTaskFailure(error))
            }
    }

    fun completeFlexibleUpdate(): Promise<UpdateStatusNative> {
        val promise = Promise<UpdateStatusNative>()
        completeFlexibleUpdate(
            onSuccess = { promise.resolve(it) },
            onFailure = { promise.reject(it) }
        )
        return promise
    }

    internal fun completeFlexibleUpdate(
        onSuccess: (UpdateStatusNative) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val context = activityProvider.applicationContext

        val earlyStatus = checkEarlyEnvironment(context, envChecker)
        if (earlyStatus != null) {
            onSuccess(earlyStatus)
            return
        }

        val appUpdateManager = managerProvider.getManager(context!!)
        appUpdateManager.appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->
                val immediateAllowed = appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
                if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                    appUpdateManager.completeUpdate()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                onSuccess(createStatus(
                                    supported = true,
                                    updateAvailable = true,
                                    reason = "flexible-update-downloaded",
                                    immediateAllowed = immediateAllowed,
                                    flexibleAllowed = true
                                ))
                            } else {
                                onFailure(encodeTaskFailure(task.exception ?: Exception("completeUpdate failed")))
                            }
                        }
                } else {
                    onSuccess(createStatus(
                        supported = true,
                        updateAvailable = true,
                        reason = "update-not-allowed",
                        immediateAllowed = immediateAllowed,
                        flexibleAllowed = appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
                    ))
                }
            }
            .addOnFailureListener { error ->
                onFailure(encodeTaskFailure(error))
            }
    }

    private fun startUpdateFlow(
        appUpdateManager: AppUpdateManager,
        appUpdateInfo: com.google.android.play.core.appupdate.AppUpdateInfo,
        activity: Activity,
        onSuccess: (UpdateStatusNative) -> Unit,
        onFailure: (Exception) -> Unit,
        immediateAllowed: Boolean,
        allowAssetPackDeletion: Boolean? = null
    ) {
        appUpdateManager.startUpdateFlow(
            appUpdateInfo,
            activity,
            buildAppUpdateOptions(AppUpdateType.FLEXIBLE, allowAssetPackDeletion)
        ).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onSuccess(createStatus(
                    supported = true,
                    updateAvailable = true,
                    reason = "update-available",
                    immediateAllowed = immediateAllowed,
                    flexibleAllowed = true
                ))
            } else {
                onFailure(encodeTaskFailure(task.exception ?: Exception("Flexible update flow failed")))
            }
        }
    }
}
