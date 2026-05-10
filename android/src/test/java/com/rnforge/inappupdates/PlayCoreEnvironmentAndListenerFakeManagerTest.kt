package com.rnforge.inappupdates

import com.google.android.gms.common.ConnectionResult
import com.google.android.play.core.appupdate.testing.FakeAppUpdateManager
import com.google.android.play.core.install.model.UpdateAvailability
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PlayCoreEnvironmentAndListenerFakeManagerTest {

    private lateinit var fakeManager: FakeAppUpdateManager
    private lateinit var application: android.content.Context

    @Before
    fun setUp() {
        application = org.robolectric.RuntimeEnvironment.getApplication()
        fakeManager = FakeAppUpdateManager(application)
    }

    @Test
    fun checkEarlyEnvironment_nonPlayInstallSource_returnsUnsupportedInstallSource() {
        val status = checkEarlyEnvironment(
            application,
            FakeEnvironmentChecker("com.example.other", ConnectionResult.SUCCESS)
        )

        assertNotNull(status)
        assertEquals("unsupported-install-source", status!!.reason)
        assertFalse(status.supported)
    }

    @Test
    fun checkEarlyEnvironment_playServicesUnavailable_returnsPlayCoreUnavailable() {
        val status = checkEarlyEnvironment(
            application,
            FakeEnvironmentChecker("com.android.vending", 1)
        )

        assertNotNull(status)
        assertEquals("play-core-unavailable", status!!.reason)
        assertFalse(status.supported)
    }

    @Test
    fun installStateListener_receivesDownloadingAndDownloadedEvents() {
        val activity = Robolectric.buildActivity(android.app.Activity::class.java).setup().get()
        val events = mutableListOf<String>()
        val service = PlayCoreInstallStateListenerService(
            managerProvider = FakeManagerProvider(fakeManager),
            activityProvider = FakeActivityProvider(application, activity)
        )
        val updateService = PlayCoreFlexibleUpdateService(
            managerProvider = FakeManagerProvider(fakeManager),
            envChecker = FakeEnvironmentChecker(),
            activityProvider = FakeActivityProvider(application, activity)
        )

        val listenerId = service.addInstallStateListener { event ->
            events.add(event.installStatus)
        }

        fakeManager.setUpdateAvailable(UpdateAvailability.UPDATE_AVAILABLE)
        awaitStatus { onSuccess, onFailure ->
            updateService.startFlexibleUpdate(null, onSuccess, onFailure)
        }
        fakeManager.userAcceptsUpdate()
        fakeManager.downloadStarts()
        fakeManager.setTotalBytesToDownload(20)
        fakeManager.setBytesDownloaded(10)
        fakeManager.downloadCompletes()

        service.removeInstallStateListener(listenerId)

        assertTrue(events.contains("downloading"))
        assertTrue(events.contains("downloaded"))
    }

    @Test
    fun completeFlexibleUpdate_afterDownloaded_returnsDownloadedStatus() {
        val activity = Robolectric.buildActivity(android.app.Activity::class.java).setup().get()
        fakeManager.setUpdateAvailable(UpdateAvailability.UPDATE_AVAILABLE)
        val startService = PlayCoreFlexibleUpdateService(
            managerProvider = FakeManagerProvider(fakeManager),
            envChecker = FakeEnvironmentChecker(),
            activityProvider = FakeActivityProvider(application, activity)
        )

        awaitStatus { onSuccess, onFailure ->
            startService.startFlexibleUpdate(null, onSuccess, onFailure)
        }
        fakeManager.userAcceptsUpdate()
        fakeManager.downloadStarts()
        fakeManager.setTotalBytesToDownload(20)
        fakeManager.setBytesDownloaded(20)
        fakeManager.downloadCompletes()

        val service = PlayCoreFlexibleUpdateService(
            managerProvider = FakeManagerProvider(fakeManager),
            envChecker = FakeEnvironmentChecker(),
            activityProvider = FakeActivityProvider(application, activity)
        )

        val status = awaitStatus { onSuccess, onFailure ->
            service.completeFlexibleUpdate(onSuccess, onFailure)
        }

        assertEquals("flexible-update-downloaded", status.reason)
        assertTrue(status.allowed.flexible)
    }
}
