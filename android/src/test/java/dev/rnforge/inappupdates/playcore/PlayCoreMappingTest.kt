package dev.rnforge.inappupdates.playcore

import android.app.Activity
import android.net.Uri
import com.google.android.play.core.install.InstallException
import com.google.android.play.core.install.model.ActivityResult
import com.google.android.play.core.install.model.InstallErrorCode
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
    fun mapInstallErrorCodeLabel_failedStatus_knownCodes() {
        assertEquals("error-unknown", mapInstallErrorCodeLabel(InstallStatus.FAILED, InstallErrorCode.ERROR_UNKNOWN))
        assertEquals("error-api-not-available", mapInstallErrorCodeLabel(InstallStatus.FAILED, InstallErrorCode.ERROR_API_NOT_AVAILABLE))
        assertEquals("error-app-not-owned", mapInstallErrorCodeLabel(InstallStatus.FAILED, InstallErrorCode.ERROR_APP_NOT_OWNED))
        assertEquals("error-download-not-present", mapInstallErrorCodeLabel(InstallStatus.FAILED, InstallErrorCode.ERROR_DOWNLOAD_NOT_PRESENT))
        assertEquals("error-install-in-progress", mapInstallErrorCodeLabel(InstallStatus.FAILED, -8))
        assertEquals("error-install-not-allowed", mapInstallErrorCodeLabel(InstallStatus.FAILED, InstallErrorCode.ERROR_INSTALL_NOT_ALLOWED))
        assertEquals("error-install-unavailable", mapInstallErrorCodeLabel(InstallStatus.FAILED, InstallErrorCode.ERROR_INSTALL_UNAVAILABLE))
        assertEquals("error-internal-error", mapInstallErrorCodeLabel(InstallStatus.FAILED, InstallErrorCode.ERROR_INTERNAL_ERROR))
        assertEquals("error-invalid-request", mapInstallErrorCodeLabel(InstallStatus.FAILED, InstallErrorCode.ERROR_INVALID_REQUEST))
        assertEquals("error-play-store-not-found", mapInstallErrorCodeLabel(InstallStatus.FAILED, InstallErrorCode.ERROR_PLAY_STORE_NOT_FOUND))
    }

    @Test
    fun mapInstallErrorCodeLabel_failedStatus_unknownCode() {
        assertEquals("install-error-42", mapInstallErrorCodeLabel(InstallStatus.FAILED, 42))
        assertEquals("install-error--99", mapInstallErrorCodeLabel(InstallStatus.FAILED, -99))
    }

    @Test
    @Suppress("DEPRECATION")
    fun mapInstallErrorCodeLabel_failedStatus_noError() {
        assertNull(mapInstallErrorCodeLabel(InstallStatus.FAILED, InstallErrorCode.NO_ERROR))
        assertNull(mapInstallErrorCodeLabel(InstallStatus.FAILED, InstallErrorCode.NO_ERROR_PARTIALLY_ALLOWED))
    }

    @Test
    fun mapInstallErrorCodeLabel_nonFailedStatus() {
        assertNull(mapInstallErrorCodeLabel(InstallStatus.DOWNLOADING, InstallErrorCode.ERROR_UNKNOWN))
        assertNull(mapInstallErrorCodeLabel(InstallStatus.INSTALLED, InstallErrorCode.ERROR_INTERNAL_ERROR))
        assertNull(mapInstallErrorCodeLabel(InstallStatus.UNKNOWN, InstallErrorCode.NO_ERROR))
    }

    @Test
    fun mapUpdatePrecondition_knownValues() {
        assertEquals("unknown", mapUpdatePrecondition(0))
        assertEquals("cannot-display", mapUpdatePrecondition(1))
        assertEquals("need-store-to-proceed", mapUpdatePrecondition(2))
        assertEquals("insufficient-storage", mapUpdatePrecondition(3))
        assertEquals("device-status", mapUpdatePrecondition(4))
        assertEquals("app-version-fresh", mapUpdatePrecondition(5))
    }

    @Test
    fun mapUpdatePrecondition_unknownValue() {
        assertEquals("unknown-99", mapUpdatePrecondition(99))
        assertEquals("unknown--1", mapUpdatePrecondition(-1))
    }

    @Test
    fun mapFlowResultReason_null_returnsUpdateAvailable() {
        assertEquals("update-available", mapFlowResultReason(null))
    }

    @Test
    fun mapFlowResultReason_RESULT_OK_returnsUpdateAvailable() {
        assertEquals("update-available", mapFlowResultReason(Activity.RESULT_OK))
    }

    @Test
    fun mapFlowResultReason_RESULT_CANCELED_returnsUserCanceled() {
        assertEquals("user-canceled", mapFlowResultReason(Activity.RESULT_CANCELED))
    }

    @Test
    fun mapFlowResultReason_RESULT_IN_APP_UPDATE_FAILED_returnsUnknown() {
        assertEquals("unknown", mapFlowResultReason(ActivityResult.RESULT_IN_APP_UPDATE_FAILED))
    }

    @Test
    fun mapFlowResultReason_unexpectedResultCode_returnsUnknown() {
        assertEquals("unknown", mapFlowResultReason(42))
        assertEquals("unknown", mapFlowResultReason(-99))
    }

    @Test
    fun createUnsupportedStatus_structure() {
        val status = createUnsupportedStatus("play-core-unavailable")
        assertEquals("android", status.platform)
        assertFalse(status.supported)
        assertEquals("play-core-unavailable", status.reason)
        assertFalse(status.capabilities.immediate)
        assertFalse(status.capabilities.flexible)
        assertTrue(status.capabilities.storePage)
        assertFalse(status.capabilities.latestVersionLookup)
        assertFalse(status.capabilities.installStateListener)
        assertFalse(status.allowed.immediate)
        assertFalse(status.allowed.flexible)
    }

    @Test
    fun createUnsupportedStatus_unsupportedInstallSource_preservesStorePage() {
        val status = createUnsupportedStatus("unsupported-install-source")
        assertEquals("unsupported-install-source", status.reason)
        assertFalse(status.supported)
        assertFalse(status.capabilities.immediate)
        assertFalse(status.capabilities.flexible)
        assertTrue(status.capabilities.storePage)
        assertFalse(status.capabilities.latestVersionLookup)
        assertFalse(status.capabilities.installStateListener)
    }

    @Test
    fun createUnsupportedStatus_playCoreUnavailable_preservesStorePage() {
        val status = createUnsupportedStatus("play-core-unavailable")
        assertEquals("play-core-unavailable", status.reason)
        assertFalse(status.supported)
        assertFalse(status.capabilities.immediate)
        assertFalse(status.capabilities.flexible)
        assertTrue(status.capabilities.storePage)
        assertFalse(status.capabilities.latestVersionLookup)
        assertFalse(status.capabilities.installStateListener)
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

    @Test
    fun createStatus_withBuildFields() {
        val status = createStatus(
            supported = true,
            updateAvailable = true,
            reason = "update-available",
            currentVersion = "1.2.3",
            currentBuild = "42",
            latestStoreBuild = "43"
        )
        assertEquals("1.2.3", status.currentVersion)
        assertEquals("42", status.currentBuild?.asFirstOrNull())
        assertEquals("43", status.latestStoreBuild?.asFirstOrNull())
    }

    @Test
    fun createStatus_buildFieldsDefaultToNull() {
        val status = createStatus(
            supported = true,
            updateAvailable = true,
            reason = "update-available"
        )
        assertNull(status.currentVersion)
        assertNull(status.currentBuild)
        assertNull(status.latestStoreBuild)
    }

    @Test
    fun createStatus_currentVersionWithoutBuildFields() {
        val status = createStatus(
            supported = true,
            updateAvailable = true,
            reason = "update-available",
            currentVersion = "2.0.0"
        )
        assertEquals("2.0.0", status.currentVersion)
        assertNull(status.currentBuild)
        assertNull(status.latestStoreBuild)
    }
}
