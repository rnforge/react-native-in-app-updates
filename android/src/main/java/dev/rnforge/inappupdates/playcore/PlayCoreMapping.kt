package dev.rnforge.inappupdates.playcore

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallException
import com.google.android.play.core.install.model.InstallStatus
import com.margelo.nitro.core.NullType
import com.margelo.nitro.rnforge.inappupdates.AllowedFlowsNative
import com.margelo.nitro.rnforge.inappupdates.AndroidDetailsNative
import com.margelo.nitro.rnforge.inappupdates.CapabilitiesNative
import com.margelo.nitro.rnforge.inappupdates.UpdateStatusNative
import com.margelo.nitro.rnforge.inappupdates.Variant_NullType_Boolean

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

fun createUnsupportedStatus(reason: String): UpdateStatusNative {
    return UpdateStatusNative(
        platform = "android",
        supported = false,
        updateAvailable = Variant_NullType_Boolean.create(NullType.NULL),
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
        reason = reason,
        currentVersion = null,
        currentBuild = null,
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
    android: AndroidDetailsNative? = null
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
        currentVersion = null,
        currentBuild = null,
        latestStoreVersion = null,
        latestStoreBuild = null,
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
        (method.invoke(appUpdateInfo, options) as? Iterable<Any>)?.map { it.toString() }
    } catch (_: Exception) {
        null
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

fun mapInstallErrorCodeLabel(status: Int, rawErrorCode: Int): String? {
    return if (status == InstallStatus.FAILED) {
        "INSTALL_ERROR_$rawErrorCode"
    } else {
        null
    }
}
