package dev.rnforge.inappupdates

import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.module.model.ReactModuleInfoProvider
import com.facebook.react.BaseReactPackage
import com.margelo.nitro.rnforge.inappupdates.InAppUpdatesOnLoad

/**
 * React Package for InAppUpdates.
 *
 * This package's primary responsibility is ensuring the native C++ library
 * is loaded via [InAppUpdatesOnLoad.initializeNative()]. The ReactApplicationContext
 * needed by [InAppUpdatesActivityProvider] comes from [com.margelo.nitro.NitroModules.applicationContext],
 * which is set when the NitroModules TurboModule is initialized — a more reliable path
 * than depending on this package's [getModule] being called.
 */
public class InAppUpdatesPackage : BaseReactPackage() {
  override fun getModule(name: String, reactContext: ReactApplicationContext): NativeModule? {
    return null
  }

  override fun getReactModuleInfoProvider(): ReactModuleInfoProvider = ReactModuleInfoProvider { emptyMap() }

  companion object {
    init {
      InAppUpdatesOnLoad.initializeNative()
    }
  }
}
