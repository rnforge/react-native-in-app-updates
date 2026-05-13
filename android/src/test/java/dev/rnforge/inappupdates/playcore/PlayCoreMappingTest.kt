package dev.rnforge.inappupdates.playcore

import android.net.Uri
import com.google.android.play.core.install.InstallException
import com.google.android.play.core.install.model.InstallStatus
import org.junit.Assume.assumeNotNull
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Tests for mapping functions in PlayCoreMapping.kt.
 *
 * These target mapping logic and do not require Play Store availability,
 * an emulator, or instrumentation. They still require the Android/Play Core/
 * Nitro test classpath from a Gradle harness.
 */
@RunWith(RobolectricTestRunner::class)
class PlayCoreMappingTest {

    @Test
    fun mapInstallStatus_knownStatuses() {
        assertEquals("unknown", mapInstallStatus(InstallStatus.UNKNOWN))
        assertEquals("pending", mapInstallStatus(InstallStatus.PENDING))
        assertEquals("downloading", mapInstallStatus(InstallStatus.DOWNLOADING))
        assertEquals("downloaded", mapInstallStatus(InstallStatus.DOWNLOADED))
        assertEquals("installing", mapInstallStatus(InstallStatus.INSTALLING))
        assertEquals("installed", mapInstallStatus(InstallStatus.INSTALLED))
        assertEquals("failed", mapInstallStatus(InstallStatus.FAILED))
        assertEquals("canceled", mapInstallStatus(InstallStatus.CANCELED))
    }

    @Test
    fun mapInstallStatus_unknownValue() {
        assertEquals("unknown", mapInstallStatus(-1))
        assertEquals("unknown", mapInstallStatus(999))
    }

    @Test
    fun buildAppUpdateOptions_default() {
        val options = buildAppUpdateOptions(0)
        assertNotNull(options)
        // Default options do not allow asset pack deletion.
        // We verify the object is created without crashing.
    }

    @Test
    fun buildAppUpdateOptions_allowAssetPackDeletion_true() {
        val options = buildAppUpdateOptions(0, allowAssetPackDeletion = true)
        assertNotNull(options)
        // allowAssetPackDeletion is set via builder; there is no public getter,
        // but we verify the object is created without crashing.
    }

    @Test
    fun buildAppUpdateOptions_allowAssetPackDeletion_false() {
        val options = buildAppUpdateOptions(0, allowAssetPackDeletion = false)
        assertNotNull(options)
    }

    @Test
    fun buildAppUpdateOptions_allowAssetPackDeletion_null() {
        val options = buildAppUpdateOptions(0, allowAssetPackDeletion = null)
        assertNotNull(options)
    }

    @Test
    fun encodeTaskFailure_regularException() {
        val original = Exception("Something went wrong")
        val encoded = encodeTaskFailure(original)
        assertTrue(encoded.message!!.contains("Something went wrong"))
        assertSame(original, encoded.cause)
    }

    @Test
    fun encodeTaskFailure_installException() {
        // InstallException constructor is package-private in some Play Core versions;
        // we construct via reflection to keep the test stable.
        val installException = try {
            val ctor = InstallException::class.java.getDeclaredConstructor(Int::class.java)
            ctor.isAccessible = true
            ctor.newInstance(2) // error code 2 = ERROR_PLAY_STORE_NOT_FOUND
        } catch (_: Exception) {
            null
        }

        assumeNotNull("InstallException could not be constructed via reflection", installException)

        val encoded = encodeTaskFailure(installException as Exception)
        assertTrue("Should contain PLAY_CORE_TASK_FAILURE prefix", encoded.message!!.startsWith("PLAY_CORE_TASK_FAILURE"))
        assertTrue("Should contain taskErrorCode", encoded.message!!.contains("taskErrorCode="))
    }

    @Test
    fun mapInstallErrorCodeLabel_failedStatus() {
        assertEquals("INSTALL_ERROR_1", mapInstallErrorCodeLabel(InstallStatus.FAILED, 1))
        assertEquals("INSTALL_ERROR_42", mapInstallErrorCodeLabel(InstallStatus.FAILED, 42))
    }

    @Test
    fun mapInstallErrorCodeLabel_nonFailedStatus() {
        assertNull(mapInstallErrorCodeLabel(InstallStatus.DOWNLOADING, 1))
        assertNull(mapInstallErrorCodeLabel(InstallStatus.INSTALLED, 42))
        assertNull(mapInstallErrorCodeLabel(InstallStatus.UNKNOWN, 0))
    }

    @Test
    fun createUnsupportedStatus_structure() {
        val status = createUnsupportedStatus("play-core-unavailable")
        assertEquals("android", status.platform)
        assertFalse(status.supported)
        assertEquals("play-core-unavailable", status.reason)
        assertFalse(status.capabilities.immediate)
        assertFalse(status.capabilities.flexible)
        assertFalse(status.capabilities.storePage)
        assertFalse(status.capabilities.latestVersionLookup)
        assertFalse(status.capabilities.installStateListener)
        assertFalse(status.allowed.immediate)
        assertFalse(status.allowed.flexible)
    }

    @Test
    fun createStatus_withAllFields() {
        val status = createStatus(
            supported = true,
            updateAvailable = true,
            reason = "update-available",
            immediateAllowed = true,
            flexibleAllowed = false,
            installStatus = "downloading"
        )
        assertEquals("android", status.platform)
        assertTrue(status.supported)
        assertTrue(status.updateAvailable?.asSecondOrNull() ?: false)
        assertEquals("update-available", status.reason)
        assertTrue(status.allowed.immediate)
        assertFalse(status.allowed.flexible)
        assertEquals("downloading", status.installStatus)
    }

    @Test
    fun createStatus_nullUpdateAvailable() {
        val status = createStatus(
            supported = true,
            updateAvailable = null,
            reason = "update-not-allowed"
        )
        assertTrue(status.supported)
        assertTrue(status.updateAvailable?.isFirst ?: false)
        assertEquals("update-not-allowed", status.reason)
    }
}
