package dev.rnforge.inappupdates

import android.app.Activity
import org.junit.Assert.assertNotNull
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

/**
 * Guards against the regression where DefaultEnvironmentChecker.getInstallSource()
 * called itself recursively instead of delegating to the PlayCore helper.
 *
 * The current proper implementation must not throw StackOverflowError
 * and must complete with a valid install-source String or null.
 */
@RunWith(RobolectricTestRunner::class)
class DefaultEnvironmentCheckerRecursionTest {

    @Test
    fun getInstallSource_completesWithoutRecursion() {
        val activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        val context = activity.applicationContext
        assertNotNull(context)

        try {
            val source = DefaultEnvironmentChecker.getInstallSource(context)
        } catch (e: StackOverflowError) {
            fail("getInstallSource() recursed into itself (unqualified call)")
        }
    }
}
