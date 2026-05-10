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
class PlayCoreFlexibleUpdateServiceFakeManagerTest {

    private lateinit var fakeManager: FakeAppUpdateManager
    private lateinit var application: android.content.Context

    @Before
    fun setUp() {
        application = org.robolectric.RuntimeEnvironment.getApplication()
        fakeManager = FakeAppUpdateManager(application)
    }

    @Test
    fun startFlexibleUpdate_noUpdate_returnsNoUpdateAvailable() {
        fakeManager.setUpdateNotAvailable()
        val activity = Robolectric.buildActivity(android.app.Activity::class.java).setup().get()
        val service = service(activity)

        val status = awaitStatus { onSuccess, onFailure ->
            service.startFlexibleUpdate(null, onSuccess, onFailure)
        }

        assertTrue(status.supported)
        assertFalse(status.updateAvailable!!.asSecondOrNull() ?: true)
        assertEquals("no-update-available", status.reason)
    }

    @Test
    fun startFlexibleUpdate_updateAvailable_startsFlexibleFlow() {
        fakeManager.setUpdateAvailable(UpdateAvailability.UPDATE_AVAILABLE)
        val activity = Robolectric.buildActivity(android.app.Activity::class.java).setup().get()
        val service = service(activity)

        val status = awaitStatus { onSuccess, onFailure ->
            service.startFlexibleUpdate(null, onSuccess, onFailure)
        }

        assertEquals("update-available", status.reason)
        assertTrue(status.allowed.flexible)
        assertTrue(fakeManager.isConfirmationDialogVisible)
    }

    @Test
    fun startFlexibleUpdate_updateAvailableWithoutActivity_returnsUpdateNotAllowed() {
        fakeManager.setUpdateAvailable(UpdateAvailability.UPDATE_AVAILABLE)
        val service = service(activity = null)

        val status = awaitStatus { onSuccess, onFailure ->
            service.startFlexibleUpdate(null, onSuccess, onFailure)
        }

        assertEquals("update-not-allowed", status.reason)
        assertTrue(status.updateAvailable!!.asSecondOrNull() ?: false)
    }

    @Test
    fun startFlexibleUpdate_immediateOnly_returnsUpdateNotAllowed() {
        fakeManager.setUpdateAvailable(UpdateAvailability.UPDATE_AVAILABLE, AppUpdateType.IMMEDIATE)
        val activity = Robolectric.buildActivity(android.app.Activity::class.java).setup().get()
        val service = service(activity)

        val status = awaitStatus { onSuccess, onFailure ->
            service.startFlexibleUpdate(null, onSuccess, onFailure)
        }

        assertEquals("update-not-allowed", status.reason)
        assertTrue(status.allowed.immediate)
        assertFalse(status.allowed.flexible)
    }

    @Test
    fun completeFlexibleUpdate_withoutDownloadedUpdate_returnsUpdateNotAllowed() {
        fakeManager.setUpdateAvailable(UpdateAvailability.UPDATE_AVAILABLE)
        val activity = Robolectric.buildActivity(android.app.Activity::class.java).setup().get()
        val service = service(activity)

        val status = awaitStatus { onSuccess, onFailure ->
            service.completeFlexibleUpdate(onSuccess, onFailure)
        }

        assertEquals("update-not-allowed", status.reason)
        assertTrue(status.updateAvailable!!.asSecondOrNull() ?: false)
    }

    @Test
    fun completeFlexibleUpdate_playServicesUnavailable_returnsUnsupported() {
        val service = PlayCoreFlexibleUpdateService(
            managerProvider = FakeManagerProvider(fakeManager),
            envChecker = FakeEnvironmentChecker("com.android.vending", ConnectionResult.SERVICE_MISSING),
            activityProvider = FakeActivityProvider(application, null)
        )

        val status = awaitStatus { onSuccess, onFailure ->
            service.completeFlexibleUpdate(onSuccess, onFailure)
        }

        assertEquals("play-core-unavailable", status.reason)
        assertFalse(status.supported)
    }

    private fun service(activity: android.app.Activity?): PlayCoreFlexibleUpdateService {
        return PlayCoreFlexibleUpdateService(
            managerProvider = FakeManagerProvider(fakeManager),
            envChecker = FakeEnvironmentChecker(),
            activityProvider = FakeActivityProvider(application, activity)
        )
    }
}
