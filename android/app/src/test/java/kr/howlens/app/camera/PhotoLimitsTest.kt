package kr.howlens.app.camera

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PhotoLimitsTest {
    @Test
    fun acceptsPositiveResolutionAtOrBelowTwentyMegapixels() {
        assertTrue(isResolutionWithinPhotoLimit(5000, 4000))
        assertTrue(isResolutionWithinPhotoLimit(4000, 5000))
        assertFalse(isResolutionWithinPhotoLimit(5001, 4000))
        assertFalse(isResolutionWithinPhotoLimit(0, 1000))
    }

    @Test
    fun acceptsNonEmptyJpegAtOrBelowTenMebibytes() {
        assertFalse(isJpegSizeWithinPhotoLimit(0L))
        assertTrue(isJpegSizeWithinPhotoLimit(10L * 1024L * 1024L))
        assertFalse(isJpegSizeWithinPhotoLimit(10L * 1024L * 1024L + 1L))
    }

    @Test
    fun permissionStateUpdatesOnFirstGrantDenialAndSettingsReturn() {
        val initial = resolveCameraPermissionUiState(
            granted = false,
            requestWasMade = false,
            shouldShowRationale = false,
        )
        assertFalse(initial.granted)
        assertFalse(initial.denied)
        assertFalse(initial.permanentlyDenied)

        val firstGrant = resolveCameraPermissionUiState(
            granted = true,
            requestWasMade = true,
            shouldShowRationale = false,
        )
        assertTrue(firstGrant.granted)
        assertFalse(firstGrant.denied)
        assertFalse(firstGrant.permanentlyDenied)

        val deniedWithRationale = resolveCameraPermissionUiState(
            granted = false,
            requestWasMade = true,
            shouldShowRationale = true,
        )
        assertTrue(deniedWithRationale.denied)
        assertFalse(deniedWithRationale.permanentlyDenied)

        val deniedWithoutRationale = resolveCameraPermissionUiState(
            granted = false,
            requestWasMade = true,
            shouldShowRationale = false,
        )
        assertTrue(deniedWithoutRationale.permanentlyDenied)

        val returnedFromSettings = resolveCameraPermissionUiState(
            granted = true,
            requestWasMade = true,
            shouldShowRationale = false,
        )
        assertTrue(returnedFromSettings.granted)
    }

    @Test
    fun captureDisposalInvalidatesOldCallbackAcrossReentry() {
        val requests = CaptureGeneration()
        val firstCapture = requests.begin()
        assertTrue(requests.isCurrent(firstCapture))

        requests.invalidate()
        assertFalse(requests.isCurrent(firstCapture))

        val reenteredCapture = requests.begin()
        assertTrue(requests.isCurrent(reenteredCapture))
        assertFalse(requests.isCurrent(firstCapture))
    }
}
