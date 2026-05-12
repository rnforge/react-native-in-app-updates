package dev.rnforge.inappupdates

import android.app.Activity
import android.content.Context
import com.facebook.react.bridge.ReactApplicationContext
import com.margelo.nitro.NitroModules

/**
 * Provides access to the current React Native Activity and application context.
 *
 * Uses [NitroModules.applicationContext] as the source of truth, which is set
 * when the NitroModules TurboModule is initialized by React Native.
 *
 * This is more reliable than relying on our own Package.getModule() path,
 * because NitroModules is a properly registered TurboModule that React Native
 * always initializes before HybridObjects can be created.
 */
object InAppUpdatesActivityProvider {

    @JvmStatic
    val applicationContext: Context?
        get() = NitroModules.applicationContext?.applicationContext

    @JvmStatic
    val reactApplicationContext: ReactApplicationContext?
        get() = NitroModules.applicationContext

    @JvmStatic
    val currentActivity: Activity?
        get() = NitroModules.applicationContext?.currentActivity
}
