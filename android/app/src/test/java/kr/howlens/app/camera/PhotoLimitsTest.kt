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
}
