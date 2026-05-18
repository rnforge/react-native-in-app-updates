package dev.rnforge.inappupdates.playcore

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.install.model.AppUpdateType
import com.margelo.nitro.rnforge.inappupdates.AndroidDetailsNative
import com.margelo.nitro.rnforge.inappupdates.PlayCoreDetailsNative
import com.margelo.nitro.rnforge.inappupdates.UpdateStatusNative

/**
 * Builds a status result from an [AppUpdateInfo] with consistent version fields.
 *
 * Used by all service paths that have both [context] and [info].
 * Populates:
 * - `currentVersion` from installed app `PackageInfo.versionName`
 * - `currentBuild` from installed app version code (longVersionCode on API 28+)
 * - `latestStoreBuild` from `availableVersionCode()` only when update is available or in progress
 * - `latestStoreVersion` remains null (Play Core does not expose store versionName)
 */
internal fun buildUpdateStatusFromInfo(
    context: Context,
    info: AppUpdateInfo,
    supported: Boolean,
    updateAvailable: Boolean?,
    reason: String,
    immediateAllowed: Boolean? = null,
    flexibleAllowed: Boolean? = null,
    installStatus: String? = null,
    additionalPlayCore: PlayCoreDetailsNative? = null
): UpdateStatusNative {
    val currentVersion = getAppVersionName(context)
    val currentBuild = getAppVersionCode(context)

    // availableVersionCode() is only meaningful when an update is available or in progress.
    // Android docs: returns arbitrary value otherwise.
    val hasUpdateInfo = info.updateAvailability() ==
        com.google.android.play.core.install.model.UpdateAvailability.UPDATE_AVAILABLE ||
        info.updateAvailability() ==
        com.google.android.play.core.install.model.UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
    val latestStoreBuild = if (hasUpdateInfo) info.availableVersionCode().toString() else null

    val androidDetails = additionalPlayCore?.let {
        AndroidDetailsNative(
            packageName = context.packageName,
            playCore = it
        )
    }

    return createStatus(
        supported = supported,
        updateAvailable = updateAvailable,
        reason = reason,
        immediateAllowed = immediateAllowed,
        flexibleAllowed = flexibleAllowed,
        installStatus = installStatus,
        android = androidDetails,
        currentVersion = currentVersion,
        currentBuild = currentBuild,
        latestStoreBuild = latestStoreBuild
    )
}

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

    return buildUpdateStatusFromInfo(
        context = context,
        info = info,
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
        additionalPlayCore = playCoreDetails
    )
}

/**
 * Returns the installed app's versionName.
 * Uses the modern API on Android Tiramisu+, falls back on older versions.
 */
internal fun getAppVersionName(context: Context): String? {
    return try {
        val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(context.packageName, 0)
        }
        packageInfo.versionName
    } catch (_: Exception) {
        null
    }
}

/**
 * Returns the installed app's version code as a string.
 * Uses `longVersionCode` on API 28+ (Pie), falls back to deprecated `versionCode` below.
 */
internal fun getAppVersionCode(context: Context): String? {
    return try {
        val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(context.packageName, 0)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode.toString()
        } else {
            @Suppress("DEPRECATION")
            packageInfo.versionCode.toString()
        }
    } catch (_: Exception) {
        null
    }
}

private fun mapUpdateAvailability(availability: Int): String {
    return when (availability) {
        com.google.android.play.core.install.model.UpdateAvailability.UPDATE_AVAILABLE -> "UPDATE_AVAILABLE"
        com.google.android.play.core.install.model.UpdateAvailability.UPDATE_NOT_AVAILABLE -> "UPDATE_NOT_AVAILABLE"
        com.google.android.play.core.install.model.UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> "DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS"
        else -> "UNKNOWN"
    }
}
