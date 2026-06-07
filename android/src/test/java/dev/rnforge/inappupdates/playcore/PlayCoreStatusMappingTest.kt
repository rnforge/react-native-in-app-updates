package dev.rnforge.inappupdates.playcore

import android.content.Context
import android.content.pm.PackageInfo
import android.os.Build
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.margelo.nitro.rnforge.inappupdates.PlayCoreDetailsNative
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class PlayCoreStatusMappingTest {

    private lateinit var application: Context
    private lateinit var mockInfo: AppUpdateInfo

    @Before
    fun setUp() {
        application = org.robolectric.RuntimeEnvironment.getApplication()

        val packageManager = Shadows.shadowOf(application.packageManager)
        val packageInfo = PackageInfo().apply {
            packageName = application.packageName
            versionName = "1.0.0"
            longVersionCode = 42L
        }
        packageManager.addPackage(packageInfo)

        mockInfo = Mockito.mock(AppUpdateInfo::class.java)
        Mockito.`when`(mockInfo.updatePriority()).thenReturn(5)
        Mockito.`when`(mockInfo.availableVersionCode()).thenReturn(100)
        Mockito.`when`(mockInfo.bytesDownloaded()).thenReturn(0L)
        Mockito.`when`(mockInfo.totalBytesToDownload()).thenReturn(0L)
        Mockito.`when`(mockInfo.installStatus()).thenReturn(InstallStatus.PENDING)
        Mockito.`when`(mockInfo.clientVersionStalenessDays()).thenReturn(null)
        Mockito.`when`(mockInfo.isUpdateTypeAllowed(Mockito.any<AppUpdateOptions>())).thenReturn(true)
    }

    // ---- mapAppUpdateInfoToStatus ----

    @Test
    fun mapAppUpdateInfoToStatus_updateAvailable() {
        Mockito.`when`(mockInfo.updateAvailability()).thenReturn(UpdateAvailability.UPDATE_AVAILABLE)

        val status = mapAppUpdateInfoToStatus(mockInfo, application, null)

        assertTrue(status.supported)
        assertTrue(status.updateAvailable?.asSecondOrNull() ?: false)
        assertEquals("update-available", status.reason)
        assertTrue(status.allowed.immediate)
        assertTrue(status.allowed.flexible)
        assertNotNull(status.android?.playCore)
        assertEquals("UPDATE_AVAILABLE", status.android!!.playCore!!.updateAvailability)
        assertEquals("1.0.0", status.currentVersion)
        assertNotNull(status.latestStoreBuild)
    }

    @Test
    fun mapAppUpdateInfoToStatus_updateNotAvailable() {
        Mockito.`when`(mockInfo.updateAvailability()).thenReturn(UpdateAvailability.UPDATE_NOT_AVAILABLE)

        val status = mapAppUpdateInfoToStatus(mockInfo, application, null)

        assertTrue(status.supported)
        assertFalse(status.updateAvailable?.asSecondOrNull() ?: true)
        assertEquals("no-update-available", status.reason)
        assertNull(status.latestStoreBuild)
    }

    @Test
    fun mapAppUpdateInfoToStatus_developerTriggeredUpdateInProgress() {
        Mockito.`when`(mockInfo.updateAvailability()).thenReturn(
            UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
        )

        val status = mapAppUpdateInfoToStatus(mockInfo, application, null)

        assertTrue(status.supported)
        assertTrue(status.updateAvailable?.asSecondOrNull() ?: false)
        assertEquals("developer-triggered-update-in-progress", status.reason)
        assertNotNull(status.latestStoreBuild)
    }

    @Test
    fun mapAppUpdateInfoToStatus_unknownAvailability() {
        Mockito.`when`(mockInfo.updateAvailability()).thenReturn(-99)

        val status = mapAppUpdateInfoToStatus(mockInfo, application, null)

        assertTrue(status.supported)
        assertFalse(status.updateAvailable?.asSecondOrNull() ?: true)
        assertEquals("unknown", status.reason)
        assertNull(status.latestStoreBuild)
    }

    @Test
    fun mapAppUpdateInfoToStatus_immediateOnly() {
        Mockito.`when`(mockInfo.updateAvailability()).thenReturn(UpdateAvailability.UPDATE_AVAILABLE)
        Mockito.`when`(mockInfo.isUpdateTypeAllowed(Mockito.any<AppUpdateOptions>())).thenAnswer { invocation ->
            val options = invocation.getArgument<AppUpdateOptions>(0)
            options.appUpdateType() == AppUpdateType.IMMEDIATE
        }

        val status = mapAppUpdateInfoToStatus(mockInfo, application, null)

        assertTrue(status.allowed.immediate)
        assertFalse(status.allowed.flexible)
        assertTrue(status.android?.playCore?.immediateAllowed ?: false)
        assertFalse(status.android?.playCore?.flexibleAllowed ?: true)
    }

    @Test
    fun mapAppUpdateInfoToStatus_flexibleOnly() {
        Mockito.`when`(mockInfo.updateAvailability()).thenReturn(UpdateAvailability.UPDATE_AVAILABLE)
        Mockito.`when`(mockInfo.isUpdateTypeAllowed(Mockito.any<AppUpdateOptions>())).thenAnswer { invocation ->
            val options = invocation.getArgument<AppUpdateOptions>(0)
            options.appUpdateType() == AppUpdateType.FLEXIBLE
        }

        val status = mapAppUpdateInfoToStatus(mockInfo, application, null)

        assertFalse(status.allowed.immediate)
        assertTrue(status.allowed.flexible)
    }

    // ---- buildUpdateStatusFromInfo ----

    @Test
    fun buildUpdateStatusFromInfo_supportedTrue_updateAvailableTrue() {
        Mockito.`when`(mockInfo.updateAvailability()).thenReturn(UpdateAvailability.UPDATE_AVAILABLE)

        val status = buildUpdateStatusFromInfo(
            context = application,
            info = mockInfo,
            supported = true,
            updateAvailable = true,
            reason = "update-available",
            immediateAllowed = true,
            flexibleAllowed = false,
            installStatus = "pending"
        )

        assertTrue(status.supported)
        assertTrue(status.updateAvailable?.asSecondOrNull() ?: false)
        assertEquals("update-available", status.reason)
        assertTrue(status.allowed.immediate)
        assertFalse(status.allowed.flexible)
        assertEquals("pending", status.installStatus)
        assertEquals("1.0.0", status.currentVersion)
        assertNotNull(status.latestStoreBuild)
    }

    @Test
    fun buildUpdateStatusFromInfo_supportedTrue_updateAvailableFalse() {
        Mockito.`when`(mockInfo.updateAvailability()).thenReturn(UpdateAvailability.UPDATE_NOT_AVAILABLE)

        val status = buildUpdateStatusFromInfo(
            context = application,
            info = mockInfo,
            supported = true,
            updateAvailable = false,
            reason = "no-update-available"
        )

        assertTrue(status.supported)
        assertFalse(status.updateAvailable?.asSecondOrNull() ?: true)
        assertEquals("no-update-available", status.reason)
        assertNull(status.latestStoreBuild)
    }

    @Test
    fun buildUpdateStatusFromInfo_withAdditionalPlayCore() {
        Mockito.`when`(mockInfo.updateAvailability()).thenReturn(UpdateAvailability.UPDATE_AVAILABLE)

        val pc = PlayCoreDetailsNative(
            updateAvailability = "UPDATE_AVAILABLE",
            installStatus = "pending",
            immediateAllowed = true,
            flexibleAllowed = true,
            immediateFailedPreconditions = null,
            flexibleFailedPreconditions = null,
            installErrorCode = null,
            taskErrorCode = null,
            updatePriority = null,
            clientVersionStalenessDays = null,
            availableVersionCode = null,
            bytesDownloaded = null,
            totalBytesToDownload = null
        )

        val status = buildUpdateStatusFromInfo(
            context = application,
            info = mockInfo,
            supported = true,
            updateAvailable = true,
            reason = "update-available",
            additionalPlayCore = pc
        )

        assertNotNull(status.android)
        assertEquals(application.packageName, status.android!!.packageName)
        assertNotNull(status.android!!.playCore)
        assertEquals("UPDATE_AVAILABLE", status.android!!.playCore!!.updateAvailability)
    }

    @Test
    fun buildUpdateStatusFromInfo_nullAdditionalPlayCore_noAndroidDetails() {
        Mockito.`when`(mockInfo.updateAvailability()).thenReturn(UpdateAvailability.UPDATE_NOT_AVAILABLE)

        val status = buildUpdateStatusFromInfo(
            context = application,
            info = mockInfo,
            supported = true,
            updateAvailable = false,
            reason = "no-update-available",
            additionalPlayCore = null
        )

        assertNull(status.android)
    }

    @Test
    fun buildUpdateStatusFromInfo_developerTriggered_populatesLatestStoreBuild() {
        Mockito.`when`(mockInfo.updateAvailability()).thenReturn(
            UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
        )

        val status = buildUpdateStatusFromInfo(
            context = application,
            info = mockInfo,
            supported = true,
            updateAvailable = true,
            reason = "developer-triggered-update-in-progress"
        )

        assertNotNull("latestStoreBuild should be populated when update is in progress", status.latestStoreBuild)
    }

    @Test
    fun buildUpdateStatusFromInfo_supportedFalse_preservesVersionFields() {
        Mockito.`when`(mockInfo.updateAvailability()).thenReturn(UpdateAvailability.UPDATE_AVAILABLE)

        val status = buildUpdateStatusFromInfo(
            context = application,
            info = mockInfo,
            supported = false,
            updateAvailable = null,
            reason = "unsupported-install-source"
        )

        assertFalse(status.supported)
        assertEquals("unsupported-install-source", status.reason)
        assertEquals("1.0.0", status.currentVersion)
        assertNotNull(status.latestStoreBuild) // still populated because availability is UPDATE_AVAILABLE
    }

    // ---- buildFlowPlayCoreDetails ----

    @Test
    fun buildFlowPlayCoreDetails_populatesAvailabilityAndInstallStatus() {
        Mockito.`when`(mockInfo.updateAvailability()).thenReturn(UpdateAvailability.UPDATE_AVAILABLE)
        Mockito.`when`(mockInfo.installStatus()).thenReturn(InstallStatus.DOWNLOADING)

        val details = buildFlowPlayCoreDetails(mockInfo, true, false)

        assertEquals("UPDATE_AVAILABLE", details.updateAvailability)
        assertEquals("downloading", details.installStatus)
        assertEquals(true, details.immediateAllowed)
        assertEquals(false, details.flexibleAllowed)
    }

    @Test
    fun buildFlowPlayCoreDetails_nullAllowed() {
        Mockito.`when`(mockInfo.updateAvailability()).thenReturn(UpdateAvailability.UPDATE_NOT_AVAILABLE)
        Mockito.`when`(mockInfo.installStatus()).thenReturn(InstallStatus.UNKNOWN)

        val details = buildFlowPlayCoreDetails(mockInfo, null, null)

        assertEquals("UPDATE_NOT_AVAILABLE", details.updateAvailability)
        assertEquals("unknown", details.installStatus)
        assertNull(details.immediateAllowed)
        assertNull(details.flexibleAllowed)
    }

    @Test
    fun buildFlowPlayCoreDetails_omitsExpensiveFields() {
        Mockito.`when`(mockInfo.updateAvailability()).thenReturn(UpdateAvailability.UPDATE_AVAILABLE)
        Mockito.`when`(mockInfo.installStatus()).thenReturn(InstallStatus.INSTALLED)

        val details = buildFlowPlayCoreDetails(mockInfo, false, true)

        assertNull(details.immediateFailedPreconditions)
        assertNull(details.flexibleFailedPreconditions)
        assertNull(details.installErrorCode)
        assertNull(details.taskErrorCode)
        assertNull(details.updatePriority)
        assertNull(details.clientVersionStalenessDays)
        assertNull(details.availableVersionCode)
        assertNull(details.bytesDownloaded)
        assertNull(details.totalBytesToDownload)
    }

    // ---- mapUpdateAvailability ----

    @Test
    fun mapUpdateAvailability_knownValues() {
        assertEquals("UPDATE_AVAILABLE", mapUpdateAvailability(UpdateAvailability.UPDATE_AVAILABLE))
        assertEquals("UPDATE_NOT_AVAILABLE", mapUpdateAvailability(UpdateAvailability.UPDATE_NOT_AVAILABLE))
        assertEquals("DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS",
            mapUpdateAvailability(UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS))
    }

    @Test
    fun mapUpdateAvailability_unknownValue() {
        assertEquals("UNKNOWN", mapUpdateAvailability(-99))
        assertEquals("UNKNOWN", mapUpdateAvailability(42))
    }
}
