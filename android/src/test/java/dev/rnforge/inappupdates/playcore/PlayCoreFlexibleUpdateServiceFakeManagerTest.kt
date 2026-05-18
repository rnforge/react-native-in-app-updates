package dev.rnforge.inappupdates.playcore

import com.google.android.gms.common.ConnectionResult
import com.google.android.play.core.appupdate.testing.FakeAppUpdateManager
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import android.content.pm.PackageInfo
import android.os.Build

@RunWith(RobolectricTestRunner::class)
class PlayCoreFlexibleUpdateServiceFakeManagerTest {

    private lateinit var fakeManager: FakeAppUpdateManager
    private lateinit var application: android.content.Context

    @Before
    fun setUp() {
        application = org.robolectric.RuntimeEnvironment.getApplication()
        fakeManager = FakeAppUpdateManager(application)

        val packageManager = Shadows.shadowOf(application.packageManager)
        val packageInfo = PackageInfo().apply {
            packageName = application.packageName
            versionName = "1.0.0"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                longVersionCode = 42L
            } else {
                @Suppress("DEPRECATION")
                versionCode = 42
            }
        }
        packageManager.addPackage(packageInfo)
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

    @Test
    fun startFlexibleUpdate_updateAvailable_populatesCurrentVersion() {
        fakeManager.setUpdateAvailable(UpdateAvailability.UPDATE_AVAILABLE)
        val activity = Robolectric.buildActivity(android.app.Activity::class.java).setup().get()
        val service = service(activity)

        val status = awaitStatus { onSuccess, onFailure ->
            service.startFlexibleUpdate(null, onSuccess, onFailure)
        }

        assertEquals("1.0.0", status.currentVersion)
    }

    @Test
    fun startFlexibleUpdate_updateAvailable_populatesCurrentBuild() {
        fakeManager.setUpdateAvailable(UpdateAvailability.UPDATE_AVAILABLE)
        val activity = Robolectric.buildActivity(android.app.Activity::class.java).setup().get()
        val service = service(activity)

        val status = awaitStatus { onSuccess, onFailure ->
            service.startFlexibleUpdate(null, onSuccess, onFailure)
        }

        assertEquals("42", status.currentBuild?.asFirstOrNull())
    }

    @Test
    fun startFlexibleUpdate_updateAvailable_populatesLatestStoreBuild() {
        fakeManager.setUpdateAvailable(UpdateAvailability.UPDATE_AVAILABLE)
        val activity = Robolectric.buildActivity(android.app.Activity::class.java).setup().get()
        val service = service(activity)

        val status = awaitStatus { onSuccess, onFailure ->
            service.startFlexibleUpdate(null, onSuccess, onFailure)
        }

        assertNotNull("latestStoreBuild should be populated when update is available", status.latestStoreBuild)
    }

    @Test
    fun startFlexibleUpdate_noUpdate_available_latestStoreBuildIsNull() {
        fakeManager.setUpdateNotAvailable()
        val activity = Robolectric.buildActivity(android.app.Activity::class.java).setup().get()
        val service = service(activity)

        val status = awaitStatus { onSuccess, onFailure ->
            service.startFlexibleUpdate(null, onSuccess, onFailure)
        }

        assertNull("latestStoreBuild should be null when update is not available", status.latestStoreBuild)
    }

    private fun service(activity: android.app.Activity?): PlayCoreFlexibleUpdateService {
        return PlayCoreFlexibleUpdateService(
            managerProvider = FakeManagerProvider(fakeManager),
            envChecker = FakeEnvironmentChecker(),
            activityProvider = FakeActivityProvider(application, activity)
        )
    }
}
