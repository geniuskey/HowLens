package kr.howlens.app.data

import android.graphics.Bitmap
import java.io.ByteArrayOutputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

/** Runs with Android's BitmapFactory; image bytes here are synthetic test fixtures. */
class RepositoryRegressionTest {
    @Test
    fun signatureOnlyPngIsRejected() {
        val signatureOnly = byteArrayOf(0x89.toByte(), 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a)

        try {
            PngAssetValidator.requireDecodable(signatureOnly)
            fail("A PNG signature without image data must not be accepted")
        } catch (failure: ApiFailure) {
            assertEquals("invalid_image", failure.code)
        }
    }

    @Test
    fun validPngIsAcceptedByAndroidDecoder() {
        val bitmap = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888)
        val output = ByteArrayOutputStream()
        try {
            check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output))
        } finally {
            bitmap.recycle()
        }

        PngAssetValidator.requireDecodable(output.toByteArray())
    }
}
