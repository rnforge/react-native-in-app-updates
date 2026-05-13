package dev.rnforge.inappupdates

import android.content.Context
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.tasks.Tasks
import com.google.android.play.core.appupdate.AppUpdateManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

@RunWith(RobolectricTestRunner::class)
class PlayCoreServiceFailureTest {

    @Test
    fun statusService_nullContext_returnsTypedStatusWithoutManager() {
        val service = PlayCoreStatusService(
            managerProvider = ThrowingManagerProvider(),
            envChecker = FakeEnvironmentChecker(),
            activityProvider = FakeActivityProvider(null, null)
        )

        val status = awaitStatus { onSuccess, onFailure ->
            service.getUpdateStatus(null, onSuccess, onFailure)
        }

        assertTrue(status.supported)
        assertTrue(status.updateAvailable?.isFirst ?: false)
        assertEquals("update-not-allowed", status.reason)
    }

    @Test
    fun immediateService_playServicesUnavailable_returnsTypedUnsupported() {
        val context = org.robolectric.RuntimeEnvironment.getApplication()
        val service = PlayCoreImmediateUpdateService(
            managerProvider = ThrowingManagerProvider(),
            envChecker = FakeEnvironmentChecker("com.android.vending", ConnectionResult.SERVICE_MISSING),
            activityProvider = FakeActivityProvider(context, null)
        )

        val status = awaitStatus { onSuccess, onFailure ->
            service.startImmediateUpdate(null, onSuccess, onFailure)
        }

        assertFalse(status.supported)
        assertEquals("play-core-unavailable", status.reason)
    }

    @Test
    fun statusService_appUpdateInfoFailure_returnsEncodedFailure() {
        val context = org.robolectric.RuntimeEnvironment.getApplication()
        val service = PlayCoreStatusService(
            managerProvider = FailingInfoManagerProvider(Exception("status failed")),
            envChecker = FakeEnvironmentChecker(),
            activityProvider = FakeActivityProvider(context, null)
        )

        val error = awaitFailure { onSuccess, onFailure ->
            service.getUpdateStatus(null, onSuccess, onFailure)
        }

        assertEquals("status failed", error.message)
    }

    @Test
    fun flexibleService_appUpdateInfoFailure_returnsEncodedFailure() {
        val context = org.robolectric.RuntimeEnvironment.getApplication()
        val service = PlayCoreFlexibleUpdateService(
            managerProvider = FailingInfoManagerProvider(Exception("flex failed")),
            envChecker = FakeEnvironmentChecker(),
            activityProvider = FakeActivityProvider(context, null)
        )

        val error = awaitFailure { onSuccess, onFailure ->
            service.startFlexibleUpdate(null, onSuccess, onFailure)
        }

        assertEquals("flex failed", error.message)
    }

    @Test
    fun immediateService_appUpdateInfoFailure_returnsEncodedFailure() {
        val context = org.robolectric.RuntimeEnvironment.getApplication()
        val service = PlayCoreImmediateUpdateService(
            managerProvider = FailingInfoManagerProvider(Exception("immediate failed")),
            envChecker = FakeEnvironmentChecker(),
            activityProvider = FakeActivityProvider(context, null)
        )

        val error = awaitFailure { onSuccess, onFailure ->
            service.startImmediateUpdate(null, onSuccess, onFailure)
        }

        assertEquals("immediate failed", error.message)
    }

    private class ThrowingManagerProvider : AppUpdateManagerProvider {
        override fun getManager(context: Context): AppUpdateManager {
            throw AssertionError("Manager should not be requested")
        }
    }

    private class FailingInfoManagerProvider(
        private val error: Exception
    ) : AppUpdateManagerProvider {
        override fun getManager(context: Context): AppUpdateManager {
            val manager = mock(AppUpdateManager::class.java)
            `when`(manager.appUpdateInfo).thenReturn(Tasks.forException(error))
            return manager
        }
    }

    private fun awaitFailure(
        call: (
            onSuccess: (com.margelo.nitro.rnforge.inappupdates.UpdateStatusNative) -> Unit,
            onFailure: (Exception) -> Unit
        ) -> Unit
    ): Exception {
        val latch = CountDownLatch(1)
        val errorRef = AtomicReference<Exception?>()

        call(
            { throw AssertionError("Expected failure") },
            { error ->
                errorRef.set(error)
                latch.countDown()
            }
        )
        shadowOf(android.os.Looper.getMainLooper()).idle()

        assertTrue("Timed out waiting for failure", latch.await(5, TimeUnit.SECONDS))
        return requireNotNull(errorRef.get()) { "Expected failure" }
    }
}
