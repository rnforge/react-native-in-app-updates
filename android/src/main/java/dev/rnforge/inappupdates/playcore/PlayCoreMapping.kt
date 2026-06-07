package dev.rnforge.inappupdates.playcore

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallException
import com.google.android.play.core.install.model.InstallErrorCode
import com.google.android.play.core.install.model.InstallStatus
import com.margelo.nitro.core.NullType
import com.margelo.nitro.rnforge.inappupdates.AllowedFlowsNative
import com.margelo.nitro.rnforge.inappupdates.AndroidDetailsNative
import com.margelo.nitro.rnforge.inappupdates.CapabilitiesNative
import com.margelo.nitro.rnforge.inappupdates.UpdateStatusNative
import com.margelo.nitro.rnforge.inappupdates.Variant_NullType_Boolean
import com.margelo.nitro.rnforge.inappupdates.Variant_String_Double

fun getInstallSource(context: Context): String? {
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

fun mapInstallStatus(status: Int): String {
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

fun createUnsupportedStatus(
    reason: String,
    currentVersion: String? = null,
    currentBuild: String? = null
): UpdateStatusNative {
    return UpdateStatusNative(
        platform = "android",
        supported = false,
        updateAvailable = Variant_NullType_Boolean.create(NullType.NULL),
        capabilities = CapabilitiesNative(
            immediate = false,
            flexible = false,
            storePage = reason == "unsupported-install-source" || reason == "play-core-unavailable",
            latestVersionLookup = false,
            installStateListener = false
        ),
        allowed = AllowedFlowsNative(
            immediate = false,
            flexible = false
        ),
        reason = reason,
        currentVersion = currentVersion,
        currentBuild = currentBuild?.let { Variant_String_Double.create(it) },
        latestStoreVersion = null,
        latestStoreBuild = null,
        installStatus = null,
        android = null,
        ios = null
    )
}

fun createStatus(
    supported: Boolean,
    updateAvailable: Boolean?,
    reason: String,
    immediateAllowed: Boolean? = null,
    flexibleAllowed: Boolean? = null,
    installStatus: String? = null,
    android: AndroidDetailsNative? = null,
    currentVersion: String? = null,
    currentBuild: String? = null,
    latestStoreBuild: String? = null
): UpdateStatusNative {
    return UpdateStatusNative(
        platform = "android",
        supported = supported,
        updateAvailable = updateAvailable?.let { Variant_NullType_Boolean.create(it) }
            ?: Variant_NullType_Boolean.create(NullType.NULL),
        capabilities = CapabilitiesNative(
            immediate = true,
            flexible = true,
            storePage = true,
            latestVersionLookup = false,
            installStateListener = true
        ),
        allowed = AllowedFlowsNative(
            immediate = immediateAllowed ?: false,
            flexible = flexibleAllowed ?: false
        ),
        reason = reason,
        currentVersion = currentVersion,
        currentBuild = currentBuild?.let { Variant_String_Double.create(it) },
        latestStoreVersion = null,
        latestStoreBuild = latestStoreBuild?.let { Variant_String_Double.create(it) },
        installStatus = installStatus,
        android = android,
        ios = null
    )
}

fun mapFailedUpdatePreconditionsOrNull(
    appUpdateInfo: AppUpdateInfo,
    appUpdateType: Int,
    allowAssetPackDeletion: Boolean? = null
): List<String>? {
    return try {
        val method = appUpdateInfo.javaClass.getMethod(
            "getFailedUpdatePreconditions",
            AppUpdateOptions::class.java
        )
        val options = buildAppUpdateOptions(appUpdateType, allowAssetPackDeletion)
        @Suppress("UNCHECKED_CAST")
        (method.invoke(appUpdateInfo, options) as? Iterable<Any>)?.map { precondition ->
            val rawValue = when (precondition) {
                is Int -> precondition
                is Number -> precondition.toInt()
                else -> precondition.toString().toIntOrNull()
            }
            if (rawValue != null) mapUpdatePrecondition(rawValue) else "unknown-${precondition}"
        }
    } catch (_: Exception) {
        null
    }
}

/**
 * Maps official Play Core [UpdatePrecondition] integer constants to stable RNForge semantic labels.
 *
 * Official constants: https://developer.android.com/reference/com/google/android/play/core/install/model/UpdatePrecondition
 */
internal fun mapUpdatePrecondition(rawValue: Int): String {
    return when (rawValue) {
        0 -> "unknown"
        1 -> "cannot-display"
        2 -> "need-store-to-proceed"
        3 -> "insufficient-storage"
        4 -> "device-status"
        5 -> "app-version-fresh"
        else -> "unknown-$rawValue"
    }
}

fun buildAppUpdateOptions(
    appUpdateType: Int,
    allowAssetPackDeletion: Boolean? = null
): AppUpdateOptions {
    return if (allowAssetPackDeletion == true) {
        AppUpdateOptions.newBuilder(appUpdateType)
            .setAllowAssetPackDeletion(true)
            .build()
    } else {
        AppUpdateOptions.defaultOptions(appUpdateType)
    }
}

fun encodeTaskFailure(error: Exception): Exception {
    val message = when (error) {
        is InstallException -> {
            "PLAY_CORE_TASK_FAILURE|message=${Uri.encode(error.message ?: "Play Core task failed")}|taskErrorCode=${error.errorCode}"
        }
        else -> error.message ?: "Play Core task failed"
    }
    return Exception(message, error)
}

fun mapFlowResultReason(resultCode: Int?): String {
    return when (resultCode) {
        null, Activity.RESULT_OK -> "update-available"
        Activity.RESULT_CANCELED -> "user-canceled"
        else -> "unknown"
    }
}

/**
 * Maps Play Core [InstallErrorCode] values to official semantic labels.
 *
 * Returns null for non-failed statuses or for error code 0 (NO_ERROR).
 * Falls back to `install-error-<code>` for unknown codes.
 *
 * Official constants: https://developer.android.com/reference/com/google/android/play/core/install/model/InstallErrorCode
 */
fun mapInstallErrorCodeLabel(status: Int, rawErrorCode: Int): String? {
    if (status != InstallStatus.FAILED) return null
    if (rawErrorCode == InstallErrorCode.NO_ERROR) return null
    @Suppress("DEPRECATION")
    if (rawErrorCode == InstallErrorCode.NO_ERROR_PARTIALLY_ALLOWED) return null

    // ERROR_INSTALL_IN_PROGRESS = -8 is documented in current official Android docs
    // but is not exposed as a constant in com.google.android.play:app-update:2.1.0.
    // Map the raw value directly for compile-safe compatibility.
    if (rawErrorCode == -8) return "error-install-in-progress"

    return when (rawErrorCode) {
        InstallErrorCode.ERROR_UNKNOWN -> "error-unknown"
        InstallErrorCode.ERROR_API_NOT_AVAILABLE -> "error-api-not-available"
        InstallErrorCode.ERROR_APP_NOT_OWNED -> "error-app-not-owned"
        InstallErrorCode.ERROR_DOWNLOAD_NOT_PRESENT -> "error-download-not-present"
        InstallErrorCode.ERROR_INSTALL_NOT_ALLOWED -> "error-install-not-allowed"
        InstallErrorCode.ERROR_INSTALL_UNAVAILABLE -> "error-install-unavailable"
        InstallErrorCode.ERROR_INTERNAL_ERROR -> "error-internal-error"
        InstallErrorCode.ERROR_INVALID_REQUEST -> "error-invalid-request"
        InstallErrorCode.ERROR_PLAY_STORE_NOT_FOUND -> "error-play-store-not-found"
        else -> "install-error-$rawErrorCode"
    }
}
