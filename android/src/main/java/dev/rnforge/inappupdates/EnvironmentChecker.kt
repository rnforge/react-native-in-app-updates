package dev.rnforge.inappupdates

import dev.rnforge.inappupdates.playcore.getInstallSource

import android.content.Context
import android.os.Build
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

/**
 * Injectable seam for environment checks that gate Play Core behavior.
 *
 * Production uses [DefaultEnvironmentChecker] which queries the real device.
 * Tests can inject a fake to control install source and Play Services state.
 */
interface EnvironmentChecker {
    fun isSupportedOsVersion(): Boolean
    fun hasApkExpansionFiles(context: Context): Boolean
    fun getInstallSource(context: Context): String?
    fun isGooglePlayServicesAvailable(context: Context): Int
}

/**
 * Default production checker using PackageManager and GoogleApiAvailability.
 */
object DefaultEnvironmentChecker : EnvironmentChecker {
    override fun isSupportedOsVersion(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
    }

    override fun hasApkExpansionFiles(context: Context): Boolean {
        return try {
            context.obbDirs.any { dir ->
                dir?.listFiles()?.any { file ->
                    file.isFile && file.name.endsWith(".obb", ignoreCase = true)
                } == true
            }
        } catch (e: Exception) {
            false
        }
    }

    override fun getInstallSource(context: Context): String? {
        return dev.rnforge.inappupdates.playcore.getInstallSource(context)
    }

    override fun isGooglePlayServicesAvailable(context: Context): Int {
        return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
    }
}
