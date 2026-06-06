package dev.rnforge.inappupdates.playcore

import android.app.Activity
import android.content.pm.PackageInfo
import android.os.Build
import com.google.android.gms.tasks.Tasks
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows

@RunWith(RobolectricTestRunner::class)
class PlayCoreFlowResultMockManagerTest {

    private lateinit var application: android.content.Context
    private lateinit var mockInfo: AppUpdateInfo
    private lateinit var mockManager: AppUpdateManager

    @Before
    fun setUp() {
        application = org.robolectric.RuntimeEnvironment.getApplication()

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

        mockInfo = Mockito.mock(AppUpdateInfo::class.java)
        Mockito.`when`(mockInfo.updateAvailability()).thenReturn(UpdateAvailability.UPDATE_AVAILABLE)
        Mockito.`when`(mockInfo.isUpdateTypeAllowed(Mockito.any<AppUpdateOptions>())).thenReturn(true)
        Mockito.`when`(mockInfo.availableVersionCode()).thenReturn(100)
        Mockito.`when`(mockInfo.installStatus()).thenReturn(InstallStatus.PENDING)

        mockManager = Mockito.mock(AppUpdateManager::class.java)
        Mockito.`when`(mockManager.getAppUpdateInfo()).thenReturn(Tasks.forResult(mockInfo))
    }

    @Test
    fun startImmediateUpdate_userCancels_returnsUserCanceled() {
        Mockito.`when`(
            mockManager.startUpdateFlow(
                Mockito.any(),
                Mockito.any(),
                Mockito.any()
            )
        ).thenReturn(Tasks.forResult(Activity.RESULT_CANCELED as Int))

        val activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        val service = immediateService(activity)

        val status = awaitStatus { onSuccess, onFailure ->
            service.startImmediateUpdate(null, onSuccess, onFailure)
        }

        assertEquals("RESULT_CANCELED should map to 'user-canceled'",
            "user-canceled", status.reason)
        assertNotNull(status.android)
        assertEquals(application.packageName, status.android!!.packageName)
        assertNotNull(status.android!!.playCore)
        assertEquals("UPDATE_AVAILABLE", status.android!!.playCore!!.updateAvailability)
    }

    @Test
    fun startFlexibleUpdate_userCancels_returnsUserCanceled() {
        Mockito.`when`(
            mockManager.startUpdateFlow(
                Mockito.any(),
                Mockito.any(),
                Mockito.any()
            )
        ).thenReturn(Tasks.forResult(Activity.RESULT_CANCELED as Int))

        val activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        val service = flexibleService(activity)

        val status = awaitStatus { onSuccess, onFailure ->
            service.startFlexibleUpdate(null, onSuccess, onFailure)
        }

        assertEquals("RESULT_CANCELED should map to 'user-canceled'",
            "user-canceled", status.reason)
        assertNotNull(status.android)
        assertEquals(application.packageName, status.android!!.packageName)
        assertNotNull(status.android!!.playCore)
        assertEquals("UPDATE_AVAILABLE", status.android!!.playCore!!.updateAvailability)
    }

    @Test
    fun startImmediateUpdate_userCancels_preservesDiagnostics() {
        Mockito.`when`(
            mockManager.startUpdateFlow(
                Mockito.any(),
                Mockito.any(),
                Mockito.any()
            )
        ).thenReturn(Tasks.forResult(Activity.RESULT_CANCELED as Int))

        val activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        val service = immediateService(activity)

        val status = awaitStatus { onSuccess, onFailure ->
            service.startImmediateUpdate(null, onSuccess, onFailure)
        }

        assertNotNull("android details should be present", status.android)
        assertEquals(application.packageName, status.android!!.packageName)
        assertNotNull("playCore details should be present", status.android!!.playCore)
        assertEquals("UPDATE_AVAILABLE", status.android!!.playCore!!.updateAvailability)
        assertEquals("1.0.0", status.currentVersion)
    }

    @Test
    fun startFlexibleUpdate_userCancels_preservesDiagnostics() {
        Mockito.`when`(
            mockManager.startUpdateFlow(
                Mockito.any(),
                Mockito.any(),
                Mockito.any()
            )
        ).thenReturn(Tasks.forResult(Activity.RESULT_CANCELED as Int))

        val activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        val service = flexibleService(activity)

        val status = awaitStatus { onSuccess, onFailure ->
            service.startFlexibleUpdate(null, onSuccess, onFailure)
        }

        assertNotNull("android details should be present", status.android)
        assertEquals(application.packageName, status.android!!.packageName)
        assertNotNull("playCore details should be present", status.android!!.playCore)
        assertEquals("UPDATE_AVAILABLE", status.android!!.playCore!!.updateAvailability)
        assertEquals("1.0.0", status.currentVersion)
    }

    private fun immediateService(activity: Activity?): PlayCoreImmediateUpdateService {
        return PlayCoreImmediateUpdateService(
            managerProvider = FakeManagerProvider(mockManager),
            envChecker = FakeEnvironmentChecker(),
            activityProvider = FakeActivityProvider(application, activity)
        )
    }

    private fun flexibleService(activity: Activity?): PlayCoreFlexibleUpdateService {
        return PlayCoreFlexibleUpdateService(
            managerProvider = FakeManagerProvider(mockManager),
            envChecker = FakeEnvironmentChecker(),
            activityProvider = FakeActivityProvider(application, activity)
        )
    }
}
