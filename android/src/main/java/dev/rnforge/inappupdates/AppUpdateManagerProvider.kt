package dev.rnforge.inappupdates

import android.content.Context
import com.google.android.play.core.appupdate.AppUpdateManager

/**
 * Injectable seam for obtaining a Play Core [AppUpdateManager].
 *
 * Production uses [PlayCoreAppUpdateManagerProvider] which delegates to the
 * shared singleton. Tests can inject a fake or mock provider to control
 * [AppUpdateInfo] and task behavior without real Play Store availability.
 */
interface AppUpdateManagerProvider {
    fun getManager(context: Context): AppUpdateManager
}

/**
 * Default production provider backed by [PlayCoreAppUpdateManager].
 */
object PlayCoreAppUpdateManagerProvider : AppUpdateManagerProvider {
    override fun getManager(context: Context): AppUpdateManager {
        return PlayCoreAppUpdateManager.getInstance(context)
    }
}
