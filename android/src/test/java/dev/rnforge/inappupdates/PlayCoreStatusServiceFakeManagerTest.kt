package dev.rnforge.inappupdates

import com.google.android.play.core.appupdate.testing.FakeAppUpdateManager
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PlayCoreStatusServiceFakeManagerTest {

    private lateinit var fakeManager: FakeAppUpdateManager
    private lateinit var activityProvider: ActivityProvider

    @Before
    fun setUp() {
        val application = org.robolectric.RuntimeEnvironment.getApplication()
        fakeManager = FakeAppUpdateManager(application)
        activityProvider = FakeActivityProvider(application, null)
    }

    @Test
    fun getUpdateStatus_noUpdate_available_returnsNotAvailable() {
        fakeManager.setUpdateNotAvailable()

        val service = PlayCoreStatusService(
            managerProvider = FakeManagerProvider(fakeManager),
            envChecker = FakeEnvironmentChecker(),
            activityProvider = activityProvider
        )

        val status = awaitStatus { onSuccess, onFailure ->
            service.getUpdateStatus(null, onSuccess, onFailure)
        }

        assertTrue(status.supported)
        assertFalse(status.updateAvailable!!.asSecondOrNull() ?: true)
        assertEquals("no-update-available", status.reason)
    }

    @Test
    fun getUpdateStatus_updateAvailable_allowsBothFlows() {
        fakeManager.setUpdateAvailable(UpdateAvailability.UPDATE_AVAILABLE)

        val service = PlayCoreStatusService(
            managerProvider = FakeManagerProvider(fakeManager),
            envChecker = FakeEnvironmentChecker(),
            activityProvider = activityProvider
        )

        val status = awaitStatus { onSuccess, onFailure ->
            service.getUpdateStatus(null, onSuccess, onFailure)
        }

        assertTrue(status.supported)
        assertTrue(status.updateAvailable!!.asSecondOrNull() ?: false)
        assertEquals("update-available", status.reason)
        assertTrue(status.allowed.immediate)
        assertTrue(status.allowed.flexible)
        assertEquals("UPDATE_AVAILABLE", status.android?.playCore?.updateAvailability)
    }

    @Test
    fun getUpdateStatus_updateAvailable_immediateOnly() {
        fakeManager.setUpdateAvailable(UpdateAvailability.UPDATE_AVAILABLE, AppUpdateType.IMMEDIATE)

        val service = PlayCoreStatusService(
            managerProvider = FakeManagerProvider(fakeManager),
            envChecker = FakeEnvironmentChecker(),
            activityProvider = activityProvider
        )

        val status = awaitStatus { onSuccess, onFailure ->
            service.getUpdateStatus(null, onSuccess, onFailure)
        }

        assertTrue(status.allowed.immediate)
        assertFalse(status.allowed.flexible)
    }

    @Test
    fun getUpdateStatus_updateAvailable_flexibleOnly() {
        fakeManager.setUpdateAvailable(UpdateAvailability.UPDATE_AVAILABLE, AppUpdateType.FLEXIBLE)

        val service = PlayCoreStatusService(
            managerProvider = FakeManagerProvider(fakeManager),
            envChecker = FakeEnvironmentChecker(),
            activityProvider = activityProvider
        )

        val status = awaitStatus { onSuccess, onFailure ->
            service.getUpdateStatus(null, onSuccess, onFailure)
        }

        assertFalse(status.allowed.immediate)
        assertTrue(status.allowed.flexible)
    }
}
