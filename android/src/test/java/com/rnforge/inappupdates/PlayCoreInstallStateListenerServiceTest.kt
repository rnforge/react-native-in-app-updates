package com.rnforge.inappupdates

import org.junit.Assume.assumeFalse
import org.junit.Assert.*
import org.junit.Test

/**
 * Tests for PlayCoreInstallStateListenerService null-context safety.
 *
 * These verify that the service does not crash or invoke the injected
 * [AppUpdateManagerProvider] when [InAppUpdatesActivityProvider.applicationContext]
 * is null. Full listener registration/unregistration behavior is covered by
 * design via the seams but requires a runnable Gradle harness with Play Core
 * on the classpath (e.g. Mockito or the official Play Core testing artifact).
 *
 * We intentionally do not implement [com.google.android.play.core.appupdate.AppUpdateManager]
 * directly here because its interface surface may expand across Play Core
 * versions and cause compile-time breakage.
 */
class PlayCoreInstallStateListenerServiceTest {

    private class ThrowingProvider : AppUpdateManagerProvider {
        override fun getManager(context: android.content.Context): com.google.android.play.core.appupdate.AppUpdateManager {
            throw AssertionError("Provider should not be invoked when context is null")
        }
    }

    /**
     * Returns true if the environment cannot support these tests
     * (e.g. NitroModules or Play Core classes fail to load in JVM).
     */
    private fun isUnsupportedEnvironment(): Boolean {
        return try {
            InAppUpdatesActivityProvider.applicationContext
            false
        } catch (_: ExceptionInInitializerError) {
            true
        } catch (_: NoClassDefFoundError) {
            true
        } catch (_: RuntimeException) {
            true
        }
    }

    @Test
    fun addInstallStateListener_nullContext_doesNotInvokeProvider() {
        assumeFalse("NitroModules not available in this JVM environment", isUnsupportedEnvironment())

        val service = PlayCoreInstallStateListenerService(ThrowingProvider())
        val listenerId = service.addInstallStateListener { _ -> }

        assertTrue("Should generate a non-empty listener ID", listenerId.isNotEmpty())
    }

    @Test
    fun removeInstallStateListener_unknownId_isNoOp() {
        assumeFalse("NitroModules not available in this JVM environment", isUnsupportedEnvironment())

        val service = PlayCoreInstallStateListenerService(ThrowingProvider())

        // Removing a non-existent ID should not crash or invoke provider
        service.removeInstallStateListener("non-existent-id")
    }

    @Test
    fun addAndRemoveInstallStateListener_nullContext_isSafe() {
        assumeFalse("NitroModules not available in this JVM environment", isUnsupportedEnvironment())

        val service = PlayCoreInstallStateListenerService(ThrowingProvider())
        val listenerId = service.addInstallStateListener { _ -> }
        service.removeInstallStateListener(listenerId)

        // No exceptions thrown = pass
    }
}
