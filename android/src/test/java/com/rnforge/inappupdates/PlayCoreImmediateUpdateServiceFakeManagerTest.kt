package com.rnforge.inappupdates

import com.google.android.gms.common.ConnectionResult
import com.google.android.play.core.appupdate.testing.FakeAppUpdateManager
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PlayCoreImmediateUpdateServiceFakeManagerTest {

    private lateinit var fakeManager: FakeAppUpdateManager
    private lateinit var application: android.content.Context

    @Before
    fun setUp() {
        application = org.robolectric.RuntimeEnvironment.getApplication()
        fakeManager = FakeAppUpdateManager(application)
    }

    @Test
    fun startImmediateUpdate_noUpdate_returnsNoUpdateAvailable() {
        fakeManager.setUpdateNotAvailable()
        val activity = Robolectric.buildActivity(android.app.Activity::class.java).setup().get()
        val service = service(activity)

        val status = awaitStatus { onSuccess, onFailure ->
            service.startImmediateUpdate(null, onSuccess, onFailure)
        }

        assertTrue(status.supported)
        assertFalse(status.updateAvailable!!.asSecondOrNull() ?: true)
        assertEquals("no-update-available", status.reason)
    }

    @Test
    fun startImmediateUpdate_updateAvailable_startsImmediateFlow() {
        fakeManager.setUpdateAvailable(UpdateAvailability.UPDATE_AVAILABLE)
        val activity = Robolectric.buildActivity(android.app.Activity::class.java).setup().get()
        val service = service(activity)

        val status = awaitStatus { onSuccess, onFailure ->
            service.startImmediateUpdate(null, onSuccess, onFailure)
        }

        assertEquals("update-available", status.reason)
        assertTrue(status.allowed.immediate)
        assertTrue(fakeManager.isImmediateFlowVisible)
    }

    @Test
    fun startImmediateUpdate_updateAvailableWithoutActivity_returnsUpdateNotAllowed() {
        fakeManager.setUpdateAvailable(UpdateAvailability.UPDATE_AVAILABLE)
        val service = service(activity = null)

        val status = awaitStatus { onSuccess, onFailure ->
            service.startImmediateUpdate(null, onSuccess, onFailure)
        }

        assertEquals("update-not-allowed", status.reason)
        assertTrue(status.updateAvailable!!.asSecondOrNull() ?: false)
    }

    @Test
    fun startImmediateUpdate_flexibleOnly_returnsUpdateNotAllowed() {
        fakeManager.setUpdateAvailable(UpdateAvailability.UPDATE_AVAILABLE, AppUpdateType.FLEXIBLE)
        val activity = Robolectric.buildActivity(android.app.Activity::class.java).setup().get()
        val service = service(activity)

        val status = awaitStatus { onSuccess, onFailure ->
            service.startImmediateUpdate(null, onSuccess, onFailure)
        }

        assertEquals("update-not-allowed", status.reason)
        assertFalse(status.allowed.immediate)
        assertTrue(status.allowed.flexible)
    }

    @Test
    fun startImmediateUpdate_nonPlayInstall_returnsUnsupportedInstallSource() {
        fakeManager.setUpdateAvailable(UpdateAvailability.UPDATE_AVAILABLE)
        val service = PlayCoreImmediateUpdateService(
            managerProvider = FakeManagerProvider(fakeManager),
            envChecker = FakeEnvironmentChecker("com.example.store", ConnectionResult.SUCCESS),
            activityProvider = FakeActivityProvider(application, null)
        )

        val status = awaitStatus { onSuccess, onFailure ->
            service.startImmediateUpdate(null, onSuccess, onFailure)
        }

        assertEquals("unsupported-install-source", status.reason)
        assertFalse(status.supported)
    }

    private fun service(activity: android.app.Activity?): PlayCoreImmediateUpdateService {
        return PlayCoreImmediateUpdateService(
            managerProvider = FakeManagerProvider(fakeManager),
            envChecker = FakeEnvironmentChecker(),
            activityProvider = FakeActivityProvider(application, activity)
        )
    }
}
