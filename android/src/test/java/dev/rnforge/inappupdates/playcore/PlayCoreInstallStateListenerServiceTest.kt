package dev.rnforge.inappupdates.playcore

import dev.rnforge.inappupdates.ActivityProvider
import dev.rnforge.inappupdates.EnvironmentChecker

import org.junit.Assert.*
import org.junit.Test

/**
 * Tests for PlayCoreInstallStateListenerService null-context safety.
 *
 * These verify that the service does not crash or invoke the injected
 * [AppUpdateManagerProvider] when the injected [ActivityProvider] has no
 * application context.
 *
 * We intentionally do not implement [com.google.android.play.core.appupdate.AppUpdateManager]
 * directly here because its interface surface may expand across Play Core
 * versions and cause compile-time breakage.
 */
class PlayCoreInstallStateListenerServiceTest {

    private object NullActivityProvider : ActivityProvider {
        override val applicationContext: android.content.Context? = null
        override val currentActivity: android.app.Activity? = null
    }

    private class ThrowingProvider : AppUpdateManagerProvider {
        override fun getManager(context: android.content.Context): com.google.android.play.core.appupdate.AppUpdateManager {
            throw AssertionError("Provider should not be invoked when context is null")
        }
    }

    @Test
    fun addInstallStateListener_nullContext_doesNotInvokeProvider() {
        val service = PlayCoreInstallStateListenerService(ThrowingProvider(), NullActivityProvider)
        val listenerId = service.addInstallStateListener { _ -> }

        assertTrue("Should generate a non-empty listener ID", listenerId.isNotEmpty())
    }

    @Test
    fun removeInstallStateListener_unknownId_isNoOp() {
        val service = PlayCoreInstallStateListenerService(ThrowingProvider(), NullActivityProvider)

        service.removeInstallStateListener("non-existent-id")
    }

    @Test
    fun addAndRemoveInstallStateListener_nullContext_isSafe() {
        val service = PlayCoreInstallStateListenerService(ThrowingProvider(), NullActivityProvider)
        val listenerId = service.addInstallStateListener { _ -> }
        service.removeInstallStateListener(listenerId)
    }
}
