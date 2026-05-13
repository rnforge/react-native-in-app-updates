package dev.rnforge.inappupdates

import dev.rnforge.inappupdates.playcore.getInstallSource

import android.content.Context
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

/**
 * Injectable seam for environment checks that gate Play Core behavior.
 *
 * Production uses [DefaultEnvironmentChecker] which queries the real device.
 * Tests can inject a fake to control install source and Play Services state.
 */
interface EnvironmentChecker {
    fun getInstallSource(context: Context): String?
    fun isGooglePlayServicesAvailable(context: Context): Int
}

/**
 * Default production checker using PackageManager and GoogleApiAvailability.
 */
object DefaultEnvironmentChecker : EnvironmentChecker {
    override fun getInstallSource(context: Context): String? {
        return getInstallSource(context)
    }

    override fun isGooglePlayServicesAvailable(context: Context): Int {
        return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
    }
}
