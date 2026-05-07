package com.rnforge.inappupdates

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.google.android.play.core.install.model.InstallStatus
import com.margelo.nitro.core.NullType
import com.margelo.nitro.rnforge_inappupdates.AllowedFlowsNative
import com.margelo.nitro.rnforge_inappupdates.AndroidDetailsNative
import com.margelo.nitro.rnforge_inappupdates.CapabilitiesNative
import com.margelo.nitro.rnforge_inappupdates.UpdateStatusNative
import com.margelo.nitro.rnforge_inappupdates.Variant_NullType_Boolean

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
            ?: Variant_NullType_Boolean.create(NullType.null),
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
        installStatus = installStatus,
        android = android
    )
}
