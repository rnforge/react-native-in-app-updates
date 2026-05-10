package com.rnforge.inappupdates

import android.app.Activity
import android.content.Context
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.atomic.AtomicReference

@RunWith(RobolectricTestRunner::class)
class PlayCoreStoreServiceTest {

    @Test
    fun openStorePage_nullContext_returnsFailure() {
        val service = PlayCoreStoreService(FakeActivityProvider(null, null))
        val errorRef = AtomicReference<Exception?>()

        service.openStorePage(
            onSuccess = { throw AssertionError("Expected failure") },
            onFailure = { errorRef.set(it) }
        )

        assertEquals("Context not available", errorRef.get()?.message)
    }

    @Test
    fun openStorePage_opensStoreIntentWithNewTaskFlag() {
        val activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        val service = PlayCoreStoreService(FakeActivityProvider(activity.applicationContext, activity))
        val errorRef = AtomicReference<Exception?>()
        var success = false

        service.openStorePage(
            onSuccess = { success = true },
            onFailure = { errorRef.set(it) }
        )

        assertNull(errorRef.get())
        assertTrue(success)
        val startedIntent = org.robolectric.Shadows.shadowOf(activity.application).nextStartedActivity
        assertNotNull(startedIntent)
        assertEquals("android.intent.action.VIEW", startedIntent.action)
        assertTrue(startedIntent.data.toString().contains(activity.packageName))
        assertTrue((startedIntent.flags and android.content.Intent.FLAG_ACTIVITY_NEW_TASK) != 0)
    }
}
