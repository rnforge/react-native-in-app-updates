package dev.rnforge.inappupdates.playcore

import org.junit.Assert.*
import org.junit.Test

/**
 * Tests for the shared environment guard used by Play Core services.
 *
 * These verify the null-context early-return path. Non-null context paths
 * (install source and Play Services checks) require a real or mocked Context
 * and are not covered here.
 */
class PlayCoreEnvironmentTest {

    @Test
    fun checkEarlyEnvironment_nullContext_returnsUpdateNotAllowed() {
        val checker = FakeEnvironmentChecker()
        val status = checkEarlyEnvironment(null, checker)

        assertNotNull(status)
        assertTrue(status!!.supported)
        assertTrue(status.updateAvailable?.isFirst ?: false)
        assertEquals("update-not-allowed", status.reason)
    }
}
