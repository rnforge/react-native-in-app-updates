package com.rnforge.inappupdates

import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.InstallStatus
import com.margelo.nitro.rnforge_inappupdates.AndroidDetailsNative
import com.margelo.nitro.rnforge_inappupdates.InstallStateEventNative
import com.margelo.nitro.rnforge_inappupdates.PlayCoreDetailsNative
import java.util.UUID

/**
 * Manages Play Core install-state listeners for flexible update progress/events.
 *
 * Listeners are registered on the shared AppUpdateManager so that flexible
 * update downloads started by startFlexibleUpdate() can be observed.
 */
class PlayCoreInstallStateListenerService {

    private val listeners = mutableMapOf<String, InstallStateUpdatedListener>()

    fun addInstallStateListener(callback: (event: InstallStateEventNative) -> Unit): String {
        val listenerId = UUID.randomUUID().toString()
        val context = InAppUpdatesActivityProvider.applicationContext

        val listener = InstallStateUpdatedListener { state ->
            val bytesDownloaded = state.bytesDownloaded().toDouble()
            val totalBytesToDownload = state.totalBytesToDownload().toDouble()
            val progress = if (totalBytesToDownload > 0) bytesDownloaded / totalBytesToDownload else null
            val installStatus = mapInstallStatus(state.installStatus())
            val reason = mapReason(state.installStatus())

            val event = InstallStateEventNative(
                platform = "android",
                supported = true,
                installStatus = installStatus,
                reason = reason,
                bytesDownloaded = bytesDownloaded,
                totalBytesToDownload = totalBytesToDownload,
                progress = progress,
                errorCode = null,
                message = null,
                android = AndroidDetailsNative(
                    packageName = context?.packageName,
                    playCore = PlayCoreDetailsNative(
                        installStatus = installStatus,
                        bytesDownloaded = bytesDownloaded,
                        totalBytesToDownload = totalBytesToDownload
                    )
                )
            )
            callback(event)
        }

        listeners[listenerId] = listener

        if (context != null) {
            val appUpdateManager = PlayCoreAppUpdateManager.getInstance(context)
            appUpdateManager.registerListener(listener)
        }

        return listenerId
    }

    fun removeInstallStateListener(listenerId: String) {
        val listener = listeners.remove(listenerId)
        if (listener != null) {
            val context = InAppUpdatesActivityProvider.applicationContext
            if (context != null) {
                val appUpdateManager = PlayCoreAppUpdateManager.getInstance(context)
                appUpdateManager.unregisterListener(listener)
            }
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

    private fun mapReason(status: Int): String {
        return when (status) {
            InstallStatus.DOWNLOADING -> "download-progress"
            InstallStatus.DOWNLOADED -> "flexible-update-downloaded"
            InstallStatus.INSTALLING,
            InstallStatus.INSTALLED,
            InstallStatus.FAILED,
            InstallStatus.CANCELED -> "install-state-changed"
            else -> "unknown"
        }
    }
}
