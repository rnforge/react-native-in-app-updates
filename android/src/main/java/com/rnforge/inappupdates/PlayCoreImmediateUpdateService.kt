package com.rnforge.inappupdates

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.ConnectionResult
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.margelo.nitro.core.NullType
import com.margelo.nitro.core.Promise
import com.margelo.nitro.rnforge_inappupdates.AllowedFlowsNative
import com.margelo.nitro.rnforge_inappupdates.CapabilitiesNative
import com.margelo.nitro.rnforge_inappupdates.UpdateStatusNative
import com.margelo.nitro.rnforge_inappupdates.Variant_NullType_Boolean

/**
 * Handles Play Core immediate update flow.
 * Requests fresh AppUpdateInfo for every start attempt.
 */
class PlayCoreImmediateUpdateService {

    fun startImmediateUpdate(): Promise<UpdateStatusNative> {
        val promise = Promise<UpdateStatusNative>()
        val context = InAppUpdatesActivityProvider.applicationContext

        if (context == null) {
            // NitroModules context not yet available — return typed unavailable,
            // not a thrown exception. This can happen if startImmediateUpdate()
            // is called extremely early in app startup before TurboModule init.
            promise.resolve(createStatus(
                supported = true,
                updateAvailable = null,
                reason = "update-not-allowed"
            ))
            return promise
        }

        // Check install source
        val installSource = getInstallSource(context)
        if (installSource != "com.android.vending") {
            promise.resolve(createUnsupportedStatus("unsupported-install-source"))
            return promise
        }

        // Check Google Play Services availability
        val playServicesResult = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
        if (playServicesResult != ConnectionResult.SUCCESS) {
            promise.resolve(createUnsupportedStatus("play-core-unavailable"))
            return promise
        }

        // Request fresh AppUpdateInfo
        val appUpdateManager = AppUpdateManagerFactory.create(context)
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
                        if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                            val activity = InAppUpdatesActivityProvider.currentActivity
                            if (activity != null) {
                                startUpdateFlow(appUpdateManager, appUpdateInfo, activity, promise)
                            } else {
                                promise.resolve(createStatus(
                                    supported = true,
                                    updateAvailable = true,
                                    reason = "update-not-allowed"
                                ))
                            }
                        } else {
                            promise.resolve(createStatus(
                                supported = true,
                                updateAvailable = true,
                                reason = "update-not-allowed",
                                immediateAllowed = false
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
                promise.reject(error)
            }

        return promise
    }

    private fun startUpdateFlow(
        appUpdateManager: AppUpdateManager,
        appUpdateInfo: com.google.android.play.core.appupdate.AppUpdateInfo,
        activity: Activity,
        promise: Promise<UpdateStatusNative>
    ) {
        appUpdateManager.startUpdateFlow(
            appUpdateInfo,
            activity,
            AppUpdateOptions.defaultOptions(AppUpdateType.IMMEDIATE)
        ).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                promise.resolve(createStatus(
                    supported = true,
                    updateAvailable = true,
                    reason = "update-available",
                    immediateAllowed = true
                ))
            } else {
                promise.reject(task.exception ?: Exception("Immediate update flow failed"))
            }
        }
    }

    private fun getInstallSource(context: Context): String? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.packageManager.getInstallSourceInfo(context.packageName).installingPackageName
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getInstallerPackageName(context.packageName)
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun createUnsupportedStatus(reason: String): UpdateStatusNative {
        return UpdateStatusNative(
            platform = "android",
            supported = false,
            updateAvailable = Variant_NullType_Boolean.create(NullType.null),
            capabilities = CapabilitiesNative(
                immediate = false,
                flexible = false,
                storePage = false,
                latestVersionLookup = false,
                installStateListener = false
            ),
            allowed = AllowedFlowsNative(
                immediate = false,
                flexible = false
            ),
            reason = reason
        )
    }

    private fun createStatus(
        supported: Boolean,
        updateAvailable: Boolean?,
        reason: String,
        immediateAllowed: Boolean? = null
    ): UpdateStatusNative {
        return UpdateStatusNative(
            platform = "android",
            supported = supported,
            updateAvailable = updateAvailable?.let { Variant_NullType_Boolean.create(it) }
                ?: Variant_NullType_Boolean.create(NullType.null),
            capabilities = CapabilitiesNative(
                immediate = immediateAllowed ?: false,
                flexible = false,
                storePage = false,
                latestVersionLookup = false,
                installStateListener = false
            ),
            allowed = AllowedFlowsNative(
                immediate = immediateAllowed ?: false,
                flexible = false
            ),
            reason = reason
        )
    }
}
