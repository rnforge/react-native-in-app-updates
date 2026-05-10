package com.rnforge.inappupdates

import android.content.Context
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.install.model.AppUpdateType
import com.margelo.nitro.rnforge_inappupdates.PlayCoreDetailsNative
import com.margelo.nitro.rnforge_inappupdates.UpdateStatusNative

/**
 * Pure mapping from Play Core [AppUpdateInfo] to RNForge [UpdateStatusNative].
 *
 * This function is intentionally free of Play Core I/O so it can be unit-tested
 * without real Play Store availability. Services call it inside success callbacks.
 */
fun mapAppUpdateInfoToStatus(
    info: AppUpdateInfo,
    context: Context,
    allowAssetPackDeletion: Boolean?
): UpdateStatusNative {
    val immediateAllowed = info.isUpdateTypeAllowed(
        buildAppUpdateOptions(AppUpdateType.IMMEDIATE, allowAssetPackDeletion)
    )
    val flexibleAllowed = info.isUpdateTypeAllowed(
        buildAppUpdateOptions(AppUpdateType.FLEXIBLE, allowAssetPackDeletion)
    )
    val immediateFailedPreconditions = mapFailedUpdatePreconditionsOrNull(
        info, AppUpdateType.IMMEDIATE, allowAssetPackDeletion
    )
    val flexibleFailedPreconditions = mapFailedUpdatePreconditionsOrNull(
        info, AppUpdateType.FLEXIBLE, allowAssetPackDeletion
    )
    val installStatus = mapInstallStatus(info.installStatus())

    val playCoreDetails = PlayCoreDetailsNative(
        immediateFailedPreconditions = immediateFailedPreconditions?.toTypedArray(),
        flexibleFailedPreconditions = flexibleFailedPreconditions?.toTypedArray(),
        installErrorCode = null,
        taskErrorCode = null,
        updateAvailability = mapUpdateAvailability(info.updateAvailability()),
        installStatus = installStatus,
        updatePriority = info.updatePriority().toDouble(),
        clientVersionStalenessDays = info.clientVersionStalenessDays()?.toDouble(),
        availableVersionCode = info.availableVersionCode().toDouble(),
        bytesDownloaded = info.bytesDownloaded().toDouble(),
        totalBytesToDownload = info.totalBytesToDownload().toDouble(),
        immediateAllowed = immediateAllowed,
        flexibleAllowed = flexibleAllowed
    )

    return createStatus(
        supported = true,
        updateAvailable = when (info.updateAvailability()) {
            com.google.android.play.core.install.model.UpdateAvailability.UPDATE_AVAILABLE -> true
            com.google.android.play.core.install.model.UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> true
            else -> false
        },
        reason = when (info.updateAvailability()) {
            com.google.android.play.core.install.model.UpdateAvailability.UPDATE_AVAILABLE -> "update-available"
            com.google.android.play.core.install.model.UpdateAvailability.UPDATE_NOT_AVAILABLE -> "no-update-available"
            com.google.android.play.core.install.model.UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> "developer-triggered-update-in-progress"
            else -> "unknown"
        },
        immediateAllowed = immediateAllowed,
        flexibleAllowed = flexibleAllowed,
        installStatus = installStatus,
        android = com.margelo.nitro.rnforge_inappupdates.AndroidDetailsNative(
            packageName = context.packageName,
            playCore = playCoreDetails
        )
    )
}

private fun mapUpdateAvailability(availability: Int): String {
    return when (availability) {
        com.google.android.play.core.install.model.UpdateAvailability.UPDATE_AVAILABLE -> "UPDATE_AVAILABLE"
        com.google.android.play.core.install.model.UpdateAvailability.UPDATE_NOT_AVAILABLE -> "UPDATE_NOT_AVAILABLE"
        com.google.android.play.core.install.model.UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> "DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS"
        else -> "UNKNOWN"
    }
}
