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
 *
 * Testability: inject [AppUpdateManagerProvider] to control the manager
 * used for register/unregister operations.
 */
class PlayCoreInstallStateListenerService(
    private val managerProvider: AppUpdateManagerProvider = PlayCoreAppUpdateManagerProvider
) {

    private data class ListenerRegistration(
        val listener: InstallStateUpdatedListener,
        val manager: com.google.android.play.core.appupdate.AppUpdateManager?
    )

    private val listeners = mutableMapOf<String, ListenerRegistration>()

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
                errorCode = mapInstallErrorCodeLabel(state.installStatus(), state.installErrorCode()),
                message = null,
                android = AndroidDetailsNative(
                    packageName = context?.packageName,
                    playCore = PlayCoreDetailsNative(
                        installStatus = installStatus,
                        bytesDownloaded = bytesDownloaded,
                        totalBytesToDownload = totalBytesToDownload,
                        installErrorCode = state.installErrorCode().toDouble()
                    )
                )
            )
            callback(event)
        }

        val manager = if (context != null) {
            managerProvider.getManager(context).also { it.registerListener(listener) }
        } else null

        listeners[listenerId] = ListenerRegistration(listener, manager)

        return listenerId
    }

    fun removeInstallStateListener(listenerId: String) {
        val registration = listeners.remove(listenerId)
        if (registration != null) {
            registration.manager?.unregisterListener(registration.listener)
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
