package com.rnforge.inappupdates

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.ConnectionResult
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.margelo.nitro.core.NullType
import com.margelo.nitro.core.Promise
import com.margelo.nitro.rnforge_inappupdates.AllowedFlowsNative
import com.margelo.nitro.rnforge_inappupdates.AndroidDetailsNative
import com.margelo.nitro.rnforge_inappupdates.CapabilitiesNative
import com.margelo.nitro.rnforge_inappupdates.GetUpdateStatusOptionsNative
import com.margelo.nitro.rnforge_inappupdates.PlayCoreDetailsNative
import com.margelo.nitro.rnforge_inappupdates.UpdateStatusNative
import com.margelo.nitro.rnforge_inappupdates.Variant_NullType_Boolean

/**
 * Handles Play Core status snapshot for getUpdateStatus().
 * Requests fresh AppUpdateInfo for every call.
 */
class PlayCoreStatusService {

    fun getUpdateStatus(options: GetUpdateStatusOptionsNative?): Promise<UpdateStatusNative> {
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
                val flexibleAllowed = appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)

                val installStatus = mapInstallStatus(appUpdateInfo.installStatus())

                when (appUpdateInfo.updateAvailability()) {
                    UpdateAvailability.UPDATE_NOT_AVAILABLE -> {
                        promise.resolve(createStatus(
                            supported = true,
                            updateAvailable = false,
                            reason = "no-update-available",
                            immediateAllowed = immediateAllowed,
                            flexibleAllowed = flexibleAllowed,
                            installStatus = installStatus,
                            appUpdateInfo = appUpdateInfo,
                            context = context
                        ))
                    }
                    UpdateAvailability.UPDATE_AVAILABLE -> {
                        promise.resolve(createStatus(
                            supported = true,
                            updateAvailable = true,
                            reason = "update-available",
                            immediateAllowed = immediateAllowed,
                            flexibleAllowed = flexibleAllowed,
                            installStatus = installStatus,
                            appUpdateInfo = appUpdateInfo,
                            context = context
                        ))
                    }
                    UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> {
                        promise.resolve(createStatus(
                            supported = true,
                            updateAvailable = true,
                            reason = "developer-triggered-update-in-progress",
                            immediateAllowed = immediateAllowed,
                            flexibleAllowed = flexibleAllowed,
                            installStatus = installStatus,
                            appUpdateInfo = appUpdateInfo,
                            context = context
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

    private fun mapInstallStatus(status: Int): String {
        return when (status) {
            InstallStatus.UNKNOWN -> "unknown"
            InstallStatus.PENDING -> "pending"
            InstallStatus.DOWNLOADING -> "downloading"
            InstallStatus.DOWNLOADED -> "downloaded"
            InstallStatus.INSTALLING -> "installing"
            InstallStatus.INSTALLED -> "installed"
            InstallStatus.FAILED -> "failed"
            InstallStatus.CANCELED -> "canceled"
            else -> "unknown"
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
        immediateAllowed: Boolean? = null,
        flexibleAllowed: Boolean? = null,
        installStatus: String? = null,
        appUpdateInfo: com.google.android.play.core.appupdate.AppUpdateInfo? = null,
        context: Context? = null
    ): UpdateStatusNative {
        val playCoreDetails = if (appUpdateInfo != null) {
            PlayCoreDetailsNative(
                updateAvailability = when (appUpdateInfo.updateAvailability()) {
                    UpdateAvailability.UPDATE_AVAILABLE -> "UPDATE_AVAILABLE"
                    UpdateAvailability.UPDATE_NOT_AVAILABLE -> "UPDATE_NOT_AVAILABLE"
                    UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> "DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS"
                    else -> "UNKNOWN"
                },
                installStatus = installStatus,
                updatePriority = appUpdateInfo.updatePriority().toDouble(),
                clientVersionStalenessDays = if (appUpdateInfo.clientVersionStalenessDays() != null) appUpdateInfo.clientVersionStalenessDays()!!.toDouble() else null,
                availableVersionCode = appUpdateInfo.availableVersionCode().toDouble(),
                bytesDownloaded = appUpdateInfo.bytesDownloaded().toDouble(),
                totalBytesToDownload = appUpdateInfo.totalBytesToDownload().toDouble(),
                immediateAllowed = immediateAllowed,
                flexibleAllowed = flexibleAllowed
            )
        } else null

        return UpdateStatusNative(
            platform = "android",
            supported = supported,
            updateAvailable = updateAvailable?.let { Variant_NullType_Boolean.create(it) }
                ?: Variant_NullType_Boolean.create(NullType.null),
            capabilities = CapabilitiesNative(
                immediate = true,
                flexible = true,
                storePage = false,
                latestVersionLookup = false,
                installStateListener = true
            ),
            allowed = AllowedFlowsNative(
                immediate = immediateAllowed ?: false,
                flexible = flexibleAllowed ?: false
            ),
            reason = reason,
            installStatus = installStatus,
            android = if (context != null) AndroidDetailsNative(
                packageName = context.packageName,
                playCore = playCoreDetails
            ) else null
        )
    }
}
