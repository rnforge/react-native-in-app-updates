package dev.rnforge.inappupdates.playcore

import dev.rnforge.inappupdates.ActivityProvider
import dev.rnforge.inappupdates.EnvironmentChecker

import android.app.Activity
import android.content.Context
import android.os.Looper
import com.google.android.gms.common.ConnectionResult
import com.google.android.play.core.appupdate.AppUpdateManager
import com.margelo.nitro.rnforge.inappupdates.UpdateStatusNative
import org.junit.Assert.assertTrue
import org.robolectric.Shadows.shadowOf
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

internal data class FakeActivityProvider(
    override val applicationContext: Context?,
    override val currentActivity: Activity?
) : ActivityProvider

internal class FakeEnvironmentChecker(
    private val installSource: String? = "com.android.vending",
    private val playServicesResult: Int = ConnectionResult.SUCCESS
) : EnvironmentChecker {
    override fun getInstallSource(context: Context): String? = installSource
    override fun isGooglePlayServicesAvailable(context: Context): Int = playServicesResult
}

internal class FakeManagerProvider(
    private val manager: AppUpdateManager
) : AppUpdateManagerProvider {
    override fun getManager(context: Context): AppUpdateManager = manager
}

internal fun awaitStatus(
    call: (
        onSuccess: (UpdateStatusNative) -> Unit,
        onFailure: (Exception) -> Unit
    ) -> Unit
): UpdateStatusNative {
    val latch = CountDownLatch(1)
    val statusRef = AtomicReference<UpdateStatusNative?>()
    val errorRef = AtomicReference<Exception?>()

    call(
        { status ->
            statusRef.set(status)
            latch.countDown()
        },
        { error ->
            errorRef.set(error)
            latch.countDown()
        }
    )
    shadowOf(Looper.getMainLooper()).idle()

    assertTrue("Timed out waiting for status", latch.await(5, TimeUnit.SECONDS))
    errorRef.get()?.let { throw it }
    return requireNotNull(statusRef.get()) { "Expected status" }
}
