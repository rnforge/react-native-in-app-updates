package dev.rnforge.inappupdates.playcore

import android.content.Context
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory

/**
 * Provides a shared AppUpdateManager instance.
 *
 * The instance is lazily created and cached. Listeners and update flows
 * share this instance so that flexible update downloads started by
 * startFlexibleUpdate() can be observed by addInstallStateListener().
 */
object PlayCoreAppUpdateManager {

    @Volatile
    private var instance: AppUpdateManager? = null

    fun getInstance(context: Context): AppUpdateManager {
        return instance ?: synchronized(this) {
            instance ?: AppUpdateManagerFactory.create(context).also {
                instance = it
            }
        }
    }
}
