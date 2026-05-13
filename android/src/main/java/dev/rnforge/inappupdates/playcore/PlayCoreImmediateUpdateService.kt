package dev.rnforge.inappupdates.playcore

import dev.rnforge.inappupdates.ActivityProvider
import dev.rnforge.inappupdates.DefaultActivityProvider
import dev.rnforge.inappupdates.EnvironmentChecker
import dev.rnforge.inappupdates.DefaultEnvironmentChecker

import android.app.Activity
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.margelo.nitro.core.Promise
import com.margelo.nitro.rnforge.inappupdates.StartImmediateUpdateOptionsNative
import com.margelo.nitro.rnforge.inappupdates.UpdateStatusNative

/**
 * Handles Play Core immediate update flow.
 * Requests fresh AppUpdateInfo for every start attempt.
 *
 * Testability: inject [AppUpdateManagerProvider] and [EnvironmentChecker]
 * to control Play Core responses and environment state.
 */
class PlayCoreImmediateUpdateService(
    private val managerProvider: AppUpdateManagerProvider = PlayCoreAppUpdateManagerProvider,
    private val envChecker: EnvironmentChecker = DefaultEnvironmentChecker,
    private val activityProvider: ActivityProvider = DefaultActivityProvider
) {

    fun startImmediateUpdate(options: StartImmediateUpdateOptionsNative?): Promise<UpdateStatusNative> {
        val promise = Promise<UpdateStatusNative>()
        startImmediateUpdate(
            options = options,
            onSuccess = { promise.resolve(it) },
            onFailure = { promise.reject(it) }
        )
        return promise
    }

    internal fun startImmediateUpdate(
        options: StartImmediateUpdateOptionsNative?,
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
                        val immediateAllowed = appUpdateInfo.isUpdateTypeAllowed(
                            buildAppUpdateOptions(AppUpdateType.IMMEDIATE, allowAssetPackDeletion)
                        )
                        val flexibleAllowed = appUpdateInfo.isUpdateTypeAllowed(
                            buildAppUpdateOptions(AppUpdateType.FLEXIBLE, allowAssetPackDeletion)
                        )
                        if (immediateAllowed) {
                            val activity = activityProvider.currentActivity
                            if (activity != null) {
                                startUpdateFlow(
                                    appUpdateManager = appUpdateManager,
                                    appUpdateInfo = appUpdateInfo,
                                    activity = activity,
                                    onSuccess = onSuccess,
                                    onFailure = onFailure,
                                    flexibleAllowed = flexibleAllowed,
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
                                immediateAllowed = false,
                                flexibleAllowed = flexibleAllowed
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

    private fun startUpdateFlow(
        appUpdateManager: AppUpdateManager,
        appUpdateInfo: com.google.android.play.core.appupdate.AppUpdateInfo,
        activity: Activity,
        onSuccess: (UpdateStatusNative) -> Unit,
        onFailure: (Exception) -> Unit,
        flexibleAllowed: Boolean,
        allowAssetPackDeletion: Boolean? = null
    ) {
        appUpdateManager.startUpdateFlow(
            appUpdateInfo,
            activity,
            buildAppUpdateOptions(AppUpdateType.IMMEDIATE, allowAssetPackDeletion)
        ).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onSuccess(createStatus(
                    supported = true,
                    updateAvailable = true,
                    reason = "update-available",
                    immediateAllowed = true,
                    flexibleAllowed = flexibleAllowed
                ))
            } else {
                onFailure(encodeTaskFailure(task.exception ?: Exception("Immediate update flow failed")))
            }
        }
    }
}
